package mkl.testarea.pdfbox2.sign;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.List;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.examples.signature.CreateSignatureBase;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSignDesigner;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.util.Matrix;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <a href="https://stackoverflow.com/questions/65020501/pdfbox-multiple-signature-giving-invalid-signature-java">
 * PDFBox multiple signature giving invalid signature Java
 * </a>
 * <br/>
 * <a href="https://drive.google.com/drive/folders/1D-Vk2ZkqcnHwODW7u3TdnuWIfPAfROuG?usp=sharing">
 * hello.pdf
 * </a>
 * <br/>
 * <a href="https://www.trackandtest.co.uk/wp-content/uploads/green-tick-png-green-tick-icon-image-14141-1000.png">
 * green-tick-png-green-tick-icon-image-14141-1000.png
 * </a>
 * <p>
 * This test contains the relevant code the OP shared. As it turns out,
 * the issue is already in the preparation step, here the OP adds a page
 * from the document to that very document again (see the first '!!!' mark
 * in {@link #tagPDFSignatureFields(PDDocument)}). This causes a duplicate
 * Pages tree entry which Adobe Reader does not like.
 * </p>
 * <p>
 * There also are other issues, see the other '!!!' marks for examples,
 * but the problem at hand, the second signature invalidating the first
 * one, is already prevented by fixing the duplicate page issue.
 * </p>
 * 
 * @author mkl
 */
public class CreateFieldsAndSignaturesLikeNix {
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

        ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream(KEYSTORE), PASSWORD);
        String alias = (String) ks.aliases().nextElement();
        pk = (PrivateKey) ks.getKey(alias, PASSWORD);
        chain = ks.getCertificateChain(alias);
    }

    @Test
    public void testCreateFieldsAndSignaturesLikeNix() throws Exception {
        File withFields = new File(RESULT_FOLDER, "hello-fields.pdf");
        File signedOnce = new File(RESULT_FOLDER, "hello-fields-signed.pdf");
        File signedTwice = new File(RESULT_FOLDER, "hello-fields-signed-twice.pdf");

        try (
            InputStream resource = getClass().getResourceAsStream("hello.pdf");
            PDDocument document = PDDocument.load(resource);
        ) {
            tagPDFSignatureFields(document);
            document.save(withFields);
        }

        {
            SignAndIdentifySignatureFields signing = new SignAndIdentifySignatureFields(ks, PASSWORD);
            signing.setExternalSigning(false);
            signing.addEmptySignField(new String[] {withFields.getAbsolutePath(), signedOnce.getAbsolutePath()});
        }

        {
            Sign2 signing = new Sign2(ks, PASSWORD);
            signing.setExternalSigning(false);
            signing.addEmptySignField(new String[] {signedOnce.getAbsolutePath(), signedTwice.getAbsolutePath()});
        }
    }

    /**
     * Code from TagPDFSignatureFields.java 
     */
    void tagPDFSignatureFields(PDDocument document) throws IOException {
        PDPage page = document.getPage(0);
        //document.addPage(page);   //!!!
        /*PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);*/

        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), font);

        // Add a new AcroForm and add that to the document
        PDAcroForm acroForm = new PDAcroForm(document);
        document.getDocumentCatalog().setAcroForm(acroForm);

        // Add and set the resources and default appearance at the form level
        acroForm.setDefaultResources(resources);

        // Acrobat sets the font size on the form level to be
        // auto sized as default. This is done by setting the font size to '0'
        String defaultAppearanceString = "/Helv 0 Tf 0 g";
        acroForm.setDefaultAppearance(defaultAppearanceString);
        // --- end of general AcroForm stuff ---

        // Create empty signature field, it will get the name "Signature1"
        PDSignatureField signatureField = new PDSignatureField(acroForm);
        signatureField.setAlternateFieldName("suhasd@gmail.com");   //!!!
        signatureField.setPartialName("suhasd@gmail.com");          //!!!
        
        
        PDAnnotationWidget widget = signatureField.getWidgets().get(0);
        PDRectangle rect = new PDRectangle(50, 500, 200, 100);
        widget.setRectangle(rect);
        widget.setPage(page);
        page.getAnnotations().add(widget);

        acroForm.getFields().add(signatureField);
        
        
        PDSignatureField signatureField1 = new PDSignatureField(acroForm);
        signatureField1.setAlternateFieldName("nikhil.courser@gmail.com");  //!!!
        signatureField1.setPartialName("nikhil.courser@gmail.com");         //!!!
        
        PDAnnotationWidget widget1 = signatureField1.getWidgets().get(0);
        PDRectangle rect1 = new PDRectangle(50, 650, 200, 50);
        widget1.setRectangle(rect1);
        widget1.setPage(page);
        page.getAnnotations().add(widget1);
        
        acroForm.getFields().add(signatureField1);
        System.out.println("Total existing signature fields : "+document.getSignatureFields().size());
    }

    /**
     * Code from SignAndIdentifySignatureFields.java 
     */
    public static class SignAndIdentifySignatureFields extends CreateSignatureBase {
        
        public SignAndIdentifySignatureFields(KeyStore keystore, char[] pin) throws KeyStoreException,
                UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException {
            super(keystore, pin);
            // TODO Auto-generated constructor stub
        }

        private final static PDVisibleSigProperties visibleSignatureProperties = new PDVisibleSigProperties();
        private static SignatureOptions signatureOptions;
        private static PDVisibleSignDesigner visibleSignDesigner;
        final static String TICK_IMG_PATH = "src/test/resources/mkl/testarea/pdfbox2/sign/green-tick-png-green-tick-icon-image-14141-1000.png";// "C:\\Users\\nikhil.wankhade\\Documents\\tick.png";
        private static File imageFile = new File(TICK_IMG_PATH);
        
        public static void main(String[] args) throws Exception
        {
            
            String args1[] = new String[] {"C:\\Users\\nikhil.wankhade\\Documents\\hello_tag.pdf", 
                    
                    "C:\\Users\\nikhil.wankhade\\Documents\\hello_signed.pdf",
                    
                    "e:\\es\\sign\\pkcs12test.p12",
            
            "nsdl12"};
            
            args = args1;
            

            // load the keystore
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            char[] password = args[3].toCharArray(); // TODO use Java 6 java.io.Console.readPassword
            keystore.load(new FileInputStream(args[2]), password);
            
            // sign PDF
            SignAndIdentifySignatureFields signing = new SignAndIdentifySignatureFields(keystore, password);
            signing.setExternalSigning(false);
            
            signing.addEmptySignField(args);
        }
        
        private void addEmptySignField(String[] args) throws Exception, IOException {
            // Create a new document with an empty page.
            try (PDDocument document = PDDocument.load(new File(args[0]));)
            {
                
                PDPage page = document.getPage(0);
                document.setVersion(1.0f);    //!!!
                document.addPage(page);
                /*PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);*/

                // Adobe Acrobat uses Helvetica as a default font and
                // stores that under the name '/Helv' in the resources dictionary
                PDFont font = PDType1Font.HELVETICA;
                PDResources resources = new PDResources();
                resources.put(COSName.getPDFName("Helv"), font);

                // Add a new AcroForm and add that to the document
                PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();

                // Add and set the resources and default appearance at the form level
                acroForm.setDefaultResources(resources);  //!!!

                // Acrobat sets the font size on the form level to be
                // auto sized as default. This is done by setting the font size to '0'
                String defaultAppearanceString = "/Helv 0 Tf 0 g";
                acroForm.setDefaultAppearance(defaultAppearanceString);   //!!!
                // --- end of general AcroForm stuff ---

                // Create empty signature field, it will get the name "Signature1"
                PDSignature signature = new PDSignature();
                signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
                signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
                signature.setName("fgfdg");
                signature.setLocation("sdfdf");
                signature.setReason("fdd");
                // the signing date, needed for valid signature
                signature.setSignDate(Calendar.getInstance());
                //signature.setByteRange(document.getLastSignatureDictionary().getByteRange());
                //signature.setContents(bytes);
                
                //PDSignatureField signatureField = (PDSignatureField) acroForm.getFields().get(0);
                
                PDSignatureField signatureField = (PDSignatureField) acroForm.getField("nikhil.courser@gmail.com"); //!!!
                acroForm.setAppendOnly(true);
                acroForm.setSignaturesExist(false);
                
                System.out.println("signatureField:" + signatureField.getFullyQualifiedName());
                COSDictionary cos = signatureField.getCOSObject();
                cos.setDirect(true);
                cos.setItem(COSName.V, signature);
                
                signatureOptions = new SignatureOptions();
                
                //ByteArrayInputStream b = createVisualSignatureTemplate(document, signature, signatureField);
                
                PDVisibleSigProperties signatureProperties2 = new PDVisibleSigProperties();
                
                FileInputStream tickImg = new FileInputStream(TICK_IMG_PATH);
                
                PDVisibleSignDesigner visibleSig = new PDVisibleSignDesigner(document, tickImg, 1);
                  visibleSig.xAxis(10).yAxis(20).zoom(-95.0F).signatureFieldName("Signature1");
                  signatureProperties2.signerName("name").signerLocation("location").signatureReason("Security").preferredSize(0)
                    .page(1).visualSignEnabled(true).setPdVisibleSignature(visibleSig).buildSignature();
                 
                  
                  System.out.println("signatureProperties2:" + signatureProperties2.isVisualSignEnabled());
                  
                    if ((signatureProperties2 != null) && (signatureProperties2.isVisualSignEnabled())){
                        signatureOptions.setVisualSignature(signatureProperties2);
                        signatureOptions.setPage(signatureProperties2.getPage());
                    }

                
                //signatureOptions.setVisualSignature(b);
                            
                document.addSignature(signature,this, signatureOptions);
                // write incremental (only for signing purpose)
                FileOutputStream fos = new FileOutputStream(args[1]);
                document.saveIncremental(fos);
                //signPDF(acroForm);
                //document.save(args[1]);
                
                //document.close();
            }
            
        }

        private ByteArrayInputStream createVisualSignatureTemplate(PDDocument srcDoc,
                     PDSignature signature, PDSignatureField signatureField) throws IOException
        {
                /*PDDocument doc = new PDDocument();
        
             PDPage page = new PDPage(srcDoc.getPage(pageNum).getMediaBox());
             doc.addPage(page);
             PDAcroForm acroForm = new PDAcroForm(doc);
             doc.getDocumentCatalog().setAcroForm(acroForm);
             PDSignatureField signatureField = new PDSignatureField(acroForm);*/
             PDAcroForm acroForm = srcDoc.getDocumentCatalog().getAcroForm();
             
             
             System.out.println("fieldNAme:" + signatureField.getFullyQualifiedName());
             
             PDAnnotationWidget widget = signatureField.getWidgets().get(0);
             List<PDField> acroFormFields = acroForm.getFields();
             acroForm.setSignaturesExist(true);
             acroForm.setAppendOnly(true);
             acroForm.getCOSObject().setDirect(true);
             acroFormFields.add(signatureField);
        
             widget.setRectangle(widget.getRectangle());
        
             // from PDVisualSigBuilder.createHolderForm()
             PDStream stream = new PDStream(srcDoc);
             PDFormXObject form = new PDFormXObject(stream);
             PDResources res = new PDResources();
             form.setResources(res);
             form.setFormType(1);
             PDRectangle bbox = new PDRectangle(widget.getRectangle().getWidth(), widget.getRectangle().getHeight());
             float height = bbox.getHeight();
             Matrix initialScale = null;
             switch (srcDoc.getPage(0).getRotation())
             {
                 case 90:
                     form.setMatrix(AffineTransform.getQuadrantRotateInstance(1));
                     initialScale = Matrix.getScaleInstance(bbox.getWidth() / bbox.getHeight(), bbox.getHeight() / bbox.getWidth());
                     height = bbox.getWidth();
                     break;
                 case 180:
                     form.setMatrix(AffineTransform.getQuadrantRotateInstance(2));
                     break;
                 case 270:
                     form.setMatrix(AffineTransform.getQuadrantRotateInstance(3));
                     initialScale = Matrix.getScaleInstance(bbox.getWidth() / bbox.getHeight(), bbox.getHeight() / bbox.getWidth());
                     height = bbox.getWidth();
                     break;
                 case 0:
                 default:
                     break;
             }
             form.setBBox(bbox);
             PDFont font = PDType1Font.HELVETICA_BOLD;
        
             // from PDVisualSigBuilder.createAppearanceDictionary()
             PDAppearanceDictionary appearance = new PDAppearanceDictionary();
             appearance.getCOSObject().setDirect(true);
             PDAppearanceStream appearanceStream = new PDAppearanceStream(form.getCOSObject());
             appearance.setNormalAppearance(appearanceStream);
             widget.setAppearance(appearance);
        
             PDPageContentStream cs = new PDPageContentStream(srcDoc, appearanceStream);
        
             // for 90° and 270° scale ratio of width / height
             // not really sure about this
             // why does scale have no effect when done in the form matrix???
             if (initialScale != null)
             {
                 cs.transform(initialScale);
             }
        
             // show background (just for debugging, to see the rect size + position)
             //cs.setNonStrokingColor(Color.yellow);
             cs.addRect(-5000, -5000, 10000, 10000);
             //cs.fill();
        
             
             if (imageFile != null)
             {
                 // show background image
                 // save and restore graphics if the image is too large and needs to be scaled
                 cs.saveGraphicsState();
                 cs.transform(Matrix.getScaleInstance(0.25f, 0.25f));
                 PDImageXObject img = PDImageXObject.createFromFileByExtension(imageFile, srcDoc);
                 cs.drawImage(img, 0, 0);
                 cs.restoreGraphicsState();
             }
             
             // show text
             float fontSize = 10;
             float leading = fontSize * 1.5f;
             cs.beginText();
             cs.setFont(font, fontSize);
             cs.setNonStrokingColor(Color.black);
             cs.newLineAtOffset(fontSize, height - leading);
             cs.setLeading(leading);
        
             String name = signature.getName();
        
             // See https://stackoverflow.com/questions/12575990
             // for better date formatting
             String date = signature.getSignDate().getTime().toString();
             String reason = signature.getReason();
        
             cs.showText("Signer: " + name);
             cs.newLine();
             cs.showText(date);
             cs.newLine();
             cs.showText("Reason: " + reason);
        
             cs.endText();
        
             cs.close();
        
             // no need to set annotations and /P entry
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             srcDoc.save(baos);
             //doc.close();
             return new ByteArrayInputStream(baos.toByteArray());
         }

        public static void signPDF(PDAcroForm acroForm) {
            
            PDSignature signature = null;
            PDSignatureField signatureField = null;
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            
            visibleSignatureProperties.signerName("Test").signerLocation("NSDL").signatureReason("Testing").
            preferredSize(0).page(1).visualSignEnabled(true).
            setPdVisibleSignature(visibleSignDesigner.signatureFieldName("Cbt_SuhasB@nsdl.co.in"));
            // TODO extract the above details from the signing certificate? Reason as a parameter?
            
            if (acroForm != null)
            {
                signatureField = (PDSignatureField) acroForm.getField("Cbt_SuhasB@nsdl.co.in");
                if (signatureField != null)
                {
                    // retrieve signature dictionary
                    signature = signatureField.getSignature();
                    if (signature == null)
                    {
                        signature = new PDSignature();
                        // after solving PDFBOX-3524
                        // signatureField.setValue(signature)
                        // until then:
                        signatureField.getCOSObject().setItem(COSName.V, signature);
                    }
                    else
                    {
                        throw new IllegalStateException("The signature field Cbt_SuhasB@nsdl.co.in is already signed.");
                    }
                }
            }
           
        }
        
        /*public void m1(){
            visibleSignDesigner = new PDVisibleSignDesigner(document, new inpu imageFile, page);
            visibleSignDesigner.xAxis(x).yAxis(y).zoom(zoomPercent).adjustForRotation();
            visibleSignatureProperties.signerName("Test").signerLocation("NSDL").signatureReason("Testing").
            preferredSize(0).page(1).visualSignEnabled(true).
            setPdVisibleSignature(visibleSignDesigner);
            signatureOptions = new SignatureOptions();
            signatureOptions.setVisualSignature(visibleSignatureProperties.getVisibleSignature());
            PDSignature signature = new PDSignature();
            
            
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName(visibleSignatureProperties.getSignerName());
            signature.setLocation(visibleSignatureProperties.getSignerLocation());
            signature.setReason(visibleSignatureProperties.getSignatureReason());

            // the signing date, needed for valid signature
            signature.setSignDate(Calendar.getInstance());
            signatureField.getCOSObject().setItem(COSName.V, signature);
            
            document.addSignature(signature, signatureOptions);
            // write incremental (only for signing purpose)
            //document.saveIncremental(args[1]);

        }*/
        
    }

    /**
     * Code from Sign2.java 
     */
    public static class Sign2 extends CreateSignatureBase{
        
        public Sign2(KeyStore keystore, char[] pin) throws KeyStoreException,
                UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException {
            super(keystore, pin);
            // TODO Auto-generated constructor stub
        }

        private final static PDVisibleSigProperties visibleSignatureProperties = new PDVisibleSigProperties();
        private static SignatureOptions signatureOptions;
        private static PDVisibleSignDesigner visibleSignDesigner;
        final static String TICK_IMG_PATH = "src/test/resources/mkl/testarea/pdfbox2/sign/green-tick-png-green-tick-icon-image-14141-1000.png";// "C:\\Users\\nikhil.wankhade\\Documents\\tick.png";
        private static File imageFile = new File(TICK_IMG_PATH);
        
        public static void main(String[] args) throws Exception
        {
            
            String args1[] = new String[] {"C:\\Users\\nikhil.wankhade\\Documents\\hello_signed.pdf", 
                    
                    "C:\\Users\\nikhil.wankhade\\Documents\\hello_signed2.pdf",
                    
                    "e:\\es\\sign\\pkcs12test.p12",
            
            "nsdl12"};
            
            args = args1;
            

            // load the keystore
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            char[] password = args[3].toCharArray(); // TODO use Java 6 java.io.Console.readPassword
            keystore.load(new FileInputStream(args[2]), password);
            
            // sign PDF
            Sign2 signing = new Sign2(keystore, password);
            signing.setExternalSigning(false);
            
            signing.addEmptySignField(args);
        }
        
        private void addEmptySignField(String[] args) throws Exception, IOException {
            // Create a new document with an empty page.
            try (PDDocument document = PDDocument.load(new File(args[0]));)
            {
                
                PDPage page = document.getPage(0);
                document.setVersion(2.0f);    //!!!
                document.addPage(page);
                /*PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);*/

                // Adobe Acrobat uses Helvetica as a default font and
                // stores that under the name '/Helv' in the resources dictionary
                PDFont font = PDType1Font.HELVETICA;
                PDResources resources = new PDResources();
                resources.put(COSName.getPDFName("Helv"), font);

                // Add a new AcroForm and add that to the document
                PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();

                // Add and set the resources and default appearance at the form level
                acroForm.setDefaultResources(resources);  //!!!

                // Acrobat sets the font size on the form level to be
                // auto sized as default. This is done by setting the font size to '0'
                String defaultAppearanceString = "/Helv 0 Tf 0 g";
                acroForm.setDefaultAppearance(defaultAppearanceString);   //!!!
                // --- end of general AcroForm stuff ---

                // Create empty signature field, it will get the name "Signature1"
                PDSignature signature = new PDSignature();
                
                
                signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
                signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
                signature.setName("fgfdg");
                signature.setLocation("sdfdf");
                signature.setReason("fdd");
                
                //signature.setByteRange(document.getLastSignatureDictionary().getByteRange());

                // the signing date, needed for valid signature
                signature.setSignDate(Calendar.getInstance());
                
                //PDSignatureField signatureField = (PDSignatureField) acroForm.getFields().get(0);
                
                PDSignatureField signatureField = (PDSignatureField) acroForm.getField("suhasd@gmail.com"); //!!!
                
                acroForm.setAppendOnly(true);
                acroForm.setSignaturesExist(true);
                
                System.out.println("signatureField:" + signatureField.getFullyQualifiedName());
                
                COSDictionary cos = signatureField.getCOSObject();
                cos.setDirect(true);
                cos.setItem(COSName.V, signature);
                
                signatureOptions = new SignatureOptions();
                //ByteArrayInputStream b = createVisualSignatureTemplate(document, signature, signatureField);
                
                PDVisibleSigProperties signatureProperties2 = new PDVisibleSigProperties();
                
                FileInputStream tickImg = new FileInputStream(TICK_IMG_PATH);
                
                PDVisibleSignDesigner visibleSig = new PDVisibleSignDesigner(document, tickImg, 1);
                  visibleSig.xAxis(10).yAxis(20).zoom(-95.0F).signatureFieldName("Signature1");
                  signatureProperties2.signerName("name").signerLocation("location").signatureReason("Security").preferredSize(0)
                    .page(1).visualSignEnabled(true).setPdVisibleSignature(visibleSig).buildSignature();
                 
                  
                  System.out.println("signatureProperties2:" + signatureProperties2.isVisualSignEnabled());
                  
                    if ((signatureProperties2 != null) && (signatureProperties2.isVisualSignEnabled())){
                        signatureOptions.setVisualSignature(signatureProperties2);
                        signatureOptions.setPage(signatureProperties2.getPage());
                    }

                
                //signatureOptions.setVisualSignature(b);
                            
                document.addSignature(signature,this, signatureOptions);
                // write incremental (only for signing purpose)
                FileOutputStream fos = new FileOutputStream(args[1]);
                document.saveIncremental(fos);
                //document.save(fos);
                //signPDF(acroForm);
                //document.save(args[1]);
                
                //document.close();
            }
            
        }

            private ByteArrayInputStream createVisualSignatureTemplate(PDDocument srcDoc,
                     PDSignature signature, PDSignatureField signatureField) throws IOException
         {
                /*PDDocument doc = new PDDocument();
        
             PDPage page = new PDPage(srcDoc.getPage(pageNum).getMediaBox());
             doc.addPage(page);
             PDAcroForm acroForm = new PDAcroForm(doc);
             doc.getDocumentCatalog().setAcroForm(acroForm);
             PDSignatureField signatureField = new PDSignatureField(acroForm);*/
             PDAcroForm acroForm = srcDoc.getDocumentCatalog().getAcroForm();
             
             
             System.out.println("fieldNAme:" + signatureField.getFullyQualifiedName());
             
             PDAnnotationWidget widget = signatureField.getWidgets().get(0);
             List<PDField> acroFormFields = acroForm.getFields();
             acroForm.setSignaturesExist(true);
             acroForm.setAppendOnly(true);
             acroForm.getCOSObject().setDirect(true);
             acroFormFields.add(signatureField);
        
             widget.setRectangle(widget.getRectangle());
        
             // from PDVisualSigBuilder.createHolderForm()
             PDStream stream = new PDStream(srcDoc);
             PDFormXObject form = new PDFormXObject(stream);
             PDResources res = new PDResources();
             form.setResources(res);
             form.setFormType(1);
             PDRectangle bbox = new PDRectangle(widget.getRectangle().getWidth(), widget.getRectangle().getHeight());
             float height = bbox.getHeight();
             Matrix initialScale = null;
             switch (srcDoc.getPage(0).getRotation())
             {
                 case 90:
                     form.setMatrix(AffineTransform.getQuadrantRotateInstance(1));
                     initialScale = Matrix.getScaleInstance(bbox.getWidth() / bbox.getHeight(), bbox.getHeight() / bbox.getWidth());
                     height = bbox.getWidth();
                     break;
                 case 180:
                     form.setMatrix(AffineTransform.getQuadrantRotateInstance(2));
                     break;
                 case 270:
                     form.setMatrix(AffineTransform.getQuadrantRotateInstance(3));
                     initialScale = Matrix.getScaleInstance(bbox.getWidth() / bbox.getHeight(), bbox.getHeight() / bbox.getWidth());
                     height = bbox.getWidth();
                     break;
                 case 0:
                 default:
                     break;
             }
             form.setBBox(bbox);
             PDFont font = PDType1Font.HELVETICA_BOLD;
        
             // from PDVisualSigBuilder.createAppearanceDictionary()
             PDAppearanceDictionary appearance = new PDAppearanceDictionary();
             appearance.getCOSObject().setDirect(true);
             PDAppearanceStream appearanceStream = new PDAppearanceStream(form.getCOSObject());
             appearance.setNormalAppearance(appearanceStream);
             widget.setAppearance(appearance);
        
             PDPageContentStream cs = new PDPageContentStream(srcDoc, appearanceStream);
        
             // for 90° and 270° scale ratio of width / height
             // not really sure about this
             // why does scale have no effect when done in the form matrix???
             if (initialScale != null)
             {
                 cs.transform(initialScale);
             }
        
             // show background (just for debugging, to see the rect size + position)
             //cs.setNonStrokingColor(Color.yellow);
             cs.addRect(-5000, -5000, 10000, 10000);
             //cs.fill();
        
             
             if (imageFile != null)
             {
                 // show background image
                 // save and restore graphics if the image is too large and needs to be scaled
                 cs.saveGraphicsState();
                 cs.transform(Matrix.getScaleInstance(0.25f, 0.25f));
                 PDImageXObject img = PDImageXObject.createFromFileByExtension(imageFile, srcDoc);
                 cs.drawImage(img, 0, 0);
                 cs.restoreGraphicsState();
             }
             
             // show text
             float fontSize = 10;
             float leading = fontSize * 1.5f;
             cs.beginText();
             cs.setFont(font, fontSize);
             cs.setNonStrokingColor(Color.black);
             cs.newLineAtOffset(fontSize, height - leading);
             cs.setLeading(leading);
        
             String name = signature.getName();
        
             // See https://stackoverflow.com/questions/12575990
             // for better date formatting
             String date = signature.getSignDate().getTime().toString();
             String reason = signature.getReason();
        
             cs.showText("Signer: " + name);
             cs.newLine();
             cs.showText(date);
             cs.newLine();
             cs.showText("Reason: " + reason);
        
             cs.endText();
        
             cs.close();
        
             // no need to set annotations and /P entry
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             srcDoc.save(baos);
             //doc.close();
             return new ByteArrayInputStream(baos.toByteArray());
         }

        public static void signPDF(PDAcroForm acroForm) {
            
            PDSignature signature = null;
            PDSignatureField signatureField = null;
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            
            visibleSignatureProperties.signerName("Test").signerLocation("NSDL").signatureReason("Testing").
            preferredSize(0).page(1).visualSignEnabled(true).
            setPdVisibleSignature(visibleSignDesigner.signatureFieldName("Cbt_SuhasB@nsdl.co.in"));
            // TODO extract the above details from the signing certificate? Reason as a parameter?
            
            if (acroForm != null)
            {
                signatureField = (PDSignatureField) acroForm.getField("Cbt_SuhasB@nsdl.co.in");
                if (signatureField != null)
                {
                    // retrieve signature dictionary
                    signature = signatureField.getSignature();
                    if (signature == null)
                    {
                        signature = new PDSignature();
                        // after solving PDFBOX-3524
                        // signatureField.setValue(signature)
                        // until then:
                        signatureField.getCOSObject().setItem(COSName.V, signature);
                    }
                    else
                    {
                        throw new IllegalStateException("The signature field Cbt_SuhasB@nsdl.co.in is already signed.");
                    }
                }
            }
           
        }
        
        /*public void m1(){
            visibleSignDesigner = new PDVisibleSignDesigner(document, new inpu imageFile, page);
            visibleSignDesigner.xAxis(x).yAxis(y).zoom(zoomPercent).adjustForRotation();
            visibleSignatureProperties.signerName("Test").signerLocation("NSDL").signatureReason("Testing").
            preferredSize(0).page(1).visualSignEnabled(true).
            setPdVisibleSignature(visibleSignDesigner);
            signatureOptions = new SignatureOptions();
            signatureOptions.setVisualSignature(visibleSignatureProperties.getVisibleSignature());
            PDSignature signature = new PDSignature();
            
            
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName(visibleSignatureProperties.getSignerName());
            signature.setLocation(visibleSignatureProperties.getSignerLocation());
            signature.setReason(visibleSignatureProperties.getSignatureReason());

            // the signing date, needed for valid signature
            signature.setSignDate(Calendar.getInstance());
            signatureField.getCOSObject().setItem(COSName.V, signature);
            
            document.addSignature(signature, signatureOptions);
            // write incremental (only for signing purpose)
            //document.saveIncremental(args[1]);

        }*/
        
    }


}
