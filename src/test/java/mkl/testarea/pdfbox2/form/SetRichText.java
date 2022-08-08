package mkl.testarea.pdfbox2.form;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class SetRichText {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/54988511/acroform-field-setrichtextvalue-is-not-working">
     * acroform field.setRichTextValue is not working
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1jFbsYGFOnx8EMiHgDsE8LQtfwJHSa5Gh">
     * form.pdf
     * </a> as "formBee.pdf"
     * <p>
     * After correction of a few details, the code kinda runs. In
     * particular it is necessary to use PDF style rich text (and
     * not LaTeX richtext package instructions), to set the flag
     * NeedAppearances to <code>true</code>, and to provide a V
     * value equal to the RV value without markup.
     * </p>
     * <p>
     * It only "kinda" runs because the OP also wants to flatten
     * the form which doesn't work as PDFBox does not create
     * appearance streams based on the rich text value.
     * </p>
     */
    @Test
    public void testFormBee() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("formBee.pdf")) {
            PDDocument pdfDocument = PDDocument.load(resource);

            pdfDocument.getDocument().setIsXRefStream(true);
            PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();
            acroForm.setNeedAppearances(true);

            acroForm.getField("tenantDataValue").setValue("Deuxième texte");
            acroForm.getField("tradingAddressValue").setValue("Text replacé");
            acroForm.getField("buildingDataValue").setValue("Deuxième texte");
            acroForm.getField("oldRentValue").setValue("750");
            acroForm.getField("oldChargesValue").setValue("655");
            acroForm.getField("newRentValue").setValue("415");
            acroForm.getField("newChargesValue").setValue("358");
            acroForm.getField("increaseEffectiveDateValue").setValue("Texte 3eme contenu");

            PDTextField field = (PDTextField) acroForm.getField("tableData");
            field.setRichText(true);
            //String val = "\\rtpara[size=12]{para1}{This is 12pt font, while \\span{size=8}{this is 8pt font.} OK?}";
            String val1 = "<?xml version=\"1.0\"?>"
                    + "<body xfa:APIVersion=\"Acroform:2.7.0.0\" xfa:spec=\"2.1\" xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:xfa=\"http://www.xfa.org/schema/xfa-data/1.0/\">"
                    + "<p dir=\"ltr\" style=\"margin-top:0pt;margin-bottom:0pt;font-family:Helvetica;font-size:12pt\">"
                    + "This is 12pt font, while "
                    + "<span style=\"font-size:8pt\">this is 8pt font.</span>"
                    + " OK?"
                    + "</p>"
                    + "</body>";
            String val1Clean = "This is 12pt font, while this is 8pt font. OK?";
            //String val2 = "<body xmlns=\"http://www.w3.org/1999/xhtml\"><p style=\"color:#FF0000;\">Red&#13;</p><p style=\"color:#1E487C;\">Blue&#13;</p></body>";
            //String val2Clean = "Red\rBlue\r";
            field.setValue(val1Clean);
            field.setRichTextValue(val1);

            pdfDocument.save(new File(RESULT_FOLDER, "formBee-filled.pdf"));
        }
    }

}
