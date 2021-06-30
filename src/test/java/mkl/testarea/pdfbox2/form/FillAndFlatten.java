package mkl.testarea.pdfbox2.form;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.BeforeClass;
import org.junit.Test;
/**
 * @author mkl
 */
public class FillAndFlatten {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/60964782/pdfbox-inconsistent-pdtextfield-behaviour-after-setvalue">
     * PDFBox Inconsistent PDTextField Behaviour after setValue
     * </a>
     * </br/>
     * <a href="https://s3-us-west-2.amazonaws.com/kx-filing-docs/b3-3.pdf">
     * b3-3.pdf
     * </a>
     * <p>
     * Indeed, PDFBox assumes in some fields that it should not create
     * field appearances which a formatting additional action would do
     * differently anyways in a viewer. In a flattening use case this is
     * obviously incorrect.
     * </p>
     * @see #testLikeAbubakarRemoveAction()
     */
    @Test
    public void testLikeAbubakar() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("b3-3.pdf");
                PDDocument pdDocument = Loader.loadPDF(resource)    ) {
            PDDocumentCatalog catalog = pdDocument.getDocumentCatalog();
            PDAcroForm acroForm = catalog.getAcroForm();
            int i = 0;
            for (PDField field : acroForm.getFields()) {
                i=i+1;
                if (field instanceof PDTextField) {
                    PDTextField textField = (PDTextField) field;
                    textField.setValue(Integer.toString(i));
                }
            }

            pdDocument.getDocumentCatalog().getAcroForm().flatten();

            pdDocument.save(new File(RESULT_FOLDER, "b3-3-filled-and-flattened.pdf"));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/60964782/pdfbox-inconsistent-pdtextfield-behaviour-after-setvalue">
     * PDFBox Inconsistent PDTextField Behaviour after setValue
     * </a>
     * </br/>
     * <a href="https://s3-us-west-2.amazonaws.com/kx-filing-docs/b3-3.pdf">
     * b3-3.pdf
     * </a>
     * <p>
     * After removing the actions, PDFBox again sets appearances in
     * all fields.
     * </p>
     * @see #testLikeAbubakar()
     */
    @Test
    public void testLikeAbubakarRemoveAction() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("b3-3.pdf");
                PDDocument pdDocument = Loader.loadPDF(resource)    ) {
            PDDocumentCatalog catalog = pdDocument.getDocumentCatalog();
            PDAcroForm acroForm = catalog.getAcroForm();
            int i = 0;
            for (PDField field : acroForm.getFields()) {
                i=i+1;
                if (field instanceof PDTextField) {
                    PDTextField textField = (PDTextField) field;
                    textField.setActions(null);
                    textField.setValue(Integer.toString(i));
                }
            }

            pdDocument.getDocumentCatalog().getAcroForm().flatten();

            pdDocument.save(new File(RESULT_FOLDER, "b3-3-remove-action-filled-and-flattened.pdf"));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/68190255/pdfbox-field-value-cannot-display-after-flatten">
     * PDFBox field value cannot display after flatten
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1VPIMe3UltcDcmHDpyVs_2eMtXDkiSHhU/view?usp=sharing">
     * test.pdf
     * </a> as "testVinciChung.pdf"
     * <p>
     * Works as expected. The OP should simply update his PDFBox dependency.
     * </p>
     */
    @Test
    public void testLikeVinciChung() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("testVinciChung.pdf");
                PDDocument pdDocument = Loader.loadPDF(resource)    ) {
            PDAcroForm acroForm = pdDocument.getDocumentCatalog().getAcroForm();

            String defaultAppearance = "/Helv 12 Tf 0 0 1 rg";

            for(PDField field : acroForm.getFields()) {
                if (field instanceof PDTextField) {
                    PDTextField textField = (PDTextField) field;
                    textField.setActions(null);             
                    textField.setDefaultAppearance(defaultAppearance);
                }
            }

            setFormValue(acroForm, "TextBox", "testvalue");
            setFormValue(acroForm, "radioBtn", "Yes");
            setFormValue(acroForm, "chkBox", "Yes");
            acroForm.refreshAppearances();
            acroForm.flatten();

            pdDocument.save(new File(RESULT_FOLDER, "testVinciChung-filled-and-flattened.pdf"));
        }
    }

    /**
     * @see #testLikeVinciChung()
     */
    private static void setFormValue(PDAcroForm acroForm, String key, String value) throws IOException {
        PDField f = acroForm.getField(key);
        if (f != null) {
            if (value != null && value.trim().length() > 0) {
                f.setValue(value);
            }
        }
    }
}
