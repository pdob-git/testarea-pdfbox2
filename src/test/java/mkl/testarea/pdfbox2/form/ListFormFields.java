package mkl.testarea.pdfbox2.form;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTerminalField;
import org.junit.Test;

/**
 * @author mkl
 */
public class ListFormFields
{
    /**
     * <a href="https://stackoverflow.com/questions/44817793/the-method-getkids-is-undefined-for-the-type-pdfield">
     * The method getKids() is undefined for the type PDField
     * </a>
     * <br/>
     * <a href="https://issues.apache.org/jira/secure/attachment/12651245/field%20name%20test.pdf">
     * field name test.pdf
     * </a>
     * <p>
     * The problems referred to don't exist anymore.
     * </p>
     */
    @Test
    public void testListFieldsInFieldNameTest() throws InvalidPasswordException, IOException
    {
        PDDocument doc = PDDocument.load(getClass().getResourceAsStream("field name test.pdf"));
        PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
        List<PDField> fields = form.getFields();
        for (int i=0; i<fields.size(); i++) {
            PDField f = fields.get(i);
            if (f instanceof PDTerminalField)
            {
                System.out.printf("%s, %s widgets\n", f.getFullyQualifiedName(), f.getWidgets().size());
                for (PDAnnotationWidget widget : f.getWidgets())
                    System.out.printf("  %s\n", widget.getAnnotationName());
            }
            else if (f instanceof PDNonTerminalField)
            {
                List<PDField> kids = ((PDNonTerminalField)f).getChildren();
                for (int j=0; j<kids.size(); j++) {
                    if (kids.get(j) instanceof PDField) {
                        PDField kidField = (PDField) kids.get(j);
                        System.out.println(kidField.getFullyQualifiedName());
                    }
                } 
            }
        }
    }

}
