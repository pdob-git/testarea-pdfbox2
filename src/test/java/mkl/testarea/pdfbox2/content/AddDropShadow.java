package mkl.testarea.pdfbox2.content;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.ByteStreams;

/**
 * @author mkl
 */
public class AddDropShadow {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/68020506/need-help-on-applying-drop-shadow-effects-to-a-pdf-box-components-in-java">
     * Need help on applying drop shadow effects to a pdf box components in java
     * </a>
     * <p>
     * This test shows how to apply a simply drop shadow to a rectangle. It may
     * look fancier if one applied a bit of blur to the shadow.
     * </p>
     */
    @Test
    public void testRectangleWithDropShadow() throws IOException {
        PDDocument doc = new PDDocument();
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, ByteStreams.toByteArray(getClass().getResourceAsStream("Willi-1.jpg")), "Willi-1.jpg");
        PDPage page = new PDPage();
        doc.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(doc, page);

        int left = -60;
        int top = 96;
        int width = 471;
        int height = 365;

        contentStream.setStrokingColor(1, 0, 0);
        contentStream.setLineWidth(4);

        int imageOriginalWidth = 633;
        int imageOriginalHeight = 422;

        float scaleX = 0.99f;
        float scaleY = 0.99f;

        float imageWidth = imageOriginalWidth*scaleX;
        float imageHeight = imageOriginalHeight*scaleY;

        float imageY = page.getMediaBox().getHeight() - (top + imageHeight-58); 
        float imageX = -104; 

        // vvv--- code added for shadow
        PDExtendedGraphicsState extGS = new PDExtendedGraphicsState();
        extGS.setNonStrokingAlphaConstant(.2f);
        contentStream.saveGraphicsState();
        contentStream.setGraphicsStateParameters(extGS);
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.addRect(left + 5, page.getMediaBox().getHeight() - top - height - 5, width, height);
        contentStream.fill();
        contentStream.restoreGraphicsState();
        // ^^^--- code added for shadow

        contentStream.addRect(left, page.getMediaBox().getHeight() - top - height, width, height);
        contentStream.clip();
        contentStream.drawImage(pdImage, imageX, imageY, imageWidth, imageHeight);
        contentStream.close();

        doc.save(new File(RESULT_FOLDER, "RectangleWithDropShadow.pdf"));
        doc.close();
    }

}
