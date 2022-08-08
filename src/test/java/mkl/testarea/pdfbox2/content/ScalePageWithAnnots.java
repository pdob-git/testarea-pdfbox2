package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.util.Matrix;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ScalePageWithAnnots {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/72349310/how-to-scale-a-pdannotation">
     * How to scale a PDAnnotation?
     * </a>
     * <br/>
     * <a href="https://github.com/amberream/Resources/raw/master/sample.pdf">
     * sample.pdf
     * </a> as "sampleSignedByAlias.pdf"
     * <p>
     * This test extends the OP's code to also scale the rectangle of the annotations.
     * Beware: For a complete solution more has to be scaled. On one hand there are
     * annotation types with additional coordinate fields like <b>QuadPoints</b> and
     * on the other hand there are additional properties to scale in particular for
     * annotations without appearance streams, like border width, font size, ...
     * </p>
     */
    @Test
    public void testForPereZix() throws IOException {
        try (
            InputStream resource = getClass().getResourceAsStream("/mkl/testarea/pdfbox2/sign/sampleSignedByAlias.pdf");
            PDDocument pdf = PDDocument.load(resource)
        ) {
            float letterWidth = PDRectangle.A5.getWidth();
            float letterHeight = PDRectangle.A5.getHeight();
            PDPageTree tree = pdf.getDocumentCatalog().getPages();
            for (PDPage page : tree) {
                if (page.getMediaBox().getWidth() > letterWidth || page.getMediaBox().getHeight() > letterHeight) {
                    float fWidth = letterWidth / page.getMediaBox().getWidth();
                    float fHeight = letterHeight / page.getMediaBox().getHeight();
                    float factor = Math.min(fWidth, fHeight);

                    PDPageContentStream contentStream = new PDPageContentStream(pdf, page, PDPageContentStream.AppendMode.PREPEND, false);
                    contentStream.transform(Matrix.getScaleInstance(factor, factor));
                    contentStream.close();
                    page.setMediaBox(PDRectangle.A5);

                    for (PDAnnotation pdAnnotation : page.getAnnotations()) {
                        PDRectangle rectangle = pdAnnotation.getRectangle();
                        PDRectangle scaled = new PDRectangle(factor * rectangle.getLowerLeftX(), factor * rectangle.getLowerLeftY(),
                                factor * rectangle.getWidth(), factor * rectangle.getHeight());
                        pdAnnotation.setRectangle(scaled);
                    }
                }
            }
            pdf.save(new File(RESULT_FOLDER, "sampleSignedByAlias-scaled.pdf"));
        }
    }

}
