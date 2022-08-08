package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ScaleText {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/71237452/pdfbox-pdpagecontentstream-settextmatrix-making-text-disappear">
     * PDFBox PDPageContentstream.setTextMatrix() making text disappear
     * </a>
     * <p>
     * Cannot reproduce the issue, in all cases the text appears.
     * </p>
     */
    @Test
    public void testScaleLikeLinusHoja() throws IOException {
        try (   PDDocument pdDocument = new PDDocument()    ) {
            PDFont font = PDType1Font.HELVETICA;

            PDPage page = new PDPage(new PDRectangle(300, 200));
            pdDocument.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(pdDocument, page)) {
                contentStream.setFont(font, 10);
                contentStream.beginText();
                Matrix textMatrix = new Matrix();
                textMatrix.scale(1f, 2f);
                contentStream.setTextMatrix(textMatrix);
                contentStream.newLineAtOffset(50, 50);
                contentStream.setCharacterSpacing(7);
                contentStream.showText("Scale by (1f, 2f)");
                contentStream.endText();
            }

            page = new PDPage(new PDRectangle(300, 200));
            pdDocument.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(pdDocument, page)) {
                contentStream.setFont(font, 10);
                contentStream.beginText();
                Matrix textMatrix = new Matrix();
                textMatrix.scale(1f, 1f);
                contentStream.setTextMatrix(textMatrix);
                contentStream.newLineAtOffset(50, 50);
                contentStream.setCharacterSpacing(7);
                contentStream.showText("Scale by (1f, 1f)");
                contentStream.endText();
            }

            page = new PDPage(new PDRectangle(300, 200));
            pdDocument.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(pdDocument, page)) {
                contentStream.setFont(font, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 50);
                contentStream.setCharacterSpacing(7);
                contentStream.showText("No scale");
                contentStream.endText();
            }

            pdDocument.save(new File(RESULT_FOLDER, "ScaleTextLikeLinusHoja.pdf"));
        }
    }

}
