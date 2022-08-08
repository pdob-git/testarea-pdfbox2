package mkl.testarea.pdfbox2.extract;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ExtractLinesWithDir {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/55166990/pdfbox-line-rectangle-extraction">
     * PDFBox - Line / Rectangle extraction
     * </a>
     * <br/>
     * <a href="https://github.com/yashodhan19/PDF/blob/master/LineRotationTest.pdf">
     * LineRotationTest.pdf
     * </a>
     * <p>
     * This essentially is the <code>LineCatcher</code> <code>main</code> method code
     * posted by the OP, merely dressed up as a unit test. The results are coordinates
     * of the bottom left point of the line bounding boxes without consideration of
     * page rotation. 
     * </p>
     * <p>
     * The OP apparently is interested in the coordinates on the rotated page. The
     * test {@link #testExtractLineRotationTestWithDir()} shows how to do this. 
     * </p>
     */
    @Test
    public void testExtractLineRotationTestLikeYashodhanJoglekar() throws IOException {
        PDDocument document = null;
        FileOutputStream fop = null;
        File file;
        Writer osw = null;
        int numPages;
        double page_height;
        try (   InputStream resource = getClass().getResourceAsStream("LineRotationTest.pdf")   )
        {
            document = PDDocument.load(resource);
            numPages = document.getNumberOfPages();
            file = new File(RESULT_FOLDER, "LineRotationTest-LikeYashodhanJoglekar.csv");
            fop = new FileOutputStream(file);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            osw = new OutputStreamWriter(fop, "UTF8");
            osw.write(headerRecord + System.lineSeparator());
            System.out.println("Line Processing numPages:" + numPages);
            for (int n = 0; n < numPages; n++) {
                System.out.println("Line Processing page:" + n);
                rectList = new ArrayList<Rectangle2D>();
                PDPage page = document.getPage(n);
                page_height = page.getCropBox().getUpperRightY();
                LineCatcher lineCatcher = new LineCatcher(page);
                lineCatcher.processPage(page);

                for(Rectangle2D rect:rectList) {
                    String pageNum = Integer.toString(n + 1);
                    String x = Double.toString(rect.getX());
                    String y = Double.toString(page_height - rect.getY()) ;
                    String w = Double.toString(rect.getWidth());
                    String h = Double.toString(rect.getHeight());
                    writeToFile(pageNum, x, y, w, h, osw);
                }
                rectList = null;
                page = null;
                lineCatcher = null;
            };

        } finally {
            if ( osw != null ){
                osw.close();
            }
            if( document != null )
            {
                document.close();
            }
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/55166990/pdfbox-line-rectangle-extraction">
     * PDFBox - Line / Rectangle extraction
     * </a>
     * <br/>
     * <a href="https://github.com/yashodhan19/PDF/blob/master/LineRotationTest.pdf">
     * LineRotationTest.pdf
     * </a>
     * <p>
     * In contrast to {@link #testExtractLineRotationTestLikeYashodhanJoglekar()},
     * the OP's original code, this method attempts to extract the coordinates as
     * the OP wants it, i.e. the coordinates of the top left point of the line
     * bounding boxes on the rotated page, the origin in the upper left corner
     * of the page. 
     * </p>
     */
    @Test
    public void testExtractLineRotationTestWithDir() throws IOException {
        PDDocument document = null;
        FileOutputStream fop = null;
        File file;
        Writer osw = null;
        int numPages;
        try (   InputStream resource = getClass().getResourceAsStream("LineRotationTest.pdf")   )
        {
            document = PDDocument.load(resource);
            numPages = document.getNumberOfPages();
            file = new File(RESULT_FOLDER, "LineRotationTest-WithDir.csv");
            fop = new FileOutputStream(file);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            osw = new OutputStreamWriter(fop, "UTF8");
            osw.write(headerRecord + System.lineSeparator());
            System.out.println("Line Processing numPages:" + numPages);
            for (int n = 0; n < numPages; n++) {
                System.out.println("Line Processing page:" + n);
                rectList = new ArrayList<Rectangle2D>();
                PDPage page = document.getPage(n);

                int pageRotation = page.getRotation();
                PDRectangle pageCropBox = page.getCropBox();

                LineCatcher lineCatcher = new LineCatcher(page);
                lineCatcher.processPage(page);

                for(Rectangle2D rect:rectList) {
                    String pageNum = Integer.toString(n + 1);
                    String x, y, w, h;
                    switch(pageRotation) {
                    case 0:
                        x = Double.toString(rect.getX() - pageCropBox.getLowerLeftX());
                        y = Double.toString(pageCropBox.getUpperRightY() - rect.getY() + rect.getHeight());
                        w = Double.toString(rect.getWidth());
                        h = Double.toString(rect.getHeight());
                        break;
                    case 90:
                        x = Double.toString(rect.getY() - pageCropBox.getLowerLeftY());
                        y = Double.toString(rect.getX() - pageCropBox.getLowerLeftX());
                        w = Double.toString(rect.getHeight());
                        h = Double.toString(rect.getWidth());
                        break;
                    case 180:
                        x = Double.toString(pageCropBox.getUpperRightX() - rect.getX() - rect.getWidth());
                        y = Double.toString(rect.getY() - pageCropBox.getLowerLeftY());
                        w = Double.toString(rect.getWidth());
                        h = Double.toString(rect.getHeight());
                        break;
                    case 270:
                        x = Double.toString(pageCropBox.getUpperRightY() - rect.getY() + rect.getHeight());
                        y = Double.toString(pageCropBox.getUpperRightX() - rect.getX() - rect.getWidth());
                        w = Double.toString(rect.getHeight());
                        h = Double.toString(rect.getWidth());
                        break;
                    default:
                        throw new IOException(String.format("Unsupported page rotation %d on page %d.", pageRotation, page));
                    }
                    writeToFile(pageNum, x, y, w, h, osw);
                }
                rectList = null;
                page = null;
                lineCatcher = null;
            };

        } finally {
            if ( osw != null ){
                osw.close();
            }
            if( document != null )
            {
                document.close();
            }
        }
    }

    /** Here {@link LineCatcher} returns the line bounding boxes. */
    private static ArrayList<Rectangle2D> rectList= new ArrayList<Rectangle2D>();
    /** Header line for csv file with coordinates */
    private static String headerRecord = "Text|Page|x|y|width|height|space|font";
    /** Helper method for outputting a line to csv file with coordinates */
    private static void writeToFile(String pageNum, String x, String y, String w, String h, Writer osw) throws IOException {
        String c = "^" + "|" +
                pageNum + "|" +
                x + "|" +
                y + "|" +
                w + "|" +
                h + "|" +
                "999" + "|" +
                "marker-only";
        osw.write(c + System.lineSeparator());
    }

    /**
     * @see ExtractLinesWithDir#testExtractLineRotationTestLikeYashodhanJoglekar()
     * @see ExtractLinesWithDir#testExtractLineRotationTestWithDir()
     * 
     * @author Tilman Hausherr
     * @author Yashodhan Joglekar
     */
    public static class LineCatcher extends PDFGraphicsStreamEngine
    {
        private final GeneralPath linePath = new GeneralPath();
        private int clipWindingRule = -1;

        public LineCatcher(PDPage page)
        {
            super(page);
        }

        @Override
        public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException
        {
            // to ensure that the path is created in the right direction, we have to create
            // it by combining single lines instead of creating a simple rectangle
            linePath.moveTo((float) p0.getX(), (float) p0.getY());
            linePath.lineTo((float) p1.getX(), (float) p1.getY());
            linePath.lineTo((float) p2.getX(), (float) p2.getY());
            linePath.lineTo((float) p3.getX(), (float) p3.getY());

            // close the subpath instead of adding the last line so that a possible set line
            // cap style isn't taken into account at the "beginning" of the rectangle
            linePath.closePath();
        }

        @Override
        public void drawImage(PDImage pdi) throws IOException
        {
        }

        @Override
        public void clip(int windingRule) throws IOException
        {
            // the clipping path will not be updated until the succeeding painting operator is called
            clipWindingRule = windingRule;

        }

        @Override
        public void moveTo(float x, float y) throws IOException
        {
            linePath.moveTo(x, y);
        }

        @Override
        public void lineTo(float x, float y) throws IOException
        {
            linePath.lineTo(x, y);
        }

        @Override
        public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException
        {
            linePath.curveTo(x1, y1, x2, y2, x3, y3);
        }

        @Override
        public Point2D getCurrentPoint() throws IOException
        {
            return linePath.getCurrentPoint();
        }

        @Override
        public void closePath() throws IOException
        {
            linePath.closePath();
        }

        @Override
        public void endPath() throws IOException
        {
            if (clipWindingRule != -1)
            {
                linePath.setWindingRule(clipWindingRule);
                getGraphicsState().intersectClippingPath(linePath);
                clipWindingRule = -1;
            }
            linePath.reset();

        }

        @Override
        public void strokePath() throws IOException
        {
            rectList.add(linePath.getBounds2D());
            linePath.reset();
        }

        @Override
        public void fillPath(int windingRule) throws IOException
        {
            linePath.reset();
        }

        @Override
        public void fillAndStrokePath(int windingRule) throws IOException
        {
            linePath.reset();
        }

        @Override
        public void shadingFill(COSName cosn) throws IOException
        {
        }
    }
}
