package mkl.testarea.pdfbox2.sign;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Calendar;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.examples.signature.TSAClient;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class SignPadesBc {
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    public static final String KEYSTORE = "keystores/demo-rsa2048.ks"; 
    public static final char[] PASSWORD = "demo-rsa2048".toCharArray(); 

    public static KeyStore ks = null;
    public static PrivateKey pk = null;
    public static Certificate[] chain = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();

        BouncyCastleProvider bcp = new BouncyCastleProvider();
        Security.addProvider(bcp);
        //Security.insertProviderAt(bcp, 1);

        ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream(KEYSTORE), PASSWORD);
        String alias = (String) ks.aliases().nextElement();
        pk = (PrivateKey) ks.getKey(alias, PASSWORD);
        chain = ks.getCertificateChain(alias);
    }

    /**
     * <a href="https://stackoverflow.com/questions/71225696/base64-digest-pfxpkcs12-etsi-cades-detached-signature-pades-ltv">
     * Base64 digest + PFX(PKCS12) -> ETSI.CAdES.detached signature -> PAdES LTV
     * </a>
     * <p>
     * This test generates a simple PAdES BASELINE-T signature.
     * It is a trivial port of the iText 7 test with the same
     * name in the iText7 test area.
     * </p>
     */
    @Test
    public void testSignPadesBaselineT() throws IOException, GeneralSecurityException, OperatorException {
        try (   InputStream resource = getClass().getResourceAsStream("test.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "PadesBc.pdf"));
                PDDocument pdDocument = Loader.loadPDF(resource)   )
        {
            SignatureInterface signatureInterface = new PadesSignatureContainerBc(new X509CertificateHolder(chain[0].getEncoded()),
                    new JcaContentSignerBuilder("SHA512withRSA").build(pk),
                    new TSAClient(new URL("http://timestamp.server/rfc3161endpoint"), null, null, MessageDigest.getInstance("SHA-256")));

            PDSignature signature = new PDSignature();
            signature.setFilter(COSName.getPDFName("MKLx_PAdES_SIGNER"));
            signature.setSubFilter(COSName.getPDFName("ETSI.CAdES.detached"));
            signature.setName("Example User");
            signature.setLocation("Los Angeles, CA");
            signature.setReason("Testing");
            signature.setSignDate(Calendar.getInstance());
            pdDocument.addSignature(signature);

            ExternalSigningSupport externalSigning = pdDocument.saveIncrementalForExternalSigning(result);
            // invoke external signature service
            byte[] cmsSignature = signatureInterface.sign(externalSigning.getContent());
            // set signature bytes received from the service
            externalSigning.setSignature(cmsSignature);
        }
    }

}
