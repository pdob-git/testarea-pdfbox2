package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class TextAndGraphics
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/44503236/how-to-write-text-draw-a-line-and-then-again-write-text-in-a-pdf-file-using-pdf">
     * How to write text, draw a line and then again write text in a pdf file using PDFBox
     * </a>
     * <p>
     * This test shows how to draw tetx, then graphics, then again text.
     * </p>
     */
    @Test
    public void testDrawTextLineText() throws IOException
    {
        PDFont font = new PDType1Font(FontName.HELVETICA);
        float fontSize = 14;
        float fontHeight = fontSize;
        float leading = 20;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        Date date = new Date();

        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(doc, page);
        contentStream.setFont(font, fontSize);

        float yCordinate = page.getCropBox().getUpperRightY() - 30;
        float startX = page.getCropBox().getLowerLeftX() + 30;
        float endX = page.getCropBox().getUpperRightX() - 30;

        contentStream.beginText();
        contentStream.newLineAtOffset(startX, yCordinate);
        contentStream.showText("Entry Form � Header");
        yCordinate -= fontHeight;  //This line is to track the yCordinate
        contentStream.newLineAtOffset(0, -leading);
        yCordinate -= leading;
        contentStream.showText("Date Generated: " + dateFormat.format(date));
        yCordinate -= fontHeight;
        contentStream.endText(); // End of text mode

        contentStream.moveTo(startX, yCordinate);
        contentStream.lineTo(endX, yCordinate);
        contentStream.stroke();
        yCordinate -= leading;

        contentStream.beginText();
        contentStream.newLineAtOffset(startX, yCordinate);
        contentStream.showText("Name: XXXXX");
        contentStream.endText();

        contentStream.close();
        doc.save(new File(RESULT_FOLDER, "textLineText.pdf"));
    }
}
