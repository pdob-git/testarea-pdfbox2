package mkl.testarea.pdfbox2.form;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class CreateCheckBox
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/43604973/creating-a-checkbox-and-printing-it-to-pdf-file-is-not-working-using-pdfbox-1-8">
     * Creating a checkbox and printing it to pdf file is not working using pdfbox 1.8.9 api
     * </a>
     * <p>
     * This test shows how to create a check box using PDFBox 2.0.5. It still is not ideal for
     * use with Adobe Reader but issues are only cosmetical.
     * </p>
     */
    @Test
    public void testCheckboxForSureshGoud() throws IOException
    {
        PDDocument document = new PDDocument();

        PDPage page = new PDPage();
        document.addPage(page);

        PDAcroForm acroForm = new PDAcroForm(document);
        document.getDocumentCatalog().setAcroForm(acroForm);

        COSDictionary normalAppearances = new COSDictionary();
        PDAppearanceDictionary pdAppearanceDictionary = new PDAppearanceDictionary();
        pdAppearanceDictionary.setNormalAppearance(new PDAppearanceEntry(normalAppearances));
        pdAppearanceDictionary.setDownAppearance(new PDAppearanceEntry(normalAppearances));

        PDAppearanceStream pdAppearanceStream = new PDAppearanceStream(document);
        pdAppearanceStream.setResources(new PDResources());
        try (PDPageContentStream pdPageContentStream = new PDPageContentStream(document, pdAppearanceStream))
        {
            pdPageContentStream.setFont(PDType1Font.ZAPF_DINGBATS, 14.5f);
            pdPageContentStream.beginText();
            pdPageContentStream.newLineAtOffset(3, 4);
            pdPageContentStream.showText("\u2714");
            pdPageContentStream.endText();
        }
        pdAppearanceStream.setBBox(new PDRectangle(18, 18));
        normalAppearances.setItem("Yes", pdAppearanceStream);

        pdAppearanceStream = new PDAppearanceStream(document);
        pdAppearanceStream.setResources(new PDResources());
        try (PDPageContentStream pdPageContentStream = new PDPageContentStream(document, pdAppearanceStream))
        {
            pdPageContentStream.setFont(PDType1Font.ZAPF_DINGBATS, 14.5f);
            pdPageContentStream.beginText();
            pdPageContentStream.newLineAtOffset(3, 4);
            pdPageContentStream.showText("\u2718");
            pdPageContentStream.endText();
        }
        pdAppearanceStream.setBBox(new PDRectangle(18, 18));
        normalAppearances.setItem("Off", pdAppearanceStream);

        PDCheckBox checkBox = new PDCheckBox(acroForm);
        acroForm.getFields().add(checkBox);
        checkBox.setPartialName("CheckBoxField");
        checkBox.setFieldFlags(4);

        List<PDAnnotationWidget> widgets = checkBox.getWidgets();
        for (PDAnnotationWidget pdAnnotationWidget : widgets)
        {
            pdAnnotationWidget.setRectangle(new PDRectangle(50, 750, 18, 18));
            pdAnnotationWidget.setPage(page);
            page.getAnnotations().add(pdAnnotationWidget);

            pdAnnotationWidget.setAppearance(pdAppearanceDictionary);
        }

        checkBox.setReadOnly(true);
        checkBox.unCheck();

        document.save(new File(RESULT_FOLDER, "CheckBox.pdf"));
        document.close();
    }

}
