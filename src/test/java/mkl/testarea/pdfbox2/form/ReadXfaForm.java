package mkl.testarea.pdfbox2.form;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDXFAResource;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ReadXfaForm
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/42861499/xfa-building-in-pdfbox-1-8-12-not-in-2-0-4">
     * XFA building in PDFBox 1.8.12, not in 2.0.4
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/g1akyqtybfiekl5/XFA-File.pdf?dl=0">
     * XFA-File.pdf
     * </a>
     * <p>
     * In version 2.0.4 I indeed could reproduce that the oldest revision
     * of the XFA form was loaded, but using 2.0.5 or 2.1.0-SNAPSHOT the
     * current revision was loaded.
     * </p>
     * <p>
     * This appears to be a shortcoming in PDFBox which has been fixed in 2.0.5.
     * But look at {@link #testReadXfaFileFixedLikeMayank()} for the cause.
     * </p>
     */
    @Test
    public void testReadXfaFileLikeMayank() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("XFA-File.pdf")   )
        {
            byte[] xfaData = getParsableXFAForm(resource);
            Files.write(new File(RESULT_FOLDER, "XFA-File.xml").toPath(),  xfaData);
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/42861499/xfa-building-in-pdfbox-1-8-12-not-in-2-0-4">
     * XFA building in PDFBox 1.8.12, not in 2.0.4
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/g1akyqtybfiekl5/XFA-File.pdf?dl=0">
     * XFA-File.pdf
     * </a>, with the invalid leading whitespaces removed: XFA-File-fixed.pdf
     * <p>
     * Upon closer examination of the file it turns out that there are 10
     * bytes of white spaces before the start of the file. These white spaces
     * shifted offsets a bit, so PDFBox could not read the cross references
     * in the regular fashion but instead tried to repair the file.
     * </p>
     * <p>
     * This repair mechanism has been improved considerably in 2.0.5. Thus,
     * using PDFBox 2.0.5 the file could successfully completely be parsed,
     * including the most current XFA information, while repair in former
     * version was incomplete and merely returned the oldest copy of the
     * XFA form.
     * </p>
     * <p>
     * After fixing the file (i.e. after removing those ten white space
     * characters), PDFBox 2.0.4 had no problems parsing the file anymore.
     * </p>
     */
    @Test
    public void testReadXfaFileFixedLikeMayank() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("XFA-File-fixed.pdf")   )
        {
            byte[] xfaData = getParsableXFAForm(resource);
            Files.write(new File(RESULT_FOLDER, "XFA-File-fixed.xml").toPath(),  xfaData);
        }
    }

    // returns PDXFA
    public static byte[] getParsableXFAForm(InputStream file)
    {
        if (file == null)
            return null;
        PDDocument doc;
        PDDocumentCatalog catalog;
        PDAcroForm acroForm;

        PDXFAResource xfa;
        try
        {
            // String pass = null;
            doc = PDDocument.load(file);
            if (doc == null)
                return null;
            // flattenPDF(doc);
            doc.setAllSecurityToBeRemoved(true);
            // System.out.println("Security " + doc.isAllSecurityToBeRemoved());
            catalog = doc.getDocumentCatalog();
            if (catalog == null)
            {
                doc.close();
                return null;
            }
            acroForm = catalog.getAcroForm();
            if (acroForm == null)
            {
                doc.close();
                return null;
            }
            xfa = acroForm.getXFA();
            if (xfa == null)
            {
                doc.close();
                return null;
            }
            // TODO return byte[]
            byte[] xfaBytes = xfa.getBytes();
            doc.close();
            return xfaBytes;
        } catch (IOException e)
        {
            // handle IOException
            // happens when the file is corrupt.
            e.printStackTrace();
            System.out.println("XFAUtils-getParsableXFAForm-IOException");
            return null;
        }
    }
}
