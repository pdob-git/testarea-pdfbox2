// $Id$
package mkl.testarea.pdfbox2.form;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ReadForm
{
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
    }

    /**
     * <a href="http://stackoverflow.com/questions/36964496/pdfbox-2-0-overcoming-dictionary-key-encoding">
     * PDFBox 2.0: Overcoming dictionary key encoding
     * </a>
     * <br/>
     * <a href="http://www.stockholm.se/PageFiles/85478/KYF%20211%20Best%C3%A4llning%202014.pdf">
     * KYF 211 Best&auml;llning 2014.pdf
     * </a>
     * 
     * <p>
     * Indeed, the special characters in the names are replaced by the Unicode replacement
     * character. PDFBox, when parsing a PDF name, immediately interprets its bytes as UTF-8
     * encoded which fails in the document at hand.
     * </p>
     */
    @Test
    public void testReadFormOptions() throws IOException
    {
        try (   InputStream originalStream = getClass().getResourceAsStream("KYF 211 Best\u00e4llning 2014.pdf") )
        {
            PDDocument pdfDocument = PDDocument.load(originalStream);
            PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();
            
            PDField field = acroForm.getField("Krematorier");
            List<PDAnnotationWidget> widgets = field.getWidgets();
            System.out.println("Field Name: " + field.getPartialName() + " (" + widgets.size() + ")");
            for (PDAnnotationWidget annot : widgets) {
              PDAppearanceDictionary ap = annot.getAppearance();
              Set<COSName> keys = ((COSDictionary)(ap.getCOSObject().getDictionaryObject("N"))).keySet();
              ArrayList<String> keyList = new ArrayList<>(keys.size());
              for (COSName cosKey : keys) {keyList.add(cosKey.getName());}
              System.out.println(String.join("|", keyList));
            }
        }
    }

}
