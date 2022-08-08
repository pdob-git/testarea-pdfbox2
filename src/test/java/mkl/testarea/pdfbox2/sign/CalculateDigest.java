package mkl.testarea.pdfbox2.sign;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Store;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class CalculateDigest {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * <a href="https://stackoverflow.com/questions/57926872/signed-pdf-content-digest-that-was-calculated-during-verification-is-diffrent-th">
     * Signed PDF content digest that was calculated during verification is diffrent than decripted digest from signature
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1UlOZOp-UYllK7Ra35dggccoWdhcb_Ntp">
     * TEST-signed-pades-baseline-b.pdf
     * </a>
     * <p>
     * The code in {@link #verifyPDF(String)} compares the wrong CMS
     * hash with the has of the signed byte ranges, it compares the
     * hash in the primitive PKCS1 signature which actually signs the
     * signed attributes of its SignerInfo, not the signed PDF byte
     * ranges directly.
     * </p>
     */
    @Test
    public void testVerifyPdfLikeUser2893427() throws Exception {
        verifyPDF("src\\test\\resources\\mkl\\testarea\\pdfbox2\\sign\\TEST-signed-pades-baseline-b.pdf");
    }

    /** @see #testVerifyPdfLikeUser2893427() */
    public static void verifyPDF(String fileName) throws Exception {
        File fileDoc = new File(fileName);
        PDDocument document = PDDocument.load(fileDoc);
        List<PDSignature> signatures = document.getSignatureDictionaries();
        PDSignature sig = signatures.get(0);
        if (sig != null) {
            String subFilter = sig.getSubFilter();
            if (subFilter != null) {
                Collection<X509Certificate> certs = new ArrayList<X509Certificate>();
                switch (subFilter) {
                case "ETSI.CAdES.detached":
                case "adbe.pkcs7.detached":
                    FileInputStream fis = new FileInputStream(fileDoc);
                    byte[] signatureContent = sig.getContents(fis);
                    System.out.println("---------signatureContent length------------");
                    System.out.println(signatureContent.length);
                    String signatureContentB64 = Base64.getEncoder().encodeToString(signatureContent);
                    System.out.println("---------signatureContent b64------------");
                    System.out.println(signatureContentB64);
                    fis = new FileInputStream(fileDoc);
                    byte[] signedContent = sig.getSignedContent(fis);
                    String signedContentB64 = Base64.getEncoder().encodeToString(signedContent);
                    System.out.println("---------signedContent length------------");
                    System.out.println(signedContent.length);
                    System.out.println("---------signedContent b64------------");
                    System.out.println(signedContentB64);

                    // Now we construct a PKCS #7 or CMS.
                    CMSProcessable cmsProcessableInputStream = new CMSProcessableByteArray(signedContent);
                    CMSSignedData cmsSignedData = new CMSSignedData(cmsProcessableInputStream, signatureContent);
                    Store<X509CertificateHolder> certificatesStore = cmsSignedData.getCertificates();
                    Collection<SignerInformation> signers = cmsSignedData.getSignerInfos().getSigners();
                    SignerInformation signerInformation = signers.iterator().next();
                    @SuppressWarnings("unchecked")
                    Collection<X509CertificateHolder> matches = certificatesStore.getMatches(signerInformation.getSID());
                    X509CertificateHolder certificateHolder = (X509CertificateHolder) matches.iterator().next();
                    certificateHolder.getSerialNumber();
                    X509Certificate certFromSignedData = new JcaX509CertificateConverter()
                            .getCertificate(certificateHolder);
                    certs.add(certFromSignedData);

                    SignerInformationVerifier signerInformationVerifier = new JcaSimpleSignerInfoVerifierBuilder()
                            .build(certificateHolder);
                    boolean isValid = signerInformation.verify(signerInformationVerifier);

                    System.out.println("---------isValid------------");
                    System.out.println(isValid);
                    System.out.println("---------certSerialNumber dec------------");
                    System.out.println(certificateHolder.getSerialNumber());
                    System.out.println("---------certSerialNumber hex------------");
                    System.out.println(String.format("0x%08X", certificateHolder.getSerialNumber()));
                    System.out.println("---------certSubject------------");
                    System.out.println(certificateHolder.getSubject().toString());
                    System.out.println("---------getContentType------------");
                    System.out.println(signerInformation.getContentType().toString());
                    System.out.println("---------contentDigest base64------------");
                    byte[] contentDigest = signerInformation.getContentDigest();
                    String contentDigestB64 = Base64.getEncoder().encodeToString(contentDigest);
                    System.out.println(contentDigestB64);
                    System.out.println("---------contentDigest hex------------");
                    String contentDigestHex = DatatypeConverter.printHexBinary(contentDigest);
                    System.out.println(contentDigestHex);
                    System.out.println("---------digestAlgOID------------");
                    System.out.println(signerInformation.getDigestAlgOID());
                    System.out.println(signerInformation.getDigestAlgorithmID());
                    System.out.println("---------encryptionAlgOID------------");
                    System.out.println(signerInformation.getEncryptionAlgOID());
                    ;

                    // https://gist.github.com/nielsutrecht/855f3bef0cf559d8d23e94e2aecd4ede

                    byte[] signatureBytes = signerInformation.getSignature();
                    String signatureBytesB64 = Base64.getEncoder().encodeToString(signatureBytes);
                    System.out.println("---------getSignature (encripted) base64------------");
                    System.out.println(signatureBytesB64);
                    System.out.println("---------getSignature (encripted) hex------------");
                    String signatureBytesHex = DatatypeConverter.printHexBinary(signatureBytes);
                    System.out.println(signatureBytesHex);

                    System.out.println("---------getSignature (decripted) base64------------");
                    Cipher encryptCipher = Cipher.getInstance("RSA");
                    PublicKey publicKey = certFromSignedData.getPublicKey();
                    encryptCipher.init(Cipher.DECRYPT_MODE, publicKey);
                    byte[] cipherText = encryptCipher.doFinal(signatureBytes);
                    String cipherTextB64 = Base64.getEncoder().encodeToString(cipherText);
                    System.out.println(cipherTextB64);
                    System.out.println("---------getSignature (decripted) hex------------");
                    String cipherTextHex = DatatypeConverter.printHexBinary(cipherText);
                    System.out.println(cipherTextHex);

                    byte[] digest = null;

                    ASN1InputStream ais = new ASN1InputStream(cipherText);
                    ASN1Primitive obj = ais.readObject();
                    // System.out.println("---------getSignature ASN1 parse------------");
                    // System.out.println(ASN1Dump.dumpAsString(obj, true));
                    DigestInfo digestInfo = new DigestInfo((ASN1Sequence) obj);
                    System.out.println("---------getAlgorithmId------------");
                    System.out.println(digestInfo.getAlgorithmId().getAlgorithm().getId());

                    System.out.println("---------getDigest hex AND contentDigest hex DIFFER !!!!------------");
                    System.out.println("---------getDigest hex------------");
                    digest = digestInfo.getDigest();
                    System.out.println(DatatypeConverter.printHexBinary(digest));

                    ais.close();

                    System.out.println("---------contentDigest hex------------");
                    System.out.println(contentDigestHex);

                    final Signature signature = Signature.getInstance("SHA256withRSA");
                    signature.initVerify(publicKey);
                    signature.update(digest);
                    System.out.println("---------signature.verify------------");
                    System.out.println(signature.verify(signatureBytes));

                    Security.addProvider(new BouncyCastleProvider());
                    Signature bcSignature = Signature.getInstance("RSA", "BC");
                    bcSignature.initVerify(publicKey);
                    bcSignature.update(digest);
                    System.out.println("---------signature bc.verify------------");
                    System.out.println(bcSignature.verify(signatureBytes));

                    break;

                default:
                    throw new IOException("Unknown certificate type " + subFilter);

                }
                ;
            }
            ;
        }
        ;
    }

    /**
     * <a href="https://stackoverflow.com/questions/57926872/signed-pdf-content-digest-that-was-calculated-during-verification-is-diffrent-th">
     * Signed PDF content digest that was calculated during verification is diffrent than decripted digest from signature
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1UlOZOp-UYllK7Ra35dggccoWdhcb_Ntp">
     * TEST-signed-pades-baseline-b.pdf
     * </a>
     * <p>
     * The code here demonstrates how to retrieve the messageDigest
     * signed attribute value from a signed PDF. For production use
     * obviously some null checks are required.
     * </p>
     */
    @Test
    public void testExtractMessageDigestAttributeForUser2893427() throws IOException, CMSException {
        try (   InputStream resource = getClass().getResourceAsStream("TEST-signed-pades-baseline-b.pdf")   ) {
            byte[] bytes = IOUtils.toByteArray(resource);
            PDDocument document = PDDocument.load(bytes);
            List<PDSignature> signatures = document.getSignatureDictionaries();
            PDSignature sig = signatures.get(0);
            byte[] cmsBytes = sig.getContents(bytes);
            CMSSignedData cms = new CMSSignedData(cmsBytes);
            SignerInformation signerInformation = cms.getSignerInfos().iterator().next();
            Attribute attribute = signerInformation.getSignedAttributes().get(PKCSObjectIdentifiers.pkcs_9_at_messageDigest);
            ASN1Encodable value = attribute.getAttributeValues()[0];
            System.out.printf("MessageDigest attribute value: %s\n", value);
        }
    }
}
