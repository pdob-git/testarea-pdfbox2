package mkl.testarea.pdfbox2.content;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class CreateHelloWorld {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/68904812/pdfbox-issue-with-pdf-generation-3-0-0-rc1-vs-2-0-24">
     * Pdfbox - Issue with pdf generation 3.0.0-RC1 vs 2.0.24
     * </a>
     * <p>
     * I can find no issue with this file.
     * </p>
     */
    @Test
    public void testLikeOveb() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();

            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.TIMES_ROMAN, 12);
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(25, 700);
                contentStream.showText("Hello World");
                contentStream.endText();
                // Make sure that the content stream is closed:
                contentStream.close();
            }

            document.save(output);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Files.write(new File(RESULT_FOLDER, "HelloWorld.pdf").toPath(), output.toByteArray());
    }
}
