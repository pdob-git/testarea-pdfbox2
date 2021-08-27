package mkl.testarea.pdfbox2.extract;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.junit.BeforeClass;
import org.junit.Test;

import mkl.testarea.pdfbox2.extract.MarkedContentBoundingBoxFinder.MarkedContent;

/**
 * <a href="https://stackoverflow.com/questions/52821421/how-do-determine-location-of-actual-pdf-content-with-pdfbox">
 * How do determine location of actual PDF content with PDFBox?
 * </a>
 * <p>
 * This class tests the {@link BoundingBoxFinder} by applying it to a
 * number of miscellaneous PDFs and stroking the determined boxes.
 * </p>
 * <p> 
 * The {@link BoundingBoxFinder} determines the bounding box of the static
 * content of a page. Beware, it is not very sophisticated; in particular
 * it does not ignore invisible content like a white background rectangle,
 * text drawn in rendering mode "invisible", arbitrary content covered
 * by a white filled path, white parts of bitmap images, ... Furthermore,
 * it ignores clip paths.
 * </p>
 * 
 * @author mkl
 */
public class DetermineBoundingBox {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }


    @Test
    public void test00000000000005fw6q() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("00000000000005fw6q.pdf");
                PDDocument pdDocument = Loader.loadPDF(resource)   ) {
            drawBoundingBoxes(pdDocument);
            pdDocument.save(new File(RESULT_FOLDER, "00000000000005fw6q-boundingBoxes.pdf"));
        }
    }

    @Test
    public void test10948() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("10948.pdf");
                PDDocument pdDocument = Loader.loadPDF(resource)   ) {
            drawBoundingBoxes(pdDocument);
            pdDocument.save(new File(RESULT_FOLDER, "10948-boundingBoxes.pdf"));
        }
    }

    @Test
    public void testApache() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("apache.pdf");
                PDDocument pdDocument = Loader.loadPDF(resource)   ) {
            drawBoundingBoxes(pdDocument);
            pdDocument.save(new File(RESULT_FOLDER, "apache-boundingBoxes.pdf"));
        }
    }

    @Test
    public void testEMPLOYMENTCONTRACTTEMPLATEcoveredAs() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("EMPLOYMENTCONTRACTTEMPLATE.pdf");
                PDDocument pdDocument = Loader.loadPDF(resource)   ) {
            drawBoundingBoxes(pdDocument);
            pdDocument.save(new File(RESULT_FOLDER, "EMPLOYMENTCONTRACTTEMPLATE-boundingBoxes.pdf"));
        }
    }

    @Test
    public void testBal_532935_0314() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("Bal_532935_0314.pdf");
                PDDocument pdDocument = Loader.loadPDF(resource)   ) {
            drawBoundingBoxes(pdDocument);
            pdDocument.save(new File(RESULT_FOLDER, "Bal_532935_0314-boundingBoxes.pdf"));
        }
    }

    @Test
    public void testTest() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/pdfbox2/sign/test.pdf");
                PDDocument pdDocument = Loader.loadPDF(resource)   ) {
            drawBoundingBoxes(pdDocument);
            pdDocument.save(new File(RESULT_FOLDER, "test-boundingBoxes.pdf"));
        }
    }

    void drawBoundingBoxes(PDDocument pdDocument) throws IOException {
        for (PDPage pdPage : pdDocument.getPages()) {
            drawBoundingBox(pdDocument, pdPage);
        }
    }

    void drawBoundingBox(PDDocument pdDocument, PDPage pdPage) throws IOException {
        BoundingBoxFinder boxFinder = new BoundingBoxFinder(pdPage);
        boxFinder.processPage(pdPage);
        Rectangle2D box = boxFinder.getBoundingBox();
        if (box != null) {
            try (   PDPageContentStream canvas = new PDPageContentStream(pdDocument, pdPage, AppendMode.APPEND, true, true)) {
                canvas.setStrokingColor(Color.magenta);
                canvas.addRect((float)box.getMinX(), (float)box.getMinY(), (float)box.getWidth(), (float)box.getHeight());
                canvas.stroke();
            }
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/68565744/pdfbox-how-to-determine-bounding-box-of-vector-figure-path-shape">
     * PDFBox: how to determine bounding box of vector figure (path shape)
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1Z1R-SIalxPzAHH57_Qs0zGPDV3rjoqtN/view">
     * Trade_Simple1.pdf
     * </a> as "testAlexanderDyuzhev.pdf"
     * <p>
     * This test applies the {@link MarkedContentBoundingBoxFinder} to
     * a test document of some other question and frames the bounding
     * boxes accordingly.
     * </p>
     */
    @Test
    public void testMarkedContentTrade_Simple1() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/pdfbox2/content/Trade_Simple1.pdf");
                PDDocument pdDocument = Loader.loadPDF(resource)   ) {
            drawMarkedContentBoundingBoxes(pdDocument);
            pdDocument.save(new File(RESULT_FOLDER, "Trade_Simple1-boundingBoxes.pdf"));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/68565744/pdfbox-how-to-determine-bounding-box-of-vector-figure-path-shape">
     * PDFBox: how to determine bounding box of vector figure (path shape)
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/w4ksnud78bu9oz5/test.pdf?dl=0">
     * test.pdf
     * </a> as "testAlexanderDyuzhev.pdf"
     * <p>
     * This test applies the {@link MarkedContentBoundingBoxFinder} to
     * the test document provided by the OP and frames the bounding boxes
     * accordingly.
     * </p>
     */
    @Test
    public void testMarkedContentTestAlexanderDyuzhev() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("testAlexanderDyuzhev.pdf");
                PDDocument pdDocument = Loader.loadPDF(resource)   ) {
            drawMarkedContentBoundingBoxes(pdDocument);
            pdDocument.save(new File(RESULT_FOLDER, "testAlexanderDyuzhev-boundingBoxes.pdf"));
        }
    }

    void drawMarkedContentBoundingBoxes(PDDocument pdDocument) throws IOException {
        for (PDPage pdPage : pdDocument.getPages()) {
            MarkedContent markedContent = drawMarkedContentBoundingBoxes(pdDocument, pdPage);
            printMarkedContentBoundingBoxes(markedContent, "");
        }
    }

    MarkedContent drawMarkedContentBoundingBoxes(PDDocument pdDocument, PDPage pdPage) throws IOException {
        MarkedContentBoundingBoxFinder boxFinder = new MarkedContentBoundingBoxFinder(pdPage);
        boxFinder.processPage(pdPage);
        MarkedContent markedContent = boxFinder.content;
        if (markedContent.boundingBox != null) {
            try (   PDPageContentStream canvas = new PDPageContentStream(pdDocument, pdPage, AppendMode.APPEND, true, true)) {
                canvas.setStrokingColor(Color.magenta);
                drawMarkedContentBoundingBoxes(markedContent, canvas);
                canvas.stroke();
            }
        }
        return markedContent;
    }

    void drawMarkedContentBoundingBoxes(MarkedContent markedContent, PDPageContentStream canvas) throws IOException {
        Rectangle2D box = markedContent.boundingBox;
        if (box != null) {
            canvas.addRect((float)box.getMinX(), (float)box.getMinY(), (float)box.getWidth(), (float)box.getHeight());
        }

        for (MarkedContent childContent : markedContent.children) {
            drawMarkedContentBoundingBoxes(childContent, canvas);
        }
    }

    void printMarkedContentBoundingBoxes(MarkedContent markedContent, String prefix) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix).append(markedContent.tag.getName());
        builder.append(' ').append(markedContent.boundingBox);
        System.out.println(builder.toString());
        for (MarkedContent child : markedContent.children)
            printMarkedContentBoundingBoxes(child, prefix + "  ");
    }
}
