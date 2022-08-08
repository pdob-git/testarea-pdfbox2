package mkl.testarea.pdfbox2.form;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTerminalField;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author mkl
 */
public class ShowFormFieldNames
{
    /**
     * <a href="http://stackoverflow.com/questions/39574021/how-can-the-internal-labels-of-the-editable-fields-in-an-acroform-pdf-be-found">
     * How can the internal labels of the editable fields in an acroform .pdf be found and listed?
     * </a>
     * <p>
     * This test method prints the fields of the arbitrarily chosen file "ds872.pdf". 
     * </p>
     */
    @Test
    public void testShowFieldsForDs872() throws IOException
    {
        String resourceName = "ds872.pdf";
        try (   InputStream resource = getClass().getResourceAsStream(resourceName) )
        {
            PDDocument pdDocument = PDDocument.load(resource);
            List<String> fieldNames = getFormFieldNames(pdDocument);

            System.out.printf("\nForm field names of '%s':\n", resourceName);
            for (String name : fieldNames)
            {
                System.out.printf("* '%s'\n", name);
            }

            List<String> fieldNamesFancy = getFormFieldNamesFancy(pdDocument);
            Assert.assertEquals(fieldNames, fieldNamesFancy);
        }
        
    }

    /**
     * <a href="http://stackoverflow.com/questions/39574021/how-can-the-internal-labels-of-the-editable-fields-in-an-acroform-pdf-be-found">
     * How can the internal labels of the editable fields in an acroform .pdf be found and listed?
     * </a>
     * <p>
     * This method retrieves the form field names from the given {@link PDDocument}. 
     * </p>
     */
    List<String> getFormFieldNames(PDDocument pdDocument)
    {
        PDAcroForm pdAcroForm = pdDocument.getDocumentCatalog().getAcroForm();
        if (pdAcroForm == null)
            return Collections.emptyList();

        List<String> result = new ArrayList<>();
        for (PDField pdField : pdAcroForm.getFieldTree())
        {
            if (pdField instanceof PDTerminalField)
            {
                result.add(pdField.getFullyQualifiedName());
            }
        }
        return result;
    }

    /**
     * <a href="http://stackoverflow.com/questions/39574021/how-can-the-internal-labels-of-the-editable-fields-in-an-acroform-pdf-be-found">
     * How can the internal labels of the editable fields in an acroform .pdf be found and listed?
     * </a>
     * <p>
     * This method retrieves the form field names from the given {@link PDDocument}
     * using a bit more fancy streaming methods. 
     * </p>
     */
    List<String> getFormFieldNamesFancy(PDDocument pdDocument)
    {
        PDAcroForm pdAcroForm = pdDocument.getDocumentCatalog().getAcroForm();
        if (pdAcroForm == null)
            return Collections.emptyList();

        return StreamSupport.stream(pdAcroForm.getFieldTree().spliterator(), false)
                            .filter(field -> (field instanceof PDTerminalField))
                            .map(field -> field.getFullyQualifiedName())
                            .collect(Collectors.toList());
    }
}
