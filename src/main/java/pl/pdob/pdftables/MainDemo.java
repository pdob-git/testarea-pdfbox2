package pl.pdob.pdftables;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.pdob.pdftables.csv.CsvWriter;
import pl.pdob.pdftables.solution.boxfinderinternal.AllRectangles;
import pl.pdob.pdftables.solution.PdfBoxFinderMergedCells;
import pl.pdob.pdftables.solution.boxfinderinternal.RectanglesProcessor;
import pl.pdob.pdftables.solution.boxfinderinternal.Box;
import pl.pdob.pdftables.solution.boxfinderinternal.BoxProcessor;
import pl.pdob.pdftables.solution.boxfinderinternal.ThinRectangle;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 * Class to demonstrate issue with tables with merged cells<br>
 * <a href="https://stackoverflow.com/questions/78001237/extracting-text-from-pdf-java-using-pdfbox-library-from-a-tables-with-merged-c">
 * Extracting text from pdf (java using pdfbox library) from a tables with merged cells
 * </a>
 * <br>
 * Method Drawing found rectangles taken from  mkl.testarea.pdfbox2.extract.ExtractBoxedText<br>
 * and modified
 */
public class MainDemo {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    public static final File RESULT_FOLDER = new File("pl.pdob.results");

    public static final File INPUT_FOLDER = new File("pl.pdob.input");

    public static final String INPUT = "Minimal_example.pdf";



    static {

        if (!INPUT_FOLDER.exists()) {
            //noinspection ResultOfMethodCallIgnored
            INPUT_FOLDER.mkdirs();
        }

        if (!RESULT_FOLDER.exists()) {
            //noinspection ResultOfMethodCallIgnored
            RESULT_FOLDER.mkdirs();
        }
    }

    private final BoxProcessor boxProcessor;

    private AllRectangles allRectangles;
    private final RectanglesProcessor rectanglesProcessor;

    public MainDemo() {
        this.rectanglesProcessor = new RectanglesProcessor();
        this.boxProcessor = new BoxProcessor();
    }

    public static void main(String[] args) throws IOException {
        MainDemo mainDemo = new MainDemo();
        mainDemo.drawBoxes(INPUT);

        mainDemo.extractBoxes(new File(INPUT_FOLDER, INPUT));
        mainDemo.combine(mainDemo.allRectangles);
        mainDemo.drawAllThinRectangles(mainDemo.allRectangles);
        mainDemo.saveAsCsv(mainDemo.allRectangles);
        mainDemo.generateBoxes(mainDemo.allRectangles);

    }

    public void generateBoxes(AllRectangles allRectangles) throws IOException {
        SortedMap<String, Box> stringBoxSortedMap = boxProcessor.generateBoxes(allRectangles);
        logger.info("{}",stringBoxSortedMap);
        boxProcessor.draw(stringBoxSortedMap, Color.GREEN);
    }

    private void combine(AllRectangles allRectangles) {
        rectanglesProcessor.consolidateLists(allRectangles);
    }

    private void saveAsCsv(AllRectangles allRectangles) {
        List<ThinRectangle> horizontalThinRectangles = allRectangles.getHorizontalRectangles();
        List<ThinRectangle> verticalThinRectangles = allRectangles.getVerticalRectangles();
        SortedMap<Integer, ThinRectangle> integerHorizRectangleMap = rectanglesProcessor.removeDuplicates(horizontalThinRectangles);
        SortedMap<Integer, ThinRectangle> integerVertRectangleMap = rectanglesProcessor.removeDuplicates(verticalThinRectangles);

        List<String[]> horiz = rectanglesProcessor.toCsvString(integerHorizRectangleMap, "horiz");
        List<String[]> vert = rectanglesProcessor.toCsvString(integerVertRectangleMap, "vert");

        ArrayList<String[]> csvStrings = new ArrayList<>(horiz);
        csvStrings.addAll(vert);
        CsvWriter.csvWriterOneByOne(csvStrings, new File(RESULT_FOLDER, "thin_rectangles.csv").getAbsolutePath());
    }

    void drawAllThinRectangles(AllRectangles allRectangles) throws IOException {
        drawRectangles(allRectangles.getHorizontalRectangles(), Color.RED);
        drawRectangles(allRectangles.getVerticalRectangles(), Color.BLUE);

    }

    private void drawRectangles(List<ThinRectangle> thinRectangles, Color color) throws IOException {
        if (thinRectangles != null && !thinRectangles.isEmpty()) {
            SortedMap<Integer, ThinRectangle> rectangleMap = rectanglesProcessor.removeDuplicates(thinRectangles);
            rectanglesProcessor.draw(rectangleMap, color);
        }

    }

    public void extractBoxes(File file) throws IOException {
        if (allRectangles != null) {

            allRectangles.clear();
        }
        try (InputStream resource = new FileInputStream(file);
             PDDocument document = PDDocument.load(resource)) {
            for (PDPage page : document.getDocumentCatalog().getPages()) {
                PdfBoxFinderMergedCells boxFinder = new PdfBoxFinderMergedCells(page);
                boxFinder.processPage(page);
                boxFinder.consolidateLists();


                logger.info("All Rectangles which make borders");
                allRectangles = boxFinder.getAllRectangles();

                rectanglesProcessor.segregateData(allRectangles);

                logger.info("Horizontal Rectangles");
                logger.info("{}", allRectangles.getHorizontalRectangles());
                logger.info("Vertical Rectangles");
                logger.info("{}", allRectangles.getVerticalRectangles());


            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void drawBoxes(String fileName) throws IOException {
        File file = new File(INPUT_FOLDER, fileName);
        if (allRectangles != null) {
            allRectangles.clear();
        }

        try (
                PDDocument document = PDDocument.load(file)) {
            for (PDPage page : document.getDocumentCatalog().getPages()) {
                PdfBoxFinderMergedCells boxFinder = new PdfBoxFinderMergedCells(page);
                boxFinder.processPage(page);

                try (PDPageContentStream canvas = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    canvas.setStrokingColor(Color.RED);
                    for (Box box: boxFinder.getBoxes().values()) {
                        Rectangle2D rectangle = box.getShape();
                        canvas.addRect((float) rectangle.getX(), (float) rectangle.getY(), (float) rectangle.getWidth(), (float) rectangle.getHeight());

                    }
                    canvas.stroke();
                }
            }
            document.save(new File(RESULT_FOLDER, fileName + "-rectangles.pdf"));
        }
    }

    public AllRectangles getAllRectangles() {
        return allRectangles;
    }
}
