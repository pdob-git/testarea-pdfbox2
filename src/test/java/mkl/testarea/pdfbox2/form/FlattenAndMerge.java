package mkl.testarea.pdfbox2.form;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class FlattenAndMerge {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/47140209/pdfbox-files-are-sharing-common-cosstream-after-flatten">
     * PDFBox files are sharing common COSStream after flatten
     * </a>
     * <br/>
     * <a href="https://studentloans.gov/myDirectLoan/downloadForm.action?searchType=library&shortName=general&localeCode=en-us">
     * GeneralForbearance.pdf
     * </a>
     * <p>
     * Indeed, flattening, merging, and early closing of source documents
     * do not mingle well.
     * </p>
     */
    @Test
    public void testMergeGovernmentForms() throws IOException {
        try (   InputStream resource1 = getClass().getResourceAsStream("GeneralForbearance.pdf");
                InputStream resource2 = getClass().getResourceAsStream("GeneralForbearance.pdf")) {
            PDDocument destination = PDDocument.load(resource1);

            PDDocument source = PDDocument.load(resource2);
            source.getDocumentCatalog().getAcroForm().flatten(); //comment out just this line and the destination.save will pass

            PDFMergerUtility appender = new PDFMergerUtility();

            appender.appendDocument(destination, source);

            source.close(); //comment out just this line and the destination.save will pass

            destination.save(new File(RESULT_FOLDER, "PrintMergeIssue.pdf"));
            destination.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/47140209/pdfbox-files-are-sharing-common-cosstream-after-flatten">
     * PDFBox files are sharing common COSStream after flatten
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/18JbNK1gBivSARvv9kgd5FE8xnD_9-_15/view?usp=drivesdk">
     * GovFormPreFlattened.pdf
     * </a>
     * <p>
     * Indeed, even merely merging, and early closing of pre-flattened source documents
     * do not mingle well.
     * </p>
     */
    @Test
    public void testMergePreFlattenedGovernmentForms() throws IOException {
        try (   InputStream resource1 = getClass().getResourceAsStream("GovFormPreFlattened.pdf");
                InputStream resource2 = getClass().getResourceAsStream("GovFormPreFlattened.pdf")) {
            PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
            PDDocument src = PDDocument.load(resource1);
            PDDocument dest = PDDocument.load(resource2);
            pdfMergerUtility.appendDocument(dest, src);
            src.close(); //if we don't close the src then we don't have an error
            dest.save(new File(RESULT_FOLDER, "PreFlattenedMergeIssue.pdf"));
            dest.close();
        }
    }
}
