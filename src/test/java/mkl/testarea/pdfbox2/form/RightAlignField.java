package mkl.testarea.pdfbox2.form;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.apache.pdfbox.pdmodel.interactive.form.PDVariableText;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class RightAlignField {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/55355800/acroform-text-field-not-align-to-right">
     * acroform text field not align to right
     * </a>
     * <br/>
     * <a href="https://drive.google.com/uc?id=1jFbsYGFOnx8EMiHgDsE8LQtfwJHSa5Gh&export=download">
     * form.pdf 
     * </a> as "formBee2.pdf"
     * <p>
     * Indeed, the way the OP does this, quadding does not apply.
     * But see {@link #testAlignLikeBeeImproved()}.
     * </p>
     */
    @Test
    public void testAlignLikeBee() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("formBee2.pdf")    ) {
            PDDocument document = PDDocument.load(resource);
            PDDocumentCatalog documentCatalog = document.getDocumentCatalog();
            PDAcroForm acroForm = documentCatalog.getAcroForm();

            acroForm.getField("NewRentWithoutChargesChf").setValue("1.00");
            ((PDTextField) acroForm.getField("NewRentWithoutChargesChf")).setQ(PDVariableText.QUADDING_RIGHT);

            document.save(new File(RESULT_FOLDER, "formBee2-AlignLikeBee.pdf"));        
            document.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/55355800/acroform-text-field-not-align-to-right">
     * acroform text field not align to right
     * </a>
     * <br/>
     * <a href="https://drive.google.com/uc?id=1jFbsYGFOnx8EMiHgDsE8LQtfwJHSa5Gh&export=download">
     * form.pdf 
     * </a> as "formBee2.pdf"
     * <p>
     * Indeed, in {@link #testAlignLikeBee()} quadding does not apply.
     * But by changing the order of field value setting and quadding
     * value setting it suddenly does apply.
     * </p>
     */
    @Test
    public void testAlignLikeBeeImproved() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("formBee2.pdf")    ) {
            PDDocument document = PDDocument.load(resource);
            PDDocumentCatalog documentCatalog = document.getDocumentCatalog();
            PDAcroForm acroForm = documentCatalog.getAcroForm();

            ((PDTextField) acroForm.getField("NewRentWithoutChargesChf")).setQ(PDVariableText.QUADDING_RIGHT);
            acroForm.getField("NewRentWithoutChargesChf").setValue("1.00");

            document.save(new File(RESULT_FOLDER, "formBee2-AlignLikeBeeImproved.pdf"));        
            document.close();
        }
    }
}
