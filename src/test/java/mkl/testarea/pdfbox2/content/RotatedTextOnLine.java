package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class RotatedTextOnLine {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/52054396/rotate-text-in-pdfbox-with-java">
     * Rotate text in pdfbox with java
     * </a>
     * <p>
     * This test shows how to show rotated text above a line.
     * </p>
     */
    @Test
    public void testRotatedTextOnLineForCedrickKapema() throws IOException {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);
        PDPageContentStream cos = new PDPageContentStream(doc, page);
        cos.transform(Matrix.getRotateInstance(-Math.PI / 6, 100, 650));
        cos.moveTo(0, 0);
        cos.lineTo(125, 0);
        cos.stroke();
        cos.beginText();
        String text = "0.72";
        cos.newLineAtOffset(50, 5);
        cos.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cos.showText(text);
        cos.endText();
        cos.close();
        doc.save(new File(RESULT_FOLDER, "TextOnLine.pdf"));
        doc.close();
    }
}
