package mkl.testarea.pdfbox2.annotate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationRubberStamp;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.util.Matrix;
import org.junit.BeforeClass;
import org.junit.Test;

public class AddAnnotationIncrementally {
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/76027267/signing-a-pdf-digitally-multiple-times-and-adding-text-images-to-it-while-signin">
     * Signing a pdf digitally multiple times and adding text/images to it while signing
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/11h9aQLcjGpJKdJTwLNEl3ax4hbkxI6iw/view?usp=share_link">
     * sample-signed-first.pdf
     * </a> (first revision thereof as sample.pdf)
     * <br/>
     * <a href="https://stackoverflow.com/questions/41467415/pdfbox-opening-and-saving-a-signed-pdf-invalidates-my-signature">
     * PDFBox - opening and saving a signed pdf invalidates my signature
     * </a>
     * <p>
     * This test uses the code in EDIT 3 of the latter question to add a stamp annotation
     * to the example PDF as incremental update like the author of the former question claims
     * to have done. But the error he observed cannot be reproduced.
     * </p>
     */
    @Test
    public void testAnnotateRawatrSample() throws IOException {
        try (
            InputStream resource = getClass().getResourceAsStream("sample.pdf");
            PDDocument document = PDDocument.load(resource);
            InputStream imageStream = getClass().getResourceAsStream("/mkl/testarea/pdfbox2/content/Willi-1.jpg")
        ) {
            PDPage page = document.getPage(0);
            List<PDAnnotation> annotations = page.getAnnotations();
            PDImageXObject ximage = PDImageXObject.createFromByteArray(document, imageStream.readAllBytes(), "Willi-1.jpg");

            //stamp
            PDAnnotationRubberStamp stamp = new PDAnnotationRubberStamp();
            stamp.setName("testing rubber stamp");
            stamp.setContents("this is a test");
            stamp.setLocked(true);
            stamp.setReadOnly(true);
            stamp.setPrinted(true);

            //PDRectangle rectangle = createRectangle(100, 100, 100, 100, 100, 100);
            PDRectangle rectangle = new PDRectangle(100, 100, 100, 100);
            PDFormXObject form = new PDFormXObject(document);
            form.setResources(new PDResources());
            form.setBBox(rectangle);
            form.setFormType(1);

            form.getResources().getCOSObject().setNeedToBeUpdated(true);
            form.getResources().add(ximage);
            PDAppearanceStream appearanceStream = new PDAppearanceStream(form.getCOSObject());
            PDAppearanceDictionary appearance = new PDAppearanceDictionary(new COSDictionary());
            appearance.setNormalAppearance(appearanceStream);
            stamp.setAppearance(appearance);
            stamp.setRectangle(rectangle);
            PDPageContentStream stream = new PDPageContentStream(document, appearanceStream);
            Matrix matrix = new Matrix(100, 0, 0, 100, 100, 100);
            stream.drawImage(ximage, matrix);
            stream.close();
            //close and save   
            annotations.add(stamp);

            appearanceStream.getCOSObject().setNeedToBeUpdated(true);
            appearance.getCOSObject().setNeedToBeUpdated(true);
            rectangle.getCOSArray().setNeedToBeUpdated(true);
            stamp.getCOSObject().setNeedToBeUpdated(true);
            form.getCOSObject().setNeedToBeUpdated(true);
            COSArrayList<PDAnnotation> list = (COSArrayList<PDAnnotation>) annotations;
            COSArrayList.converterToCOSArray(list).setNeedToBeUpdated(true);
            document.getPages().getCOSObject().setNeedToBeUpdated(true);
            page.getCOSObject().setNeedToBeUpdated(true);
            document.getDocumentCatalog().getCOSObject().setNeedToBeUpdated(true);

            OutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "sample-incrementallyStamped.pdf"));
            document.saveIncremental(os);
            document.close();
            os.close();
        }
    }

}
