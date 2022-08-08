package mkl.testarea.pdfbox2.sign;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationRubberStamp;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.util.Matrix;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.ByteStreams;

/**
 * @author mklink
 *
 */
public class AddToSignedFile {
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/65469799/pdfbox-how-to-incrementally-sign-updated-document-or-changes">
     * pdfbox - How to incrementally sign updated document or changes
     * </a>
     * <p>
     * This test demonstrates how to add an image in an annotation to
     * a signed PDF without breaking the signature (given that the
     * signature allows annotation adding to start with).
     * </p>
     */
    @Test
    public void testAddImageAnnotation() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("SignatureVlidationTest.pdf");
                InputStream imageResource = getClass().getResourceAsStream("green-tick-png-green-tick-icon-image-14141-1000.png");
                PDDocument document = PDDocument.load(resource);
                ) {
            PDImageXObject image = PDImageXObject.createFromByteArray(document, ByteStreams.toByteArray(imageResource), "Green tick");

            PDAppearanceStream appearanceStream = new PDAppearanceStream(document);
            appearanceStream.setBBox(new PDRectangle(1, 1));
            appearanceStream.setResources(new PDResources());
            try (   PDPageContentStream contentStream = new PDPageContentStream(document, appearanceStream) ) {
                contentStream.drawImage(image, new Matrix());
            }

            PDAppearanceDictionary appearance = new PDAppearanceDictionary();
            appearance.setNormalAppearance(appearanceStream);

            PDAnnotationRubberStamp stamp = new PDAnnotationRubberStamp();
            stamp.setLocked(true);
            stamp.setLockedContents(true);
            stamp.setPrinted(true);
            stamp.setReadOnly(true);
            stamp.setAppearance(appearance);
            stamp.setIntent("StampImage");
            stamp.setRectangle(new PDRectangle(200, 500, 100, 100));

            PDPage page = document.getPage(0);
            page.getAnnotations().add(stamp);

            Set<COSDictionary> objectsToWrite = new HashSet<>();
            objectsToWrite.add(page.getCOSObject());

            document.saveIncremental(new FileOutputStream(new File(RESULT_FOLDER, "SignatureVlidationTest-ImageAnnotationAdded.pdf")), objectsToWrite );
        }
    }

}
