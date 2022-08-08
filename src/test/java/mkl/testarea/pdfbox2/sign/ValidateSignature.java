package mkl.testarea.pdfbox2.sign;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Security;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ValidateSignature
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        Security.addProvider(new BouncyCastleProvider());
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/41116833/pdf-signature-validation">
     * PDF Signature Validation
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0BzEmZ9pRWLhPOUJSYUdlRjg2eEU/view?usp=sharing">
     * SignatureVlidationTest.pdf
     * </a>
     * <p>
     * The code completely ignores the <b>SubFilter</b> of the signature.
     * It is appropriate for signatures with <b>SubFilter</b> values
     * <b>adbe.pkcs7.detached</b> and <b>ETSI.CAdES.detached</b>
     * but will fail for signatures with <b>SubFilter</b> values
     * <b>adbe.pkcs7.sha1</b> and <b>adbe.x509.rsa.sha1</b>.
     * </p>
     * <p>
     * The example document has been signed with a signatures with
     * <b>SubFilter</b> value <b>adbe.pkcs7.sha1</b>.
     * </p>
     */
    @Test
    public void testValidateSignatureVlidationTest() throws Exception
    {
        System.out.println("\nValidate signature in SignatureVlidationTest.pdf; original code.");
        byte[] pdfByte;
        PDDocument pdfDoc = null;
        SignerInformationVerifier verifier = null;
        try
        {
            pdfByte = IOUtils.toByteArray(this.getClass().getResourceAsStream("SignatureVlidationTest.pdf"));
            pdfDoc = PDDocument.load(new ByteArrayInputStream(pdfByte));
            PDSignature signature = pdfDoc.getSignatureDictionaries().get(0);

            byte[] signatureAsBytes = signature.getContents(pdfByte);
            byte[] signedContentAsBytes = signature.getSignedContent(pdfByte);
            CMSSignedData cms = new CMSSignedData(new CMSProcessableByteArray(signedContentAsBytes), signatureAsBytes);
            SignerInformation signerInfo = (SignerInformation) cms.getSignerInfos().getSigners().iterator().next();
            X509CertificateHolder cert = (X509CertificateHolder) cms.getCertificates().getMatches(signerInfo.getSID())
                    .iterator().next();
            verifier = new JcaSimpleSignerInfoVerifierBuilder().setProvider(new BouncyCastleProvider()).build(cert);

            // result if false
            boolean verifyRt = signerInfo.verify(verifier);
            System.out.println("Verify result: " + verifyRt);
        }
        finally
        {
            if (pdfDoc != null)
            {
                pdfDoc.close();
            }
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/41116833/pdf-signature-validation">
     * PDF Signature Validation
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0BzEmZ9pRWLhPOUJSYUdlRjg2eEU/view?usp=sharing">
     * SignatureVlidationTest.pdf
     * </a>
     * <p>
     * This code also ignores the <b>SubFilter</b> of the signature,
     * it is appropriate for signatures with <b>SubFilter</b> value
     * <b>adbe.pkcs7.sha1</b> which the example document has been
     * signed with.
     * </p>
     */
    @Test
    public void testValidateSignatureVlidationTestAdbePkcs7Sha1() throws Exception
    {
        System.out.println("\nValidate signature in SignatureVlidationTest.pdf; special adbe.pkcs7.sha1 code.");
        byte[] pdfByte;
        PDDocument pdfDoc = null;
        SignerInformationVerifier verifier = null;
        try
        {
            pdfByte = IOUtils.toByteArray(this.getClass().getResourceAsStream("SignatureVlidationTest.pdf"));
            pdfDoc = PDDocument.load(new ByteArrayInputStream(pdfByte));
            PDSignature signature = pdfDoc.getSignatureDictionaries().get(0);

            byte[] signatureAsBytes = signature.getContents(pdfByte);
            CMSSignedData cms = new CMSSignedData(new ByteArrayInputStream(signatureAsBytes));
            SignerInformation signerInfo = (SignerInformation) cms.getSignerInfos().getSigners().iterator().next();
            X509CertificateHolder cert = (X509CertificateHolder) cms.getCertificates().getMatches(signerInfo.getSID())
                    .iterator().next();
            verifier = new JcaSimpleSignerInfoVerifierBuilder().setProvider(new BouncyCastleProvider()).build(cert);

            boolean verifyRt = signerInfo.verify(verifier);
            System.out.println("Verify result: " + verifyRt);

            byte[] signedContentAsBytes = signature.getSignedContent(pdfByte);
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] calculatedDigest = md.digest(signedContentAsBytes);
            byte[] signedDigest = (byte[]) cms.getSignedContent().getContent();
            System.out.println("Document digest equals: " + Arrays.equals(calculatedDigest, signedDigest));
        }
        finally
        {
            if (pdfDoc != null)
            {
                pdfDoc.close();
            }
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/41116833/pdf-signature-validation">
     * PDF Signature Validation
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0BzEmZ9pRWLhPOUJSYUdlRjg2eEU/view?usp=sharing">
     * SignatureVlidationTest.pdf
     * </a>
     * <p>
     * This code is <b>SubFilter</b>-aware.
     * </p>
     */
    @Test
    public void testValidateSignatureVlidationTestImproved() throws Exception
    {
        System.out.println("\nValidate signature in SignatureVlidationTest.pdf; dynamic code.");
        try ( InputStream resource = getClass().getResourceAsStream("SignatureVlidationTest.pdf"))
        {
            Assert.assertTrue(validateSignaturesImproved(IOUtils.toByteArray(resource), "SignatureVlidationTest-%s.cms"));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/41116833/pdf-signature-validation">
     * PDF Signature Validation
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0BzEmZ9pRWLhPblJJWXNuY2tneHM/view?usp=sharing">
     * pkcs7DetachedFailure.pdf
     * </a>
     * <p>
     * The reason for this validation failing here while succeeding in Adobe Reader is that
     * the signed attributes here are not properly DER encoded - the order in the set is wrong.
     * Adobe Reader validates using the original, non-DER signed attributes representation
     * while BouncyCastle here validates using a DER signed attributes representation.
     * </p>
     */
    @Test
    public void testValidatePkcs7DetachedFailureImproved() throws Exception
    {
        System.out.println("\nValidate signature in pkcs7DetachedFailure.pdf; dynamic code.");
        try ( InputStream resource = getClass().getResourceAsStream("pkcs7DetachedFailure.pdf"))
        {
            Assert.assertTrue(validateSignaturesImproved(IOUtils.toByteArray(resource), "pkcs7DetachedFailure-%s.cms"));
        }
    }

    boolean validateSignaturesImproved(byte[] pdfByte, String signatureFileName) throws IOException, CMSException, OperatorCreationException, GeneralSecurityException
    {
        boolean result = true;
        try (PDDocument pdfDoc = PDDocument.load(pdfByte))
        {
            List<PDSignature> signatures = pdfDoc.getSignatureDictionaries();
            int index = 0;
            for (PDSignature signature : signatures)
            {
                String subFilter = signature.getSubFilter();
                byte[] signatureAsBytes = signature.getContents(pdfByte);
                byte[] signedContentAsBytes = signature.getSignedContent(pdfByte);
                System.out.printf("\nSignature # %s (%s)\n", ++index, subFilter);

                if (signatureFileName != null)
                {
                    String fileName = String.format(signatureFileName, index);
                    Files.write(new File(RESULT_FOLDER, fileName).toPath(), signatureAsBytes);
                    System.out.printf("    Stored as '%s'.\n", fileName);
                }

                final CMSSignedData cms;
                if ("adbe.pkcs7.detached".equals(subFilter) || "ETSI.CAdES.detached".equals(subFilter))
                {
                    cms = new CMSSignedData(new CMSProcessableByteArray(signedContentAsBytes), signatureAsBytes);
                }
                else if ("adbe.pkcs7.sha1".equals(subFilter))
                {
                    cms = new CMSSignedData(new ByteArrayInputStream(signatureAsBytes));
                }
                else if ("adbe.x509.rsa.sha1".equals(subFilter) || "ETSI.RFC3161".equals(subFilter))
                {
                    result = false;
                    System.out.printf("!!! SubFilter %s not yet supported.\n", subFilter);
                    continue;
                }
                else if (subFilter != null)
                {
                    result = false;
                    System.out.printf("!!! Unknown SubFilter %s.\n", subFilter);
                    continue;
                }
                else
                {
                    result = false;
                    System.out.println("!!! Missing SubFilter.");
                    continue;
                }

                SignerInformation signerInfo = (SignerInformation) cms.getSignerInfos().getSigners().iterator().next();
                X509CertificateHolder cert = (X509CertificateHolder) cms.getCertificates().getMatches(signerInfo.getSID())
                        .iterator().next();
                SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder().setProvider(new BouncyCastleProvider()).build(cert);

                boolean verifyResult = signerInfo.verify(verifier);
                if (verifyResult)
                    System.out.println("    Signature verification successful.");
                else
                {
                    result = false;
                    System.out.println("!!! Signature verification failed!");

                    if (signatureFileName != null)
                    {
                        String fileName = String.format(signatureFileName + "-sigAttr.der", index);
                        Files.write(new File(RESULT_FOLDER, fileName).toPath(), signerInfo.getEncodedSignedAttributes());
                        System.out.printf("    Encoded signed attributes stored as '%s'.\n", fileName);
                    }

                }

                if ("adbe.pkcs7.sha1".equals(subFilter))
                {
                    MessageDigest md = MessageDigest.getInstance("SHA1");
                    byte[] calculatedDigest = md.digest(signedContentAsBytes);
                    byte[] signedDigest = (byte[]) cms.getSignedContent().getContent();
                    boolean digestsMatch = Arrays.equals(calculatedDigest, signedDigest);
                    if (digestsMatch)
                        System.out.println("    Document SHA1 digest matches.");
                    else
                    {
                        result = false;
                        System.out.println("!!! Document SHA1 digest does not match!");
                    }
                }
            }
        }
        return result;
    }
}
