package mkl.testarea.pdfbox2.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTerminalField;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ExtractAppearanceText {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/49427615/how-to-extract-label-text-from-push-button-using-apache-pdfbox">
     * How to extract label text from Push button using Apache PDFBox?
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1iUc9AbDkjBEPhITxKdwQjyNTQbs4Dd7j">
     * btn.pdf
     * </a>
     * <p>
     * This test demonstrates how to extract the text from the appearance
     * stream of the single button in the sample PDF shared by the OP.
     * </p>
     * @see #showNormalFieldAppearanceTexts(PDDocument)
     */
    @Test
    public void testBtn() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("btn.pdf")    )
        {
            System.out.println();
            System.out.println("btn.pdf");
            PDDocument document = PDDocument.load(resource);
            showNormalFieldAppearanceTexts(document);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/49427615/how-to-extract-label-text-from-push-button-using-apache-pdfbox">
     * How to extract label text from Push button using Apache PDFBox?
     * </a>
     * <p>
     * This test demonstrates how to extract the text from the appearance
     * stream of buttons and other form elements from a form already present
     * here from some other issue.
     * </p>
     * @see #showNormalFieldAppearanceTexts(PDDocument)
     */
    @Test
    public void testKYF211Beställning2014() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/pdfbox2/form/KYF 211 Beställning 2014.pdf")    )
        {
            System.out.println();
            System.out.println("KYF 211 Beställning 2014.pdf");
            PDDocument document = PDDocument.load(resource);
            showNormalFieldAppearanceTexts(document);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/49427615/how-to-extract-label-text-from-push-button-using-apache-pdfbox">
     * How to extract label text from Push button using Apache PDFBox?
     * </a>
     * <p>
     * This method extracts the text from the normal appearance streams
     * of the form fields in the given PDF document.
     * </p>
     * @see #testBtn()
     * @see #testKYF211Beställning2014()
     */
    public void showNormalFieldAppearanceTexts(PDDocument document) throws IOException {
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();

        if (acroForm != null) {
            SimpleXObjectTextStripper stripper = new SimpleXObjectTextStripper();

            for (PDField field : acroForm.getFieldTree()) {
                if (field instanceof PDTerminalField) {
                    PDTerminalField terminalField = (PDTerminalField) field;
                    System.out.println();
                    System.out.println("* " + terminalField.getFullyQualifiedName());
                    for (PDAnnotationWidget widget : terminalField.getWidgets()) {
                        PDAppearanceDictionary appearance = widget.getAppearance();
                        if (appearance != null) {
                            PDAppearanceEntry normal = appearance.getNormalAppearance();
                            if (normal != null) {
                                Map<COSName, PDAppearanceStream> streams = normal.isSubDictionary() ? normal.getSubDictionary() :
                                    Collections.singletonMap(COSName.DEFAULT, normal.getAppearanceStream());
                                for (Map.Entry<COSName, PDAppearanceStream> entry : streams.entrySet()) {
                                    String text = stripper.getText(entry.getValue());
                                    System.out.printf("  * %s: %s\n", entry.getKey().getName(), text);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
