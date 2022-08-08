package mkl.testarea.pdfbox2.extract;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class CompareText {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/49256943/comparing-two-pdf-files-text-using-pdfbox-is-failing-eventhough-both-files-are-h">
     * Comparing two PDF files text using PDFBox is failing eventhough both files are having same text
     * </a>
     * <br/> 
     * <a href="https://drive.google.com/file/d/1SeqdWhZdp-wuc0sfR3tvaSIRlRBSYOxL/view?usp=sharing">
     * Table_Basic.pdf
     * </a>
     * <br/> 
     * <a href="https://drive.google.com/file/d/1mFD2cPSSzY3OHTP07d2ZUosaI0ZyCpf2/view?usp=sharing">
     * Table_Basicf0313153905592.pdf
     * </a>
     * <p>
     * Indeed, there is a difference in output. It is due to different
     * <b>ToUnicode</b> mappings of certain characters in a certain font.
     * As the Unicode code points in question are in a private use area,
     * this wasn't that apparent.
     * </p>
     */
    @Test
    public void testCompareSjethvaniFiles() throws IOException {
        String pdfFile1 = "Table_Basic.pdf";
        String pdfFile2 = "Table_Basicf0313153905592.pdf";
        try (   InputStream resource1 = getClass().getResourceAsStream(pdfFile1);
                InputStream resource2 = getClass().getResourceAsStream(pdfFile2)) {
            
            PDDocument pdf1 = PDDocument.load(resource1);
            PDDocument pdf2 = PDDocument.load(resource2);
            PDPageTree pdf1pages = pdf1.getDocumentCatalog().getPages();
            PDPageTree pdf2pages = pdf2.getDocumentCatalog().getPages();
            try
            {
                if (pdf1pages.getCount() != pdf2pages.getCount())
                {
                    String message = "Number of pages in the files ("+pdfFile1+","+pdfFile2+") do not match. pdfFile1 has "+pdf1pages.getCount()+" no pages, while pdf2pages has "+pdf2pages.getCount()+" no of pages";
                    fail(message);
                }
                PDFTextStripper pdfStripper = new PDFTextStripper();
                for (int i = 0; i < pdf1pages.getCount(); i++)
                {
                    pdfStripper.setStartPage(i + 1);
                    pdfStripper.setEndPage(i + 1);
                    String pdf1PageText = pdfStripper.getText(pdf1);
                    String pdf2PageText = pdfStripper.getText(pdf2);
                    if (!pdf1PageText.equals(pdf2PageText))
                    {
                        String message = "Contents of the files ("+pdfFile1+","+pdfFile2+") do not match on Page no: " + (i + 1)+" pdf1PageText is : "+pdf1PageText+" , while pdf2PageText is : "+pdf2PageText;
                        Files.write(new File(RESULT_FOLDER, pdfFile1 + '-' + i + ".txt").toPath(), Collections.singleton(pdf1PageText));
                        Files.write(new File(RESULT_FOLDER, pdfFile2 + '-' + i + ".txt").toPath(), Collections.singleton(pdf2PageText));
                        fail(message);
                    }
                }
            } finally {
                pdf1.close();
                pdf2.close();
            }
        }
    }
}
