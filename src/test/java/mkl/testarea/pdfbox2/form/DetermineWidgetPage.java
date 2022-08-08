package mkl.testarea.pdfbox2.form;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.junit.Test;

/**
 * @author mkl
 */
public class DetermineWidgetPage
{
    /**
     * <a href="https://stackoverflow.com/questions/22074449/how-to-know-if-a-field-is-on-a-particular-page">
     * how to know if a field is on a particular page?
     * </a>
     * <p>
     * This sample document does not contain the optional page entry in its annotations.
     * Thus, the fast method fails in contrast to the safe one.
     * </p>
     */
    @Test
    public void testAFieldTwice() throws IOException
    {
        System.out.println("aFieldTwice.pdf\n=================");
        try (   InputStream resource = getClass().getResourceAsStream("aFieldTwice.pdf")    )
        {
            PDDocument document = PDDocument.load(resource);
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (acroForm != null)
            {
                for (PDField field : acroForm.getFieldTree())
                {
                    System.out.println(field.getFullyQualifiedName());
                    for (PDAnnotationWidget widget : field.getWidgets())
                    {
                        System.out.print(widget.getAnnotationName() != null ? widget.getAnnotationName() : "(NN)");
                        System.out.printf(" - fast: %s", determineFast(document, widget));
                        System.out.printf(" - safe: %s\n", determineSafe(document, widget));
                    }
                }
            }
        }
        System.out.println();
    }

    /**
     * <a href="https://stackoverflow.com/questions/22074449/how-to-know-if-a-field-is-on-a-particular-page">
     * how to know if a field is on a particular page?
     * </a>
     * <p>
     * This sample document contains the optional page entry in its annotations.
     * Thus, the fast method returns the same result as the safe one.
     * </p>
     */
    @Test
    public void testTestDuplicateField2() throws IOException
    {
        System.out.println("test_duplicate_field2.pdf\n=================");
        try (   InputStream resource = getClass().getResourceAsStream("test_duplicate_field2.pdf")    )
        {
            PDDocument document = PDDocument.load(resource);
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (acroForm != null)
            {
                for (PDField field : acroForm.getFieldTree())
                {
                    System.out.println(field.getFullyQualifiedName());
                    for (PDAnnotationWidget widget : field.getWidgets())
                    {
                        System.out.print(widget.getAnnotationName() != null ? widget.getAnnotationName() : "(NN)");
                        System.out.printf(" - fast: %s", determineFast(document, widget));
                        System.out.printf(" - safe: %s\n", determineSafe(document, widget));
                    }
                }
            }
        }
        System.out.println();
    }

    int determineFast(PDDocument document, PDAnnotationWidget widget)
    {
        PDPage page = widget.getPage();
        return page != null ? document.getPages().indexOf(page) : -1;
    }

    int determineSafe(PDDocument document, PDAnnotationWidget widget) throws IOException
    {
        COSDictionary widgetObject = widget.getCOSObject();
        PDPageTree pages = document.getPages();
        for (int i = 0; i < pages.getCount(); i++)
        {
            for (PDAnnotation annotation : pages.get(i).getAnnotations())
            {
                COSDictionary annotationObject = annotation.getCOSObject();
                if (annotationObject.equals(widgetObject))
                    return i;
            }
        }
        return -1;
    }
}
