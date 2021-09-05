package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;

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
public class ArrangeText {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/46908322/apache-pdfbox-how-can-i-specify-the-position-of-the-texts-im-outputting">
     * Apache PDFBox: How can I specify the position of the texts I'm outputting
     * </a>
     * <p>
     * This test shows how to arrange text pieces using relative coordinates
     * to move from line start to line start.
     * </p>
     */
    @Test
    public void testArrangeTextForTeamotea() throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDFont font = new PDType1Font(FontName.HELVETICA);

            String text = "Text 1";
            String text1 = "Text 2";
            String text2 = "Text 3";
            String text3 = "Text 4";
            String text4 = "Text 5";
            String text5 = "Text 6";

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();

                contentStream.newLineAtOffset(175, 670);
                contentStream.setFont(font, 12);
                contentStream.setLeading(15);
                contentStream.showText(text);
                contentStream.newLine();
                contentStream.showText(text1);      

                contentStream.newLineAtOffset(225, 10);
                contentStream.setFont(font, 15);
                contentStream.showText(text2);      

                contentStream.newLineAtOffset(-390, -175);
                contentStream.setFont(font, 13.5f);
                contentStream.setLeading(17);
                contentStream.showText(text3);
                contentStream.newLine();
                contentStream.showText(text5);      

                contentStream.newLineAtOffset(300, 13.5f);
                contentStream.showText(text4);      

                contentStream.endText();

                contentStream.moveTo(0, 520);
                contentStream.lineTo(612, 520);
                contentStream.stroke();
            }

            document.save(new File(RESULT_FOLDER, "arrangedTextForTeamotea.pdf"));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/48902656/how-can-i-align-arrange-text-fields-into-two-column-layout-using-apache-pdfbox">
     * How can I Align/ Arrange text fields into two column layout using Apache PDFBox - java
     * </a>
     * <p>
     * This test shows how to align text in two columns.
     * </p>
     */
    @Test
    public void testArrangeTextForUser2967784() throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDFont fontNormal = new PDType1Font(FontName.HELVETICA);
        PDFont fontBold = new PDType1Font(FontName.HELVETICA_BOLD);
        PDPageContentStream contentStream =new PDPageContentStream(document, page);
        contentStream.beginText();
        contentStream.newLineAtOffset(100, 600);
        contentStream.setFont(fontBold, 15);
        contentStream.showText("Name: ");
        contentStream.setFont(fontNormal, 15);
        contentStream.showText ("Rajeev");
        contentStream.newLineAtOffset(200, 00);
        contentStream.setFont(fontBold, 15);
        contentStream.showText("Address: " );
        contentStream.setFont(fontNormal, 15);
        contentStream.showText ("BNG");
        contentStream.newLineAtOffset(-200, -20);
        contentStream.setFont(fontBold, 15);
        contentStream.showText("State: " );
        contentStream.setFont(fontNormal, 15);
        contentStream.showText ("KAR");
        contentStream.newLineAtOffset(200, 00);
        contentStream.setFont(fontBold, 15);
        contentStream.showText("Country: " );
        contentStream.setFont(fontNormal, 15);
        contentStream.showText ("INDIA");
        contentStream.endText();
        contentStream.close();
        document.save(new File(RESULT_FOLDER, "arrangedTextForUser2967784.pdf"));
    }
}
