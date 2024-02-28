package pl.pdob.pdftables.solution;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.pdob.pdftables.solution.boxfinderinternal.*;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * Class to demonstrate issue with tables with merged cells<br>
 * <a href="https://stackoverflow.com/questions/78001237/extracting-text-from-pdf-java-using-pdfbox-library-from-a-tables-with-merged-c">
 * Extracting text from pdf (java using pdfbox library) from a tables with merged cells
 * Improved <a href="https://github.com/mkl-public/testarea-pdfbox2/blob/master/src/main/java/mkl/testarea/pdfbox2/extract/PdfBoxFinder.java">PdfBoxFinder.java</a>
 * <a href="https://stackoverflow.com/questions/51380677/extracting-text-from-pdf-java-using-pdfbox-library-from-a-tables-rows-with-di">
 * Extracting text from pdf (java using pdfbox library) from a table's rows with different heights
 * </a>
 * <br/>
 * <a href="https://www.info.uvt.ro/wp-content/uploads/2018/07/Programare-licenta-5-Iulie-2018_1.pdf">
 * Programare-licenta-5-Iulie-2018_1.pdf
 * </a>
 * <p>
 * This stream engine class determines the lines framing table cells. It is
 * implemented to recognize lines created like in the example PDF shared by
 * the OP, i.e. lines drawn as long thin filled rectangles. It is easily
 * possible to generalize this for frame lines drawn differently, c.f. the
 * method {@link #processPath()}.  
 * </p>
 * <p>
 * For a given {@link PDPage} <code>page</code> use this class like this:
 * </p>
 * <pre>
 * PdfBoxFinder boxFinder = new PdfBoxFinder(page);
 * boxFinder.processPage(page);
 * </pre>
 * <p>
 * After this you can retrieve the boxes ({@link Rectangle2D} instances with
 * coordinates according to the PDF coordinate system, e.g. for decorating
 * the table cells) or regions ({@link Rectangle2D} instances with coordinates
 * according to the PDFBox text extraction API, e.g. for initializing the
 * regions of a {@link PDFTextStripperByArea}).
 * </p>
 * 
 * @author pdob-git
 * Based on mkl-public work
 */
public class PdfBoxFinderMergedCells extends PDFGraphicsStreamEngine {


    final List<PathElement> path = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AllRectangles allRectangles;
    private final RectanglesProcessor rectanglesProcessor;
    private final BoxProcessor boxProcessor;

    /**
     * Supply the page to analyze here; to analyze multiple pages
     * create multiple {@link PdfBoxFinderMergedCells} instances.
     */
    public PdfBoxFinderMergedCells(PDPage page) {
        super(page);
        this.allRectangles = new AllRectangles();
        this.rectanglesProcessor = new RectanglesProcessor();
        this.boxProcessor = new BoxProcessor();
    }

    /**
     * The boxes ({@link Rectangle2D} instances with coordinates according to
     * the PDF coordinate system, e.g. for decorating the table cells) the
     * {@link PdfBoxFinderMergedCells} has recognized on the current page.
     */
    public SortedMap<String, Box> getBoxes() {
        consolidateLists();
        return boxProcessor.generateBoxes(allRectangles);
    }

    /**
     * The regions ({@link Rectangle2D} instances with coordinates according
     * to the PDFBox text extraction API, e.g. for initializing the regions of
     * a {@link PDFTextStripperByArea}) the {@link PdfBoxFinderMergedCells} has recognized
     * on the current page.
     */
    public Map<String, Rectangle2D> getRegions() {
        PDRectangle cropBox = getPage().getCropBox();
        float xOffset = cropBox.getLowerLeftX();
        float yOffset = cropBox.getUpperRightY();
        SortedMap<String, Rectangle2D> result = new TreeMap<>();
        SortedMap<String, Box> boxes = getBoxes();
        for (Map.Entry<String, Box> entry : boxes.entrySet()) {
            Rectangle2D rectangle = entry.getValue().getShape();
            Rectangle2D region = new Rectangle2D.Float(xOffset + (float)rectangle.getX(), yOffset - (float)(rectangle.getY() + rectangle.getHeight()), (float)rectangle.getWidth(), (float)rectangle.getHeight());
            result.put(entry.getKey(),region);
        }
        return result;
    }

    /**
     * <p>
     * Processes the path elements currently in the {@link #path} list and
     * eventually clears the list.
     * </p>
     * <p>
     * Currently only elements are considered which 
     * </p>
     * <ul>
     * <li>are {@link ThinRectangle} instances;
     * <li>are filled fairly black;
     * <li>have a thin and long form; and
     * <li>have sides fairly parallel to the coordinate axis.
     * </ul>
     */
    void processPath() throws IOException {
        PDColor color = getGraphicsState().getNonStrokingColor();
        if (!isBlack(color)) {
            logger.debug("Dropped path due to non-black fill-color.");
            return;
        }

        for (PathElement pathElement : path) {
            if (pathElement instanceof ThinRectangle) {
                ThinRectangle thinRectangle = (ThinRectangle) pathElement;

                double p0p1 = thinRectangle.getP0().distance(thinRectangle.getP1());
                double p1p2 = thinRectangle.getP1().distance(thinRectangle.getP2());
                boolean p0p1small = p0p1 < 3;
                boolean p1p2small = p1p2 < 3;

                if (p0p1small) {
                    if (p1p2small) {
                        logger.debug("Dropped rectangle too small on both sides.");
                    } else {
                        allRectangles.addThinRectangle(new ThinRectangle(thinRectangle.getP0(), thinRectangle.getP1(), thinRectangle.getP2(), thinRectangle.getP3()));

                    }
                } else if (p1p2small) {
                    allRectangles.addThinRectangle(new ThinRectangle(thinRectangle.getP0(), thinRectangle.getP1(), thinRectangle.getP2(), thinRectangle.getP3()));
                } else {
                    logger.debug("Dropped rectangle too large on both sides.");
                }
            }
        }
        path.clear();
    }




    /**
     * Segregates data and
     * Sorts the "{@link AllRectangles#getHorizontalRectangles()}" and "{@link AllRectangles#getVerticalRectangles()}" lists and
     * merges fairly identical entries.
     */
    public void consolidateLists() {
        rectanglesProcessor.segregateData(allRectangles);
        rectanglesProcessor.consolidateLists(allRectangles);
    }

    /**
     * Checks whether the given color is black'ish.
     */
    boolean isBlack(PDColor color) throws IOException {
        int value = color.toRGB();
        for (int i = 0; i < 2; i++) {
            int component = value & 0xff;
            if (component > 5)
                return false;
            value /= 256;
        }
        return true;
    }

    //
    // PDFGraphicsStreamEngine overrides
    //




    @Override
    public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException {
        path.add(new ThinRectangle(p0, p1, p2, p3));
    }

    @Override
    public void endPath() throws IOException {
        path.clear();
    }

    @Override
    public void strokePath() throws IOException {
        path.clear();
    }

    @Override
    public void fillPath(int windingRule) throws IOException {
        processPath();
    }

    @Override
    public void fillAndStrokePath(int windingRule) throws IOException {
        processPath();
    }

    @Override public void drawImage(PDImage pdImage) throws IOException { //not required
         }
    @Override public void clip(int windingRule) throws IOException { //not required
         }
    @Override public void moveTo(float x, float y) throws IOException { //not required
         }
    @Override public void lineTo(float x, float y) throws IOException { //not required
         }
    @Override public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException {//not required
         }
    @Override public Point2D getCurrentPoint() throws IOException {
        //not required
        return null;
         }
    @Override public void closePath() throws IOException { //not required
        }
    @Override public void shadingFill(COSName shadingName) throws IOException {//not required
        }

    //
    // inner classes
    //
    static class Interval implements Comparable<Interval> {
        final float from;
        final float to;

        Interval(float... values) {
            Arrays.sort(values);
            this.from = values[0];
            this.to = values[values.length - 1];
        }

        Interval(double... values) {
            Arrays.sort(values);
            this.from = (float) values[0];
            this.to = (float) values[values.length - 1];
        }

        boolean combinableWith(Interval other) {
            if (this.from > other.from)
                return other.combinableWith(this);
            if (this.to < other.from)
                return false;
            float intersectionLength = Math.min(this.to, other.to) - other.from;
            float thisLength = this.to - this.from;
            float otherLength = other.to - other.from;
            return (intersectionLength >= thisLength * .9f) || (intersectionLength >= otherLength * .9f);
        }

        Interval combineWith(Interval other) {
            return new Interval(this.from, this.to, other.from, other.to);
        }

        @Override
        public int compareTo(Interval o) {
            return this.from == o.from ? Float.compare(this.to, o.to) : Float.compare(this.from, o.from);
        }

        @Override
        public String toString() {
            return String.format("[%3.2f, %3.2f]", from, to);
        }
    }


    public AllRectangles getAllRectangles() {
        return allRectangles;
    }
}
