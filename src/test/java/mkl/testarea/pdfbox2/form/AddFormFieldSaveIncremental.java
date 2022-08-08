package mkl.testarea.pdfbox2.form;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class AddFormFieldSaveIncremental {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/62601879/pdfbox-2-0-create-signature-field-and-save-incremental-with-already-signed-docum">
     * PDFBox 2.0 create signature field and save incremental with already signed document
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1jFfYNtf9YHi7eIXsdyqIeC-18CASyglT/view?usp=sharing">
     * test-semnat.pdf
     * </a>
     * <p>
     * The remaining problem after the question update is that the OP replaces
     * the existing AcroForm definition. That changes too much, more than allowed
     * and more than he actually wants. What he wants is to retrieve the existing
     * AcroForm definition and only mark it and its Fields entry for saving.
     * </p>
     */
    @Test
    public void testTestSemnat() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("test-semnat.pdf"); ) {
            PDDocument document = PDDocument.load(resource);

            PDPage page = document.getPage(0);

//instead of
//            // Add a new AcroForm and add that to the document
//            PDAcroForm acroForm = new PDAcroForm(document);
//            document.getDocumentCatalog().setAcroForm(acroForm);
//use
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            acroForm.getCOSObject().setNeedToBeUpdated(true);
            COSObject fields = acroForm.getCOSObject().getCOSObject(COSName.FIELDS);
            if (fields != null)
                fields.setNeedToBeUpdated(true);
//

            acroForm.setSignaturesExist(true);
            acroForm.setAppendOnly(true);
            acroForm.getCOSObject().setDirect(true);

            // Create empty signature field, it will get the name "Signature1"
            PDSignatureField signatureField = new PDSignatureField(acroForm);
            PDAnnotationWidget widget = signatureField.getWidgets().get(0);
            PDRectangle rect = new PDRectangle(50, 250, 200, 50);
            widget.setRectangle(rect);
            widget.getCOSObject().setNeedToBeUpdated(true);
            widget.setPage(page);
            page.getAnnotations().add(widget);
            page.getCOSObject().setNeedToBeUpdated(true);
            acroForm.getFields().add(signatureField);

            // general updates
            document.getDocumentCatalog().getCOSObject().setNeedToBeUpdated(true);

            OutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "test-semnat-Field.pdf"));
            document.saveIncremental(os);
            System.out.println("done");
        }
    }

}
