package mkl.testarea.pdfbox2.form;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class FillInFormSaveIncremental
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/42802996/pdfbox-form-fill-saveincremental-does-not-work">
     * PDFBox Form fill - saveIncremental does not work
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/mmjel976l6sjn4w/Skierowanie3.pdf?dl=0">
     * Skierowanie3.pdf
     * </a>
     * <p>
     * The OP's code had to be changed in a number of ways. First of all
     * all changed objects including objects building a chain to each of
     * them starting at the catalog have to have <code>NeedToBeUpdated</code>
     * set to <code>true</code>. This causes the usage rights signature
     * also to be copied into the update section, so this signature has
     * to be removed. Furthermore the form is a hybrid AcroForm/XFA form.
     * As we only change the AcroForm entries, the xfa form also has to
     * be removed. 
     * </p>
     * @see #setField(PDDocument, String, String)
     */
    @Test
    public void testFillInSkierowanie3() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Skierowanie3.pdf"))
        {
            PDDocument document = PDDocument.load(resource);
            PDDocumentCatalog doc = document.getDocumentCatalog();
            PDAcroForm Form = doc.getAcroForm();

            String formName = "topmostSubform[0].Page1[0].pana_pania[0]";
            PDField f = Form.getField(formName);
            setField(document, formName, "Artur");
            System.out.println("New value 2nd: " + f.getValueAsString());

            COSDictionary dictionary = document.getDocumentCatalog().getCOSObject();
            dictionary.removeItem(COSName.PERMS);
            dictionary.setNeedToBeUpdated(true);
            dictionary = (COSDictionary) dictionary.getDictionaryObject(COSName.ACRO_FORM);
            dictionary.removeItem(COSName.XFA);
            dictionary.setNeedToBeUpdated(true);
            COSArray array = (COSArray) dictionary.getDictionaryObject(COSName.FIELDS);
            array.setNeedToBeUpdated(true);

            document.saveIncremental(new FileOutputStream(new File(RESULT_FOLDER, "Skierowanie3-filledIncr.pdf")));
            document.close();
        }
    }

    /**
     * This helper method is part of the {@link #testFillInSkierowanie3()}
     * test case.
     */
    public static void setField(PDDocument pdfDocument, String name, String Value) throws IOException 
    {
        PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
        PDAcroForm acroForm = docCatalog.getAcroForm();
        PDField field = acroForm.getField(name);

        if (field instanceof PDCheckBox){
            field.setValue("Yes");
        }
        else if (field instanceof PDTextField){
            System.out.println("Original value: " + field.getValueAsString());
            field.setValue(Value);
            System.out.println("New value: " + field.getValueAsString());
        }
        else{
            System.out.println("Nie znaleziono pola");
        }

        COSDictionary fieldDictionary = field.getCOSObject();
        COSDictionary dictionary = (COSDictionary) fieldDictionary.getDictionaryObject(COSName.AP);
        dictionary.setNeedToBeUpdated(true);
        COSStream stream = (COSStream) dictionary.getDictionaryObject(COSName.N);
        stream.setNeedToBeUpdated(true);
        while (fieldDictionary != null)
        {
            fieldDictionary.setNeedToBeUpdated(true);
            fieldDictionary = (COSDictionary) fieldDictionary.getDictionaryObject(COSName.PARENT);
        }
        
    }
}
