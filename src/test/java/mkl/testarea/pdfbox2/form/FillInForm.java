package mkl.testarea.pdfbox2.form;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class FillInForm
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/36926060/apache-pdfbox-form-fill-truetype-text-spacing-issue">
     * Apache PDFBox Form Fill TrueType text spacing issue
     * </a>
     * <br>
     * <a href="https://www.dropbox.com/sh/b7ft1k0wfesob8s/AABnrTOgX26JlWCxl85jXns0a/FillFormField.pdf?dl=0">
     * FillFormField.pdf
     * </a>
     * <p>
     * Indeed, the issue can be reproduced, it is due to a combination of two factors:
     * </p>
     * <p>
     * <b>A quirk of PDFBox when writing text</b> - When writing text into a content stream,
     * PDFBox translates each Unicode codepoint into a name and looks up that name in a map
     * generating from the inverted font encoding. For some encodings, though, there are two
     * codes mapping to the name space, and the inverted map maps back to only one of them,
     * in the case at hand the non-breaking variant. As both are expected to be typographically
     * identical, this should not be a problem. But:
     * </p>
     * <p>
     * <b>Non-conformant font in the PDF</b> - The font Impact in the PDF is defined with
     * width 176 for the normal space glyph and 750 for the nonbreaking space glyph. Thus,
     * they typographically differ vehemently.
     * </p>
     */
    @Test
    public void testFillLikeRichardBrown() throws IOException
    {
        try (   InputStream originalStream = getClass().getResourceAsStream("FillFormField.pdf") )
        {
            // load the documents
            PDDocument pdfDocument = Loader.loadPDF(originalStream);

            // get the document catalog
            PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();

            // as there might not be an AcroForm entry a null check is necessary
            if (acroForm != null)
            {
                PDTextField field = (PDTextField) acroForm.getField( "Title" );
                field.setValue("Low Mileage Beauty Kill");
            }

            // Save and close the filled out form.
            pdfDocument.save(new File(RESULT_FOLDER, "FillFormFieldRichardBrown.pdf"));
            pdfDocument.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/39720305/ufffd-is-not-available-in-this-fonts-encoding-winansiencoding">
     * U+FFFD is not available in this font's encoding: WinAnsiEncoding
     * </a>
     * <p>
     * The issue cannot be reproduced.
     * </p>
     */
    @Test
    public void testFillLikeStDdt() throws IOException
    {
        try (   InputStream originalStream = getClass().getResourceAsStream("FillFormField.pdf") )
        {
            PDDocument pdfDocument = Loader.loadPDF(originalStream);
            PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();

            if (acroForm != null)
            {
                List<PDField> fields = acroForm.getFields();
                for (PDField field : fields) {
                    switch (field.getPartialName()) {
                        case "Title" /*"devices"*/:
                            field.setValue("Ger�t");
                            field.setReadOnly(true);
                            break;
                    }
                }
                acroForm.flatten(fields, true);
            }

            pdfDocument.save(new File(RESULT_FOLDER, "FillFormFieldStDdt.pdf"));
            pdfDocument.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/49048556/pdfbox-set-field-value-doesnt-work">
     * PDFBox set field value doesn't work
     * </a>
     * <br/>
     * <a href="https://www.inps.it/Nuovoportaleinps/image.aspx?iIDModulo=7712&tipomodulo=1">
     * SR16_ANF_DIP.pdf
     * </a>
     * <p>
     * Indeed, the form field in question is hidden. Thus, one has to un-hide it
     * to make it visible.
     * </p>
     */
    @Test
    public void testFillLikeBarbara() throws IOException
    {
        try (   InputStream originalStream = getClass().getResourceAsStream("SR16_ANF_DIP.pdf") )
        {
            PDDocument pdfDocument = Loader.loadPDF(originalStream);
            PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();

            if (acroForm != null)
            {
                PDTextField pdfField = (PDTextField) acroForm.getField("info_15a");
                pdfField.getWidgets().get(0).setHidden(false);// <===
                pdfField.setValue("xxxxxx");
            }

            pdfDocument.setAllSecurityToBeRemoved(true);
            COSDictionary dictionary = pdfDocument.getDocumentCatalog().getCOSObject();
            dictionary.removeItem(COSName.PERMS);

            pdfDocument.save(new File(RESULT_FOLDER, "SR16_ANF_DIP-filled.pdf"));
            pdfDocument.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/52059931/pdfbox-setvalue-for-multiple-pdtextfield">
     * PDFBox setValue for multiple PDTextField
     * </a>
     * <br/>
     * <a href="https://ufile.io/z8jzj">
     * testform.pdf
     * </a>
     * <p>
     * Cannot reproduce the issue.
     * </p>
     */
    @Test
    public void testFillLikeJuvi() throws IOException {
        try (   InputStream originalStream = getClass().getResourceAsStream("testform.pdf") ) {
            PDDocument document = Loader.loadPDF(originalStream);
            PDDocumentCatalog docCatalog = document.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();

            PDTextField field = (PDTextField) acroForm.getField("Check1");
            field.setValue("1111");

            PDTextField field2 = (PDTextField) acroForm.getField("Check2");
            field2.setValue("2222");

            PDTextField field3 = (PDTextField) acroForm.getField("HelloWorld");
            field3.setValue("HelloWorld");

            document.save(new File(RESULT_FOLDER, "testform-filled.pdf"));
            document.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/54820224/why-is-pdfbox-overwriting-multiple-fields-even-when-they-dont-match-the-fullyq">
     * Why is PDFBox overwriting multiple Fields, even when they don't match the fullyQualifiedName? (Kotlin Android)
     * </a>
     * <br/>
     * <a href="http://www.kylevp.com/csOnePage.pdf">
     * csOnePage.pdf
     * </a>
     * <p>
     * The problem is due to some fields of the form sharing empty
     * appearance XObjects and PDFBox assuming in case of existing
     * appearance XObjects that it can simply update this existing
     * appearance instead of having to create a new one from scratch.
     * </p>
     * <p>
     * A work-around is to remove existing appearances from a field
     * before setting its value, see below.
     * </p>
     */
    @Test
    public void testLikeKyle() throws IOException {
        try (   InputStream originalStream = getClass().getResourceAsStream("csOnePage.pdf") )
        {
            PDDocument doc = Loader.loadPDF(originalStream);
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();

            List<String> skillList = Arrays.asList("Athletics","Acrobatics","Sleight of Hand", "Stealth","Acrana", "History","Investigation","Nature", "Religion", "Animal Handling", "Insight", "Medicine", "Perception", "Survival", "Deception", "Intimidation", "Performance", "Persuasion");

            int temp = 0;
            for (String skill : skillList) {
                PDField field = acroForm.getField(skill);
                temp += 1;
                if (field == null) {
                    System.err.printf("(%d) field '%s' is null.\n", temp, skill);
                } else {
                    field.getCOSObject().removeItem(COSName.AP);
                    field.setValue(String.valueOf(temp));
                }
            }

            doc.save(new File(RESULT_FOLDER, "csOnePage-filled.pdf"));
            doc.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/56938135/pdfbox-inconsistent-pdtextfield-autosize-behavior-after-setvalue">
     * PDFBox Inconsistent PDTextField Autosize Behavior after setValue
     * </a>
     * <br/>
     * <a href="http://www.filedropper.com/0postfontload">
     * 0.pdf
     * </a>
     * <p>
     * Indeed, some fields look weird after fill-in; for some fields
     * this is due to weird pre-existing appearance streams. These can
     * be fixed as in {@link #testFill0DropOldAppearance()}.
     * </p>
     * @see #testFill0DropOldAppearance()
     * @see #testFill0DropOldAppearanceNoCombNoMax()
     * @see #testFill0DropOldAppearanceNoCombNoMaxNoMultiLine()
     */
    @Test
    public void testFill0LikeXenyal() throws IOException {
        try (   InputStream originalStream = getClass().getResourceAsStream("0.pdf");
                InputStream fontStream = getClass().getResourceAsStream("Lato-Regular.ttf"))
        {
            PDDocument doc = Loader.loadPDF(originalStream);
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();

            PDType0Font font = PDType0Font.load(doc, fontStream, false);
            String font_name = acroForm.getDefaultResources().add(font).getName();

            for (PDField field : acroForm.getFieldTree()) {
                if (field instanceof PDTextField) {
                    PDTextField textField = (PDTextField) field;
                    textField.setDefaultAppearance(String.format("/%s 0 Tf 0 g", font_name));
                    textField.setValue("Test");
                }
            }
            

            doc.save(new File(RESULT_FOLDER, "0-filledLikeXenyal.pdf"));
            doc.close();
        }        
    }

    /**
     * <a href="https://stackoverflow.com/questions/56938135/pdfbox-inconsistent-pdtextfield-autosize-behavior-after-setvalue">
     * PDFBox Inconsistent PDTextField Autosize Behavior after setValue
     * </a>
     * <br/>
     * <a href="http://www.filedropper.com/0postfontload">
     * 0.pdf
     * </a>
     * <p>
     * Removing the old appearance streams before setting the new field
     * values removes the compression of the Resident Name and Care
     * Providers Address fields. In the latter case, though, the lower
     * part of the field value now is cut off.
     * </p>
     * <p>
     * For some fields only the first two letters are visible. This is
     * due to them being two character comb fields. These can changed
     * as in {@link #testFill0DropOldAppearanceNoCombNoMax()}.
     * </p>
     * @see #testFill0LikeXenyal()
     * @see #testFill0DropOldAppearanceNoCombNoMax()
     * @see #testFill0DropOldAppearanceNoCombNoMaxNoMultiLine()
     */
    @Test
    public void testFill0DropOldAppearance() throws IOException {
        try (   InputStream originalStream = getClass().getResourceAsStream("0.pdf");
                InputStream fontStream = getClass().getResourceAsStream("Lato-Regular.ttf"))
        {
            PDDocument doc = Loader.loadPDF(originalStream);
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();

            PDType0Font font = PDType0Font.load(doc, fontStream, false);
            String font_name = acroForm.getDefaultResources().add(font).getName();

            for (PDField field : acroForm.getFieldTree()) {
                if (field instanceof PDTextField) {
                    PDTextField textField = (PDTextField) field;
                    textField.setDefaultAppearance(String.format("/%s 0 Tf 0 g", font_name));
                    textField.getWidgets().forEach(w -> w.getAppearance().setNormalAppearance((PDAppearanceEntry)null));
                    textField.setValue("Test");
                }
            }
            

            doc.save(new File(RESULT_FOLDER, "0-filledDropOldAppearance.pdf"));
            doc.close();
        }        
    }

    /**
     * <a href="https://stackoverflow.com/questions/56938135/pdfbox-inconsistent-pdtextfield-autosize-behavior-after-setvalue">
     * PDFBox Inconsistent PDTextField Autosize Behavior after setValue
     * </a>
     * <br/>
     * <a href="http://www.filedropper.com/0postfontload">
     * 0.pdf
     * </a>
     * <p>
     * Resetting the comb flags and removing maximum lengths fixes the
     * appearance of the fields in which only the first two letters were
     * visible.
     * </p>
     * <p>
     * The problem of the lower part of the field value being cut off in
     * the Care Providers Address fields is due to PDFBox in case of 
     * multi line text fields using a fixed font height and not fine
     * tuning the vertical position of the field contents. In the case
     * at hand this can be fixed as in {@link #testFill0DropOldAppearanceNoCombNoMaxNoMultiLine()}.
     * </p>
     * @see #testFill0LikeXenyal()
     * @see #testFill0DropOldAppearance()
     * @see #testFill0DropOldAppearanceNoCombNoMaxNoMultiLine()
     */
    @Test
    public void testFill0DropOldAppearanceNoCombNoMax() throws IOException {
        final int FLAG_COMB = 1 << 24;

        try (   InputStream originalStream = getClass().getResourceAsStream("0.pdf");
                InputStream fontStream = getClass().getResourceAsStream("Lato-Regular.ttf"))
        {
            PDDocument doc = Loader.loadPDF(originalStream);
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();

            PDType0Font font = PDType0Font.load(doc, fontStream, false);
            String font_name = acroForm.getDefaultResources().add(font).getName();

            for (PDField field : acroForm.getFieldTree()) {
                if (field instanceof PDTextField) {
                    PDTextField textField = (PDTextField) field;
                    textField.getCOSObject().removeItem(COSName.MAX_LEN);
                    textField.getCOSObject().setFlag(COSName.FF, FLAG_COMB, false);;
                    textField.setDefaultAppearance(String.format("/%s 0 Tf 0 g", font_name));
                    textField.getWidgets().forEach(w -> w.getAppearance().setNormalAppearance((PDAppearanceEntry)null));
                    textField.setValue("Test");
                }
            }
            

            doc.save(new File(RESULT_FOLDER, "0-filledDropOldAppearanceNoCombNoMax.pdf"));
            doc.close();
        }        
    }

    /**
     * <a href="https://stackoverflow.com/questions/56938135/pdfbox-inconsistent-pdtextfield-autosize-behavior-after-setvalue">
     * PDFBox Inconsistent PDTextField Autosize Behavior after setValue
     * </a>
     * <br/>
     * <a href="http://www.filedropper.com/0postfontload">
     * 0.pdf
     * </a>
     * <p>
     * By resetting the MultiLine flags, too, one eventually gets rid
     * of the problem of the lower part of the field value being cut
     * off in the Care Providers Address fields. This actually should
     * be considered an issue of PDFBox, though, not of the source PDF
     * here.
     * </p>
     * @see #testFill0LikeXenyal()
     * @see #testFill0DropOldAppearance()
     * @see #testFill0DropOldAppearanceNoCombNoMax()
     */
    @Test
    public void testFill0DropOldAppearanceNoCombNoMaxNoMultiLine() throws IOException {
        final int FLAG_MULTILINE = 1 << 12;
        final int FLAG_COMB = 1 << 24;

        try (   InputStream originalStream = getClass().getResourceAsStream("0.pdf");
                InputStream fontStream = getClass().getResourceAsStream("Lato-Regular.ttf"))
        {
            PDDocument doc = Loader.loadPDF(originalStream);
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();

            PDType0Font font = PDType0Font.load(doc, fontStream, false);
            String font_name = acroForm.getDefaultResources().add(font).getName();

            for (PDField field : acroForm.getFieldTree()) {
                if (field instanceof PDTextField) {
                    PDTextField textField = (PDTextField) field;
                    textField.getCOSObject().removeItem(COSName.MAX_LEN);
                    textField.getCOSObject().setFlag(COSName.FF, FLAG_COMB | FLAG_MULTILINE, false);;
                    textField.setDefaultAppearance(String.format("/%s 0 Tf 0 g", font_name));
                    textField.getWidgets().forEach(w -> w.getAppearance().setNormalAppearance((PDAppearanceEntry)null));
                    textField.setValue("Test");
                }
            }
            

            doc.save(new File(RESULT_FOLDER, "0-filledDropOldAppearanceNoCombNoMaxNoMultiLine.pdf"));
            doc.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/64912688/insert-double-value-for-equotion-in-pdf-with-pdfbox-in-java">
     * Insert double Value for equotion in PDF with pdfbox in Java
     * </a>
     * <br/>
     * <a href="https://1drv.ms/b/s!Av6exjPNXlgOioouAuXL6QV4eUGkqg?e=ocfhvC">
     * 2020-04_Beschaffungsantrag.pdf
     * </a>
     * <p>
     * The field number 30 used by the OP is not the terminal field to
     * fill-in but merely an intermediary one. The field to actually use
     * is 'Einzelpreis in € exkl USt1.0.0.0.0'. Furthermore, as the field
     * list used by the OP, it does not contain this field. Instead the
     * field should be retrieved by name from the {@link PDAcroForm}
     * object.
     * </p>
     */
    @Test
    public void testFill2020_04BeschaffungsantragEinzelpreis() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("2020-04_Beschaffungsantrag.pdf") ) {
            PDDocument pdDocument = Loader.loadPDF(resource);

            PDAcroForm acroForm = pdDocument.getDocumentCatalog().getAcroForm();
            PDField field30 = acroForm.getFields().get(30);
            System.out.printf("Field by index 30 is '%s'\n", field30.getFullyQualifiedName());
            PDField fieldByName = acroForm.getField("Einzelpreis in € exkl USt1.0.0.0.0");
            System.out.printf("Field by full name is '%s'\n", fieldByName.getFullyQualifiedName());
            fieldByName.setValue("100");

            pdDocument.save(new File(RESULT_FOLDER, "2020-04_Beschaffungsantrag-filledEinzelpreis in € exkl USt1.0.0.0.0.pdf"));
            pdDocument.close();
        }
    }
}
