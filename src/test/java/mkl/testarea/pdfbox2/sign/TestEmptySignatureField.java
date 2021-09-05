// $Id$
package mkl.testarea.pdfbox2.sign;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class TestEmptySignatureField
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/37601092/pdfbox-identify-specific-pages-and-functionalities-recommendations">
     * PDFBox identify specific pages and functionalities recommendations
     * </a>
     * 
     * <p>
     * This test shows how to add an empty signature field with a custom appearance
     * to an existing PDF.
     * </p>
     */
    @Test
    public void testAddEmptySignatureField() throws IOException
    {
        try (   InputStream sourceStream = getClass().getResourceAsStream("test.pdf");
                OutputStream output = new FileOutputStream(new File(RESULT_FOLDER, "test-with-empty-sig-field.pdf")))
        {
            PDFont font = new PDType1Font(FontName.HELVETICA);
            PDResources resources = new PDResources();
            resources.put(COSName.getPDFName("Helv"), font);

            PDDocument document = Loader.loadPDF(sourceStream);
            PDAcroForm acroForm = new PDAcroForm(document);
            acroForm.setDefaultResources(resources);
            document.getDocumentCatalog().setAcroForm(acroForm);

            PDRectangle rect = new PDRectangle(50, 750, 200, 50);

            PDAppearanceDictionary appearanceDictionary = new PDAppearanceDictionary();
            PDAppearanceStream appearanceStream = new PDAppearanceStream(document);
            appearanceStream.setBBox(rect.createRetranslatedRectangle());
            appearanceStream.setResources(resources);
            appearanceDictionary.setNormalAppearance(appearanceStream);
            PDPageContentStream contentStream = new PDPageContentStream(document, appearanceStream);
            contentStream.setStrokingColor(Color.BLACK);
            contentStream.setNonStrokingColor(Color.LIGHT_GRAY);
            contentStream.setLineWidth(2);
            contentStream.addRect(0, 0, rect.getWidth(), rect.getHeight());
            contentStream.fill();
            contentStream.moveTo(1 * rect.getHeight() / 4, 1 * rect.getHeight() / 4);
            contentStream.lineTo(2 * rect.getHeight() / 4, 3 * rect.getHeight() / 4);
            contentStream.moveTo(1 * rect.getHeight() / 4, 3 * rect.getHeight() / 4);
            contentStream.lineTo(2 * rect.getHeight() / 4, 1 * rect.getHeight() / 4);
            contentStream.moveTo(3 * rect.getHeight() / 4, 1 * rect.getHeight() / 4);
            contentStream.lineTo(rect.getWidth() - rect.getHeight() / 4, 1 * rect.getHeight() / 4);
            contentStream.stroke();
            contentStream.setNonStrokingColor(Color.DARK_GRAY);
            contentStream.beginText();
            contentStream.setFont(font, rect.getHeight() / 5);
            contentStream.newLineAtOffset(3 * rect.getHeight() / 4, -font.getBoundingBox().getLowerLeftY() * rect.getHeight() / 5000);
            contentStream.showText("Customer");
            contentStream.endText();
            contentStream.close();

            PDSignatureField signatureField = new PDSignatureField(acroForm);
            signatureField.setPartialName("SignatureField");
            PDPage page = document.getPage(0);

            PDAnnotationWidget widget = signatureField.getWidgets().get(0);
            widget.setAppearance(appearanceDictionary);
            widget.setRectangle(rect);
            widget.setPage(page);

            page.getAnnotations().add(widget);
            acroForm.getFields().add(signatureField);

            document.save(output);
            document.close();
        }
    }

}
