package mkl.testarea.pdfbox2.sign;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.examples.signature.validation.AddValidationInformation;
import org.junit.BeforeClass;
import org.junit.Test;

public class AddVri {
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/71225696/base64-digest-pfxpkcs12-etsi-cades-detached-signature-pades-ltv">
     * Base64 digest + PFX(PKCS12) -> ETSI.CAdES.detached signature -> PAdES LTV
     * </a>
     * <br/>
     * <a href="https://github.com/adelinvoicu/PAdES-T/raw/main/Resources/sample_signed.pdf">
     * sample_signed.pdf
     * </a> as "AdelinVoicu-sample_signed.pdf"
     * <p>
     * Indeed, one gets an exception. Doing some debugging it becomes clear that the issue is
     * that the AIA CA Issuer URL results in a cross-protocol redirect (http --> https) which
     * the Java URL connection does not follow automatically.
     * </p>
     */
    @Test
    public void testSampleSignedLikeAdelinVoicu() throws IOException {
        AddValidationInformation addOcspInformation = new AddValidationInformation();

        File inFile = new File("src/test/resources/mkl/testarea/pdfbox2/sign/AdelinVoicu-sample_signed.pdf");
        File outFile = new File(RESULT_FOLDER, "AdelinVoicu-sample_signed-VRI.pdf");
        addOcspInformation.validateSignature(inFile, outFile);
    }
}
