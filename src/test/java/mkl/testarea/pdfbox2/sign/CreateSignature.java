package mkl.testarea.pdfbox2.sign;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.cos.COSUpdateInfo;
import org.apache.pdfbox.examples.signature.CreateVisibleSignature;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTerminalField;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cms.CMSAbsentContent;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.DefaultSignedAttributeTableGenerator;
import org.bouncycastle.cms.SignerInfoGeneratorBuilder;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Hex;
import org.junit.BeforeClass;
import org.junit.Test;

import mkl.testarea.pdfbox2.extract.BoundingBoxFinder;

/**
 * @author mkl
 */
public class CreateSignature
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    public static final String KEYSTORE = "keystores/demo-rsa2048.ks"; 
    public static final char[] PASSWORD = "demo-rsa2048".toCharArray(); 

    public static KeyStore ks = null;
    public static PrivateKey pk = null;
    public static Certificate[] chain = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
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
     * <a href="http://stackoverflow.com/questions/41767351/create-pkcs7-signature-from-file-digest">
     * Create pkcs7 signature from file digest
     * </a>
     * <p>
     * This test uses the OP's own <code>sign</code> method: {@link #signBySnox(InputStream)}.
     * There are small errors in it, so the result is rejected by verification. These errors
     * are corrected in {@link #signWithSeparatedHashing(InputStream)} which is tested in
     * {@link #testSignWithSeparatedHashing()}.
     * </p>
     */
    @Test
    public void testSignWithSeparatedHashingLikeSnox() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("test.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "testSignedLikeSnox.pdf"));
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            sign(pdDocument, result, data -> signBySnox(data));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/41767351/create-pkcs7-signature-from-file-digest">
     * Create pkcs7 signature from file digest
     * </a>
     * <p>
     * This test uses a fixed version of the OP's <code>sign</code> method:
     * {@link #signWithSeparatedHashing(InputStream)}. Here the errors from
     * {@link #signBySnox(InputStream)} are corrected, so the result is not
     * rejected by verification anymore.
     * </p>
     */
    @Test
    public void testSignWithSeparatedHashing() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("test.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "testSignedWithSeparatedHashing.pdf"));
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            sign(pdDocument, result, data -> signWithSeparatedHashing(data));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/41767351/create-pkcs7-signature-from-file-digest">
     * Create pkcs7 signature from file digest
     * </a>
     * <p>
     * A minimal signing frame work merely requiring a {@link SignatureInterface}
     * instance.
     * </p>
     */
    void sign(PDDocument document, OutputStream output, SignatureInterface signatureInterface) throws IOException
    {
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName("Example User");
        signature.setLocation("Los Angeles, CA");
        signature.setReason("Testing");
        signature.setSignDate(Calendar.getInstance());
        document.addSignature(signature);
        ExternalSigningSupport externalSigning =
                document.saveIncrementalForExternalSigning(output);
        // invoke external signature service
        byte[] cmsSignature = signatureInterface.sign(externalSigning.getContent());
        // set signature bytes received from the service
        externalSigning.setSignature(cmsSignature);
    }

    /**
     * <a href="http://stackoverflow.com/questions/41767351/create-pkcs7-signature-from-file-digest">
     * Create pkcs7 signature from file digest
     * </a>
     * <p>
     * The OP's own <code>sign</code> method which has some errors. These
     * errors are fixed in {@link #signWithSeparatedHashing(InputStream)}.
     * </p>
     */
    public byte[] signBySnox(InputStream content) throws IOException {
        // testSHA1WithRSAAndAttributeTable
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1", "BC");
            List<Certificate> certList = new ArrayList<Certificate>();
            CMSTypedData msg = new CMSProcessableByteArray(IOUtils.toByteArray(content));

            certList.addAll(Arrays.asList(chain));

            Store<?> certs = new JcaCertStore(certList);

            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

            Attribute attr = new Attribute(CMSAttributes.messageDigest,
                    new DERSet(new DEROctetString(md.digest(IOUtils.toByteArray(content)))));

            ASN1EncodableVector v = new ASN1EncodableVector();

            v.add(attr);

            SignerInfoGeneratorBuilder builder = new SignerInfoGeneratorBuilder(new BcDigestCalculatorProvider())
                    .setSignedAttributeGenerator(new DefaultSignedAttributeTableGenerator(new AttributeTable(v)));

            AlgorithmIdentifier sha1withRSA = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(chain[0].getEncoded());
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);

            gen.addSignerInfoGenerator(builder.build(
                    new BcRSAContentSignerBuilder(sha1withRSA,
                            new DefaultDigestAlgorithmIdentifierFinder().find(sha1withRSA))
                                    .build(PrivateKeyFactory.createKey(pk.getEncoded())),
                    new JcaX509CertificateHolder(cert)));

            gen.addCertificates(certs);

            CMSSignedData s = gen.generate(new CMSAbsentContent(), false);
            return new CMSSignedData(msg, s.getEncoded()).getEncoded();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/41767351/create-pkcs7-signature-from-file-digest">
     * Create pkcs7 signature from file digest
     * </a>
     * <p>
     * The OP's <code>sign</code> method after fixing some errors. The
     * OP's original method is {@link #signBySnox(InputStream)}. The
     * errors were
     * </p>
     * <ul>
     * <li>multiple attempts at reading the {@link InputStream} parameter;
     * <li>convoluted creation of final CMS container.
     * </ul>
     * <p>
     * Additionally this method uses SHA256 instead of SHA-1.
     * </p>
     */
    public byte[] signWithSeparatedHashing(InputStream content) throws IOException
    {
        try
        {
            // Digest generation step
            MessageDigest md = MessageDigest.getInstance("SHA256", "BC");
            byte[] digest = md.digest(IOUtils.toByteArray(content));

            // Separate signature container creation step
            List<Certificate> certList = Arrays.asList(chain);
            JcaCertStore certs = new JcaCertStore(certList);

            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

            Attribute attr = new Attribute(CMSAttributes.messageDigest,
                    new DERSet(new DEROctetString(digest)));

            ASN1EncodableVector v = new ASN1EncodableVector();

            v.add(attr);

            SignerInfoGeneratorBuilder builder = new SignerInfoGeneratorBuilder(new BcDigestCalculatorProvider())
                    .setSignedAttributeGenerator(new DefaultSignedAttributeTableGenerator(new AttributeTable(v)));

            AlgorithmIdentifier sha256withRSA = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(chain[0].getEncoded());
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);

            gen.addSignerInfoGenerator(builder.build(
                    new BcRSAContentSignerBuilder(sha256withRSA,
                            new DefaultDigestAlgorithmIdentifierFinder().find(sha256withRSA))
                                    .build(PrivateKeyFactory.createKey(pk.getEncoded())),
                    new JcaX509CertificateHolder(cert)));

            gen.addCertificates(certs);

            CMSSignedData s = gen.generate(new CMSAbsentContent(), false);
            return s.getEncoded();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new IOException(e);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/49894319/lock-dictionary-in-signature-field-is-the-reason-of-broken-signature-after-sig">
     * “Lock” dictionary in signature field is the reason of broken signature after signing
     * </a>
     * <p>
     * This test shows how to properly sign signatures in a field with a
     * signature Lock dictionary. In particular important is the addition
     * of FieldMDP transform data to the signature value as here is done
     * in {@link #signExistingFieldWithLock(PDDocument, OutputStream, SignatureInterface)}.
     * </p>
     */
    @Test
    public void testSignWithLocking() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("test.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "testFieldWithLocking.pdf"));
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            PDAcroForm acroForm = pdDocument.getDocumentCatalog().getAcroForm();
            if (acroForm == null)
            {
                acroForm = new PDAcroForm(pdDocument);
                pdDocument.getDocumentCatalog().setAcroForm(acroForm);
            }
            PDSignatureField signatureField = new PDSignatureField(acroForm);
            acroForm.getFields().add(signatureField);
            signatureField.getWidgets().get(0).setPage(pdDocument.getPage(0));
            pdDocument.getPage(0).getAnnotations().add(signatureField.getWidgets().get(0));
            signatureField.getWidgets().get(0).setRectangle(new PDRectangle(100, 600, 300, 200));
            setLock(signatureField, acroForm);
            pdDocument.save(result);
        }

        try (   InputStream resource = new FileInputStream(new File(RESULT_FOLDER, "testFieldWithLocking.pdf"));
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "testSignedWithLocking.pdf"));
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            signExistingFieldWithLock(pdDocument, result, data -> signWithSeparatedHashing(data));
        }
    }

    /**
     * <p>
     * A minimal signing frame work merely requiring a {@link SignatureInterface}
     * instance signing an existing field.
     * </p>
     * @see #testSignWithLocking()
     */
    void signExistingFieldWithLock(PDDocument document, OutputStream output, SignatureInterface signatureInterface) throws IOException
    {
        PDSignatureField signatureField = document.getSignatureFields().get(0);
        PDSignature signature = new PDSignature();
        signatureField.setValue(signature);

        COSBase lock = signatureField.getCOSObject().getDictionaryObject(COS_NAME_LOCK);
        if (lock instanceof COSDictionary)
        {
            COSDictionary lockDict = (COSDictionary) lock;
            COSDictionary transformParams = new COSDictionary(lockDict);
            transformParams.setItem(COSName.TYPE, COSName.getPDFName("TransformParams"));
            transformParams.setItem(COSName.V, COSName.getPDFName("1.2"));
            transformParams.setDirect(true);
            COSDictionary sigRef = new COSDictionary();
            sigRef.setItem(COSName.TYPE, COSName.getPDFName("SigRef"));
            sigRef.setItem(COSName.getPDFName("TransformParams"), transformParams);
            sigRef.setItem(COSName.getPDFName("TransformMethod"), COSName.getPDFName("FieldMDP"));
            sigRef.setItem(COSName.getPDFName("Data"), document.getDocumentCatalog());
            sigRef.setDirect(true);
            COSArray referenceArray = new COSArray();
            referenceArray.add(sigRef);
            signature.getCOSObject().setItem(COSName.getPDFName("Reference"), referenceArray);
        }

        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName("blablabla");
        signature.setLocation("blablabla");
        signature.setReason("blablabla");
        signature.setSignDate(Calendar.getInstance());
        document.addSignature(signature);
        ExternalSigningSupport externalSigning =
                document.saveIncrementalForExternalSigning(output);
        // invoke external signature service
        byte[] cmsSignature = signatureInterface.sign(externalSigning.getContent());
        // set signature bytes received from the service
        externalSigning.setSignature(cmsSignature);
    }

    /**
     * <a href="https://stackoverflow.com/questions/49894319/lock-dictionary-in-signature-field-is-the-reason-of-broken-signature-after-sig">
     * “Lock” dictionary in signature field is the reason of broken signature after signing
     * </a>
     * <p>
     * This code originally was in the OP's SigningUtils class.
     * </p>
     */
    public static void setLock(PDSignatureField pdSignatureField, PDAcroForm acroForm) {
        COSDictionary lockDict = new COSDictionary();
        lockDict.setItem(COS_NAME_ACTION, COS_NAME_ALL);
        lockDict.setItem(COSName.TYPE, COS_NAME_SIG_FIELD_LOCK);
        pdSignatureField.getCOSObject().setItem(COS_NAME_LOCK, lockDict);
    }

    public static final COSName COS_NAME_LOCK = COSName.getPDFName("Lock");
    public static final COSName COS_NAME_ACTION = COSName.getPDFName("Action");
    public static final COSName COS_NAME_ALL = COSName.getPDFName("All");
    public static final COSName COS_NAME_SIG_FIELD_LOCK = COSName.getPDFName("SigFieldLock");

    /**
     * <a href="https://stackoverflow.com/questions/50224181/pdfbox-2-0-8-issue-while-signing-document">
     * PDFBox 2.0.8 issue while signing document
     * </a>
     * <p>
     * This test checks the OP's code which essentially is a copy of the
     * PDFBox example class CreateVisibleSignature with an extra method.
     * </p>
     * <p>
     * The cause was that the OP chose a PDDocument.addSignature overload for
     * external signing while implementing otherwise internal signing. Choosing
     * an appropriate overload fixed the problem.
     * </p>
     */
    @Test
    public void signLikeIperezmel78() throws IOException, GeneralSecurityException {
        try (   InputStream resource = getClass().getResourceAsStream("test.pdf")) {
            PDDocument document = PDDocument.load(resource);
            byte[] bytes = VisibleSignature.sign(document, KEYSTORE, PASSWORD, "/mkl/testarea/pdfbox2/content/Willi-1.jpg");
            Files.write(new File(RESULT_FOLDER, "test-signedLikeIperezmel78.pdf").toPath(),  bytes);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/52757037/how-to-generate-pkcs7-signature-from-digest">
     * How to generate PKCS#7 signature from digest?
     * </a>
     * <p>
     * Like {@link #sign(PDDocument, OutputStream, SignatureInterface)}, merely
     * the subfilter now indicates a PAdES signature, not a legacy ISO 32000-1
     * signature. The generated signature is invalid as it does not have an ESS
     * signing certificate attribute. 
     * </p>
     * @see #testSignPAdESWithSeparatedHashing()
     */
    void signPAdES(PDDocument document, OutputStream output, SignatureInterface signatureInterface) throws IOException
    {
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ETSI_CADES_DETACHED);
        signature.setName("Example User");
        signature.setLocation("Los Angeles, CA");
        signature.setReason("Testing");
        signature.setSignDate(Calendar.getInstance());
        document.addSignature(signature);
        ExternalSigningSupport externalSigning =
                document.saveIncrementalForExternalSigning(output);
        // invoke external signature service
        byte[] cmsSignature = signatureInterface.sign(externalSigning.getContent());
        // set signature bytes received from the service
        externalSigning.setSignature(cmsSignature);
    }

    /**
     * <a href="https://stackoverflow.com/questions/52757037/how-to-generate-pkcs7-signature-from-digest">
     * How to generate PKCS#7 signature from digest?
     * </a>
     * <p>
     * This test is like {@link #testSignWithSeparatedHashing()}, merely the
     * subfilter now indicates a PAdES signature, not a legacy ISO 32000-1
     * signature. The generated signature is invalid as it does not have an
     * ESS signing certificate attribute required by PAdES.
     * </p>
     */
    @Test
    public void testSignPAdESWithSeparatedHashing() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("test.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "testSignedPAdESWithSeparatedHashing.pdf"));
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            signPAdES(pdDocument, result, data -> signWithSeparatedHashing(data));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/58427451/how-to-apply-digital-signature-image-at-bottom-left-position-in-the-last-page-of">
     * How to apply digital signature image at bottom left position in the last page of pdf using pdfbox?
     * </a>
     * <br/>
     * <a href="http://www.orimi.com/pdf-test.pdf">
     * pdf-test.pdf
     * </a>
     * <p>
     * As the OP found out himself, the `BoundingBoxFinder` coordinates
     * could not be used as is in the `CreateVisibleSignature`. This test
     * demonstrates the required transformation with the example document
     * apparently used by the OP.
     * </p>
     */
    @Test
    public void signLikeHemantPdfTest() throws IOException, GeneralSecurityException {
        File documentFile = new File("src/test/resources/mkl/testarea/pdfbox2/sign/pdf-test.pdf");
        File signedDocumentFile = new File(RESULT_FOLDER, "pdf-test-signedLikeHemant.pdf");

        Rectangle2D boundingBox;
        PDRectangle mediaBox;
        try (   PDDocument document = PDDocument.load(documentFile) ) {
            PDPage pdPage = document.getPage(0);
            BoundingBoxFinder boundingBoxFinder = new BoundingBoxFinder(pdPage);
            boundingBoxFinder.processPage(pdPage);
            boundingBox = boundingBoxFinder.getBoundingBox();
            mediaBox = pdPage.getMediaBox();
        }

        CreateVisibleSignature signing = new CreateVisibleSignature(ks, PASSWORD.clone());
        try (   InputStream imageStream = getClass().getResourceAsStream("/mkl/testarea/pdfbox2/content/Willi-1.jpg")) {
            signing.setVisibleSignDesigner(documentFile.getPath(), (int)boundingBox.getX(), (int)(mediaBox.getUpperRightY() - boundingBox.getY()), -50, imageStream, 1);
        }
        signing.setVisibleSignatureProperties("name", "location", "Security", 0, 1, true);
        signing.setExternalSigning(false);
        signing.signPDF(documentFile, signedDocumentFile, null);
    }

    /**
     * <a href="https://stackoverflow.com/questions/58427451/how-to-apply-digital-signature-image-at-bottom-left-position-in-the-last-page-of">
     * How to apply digital signature image at bottom left position in the last page of pdf using pdfbox?
     * </a>
     * <p>
     * As the OP found out himself, the `BoundingBoxFinder` coordinates
     * could not be used as is in the `CreateVisibleSignature`. This test
     * demonstrates the required transformation with an arbitrary example
     * document.
     * </p>
     */
    @Test
    public void signLikeHemantTest() throws IOException, GeneralSecurityException {
        File documentFile = new File("src/test/resources/mkl/testarea/pdfbox2/sign/test.pdf");
        File signedDocumentFile = new File(RESULT_FOLDER, "test-signedLikeHemant.pdf");

        Rectangle2D boundingBox;
        PDRectangle mediaBox;
        try (   PDDocument document = PDDocument.load(documentFile) ) {
            PDPage pdPage = document.getPage(0);
            BoundingBoxFinder boundingBoxFinder = new BoundingBoxFinder(pdPage);
            boundingBoxFinder.processPage(pdPage);
            boundingBox = boundingBoxFinder.getBoundingBox();
            mediaBox = pdPage.getMediaBox();
        }

        CreateVisibleSignature signing = new CreateVisibleSignature(ks, PASSWORD.clone());
        try (   InputStream imageStream = getClass().getResourceAsStream("/mkl/testarea/pdfbox2/content/Willi-1.jpg")) {
            signing.setVisibleSignDesigner(documentFile.getPath(), (int)boundingBox.getX(), (int)(mediaBox.getUpperRightY() - boundingBox.getY()), -50, imageStream, 1);
        }
        signing.setVisibleSignatureProperties("name", "location", "Security", 0, 1, true);
        signing.setExternalSigning(false);
        signing.signPDF(documentFile, signedDocumentFile, null);
    }

    /**
     * <a href="https://stackoverflow.com/questions/59027388/signing-pdf-with-multiple-signature-fields-using-pdfbox-2-0-17">
     * Signing PDF with multiple signature fields using PDFBox 2.0.17
     * </a>
     * <br/>
     * <a href="https://github.com/lawrencelkp/pdfboxtest">
     * Fillable-2s.pdf
     * </a>
     * <p>
     * {@link #signExistingFieldWithLock(PDDocument, OutputStream, SignatureInterface)}
     * successfully signs the first signature field of the document.
     * </p>
     */
    @Test
    public void testSignFillable2sWithLocking() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Fillable-2s.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "Fillable-2s-SignedWithLocking.pdf"));
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            signExistingFieldWithLock(pdDocument, result, data -> signWithSeparatedHashing(data));
        }
    }

    /**
     * <p>
     * A minimal signing frame work merely requiring a {@link SignatureInterface}
     * instance signing an existing field and actually locking fields the transform
     * requires to be locked.
     * </p>
     * @see #signExistingFieldWithLock(PDDocument, OutputStream, SignatureInterface)
     */
    void signAndLockExistingFieldWithLock(PDDocument document, OutputStream output, SignatureInterface signatureInterface) throws IOException
    {
        PDSignatureField signatureField = document.getSignatureFields().get(0);
        PDSignature signature = new PDSignature();
        signatureField.setValue(signature);

        COSBase lock = signatureField.getCOSObject().getDictionaryObject(COS_NAME_LOCK);
        if (lock instanceof COSDictionary)
        {
            COSDictionary lockDict = (COSDictionary) lock;
            COSDictionary transformParams = new COSDictionary(lockDict);
            transformParams.setItem(COSName.TYPE, COSName.getPDFName("TransformParams"));
            transformParams.setItem(COSName.V, COSName.getPDFName("1.2"));
            transformParams.setDirect(true);
            COSDictionary sigRef = new COSDictionary();
            sigRef.setItem(COSName.TYPE, COSName.getPDFName("SigRef"));
            sigRef.setItem(COSName.getPDFName("TransformParams"), transformParams);
            sigRef.setItem(COSName.getPDFName("TransformMethod"), COSName.getPDFName("FieldMDP"));
            sigRef.setItem(COSName.getPDFName("Data"), document.getDocumentCatalog());
            sigRef.setDirect(true);
            COSArray referenceArray = new COSArray();
            referenceArray.add(sigRef);
            signature.getCOSObject().setItem(COSName.getPDFName("Reference"), referenceArray);

            final Predicate<PDField> shallBeLocked;
            final COSArray fields = lockDict.getCOSArray(COSName.FIELDS);
            final List<String> fieldNames = fields == null ? Collections.emptyList() :
                fields.toList().stream().filter(c -> (c instanceof COSString)).map(s -> ((COSString)s).getString()).collect(Collectors.toList());
            final COSName action = lockDict.getCOSName(COSName.getPDFName("Action"));
            if (action.equals(COSName.getPDFName("Include"))) {
                shallBeLocked = f -> fieldNames.contains(f.getFullyQualifiedName());
            } else if (action.equals(COSName.getPDFName("Exclude"))) {
                shallBeLocked = f -> !fieldNames.contains(f.getFullyQualifiedName());
            } else if (action.equals(COSName.getPDFName("All"))) {
                shallBeLocked = f -> true;
            } else { // unknown action, lock nothing
                shallBeLocked = f -> false;
            }
            lockFields(document.getDocumentCatalog().getAcroForm().getFields(), shallBeLocked);
        }

        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName("blablabla");
        signature.setLocation("blablabla");
        signature.setReason("blablabla");
        signature.setSignDate(Calendar.getInstance());
        document.addSignature(signature);
        ExternalSigningSupport externalSigning =
                document.saveIncrementalForExternalSigning(output);
        // invoke external signature service
        byte[] cmsSignature = signatureInterface.sign(externalSigning.getContent());
        // set signature bytes received from the service
        externalSigning.setSignature(cmsSignature);
    }

    /** @see #signAndLockExistingFieldWithLock(PDDocument, OutputStream, SignatureInterface) */
    boolean lockFields(List<PDField> fields, Predicate<PDField> shallBeLocked) {
        boolean isUpdated = false;
        if (fields != null) {
            for (PDField field : fields) {
                boolean isUpdatedField = false;
                if (shallBeLocked.test(field)) {
                    field.setFieldFlags(field.getFieldFlags() | 1);
                    if (field instanceof PDTerminalField) {
                        for (PDAnnotationWidget widget : ((PDTerminalField)field).getWidgets())
                            widget.setLocked(true);
                    }
                    isUpdatedField = true;
                }
                if (field instanceof PDNonTerminalField) {
                    if (lockFields(((PDNonTerminalField)field).getChildren(), shallBeLocked))
                        isUpdatedField = true;
                }
                if (isUpdatedField) {
                    field.getCOSObject().setNeedToBeUpdated(true);
                    isUpdated = true;
                }
            }
        }
        return isUpdated;
    }

    /**
     * <a href="https://stackoverflow.com/questions/59027388/signing-pdf-with-multiple-signature-fields-using-pdfbox-2-0-17">
     * Signing PDF with multiple signature fields using PDFBox 2.0.17
     * </a>
     * <br/>
     * <a href="https://github.com/lawrencelkp/pdfboxtest">
     * Fillable-2s.pdf
     * </a>
     * <p>
     * {@link #signAndLockExistingFieldWithLock(PDDocument, OutputStream, SignatureInterface)}
     * successfully signs the first signature field of the document while at the same time
     * making the locked fields read only and locking their widgets.
     * </p>
     */
    @Test
    public void testSignAndLockFillable2sWithLocking() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Fillable-2s.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "Fillable-2s-SignedAndLockedWithLocking.pdf"));
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            pdDocument.getDocumentCatalog().getAcroForm().getField("Text1").setValue("Text1");
            signAndLockExistingFieldWithLock(pdDocument, result, data -> signWithSeparatedHashing(data));
        }
    }

    /**
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-4702">
     * signature verification in Adobe products
     * </a>
     * <br/>
     * <a href="https://issues.apache.org/jira/secure/attachment/12987251/pdfbox-4702-unsigned.pdf">
     * pdfbox-4702-unsigned.pd
     * </a>
     * <p>
     * {@link #signAndLockExistingFieldWithLock(PDDocument, OutputStream, SignatureInterface)}
     * successfully signs the signature field of the document.
     * </p>
     */
    @Test
    public void testSignAndLockPdfbox4702UnsignedWithLocking() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("pdfbox-4702-unsigned.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "pdfbox-4702-unsigned-SignedAndLockedWithLocking.pdf"));
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            signAndLockExistingFieldWithLock(pdDocument, result, data -> signWithSeparatedHashing(data));
        }
    }

    /**
     * <a href="https://ec.europa.eu/cefdigital/tracker/browse/DSS-2260">
     * Multiple PADeS signatures with images do not work
     * </a>
     * <br/>
     * <a href="https://ec.europa.eu/cefdigital/tracker/secure/attachment/42608/signed.pdf">
     * signed.pdf
     * </a>, the original (unsigned) revision of which is used as "signed-000.pdf"
     * <br/>
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-4997">
     * Incremental update adds certain objects not marked as needing update
     * </a>
     * <p>
     * This test signs the PDF after marking the first page object
     * (and a path to it) as needing to be updated as also would
     * happen in case of a visual signature. And indeed, the issue
     * DSS-2260 can be reproduced, not only the page object is
     * copied to the incremental update, the object 54 referenced
     * from it is, too.
     * </p>
     * <p>
     * The cause is that the PDFBox incremental update mechanism
     * checks the <code>COSUpdateInfo.isNeedToBeUpdated</code>
     * value and only does not write an object if it has a value
     * of <code>false</code> there. Unfortunately the object 54
     * only contains a {@link COSName} which does not implement
     * {@link COSUpdateInfo} and, therefore, does not offer such
     * a value. See also PDFBOX-4997.
     * </p>
     */
    @Test
    public void testSignSigned000() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("signed-000.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "signed-000-once.pdf"));
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            COSDictionary catalog = pdDocument.getDocumentCatalog().getCOSObject();
            catalog.setNeedToBeUpdated(true);
            COSDictionary pages = catalog.getCOSDictionary(COSName.PAGES);
            pages.setNeedToBeUpdated(true);
            COSArray kids = pages.getCOSArray(COSName.KIDS);
            kids.setNeedToBeUpdated(true);
            COSDictionary page = (COSDictionary) kids.getObject(0);
            page.setNeedToBeUpdated(true);
            sign(pdDocument, result, data -> signWithSeparatedHashing(data));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/75230246/adding-new-signature-by-reatining-existing-unsigned-signature-fields-using-pdfbo">
     * Adding new signature by reatining existing unsigned signature fields using PDFBox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1-vu9_WIfFo198v6AxoBMxCuyX1rE2FOS/view?usp=share_link">
     * Formulier DSS-01 - DC+QV Onboarding Checklist.pdf
     * </a>
     * <p>
     * As this test shows, the pre-signing code of the OP does not cause
     * the zeroed out sections in their result file. It is unclear, though,
     * whether the "Embedding signature code" of the OP does the zeroing
     * as the method <code>makeLtv</code> called therein is not shared.
     * </p>
     */
    @Test
    public void testPresignLikeRajathRJoshi() throws IOException, NoSuchAlgorithmException {
        try (
            InputStream resource = getClass().getResourceAsStream("Formulier DSS-01 - DC+QV Onboarding Checklist.pdf");
            PDDocument doc = PDDocument.load(resource);
            FileOutputStream fos = new FileOutputStream(new File(RESULT_FOLDER, "Formulier DSS-01 - DC+QV Onboarding Checklist-presigned.pdf"));
            SignatureOptions signatureOptions = new SignatureOptions();
        ) {
           signatureOptions.setPreferredSignatureSize(SignatureOptions.DEFAULT_SIGNATURE_SIZE * 2);
           signatureOptions.setPage(0);

           PDSignature signature = new PDSignature();
           signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
           signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
           signature.setSignDate(Calendar.getInstance());
           doc.addSignature(signature, signatureOptions);
           ExternalSigningSupport externalSigning = doc.saveIncrementalForExternalSigning(fos);

           MessageDigest digest = MessageDigest.getInstance("SHA-256");
           byte[] hashBytes = digest.digest(IOUtils.toByteArray(externalSigning.getContent()));
           System.out.println(Hex.toHexString(hashBytes));
           externalSigning.setSignature(new byte[0]);
           int offset = signature.getByteRange()[1] + 1;
           IOUtils.closeQuietly(signatureOptions);
       }
    }
}
