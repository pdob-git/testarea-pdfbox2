package pl.pdob.pdftables;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.pdob.pdftables.solution.PdfBoxFinderMergedCells;
import pl.pdob.pdftables.solution.boxfinderinternal.Box;
import pl.pdob.pdftables.solution.boxfinderinternal.ThinRectangle;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

/**
 * Class to demonstrate issue with tables with merged cells<br>
 * <a href="https://stackoverflow.com/questions/78001237/extracting-text-from-pdf-java-using-pdfbox-library-from-a-tables-with-merged-c">
 * Extracting text from pdf (java using pdfbox library) from a tables with merged cells
 * This class is modified version ofr <a href="https://github.com/mkl-public/testarea-pdfbox2/blob/master/src/test/java/mkl/testarea/pdfbox2/extract/ExtractBoxedText.java">ExtractBoxedText.java</a>
 * @author pdob-git
 */
public class ExtractBoxedTextMergedCellsTest {
    final static File RESULT_FOLDER = new File("target/test-outputs", "pdftables");
    public static final String MERGED_CELLS_EXAMPLE = "merged_cells_example";
    public static final String EXTENSION = ".pdf";

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final String RECTANGLES = "-rectangles";


    @BeforeClass
    public static void setUpBeforeClass() {
        boolean mkDirs = RESULT_FOLDER.mkdirs();
        if (mkDirs) {
            logger.debug("Folder for test results created");
        }
    }

    /**
     * Class to demonstrate issue with tables with merged cells<br>
     * <a href="https://stackoverflow.com/questions/78001237/extracting-text-from-pdf-java-using-pdfbox-library-from-a-tables-with-merged-c">
     * Extracting text from pdf (java using pdfbox library) from a tables with merged cells
     * <a href="https://stackoverflow.com/questions/51380677/extracting-text-from-pdf-java-using-pdfbox-library-from-a-tables-rows-with-di">
     * Extracting text from pdf (java using pdfbox library) from a table's rows with different heights
     * </a>
     * <br/>
     * <a href="https://www.info.uvt.ro/wp-content/uploads/2018/07/Programare-licenta-5-Iulie-2018_1.pdf">
     * Programare-licenta-5-Iulie-2018_1.pdf
     * </a>
     * <p>
     * This test is a first check of the {@link PdfBoxFinderMergedCells}. It merely outputs
     * the locations of identified horizontal and vertical lines.
     * </p>
     */
    @Test
    public void testExtractBoxes() throws IOException {
        List<ThinRectangle> horizontalRectangles = null;
        List<ThinRectangle> verticalRectangles = null;
        try (InputStream resource = getClass().getResourceAsStream(MERGED_CELLS_EXAMPLE + EXTENSION);
             PDDocument document = PDDocument.load(resource) ) {
            for (PDPage page : document.getDocumentCatalog().getPages()) {
                PdfBoxFinderMergedCells boxFinder = new PdfBoxFinderMergedCells(page);
                boxFinder.processPage(page);
                boxFinder.consolidateLists();
                horizontalRectangles = boxFinder.getAllRectangles().getHorizontalRectangles();
                logger.info("Horizontal lines: {}", horizontalRectangles);
                verticalRectangles = boxFinder.getAllRectangles().getVerticalRectangles();
                logger.info("Vertical lines: {}", verticalRectangles);
            }
        }

        assertNotNull(horizontalRectangles);
        assertFalse(horizontalRectangles.isEmpty());

        assertNotNull(verticalRectangles);
        assertFalse(verticalRectangles.isEmpty());
    }

    /**
     * <a href="https://stackoverflow.com/questions/51380677/extracting-text-from-pdf-java-using-pdfbox-library-from-a-tables-rows-with-di">
     * Extracting text from pdf (java using pdfbox library) from a table's rows with different heights
     * </a>
     * <br/>
     * <a href="https://www.info.uvt.ro/wp-content/uploads/2018/07/Programare-licenta-5-Iulie-2018_1.pdf">
     * Programare-licenta-5-Iulie-2018_1.pdf
     * </a>
     * <p>
     * This test draws a grid on the test file representing the boxes found by the
     * {@link PdfBoxFinderMergedCells}.
     * </p>
     */
    @Test
    public void testDrawBoxes() throws IOException {
        File resultfile = new File(RESULT_FOLDER, MERGED_CELLS_EXAMPLE + RECTANGLES + EXTENSION);
        try (   InputStream resource = getClass().getResourceAsStream(MERGED_CELLS_EXAMPLE + EXTENSION);
                PDDocument document = PDDocument.load(resource) ) {
            for (PDPage page : document.getDocumentCatalog().getPages()) {
                PdfBoxFinderMergedCells boxFinder = new PdfBoxFinderMergedCells(page);
                boxFinder.processPage(page);

                try (PDPageContentStream canvas = new PDPageContentStream(document, page, AppendMode.APPEND, true, true)) {
                    canvas.setStrokingColor(Color.RED);
                    for (Box box: boxFinder.getBoxes().values()) {
                        Rectangle2D rectangle = box.getShape();
                        canvas.addRect((float)rectangle.getX(), (float)rectangle.getY(), (float)rectangle.getWidth(), (float)rectangle.getHeight());
                    }
                    canvas.stroke();
                }
            }

            document.save(resultfile);
        }

        assertTrue(resultfile.exists());
    }

    /**
     * <a href="https://stackoverflow.com/questions/51380677/extracting-text-from-pdf-java-using-pdfbox-library-from-a-tables-rows-with-di">
     * Extracting text from pdf (java using pdfbox library) from a table's rows with different heights
     * </a>
     * <br/>
     * <a href="https://www.info.uvt.ro/wp-content/uploads/2018/07/Programare-licenta-5-Iulie-2018_1.pdf">
     * Programare-licenta-5-Iulie-2018_1.pdf
     * </a>
     * <p>
     * This test feeds the regions found by the {@link PdfBoxFinderMergedCells} into a
     * {@link PDFTextStripperByArea} and extracts the text of the areas in questions.
     * </p>
     */
    @Test
    public void testExtractBoxedTexts() throws IOException {
        List<String> names = null;
        try (   InputStream resource = getClass().getResourceAsStream(MERGED_CELLS_EXAMPLE + EXTENSION);
                PDDocument document = PDDocument.load(resource) ) {
            for (PDPage page : document.getDocumentCatalog().getPages()) {
                PdfBoxFinderMergedCells boxFinder = new PdfBoxFinderMergedCells(page);
                boxFinder.processPage(page);

                PDFTextStripperByArea stripperByArea = new PDFTextStripperByArea();
                for (Map.Entry<String, Rectangle2D> entry : boxFinder.getRegions().entrySet()) {
                    stripperByArea.addRegion(entry.getKey(), entry.getValue());
                }

                stripperByArea.extractRegions(page);
                names = stripperByArea.getRegions();
                names.sort(null);
                for (String name : names) {
                    System.out.printf("[%s] %s\n", name, stripperByArea.getTextForRegion(name));
                }
            }
        }
        assertNotNull(names);
        assertFalse(names.isEmpty());
    }

}
