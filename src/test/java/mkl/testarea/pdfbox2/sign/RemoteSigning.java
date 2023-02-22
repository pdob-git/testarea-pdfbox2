package mkl.testarea.pdfbox2.sign;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;

import org.apache.pdfbox.examples.signature.SigUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.ess.ESSCertIDv2;
import org.bouncycastle.asn1.ess.SigningCertificateV2;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSAttributeTableGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.DefaultSignedAttributeTableGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jcajce.io.OutputStreamFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class RemoteSigning {
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    public static final String KEYSTORE = "keystores/demo-rsa2048.ks"; 
    public static final char[] PASSWORD = "demo-rsa2048".toCharArray(); 

    public static KeyStore ks = null;
    public static PrivateKey pk = null;
    public static Certificate[] chain = null;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();

        BouncyCastleProvider bcp = new BouncyCastleProvider();
        Security.addProvider(bcp);

        ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream(KEYSTORE), PASSWORD);
        String alias = (String) ks.aliases().nextElement();
        pk = (PrivateKey) ks.getKey(alias, PASSWORD);
        chain = ks.getCertificateChain(alias);
    }

    /**
     * <a href="https://stackoverflow.com/questions/75505900/pdfbox-document-getting-corrupted-after-adding-signed-attributes">
     * pdfbox - document getting corrupted after adding signed attributes
     * </a>
     * <p>
     * This is essentially the OP's code after he added a message digest attribute.
     * It still doesn't work because the signature still signs the document hash,
     * not the signed attributes.
     * </p>
     */
    @Test
    public void testSignLikeSkdjksDfksl() throws IOException, CMSException, OperatorCreationException, GeneralSecurityException {
        Object requestId = null;
        Object providerId = null;
        Object credentialId = null;
        Object accessToken = null;

        try (
            InputStream resource = getClass().getResourceAsStream("test.pdf");
            OutputStream output = new FileOutputStream(new File(RESULT_FOLDER, "TestRemoteSignLikeSkdjksDfksl.pdf"));
            PDDocument document = PDDocument.load(resource)   
        ) {
            Certificate[] certificateChain = retrieveCertificates(requestId, providerId, credentialId, accessToken);//Retrieve certificates from CSC.

            // create signature dictionary
            PDSignature signature = new PDSignature();

            int accessPermissions = SigUtils.getMDPPermission(document);
            if (accessPermissions == 1)
            {
                throw new IllegalStateException("No changes to the document are permitted due to DocMDP transform parameters dictionary");
            }

            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ETSI_CADES_DETACHED);
            signature.setName("Test Name");
            signature.setSignDate(Calendar.getInstance());

            //PDRectangle rect = new PDRectangle(100,  100,  300, 100);

            SignatureOptions signatureOptions = new SignatureOptions();
            //signatureOptions.setVisualSignature(createVisualSignatureTemplate(document, 0, rect, signature));
            signatureOptions.setPage(0);
            document.addSignature(signature, signatureOptions);

            ExternalSigningSupport externalSigning = document.saveIncrementalForExternalSigning(output);


            InputStream content = externalSigning.getContent();

            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            X509Certificate cert = (X509Certificate) certificateChain[0];
            gen.addCertificates(new JcaCertStore(Arrays.asList(certificateChain)));

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Use a buffer to read the input stream in chunks
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = content.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = digest.digest();

            ESSCertIDv2 certid = new ESSCertIDv2(
                    new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256),
                    MessageDigest.getInstance("SHA-256").digest(cert.getEncoded())
            );

            SigningCertificateV2 sigcert = new SigningCertificateV2(certid);

            final DERSet attrValues = new DERSet(sigcert);
            Attribute attr = new Attribute(PKCSObjectIdentifiers.id_aa_signingCertificateV2, attrValues);
            ASN1EncodableVector v = new ASN1EncodableVector();
            v.add(attr);
            v.add(new Attribute(CMSAttributes.messageDigest, new DERSet(new DEROctetString(hashBytes))));

            AttributeTable atttributeTable = new AttributeTable(v);
            //Create a standard attribute table from the passed in parameters - certhash
            CMSAttributeTableGenerator attrGen = new DefaultSignedAttributeTableGenerator(atttributeTable);


            final byte[] signedHash = signHash(requestId, providerId, accessToken, hashBytes); //Retrieve signed hash from CSC.

            ContentSigner contentSigner = new ContentSigner() {
                @Override
                public byte[] getSignature() {
                    return signedHash;
                }

                @Override
                public OutputStream getOutputStream() {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byteArrayOutputStream.writeBytes(hashBytes);
                    return byteArrayOutputStream;
                }

                @Override
                public AlgorithmIdentifier getAlgorithmIdentifier() {
                    return new AlgorithmIdentifier(new ASN1ObjectIdentifier("1.2.840.113549.1.1.11"));
                }
            };

            org.bouncycastle.asn1.x509.Certificate cert2 = org.bouncycastle.asn1.x509.Certificate.getInstance(ASN1Primitive.fromByteArray(cert.getEncoded()));
            JcaSignerInfoGeneratorBuilder sigb = new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build());

            sigb.setSignedAttributeGenerator(attrGen);

            gen.addSignerInfoGenerator(sigb.build(contentSigner, new X509CertificateHolder(cert2)));
            CMSTypedData msg = new CMSTypedDataInputStream(resource);

            CMSSignedData signedData = gen.generate((CMSTypedData)msg, false);
            byte[] cmsSignature = signedData.getEncoded();

            externalSigning.setSignature(cmsSignature);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/75505900/pdfbox-document-getting-corrupted-after-adding-signed-attributes">
     * pdfbox - document getting corrupted after adding signed attributes
     * </a>
     * <p>
     * This is an improved version of the OP's code in {@link #testSignLikeSkdjksDfksl()}.
     * Now the signature signs the signed attributes, not only the document hash.
     * </p>
     */
    @Test
    public void testSignLikeSkdjksDfkslImproved() throws IOException, CMSException, OperatorCreationException, GeneralSecurityException {
        Object requestId = null;
        Object providerId = null;
        Object credentialId = null;
        Object accessToken = null;

        try (
            InputStream resource = getClass().getResourceAsStream("test.pdf");
            OutputStream output = new FileOutputStream(new File(RESULT_FOLDER, "TestRemoteSignLikeSkdjksDfkslImproved.pdf"));
            PDDocument document = PDDocument.load(resource)   
        ) {
            PDSignature signature = new PDSignature();

            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ETSI_CADES_DETACHED);
            signature.setName("Test Name");
            signature.setSignDate(Calendar.getInstance());

            SignatureOptions signatureOptions = new SignatureOptions();
            signatureOptions.setPage(0);

            document.addSignature(signature, signatureOptions);
            ExternalSigningSupport externalSigning = document.saveIncrementalForExternalSigning(output);

            Certificate[] certificateChain = retrieveCertificates(requestId, providerId, credentialId, accessToken);
            X509Certificate cert = (X509Certificate) certificateChain[0];

            ESSCertIDv2 certid = new ESSCertIDv2(
                    new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256),
                    MessageDigest.getInstance("SHA-256").digest(cert.getEncoded())
            );
            SigningCertificateV2 sigcert = new SigningCertificateV2(certid);
            Attribute attr = new Attribute(PKCSObjectIdentifiers.id_aa_signingCertificateV2, new DERSet(sigcert));

            ASN1EncodableVector v = new ASN1EncodableVector();
            v.add(attr);
            AttributeTable atttributeTable = new AttributeTable(v);
            CMSAttributeTableGenerator attrGen = new DefaultSignedAttributeTableGenerator(atttributeTable);

            org.bouncycastle.asn1.x509.Certificate cert2 = org.bouncycastle.asn1.x509.Certificate.getInstance(ASN1Primitive.fromByteArray(cert.getEncoded()));
            JcaSignerInfoGeneratorBuilder sigb = new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build());
            sigb.setSignedAttributeGenerator(attrGen);

            ContentSigner contentSigner = new ContentSigner() {
                private MessageDigest digest = MessageDigest.getInstance("SHA-256");
                private OutputStream stream = OutputStreamFactory.createStream(digest);
                @Override
                public byte[] getSignature() {
                    try {
                        byte[] hash = digest.digest();
                        byte[] signedHash = signHash(requestId, providerId, accessToken, hash);
                        return signedHash;
                    } catch (Exception e) {
                        throw new RuntimeException("Exception while signing", e);
                    }
                }

                @Override
                public OutputStream getOutputStream() {
                    return stream;
                }

                @Override
                public AlgorithmIdentifier getAlgorithmIdentifier() {
                    return new AlgorithmIdentifier(new ASN1ObjectIdentifier("1.2.840.113549.1.1.11"));
                }
            };

            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            gen.addCertificates(new JcaCertStore(Arrays.asList(certificateChain)));
            gen.addSignerInfoGenerator(sigb.build(contentSigner, new X509CertificateHolder(cert2)));

            CMSTypedData msg = new CMSTypedDataInputStream(externalSigning.getContent());
            CMSSignedData signedData = gen.generate(msg, false);

            byte[] cmsSignature = signedData.getEncoded();
            externalSigning.setSignature(cmsSignature);
        }
    }

    File createVisualSignatureTemplate(PDDocument document, int i, PDRectangle rect, PDSignature signature) {
        // TODO Auto-generated method stub
        return null;
    }

    byte[] signHash(Object requestId, Object providerId, Object accessToken, byte[] hashBytes) throws GeneralSecurityException, IOException {
        Signature signature = Signature.getInstance("NONEwithRSA");
        signature.initSign(pk);

        AlgorithmIdentifier sha256Aid = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256, DERNull.INSTANCE);
        DigestInfo di = new DigestInfo(sha256Aid, hashBytes);
        signature.update(di.toASN1Primitive().getEncoded());

        return signature.sign();
    }

    Certificate[] retrieveCertificates(Object requestId, Object providerId, Object credentialId, Object accessToken) {
        return chain;
    }

    class CMSTypedDataInputStream implements CMSTypedData {
        InputStream in;

        public CMSTypedDataInputStream(InputStream is) {
            in = is;
        }

        @Override
        public ASN1ObjectIdentifier getContentType() {
            return PKCSObjectIdentifiers.data;
        }

        @Override
        public Object getContent() {
            return in;
        }

        @Override
        public void write(OutputStream out) throws IOException,
                CMSException {
            byte[] buffer = new byte[8 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
        }
    }
}
