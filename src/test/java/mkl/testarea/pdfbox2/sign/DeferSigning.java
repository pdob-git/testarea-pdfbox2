package mkl.testarea.pdfbox2.sign;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Calendar;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class DeferSigning {
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/46689478/sign-pdf-asynchronously-using-digest">
     * Sign pdf asynchronously using digest
     * </a>
     * <p>
     * This test attempts to find a way to make sure one can rebuild the
     * identical PDF document twice, first for hashing, then for injecting
     * the signature container.
     * </p>
     * <p>
     * The changes to the OP's code:
     * </p>
     * <ul>
     * <li>The SignDate must be guaranteed to be identical in both runs; thus,
     *  the time to fill in must be determined only once.</li>
     * <li>The revision id seed value must be set to identical values in both
     *  runs; we use the time in millis corresponding to the SignDate.</li>
     * </ul>
     */
    @Test
    public void testRepetitiveBuilding() throws IOException, NoSuchAlgorithmException {
        byte[] digestA, digestB;
        Calendar date = Calendar.getInstance();
        long id = date.getTimeInMillis();
        try (   InputStream resource = getClass().getResourceAsStream("test.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "testPreparedA.pdf"));
                PDDocument pdDocument = PDDocument.load(resource)   ) {
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName("Example User");
            signature.setLocation("Los Angeles, CA");
            signature.setReason("Testing");
//            Calendar date = Calendar.getInstance();
            signature.setSignDate(date);
            pdDocument.addSignature(signature);
            pdDocument.setDocumentId(id);

            ExternalSigningSupport externalSigningSupport = pdDocument.saveIncrementalForExternalSigning(result);

            byte[] content = IOUtils.toByteArray(externalSigningSupport.getContent());
            MessageDigest md = MessageDigest.getInstance("SHA256", new BouncyCastleProvider());
            digestA = md.digest(content);

            externalSigningSupport.setSignature(new byte[0]);
        }

        try (   InputStream resource = getClass().getResourceAsStream("test.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "testPreparedB.pdf"));
                PDDocument pdDocument = PDDocument.load(resource)   ) {
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName("Example User");
            signature.setLocation("Los Angeles, CA");
            signature.setReason("Testing");
//            Calendar date = Calendar.getInstance();
            signature.setSignDate(date);
            pdDocument.addSignature(signature);
            pdDocument.setDocumentId(id);

            ExternalSigningSupport externalSigningSupport = pdDocument.saveIncrementalForExternalSigning(result);

            byte[] content = IOUtils.toByteArray(externalSigningSupport.getContent());
            MessageDigest md = MessageDigest.getInstance("SHA256", new BouncyCastleProvider());
            digestB = md.digest(content);

            externalSigningSupport.setSignature(new byte[0]);
        }

        Assert.assertArrayEquals("Hashes differ", digestA, digestB);
    }

}
