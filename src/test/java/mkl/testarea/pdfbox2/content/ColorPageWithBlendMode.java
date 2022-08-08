package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.junit.BeforeClass;
import org.junit.Test;

public class ColorPageWithBlendMode {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-5101">
     * White boxes after adding color in background
     * </a>
     * <br/>
     * <a href="https://issues.apache.org/jira/secure/attachment/13020391/without%20BG%20color.PDF">
     * without BG color.PDF
     * </a>
     * <p>
     * This test shows how to apply a color to a page using Darken
     * Blend Mode.
     * </p>
     */
    @Test
    public void testForWithoutBGColor() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("without BG color.PDF");
                PDDocument document = PDDocument.load(resource)  ) {
            PDExtendedGraphicsState gState = new PDExtendedGraphicsState();
            gState.setBlendMode(BlendMode.DARKEN);

            for (PDPage page : document.getDocumentCatalog().getPages()) {
                PDRectangle cropBox = page.getCropBox();
                try (   PDPageContentStream canvas = new PDPageContentStream(document, page, AppendMode.APPEND, false, true)    ) {
                    canvas.setGraphicsStateParameters(gState);
                    canvas.setNonStrokingColor(0.95686f, 0.8f, 0.8f);
                    canvas.addRect(cropBox.getLowerLeftX(), cropBox.getLowerLeftY(), cropBox.getWidth(), cropBox.getHeight());
                    canvas.fill();
                }
            }

            document.save(new File(RESULT_FOLDER, "without BG color-darkened.PDF"));
        }
    }

}
