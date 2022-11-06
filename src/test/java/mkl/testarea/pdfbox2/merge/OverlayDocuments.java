package mkl.testarea.pdfbox2.merge;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.pdfbox.multipdf.Overlay;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class OverlayDocuments {
    final static File RESULT_FOLDER = new File("target/test-outputs", "merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-4797">
     * Overlayed PDF file do not shows the difference
     * </a>
     * <br/>
     * <a href="https://issues.apache.org/jira/secure/attachment/12996277/10.pdf">
     * 10.pdf
     * </a>
     * <br/>
     * <a href="https://issues.apache.org/jira/secure/attachment/12996276/114.pdf">
     * 114.pdf
     * </a>
     * <p>
     * This test demonstrates how to use the blend mode when overlaying documents
     * for comparison.
     * </p>
     */
    @Test
    public void testOverlayWithMultiply() throws IOException {
        try (   InputStream file1 = getClass().getResourceAsStream("10.pdf");
                InputStream file2 = getClass().getResourceAsStream("114.pdf");
                PDDocument document1 = PDDocument.load(file1);
                PDDocument document2 = PDDocument.load(file2);
                Overlay overlayer = new Overlay()) {
            overlayer.setInputPDF(document1);
            overlayer.setAllPagesOverlayPDF(document2);
            try (   PDDocument result = overlayer.overlay(Collections.emptyMap()) ) {
                result.save(new File(RESULT_FOLDER, "10and114.pdf"));

                try (   PDPageContentStream canvas = new PDPageContentStream(result, result.getPage(5), AppendMode.PREPEND, false, false)) {
                    PDExtendedGraphicsState extGState = new PDExtendedGraphicsState();
                    extGState.setBlendMode(BlendMode.MULTIPLY);
                    canvas.setGraphicsStateParameters(extGState);
                }
                result.save(new File(RESULT_FOLDER, "10and114multiply.pdf"));
            }
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/66122899/is-it-possible-to-repair-a-pdf-using-pdfbox">
     * Is it possible to “repair” a pdf using pdfbox?
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/10lYNfkQlUvxeZ2rFps2ozBO-zgGrnVeU/view?usp=sharing">
     * example_broken.pdf
     * </a>
     * <p>
     * I cannot reproduce Overlay issues with the given file.
     * But see {@link #testOverlayPreparationExampleBroken()}.
     * </p>
     */
    @Test
    public void testOverlayExampleBroken() throws IOException {
        try (   InputStream exampleBrokenFile = getClass().getResourceAsStream("example_broken.pdf");
                PDDocument empty = new PDDocument();
                PDDocument exampleBroken = PDDocument.load(exampleBrokenFile);
                Overlay overlayer = new Overlay()   ) {
            empty.addPage(new PDPage());
            overlayer.setInputPDF(empty);
            overlayer.setAllPagesOverlayPDF(exampleBroken);
            try (   PDDocument result = overlayer.overlay(Collections.emptyMap()) ) {
                result.save(new File(RESULT_FOLDER, "example_broken-overlayed.pdf"));
            }
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/66122899/is-it-possible-to-repair-a-pdf-using-pdfbox">
     * Is it possible to “repair” a pdf using pdfbox?
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/10lYNfkQlUvxeZ2rFps2ozBO-zgGrnVeU/view?usp=sharing">
     * example_broken.pdf
     * </a>
     * <p>
     * After the OP shared his code, it turns out that not the
     * overlaying (tested in {@link #testOverlayExampleBroken()})
     * but instead a preparation step is the problem: the page
     * resources in the document are inherited but the preparation
     * step loses everything inherited.
     * But see {@link #testOverlayPreparationFixedExampleBroken()}.
     * </p>
     */
    @Test
    public void testOverlayPreparationExampleBroken() throws IOException {
        try (   InputStream exampleBrokenFile = getClass().getResourceAsStream("example_broken.pdf");
                PDDocument finalOverlayDoc = new PDDocument();
                PDDocument overlayDocument = PDDocument.load(exampleBrokenFile) ) {
            Iterator<PDPage> overlayIterator = overlayDocument.getPages().iterator();
            while(overlayIterator.hasNext()) {
                PDPage pg = overlayIterator.next();
                finalOverlayDoc.addPage(pg);
            }
            finalOverlayDoc.save(new File(RESULT_FOLDER, "example_broken-preparedForOverlay.pdf"));
        }
        
    }

    /**
     * <a href="https://stackoverflow.com/questions/66122899/is-it-possible-to-repair-a-pdf-using-pdfbox">
     * Is it possible to “repair” a pdf using pdfbox?
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/10lYNfkQlUvxeZ2rFps2ozBO-zgGrnVeU/view?usp=sharing">
     * example_broken.pdf
     * </a>
     * <p>
     * After the OP shared his code, it turns out that not the
     * overlaying (tested in {@link #testOverlayExampleBroken()})
     * but instead a preparation step is the problem (tested in
     * {@link #testOverlayPreparationExampleBroken()}): the page
     * resources in the document are inherited but the preparation
     * step loses everything inherited. This can be fixed by
     * explicitly setting the page resources as shown here.
     * </p>
     */
    @Test
    public void testOverlayPreparationFixedExampleBroken() throws IOException {
        try (   InputStream exampleBrokenFile = getClass().getResourceAsStream("example_broken.pdf");
                PDDocument finalOverlayDoc = new PDDocument();
                PDDocument overlayDocument = PDDocument.load(exampleBrokenFile) ) {
            Iterator<PDPage> overlayIterator = overlayDocument.getPages().iterator();
            while(overlayIterator.hasNext()) {
                PDPage pg = overlayIterator.next();
                pg.setResources(pg.getResources());
                finalOverlayDoc.addPage(pg);
            }
            finalOverlayDoc.save(new File(RESULT_FOLDER, "example_broken-preparedForOverlay-Fixed.pdf"));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/74319965/pdfbox-2-0-27-watermarking-with-map-for-page-specific-overlay-files-results-in">
     * PDFBox 2.0.27: Watermarking with map for page specific overlay files results in no watermark
     * </a>
     * <br>
     * <a href="https://github.com/SeeAZO/stackoverflow_questions/tree/main/PDFBox_2.0.27_Watermark/res">
     * Original_PDF.pdf, A1_Horizontal_Watermark.pdf
     * </a>
     * <p>
     * The OP essentially re-discovered the regression <a href="https://issues.apache.org/jira/browse/PDFBOX-5523">PDFBOX-5523</a>. 
     * </p>
     */
    @Test
    public void testOverlayLikeSeeAZO() throws IOException {
        HashMap<Integer, String> overlayGuide = new HashMap<>();
        overlayGuide.put(1, "src\\test\\resources\\mkl\\testarea\\pdfbox2\\merge\\A1_Horizontal_Watermark.pdf");

        try (
            PDDocument originalPDF = PDDocument.load(getClass().getResourceAsStream("Original_PDF.pdf"));
            Overlay overlay = new Overlay();
        ) {
            overlay.setInputPDF(originalPDF);
            overlay.setOverlayPosition(Overlay.Position.BACKGROUND);
            PDDocument overlayedPDF = overlay.overlay(overlayGuide);

            overlayedPDF.save(new File(RESULT_FOLDER, "Original_PDF-with-A1_Horizontal_Watermark.pdf"));
            overlayedPDF.close();
        }
    }
}
