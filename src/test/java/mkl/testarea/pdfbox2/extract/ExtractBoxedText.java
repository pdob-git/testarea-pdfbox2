package mkl.testarea.pdfbox2.extract;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ExtractBoxedText {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
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
     * This test is a first check of the {@link PdfBoxFinder}. It merely outputs
     * the locations of identified horizontal and vertical lines.
     * </p>
     */
    @Test
    public void testExtractBoxes() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("Programare-licenta-5-Iulie-2018_1.pdf");
                PDDocument document = PDDocument.load(resource) ) {
            for (PDPage page : document.getDocumentCatalog().getPages()) {
                PdfBoxFinder boxFinder = new PdfBoxFinder(page);
                boxFinder.processPage(page);
                boxFinder.consolidateLists();
                System.out.println("Horizontal lines: " + boxFinder.horizontalLines);
                System.out.println("Vertical lines: " + boxFinder.verticalLines);
            }
        }
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
     * {@link PdfBoxFinder}.
     * </p>
     */
    @Test
    public void testDrawBoxes() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("Programare-licenta-5-Iulie-2018_1.pdf");
                PDDocument document = PDDocument.load(resource) ) {
            for (PDPage page : document.getDocumentCatalog().getPages()) {
                PdfBoxFinder boxFinder = new PdfBoxFinder(page);
                boxFinder.processPage(page);

                try (PDPageContentStream canvas = new PDPageContentStream(document, page, AppendMode.APPEND, true, true)) {
                    canvas.setStrokingColor(Color.RED);
                    for (Rectangle2D rectangle : boxFinder.getBoxes().values()) {
                        canvas.addRect((float)rectangle.getX(), (float)rectangle.getY(), (float)rectangle.getWidth(), (float)rectangle.getHeight());
                    }
                    canvas.stroke();
                }
            }
            document.save(new File(RESULT_FOLDER, "Programare-licenta-5-Iulie-2018_1-rectangles.pdf"));
        }
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
     * This test feeds the regions found by the {@link PdfBoxFinder} into a
     * {@link PDFTextStripperByArea} and extracts the text of the areas in questions.
     * </p>
     */
    @Test
    public void testExtractBoxedTexts() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("Programare-licenta-5-Iulie-2018_1.pdf");
                PDDocument document = PDDocument.load(resource) ) {
            for (PDPage page : document.getDocumentCatalog().getPages()) {
                PdfBoxFinder boxFinder = new PdfBoxFinder(page);
                boxFinder.processPage(page);

                PDFTextStripperByArea stripperByArea = new PDFTextStripperByArea();
                for (Map.Entry<String, Rectangle2D> entry : boxFinder.getRegions().entrySet()) {
                    stripperByArea.addRegion(entry.getKey(), entry.getValue());
                }

                stripperByArea.extractRegions(page);
                List<String> names = stripperByArea.getRegions();
                names.sort(null);
                for (String name : names) {
                    System.out.printf("[%s] %s\n", name, stripperByArea.getTextForRegion(name));
                }
            }
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/54956720/how-to-replace-a-space-with-a-word-while-extract-the-data-from-pdf-using-pdfbox">
     * How to replace a space with a word while extract the data from PDF using PDFBox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=10ZkdPlGWzMJeahwnQPzE6V7s09d1nvwq">
     * test.pdf
     * </a> as "testWPhromma.pdf"
     * <p>
     * The {@link PdfBoxFinder} can be used out of the box here.
     * </p>
     */
    @Test
    public void testExtractBoxedTextsTestWPhromma() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("testWPhromma.pdf");
                PDDocument document = PDDocument.load(resource) ) {
            for (PDPage page : document.getDocumentCatalog().getPages()) {
                PdfBoxFinder boxFinder = new PdfBoxFinder(page);
                boxFinder.processPage(page);

                PDFTextStripperByArea stripperByArea = new PDFTextStripperByArea();
                for (Map.Entry<String, Rectangle2D> entry : boxFinder.getRegions().entrySet()) {
                    stripperByArea.addRegion(entry.getKey(), entry.getValue());
                }

                stripperByArea.extractRegions(page);
                List<String> names = stripperByArea.getRegions();
                names.sort(null);
                for (String name : names) {
                    System.out.printf("[%s] %s\n", name, stripperByArea.getTextForRegion(name));
                }
            }
        }
    }
}
