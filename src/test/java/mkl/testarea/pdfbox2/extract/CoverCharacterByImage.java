package mkl.testarea.pdfbox2.extract;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class CoverCharacterByImage {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/50329988/placing-an-image-over-text-by-using-the-text-postiton-in-a-pdf-using-pdfbox">
     * Placing an image over text, by using the text postiton in a PDF using PDFBox.
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1SCoB1RyvQSNy3aVjj_KZ70IO2ksN6qfM/view?usp=sharing">
     * EMPLOYMENTCONTRACTTEMPLATE.pdf
     * </a>
     * <p>
     * This together with the {@link CoverCharByImage} class is an overhauled copy of the OP's code.
     * Changes in particular: 
     * </p>
     * <ul>
     * <li>Replaced <code>get*DirAdj()</code> calls by <code>getTextMatrix().getTranslate*() + cropBox.getLowerLeft*()</code> expressions;
     * <li>Switched on context reset in <code>PDPageContentStream</code> constructor;
     * <li>Switched to creation of a single <code>PDPageContentStream</code> instance per page only.
     * </ul>
     */
    @Test
    public void testCoverLikeLez() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("EMPLOYMENTCONTRACTTEMPLATE.pdf")) {
            PDDocument pdocument = PDDocument.load(resource);

            String imagePath = "src/test/resources/mkl/testarea/pdfbox2/content/Willi-1.jpg";
            PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, pdocument);

            CoverCharByImage stripper = new CoverCharByImage(pdImage);
            stripper.setSortByPosition(true);
            Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
            stripper.writeText(pdocument, dummy);
            pdocument.save(new File(RESULT_FOLDER, "EMPLOYMENTCONTRACTTEMPLATE-coveredAs.pdf"));
        }
    }

    /**
     * @see CoverCharacterByImage#testCoverLikeLez()
     */
    class CoverCharByImage extends PDFTextStripper {
        public CoverCharByImage(PDImageXObject pdImage) throws IOException {
            super();
            this.pdImage = pdImage;
        }

        final PDImageXObject pdImage;
        PDPageContentStream contentStream = null;

        @Override
        public void processPage(PDPage page) throws IOException {
            super.processPage(page);
            if (contentStream != null) {
                contentStream.close();
                contentStream = null;
            }
        }

        @Override
        protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
            if (contentStream == null)
                contentStream = new PDPageContentStream(document, getCurrentPage(), AppendMode.APPEND, true, true);

            PDRectangle cropBox = getCurrentPage().getCropBox();

            for (TextPosition text : textPositions) {
                if (text.getUnicode().equals("a")) {
                    contentStream.drawImage(pdImage, text.getTextMatrix().getTranslateX() + cropBox.getLowerLeftX(),
                            text.getTextMatrix().getTranslateY() + cropBox.getLowerLeftY(),
                            text.getWidthDirAdj(), text.getHeightDir());
                }
            }
        }
    }
}
