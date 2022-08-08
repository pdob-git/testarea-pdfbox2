package mkl.testarea.pdfbox2.form;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTerminalField;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <a href="https://stackoverflow.com/questions/47190189/does-pdfbox-allow-to-remove-one-field-from-acroform">
 * Does PDFBox allow to remove one field from AcroForm?
 * </a>
 * <p>
 * This class contains
 * </p>
 * <ul>
 * <li> {@link #testRemoveFormIntroSsnManually()} - a test that explicitly
 *  contains code to remove a specific field from a PDF;</li>
 * <li> {@link #removeField(PDDocument, String)} and {@link #removeWidgets(PDField)} -
 *  helper methods generalizing the approach in the test above;</li>
 * <li> {@link #testRemoveFormIntroSsn()}, {@link #testRemoveFormIntro()}, and
 *  {@link #testRemoveFormRoot()} - tests of those helper methods applied to a
 *  non-root terminal field, a non-root non-terminal field, and a root non-terminal
 *  field respectively;</li>
 * <li> {@link #testRemoveInvisibleSignature()} and {@link #testRemoveVisibleSignature()} -
 *  tests of those helper methods applied to root terminal fields which are signature
 *  fields, invisible or visible.</li>
 * </ul>
 * 
 * @author mkl
 */
public class RemoveField {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void testRemoveFormIntroSsnManually() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("GeneralForbearance.pdf")    ) {
            PDDocument document = PDDocument.load(resource);
            PDDocumentCatalog documentCatalog = document.getDocumentCatalog();
            PDAcroForm acroForm = documentCatalog.getAcroForm();

            PDField introSsnField = null;

            for (PDField field : acroForm.getFieldTree()) {
                System.out.println(field.getFullyQualifiedName());
                if ("form1[0].#subform[0].FormIntro[0].SSN[0]".equals(field.getFullyQualifiedName())) {
                    introSsnField = field;
                    break;
                }
            }
            Assert.assertNotNull("introSsnField", introSsnField);
            Assert.assertTrue("introSsnField not terminal", introSsnField instanceof PDTerminalField);

            PDNonTerminalField introField = introSsnField.getParent();
            Assert.assertNotNull("introField", introField);
            List<PDField> introChildFields = introField.getChildren();
            boolean removed = false;
            for (PDField field : introChildFields)
            {
                if (field.getCOSObject().equals(introSsnField.getCOSObject())) {
                    removed = introChildFields.remove(field);
                    break;
                }
            }
            introField.setChildren(introChildFields);
            Assert.assertTrue("introSsnField not removed from introField", removed);

            List<PDAnnotationWidget> widgets = ((PDTerminalField)introSsnField).getWidgets();
            for (PDAnnotationWidget widget : widgets) {
                System.out.println("Removing FormIntro field SSN widget from page");
                PDPage page = widget.getPage();
                Assert.assertNotNull("FormIntro field SSN widget has no page", page);
                List<PDAnnotation> annotations = page.getAnnotations();
                removed = false;
                for (PDAnnotation annotation : annotations) {
                    if (annotation.getCOSObject().equals(widget.getCOSObject()))
                    {
                        removed = annotations.remove(annotation);
                        break;
                    }
                }
                Assert.assertTrue("FormIntro field SSN widget not removed from page", removed);
            }

            document.save(new File(RESULT_FOLDER, "GeneralForbearance-RemoveFormIntroSsnManually.pdf"));
            document.close();
        }
    }

    @Test
    public void testRemoveFormIntroSsn() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("GeneralForbearance.pdf")    ) {
            PDDocument document = PDDocument.load(resource);

            PDField field = removeField(document, "form1[0].#subform[0].FormIntro[0].SSN[0]");
            Assert.assertNotNull("Field not found", field);

            document.save(new File(RESULT_FOLDER, "GeneralForbearance-RemoveFormIntroSsn.pdf"));
            document.close();
        }
    }

    @Test
    public void testRemoveFormIntro() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("GeneralForbearance.pdf")    ) {
            PDDocument document = PDDocument.load(resource);

            PDField field = removeField(document, "form1[0].#subform[0].FormIntro[0]");
            Assert.assertNotNull("Field not found", field);

            document.save(new File(RESULT_FOLDER, "GeneralForbearance-RemoveFormIntro.pdf"));
            document.close();
        }
    }

    @Test
    public void testRemoveFormRoot() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("GeneralForbearance.pdf")    ) {
            PDDocument document = PDDocument.load(resource);

            PDField field = removeField(document, "form1[0]");
            Assert.assertNotNull("Field not found", field);

            document.save(new File(RESULT_FOLDER, "GeneralForbearance-RemoveFormRoot.pdf"));
            document.close();
        }
    }

    @Test
    public void testRemoveInvisibleSignature() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/pdfbox2/sign/SignatureVlidationTest.pdf")    ) {
            PDDocument document = PDDocument.load(resource);

            PDField field = removeField(document, "Signature1");
            Assert.assertNotNull("Field not found", field);

            document.save(new File(RESULT_FOLDER, "SignatureVlidationTest-RemoveSignature1.pdf"));
            document.close();
        }
    }

    @Test
    public void testRemoveVisibleSignature() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("2g-fix-certified.pdf")    ) {
            PDDocument document = PDDocument.load(resource);

            PDField field = removeField(document, "sig");
            Assert.assertNotNull("Field not found", field);

            document.save(new File(RESULT_FOLDER, "2g-fix-certified-RemoveSig.pdf"));
            document.close();
        }
    }

    PDField removeField(PDDocument document, String fullFieldName) throws IOException {
        PDDocumentCatalog documentCatalog = document.getDocumentCatalog();
        PDAcroForm acroForm = documentCatalog.getAcroForm();

        if (acroForm == null) {
            System.out.println("No form defined.");
            return null;
        }

        PDField targetField = null;

        for (PDField field : acroForm.getFieldTree()) {
            if (fullFieldName.equals(field.getFullyQualifiedName())) {
                targetField = field;
                break;
            }
        }
        if (targetField == null) {
            System.out.println("Form does not contain field with given name.");
            return null;
        }

        PDNonTerminalField parentField = targetField.getParent();
        if (parentField != null) {
            List<PDField> childFields = parentField.getChildren();
            boolean removed = false;
            for (PDField field : childFields)
            {
                if (field.getCOSObject().equals(targetField.getCOSObject())) {
                    removed = childFields.remove(field);
                    parentField.setChildren(childFields);
                    break;
                }
            }
            if (!removed)
                System.out.println("Inconsistent form definition: Parent field does not reference the target field.");
        } else {
            List<PDField> rootFields = acroForm.getFields();
            boolean removed = false;
            for (PDField field : rootFields)
            {
                if (field.getCOSObject().equals(targetField.getCOSObject())) {
                    removed = rootFields.remove(field);
                    break;
                }
            }
            if (!removed)
                System.out.println("Inconsistent form definition: Root fields do not include the target field.");
        }

        removeWidgets(targetField);

        return targetField;
    }

    void removeWidgets(PDField targetField) throws IOException {
        if (targetField instanceof PDTerminalField) {
            List<PDAnnotationWidget> widgets = ((PDTerminalField)targetField).getWidgets();
            for (PDAnnotationWidget widget : widgets) {
                PDPage page = widget.getPage();
                if (page != null) {
                    List<PDAnnotation> annotations = page.getAnnotations();
                    boolean removed = false;
                    for (PDAnnotation annotation : annotations) {
                        if (annotation.getCOSObject().equals(widget.getCOSObject()))
                        {
                            removed = annotations.remove(annotation);
                            break;
                        }
                    }
                    if (!removed)
                        System.out.println("Inconsistent annotation definition: Page annotations do not include the target widget.");
                } else {
                    System.out.println("Widget annotation does not have an associated page; cannot remove widget.");
                    // TODO: In this case iterate all pages and try to find and remove widget in all of them
                }
            }
        } else if (targetField instanceof PDNonTerminalField) {
            List<PDField> childFields = ((PDNonTerminalField)targetField).getChildren();
            for (PDField field : childFields)
                removeWidgets(field);
        } else {
            System.out.println("Target field is neither terminal nor non-terminal; cannot remove widgets.");
        }
    }
}
