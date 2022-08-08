package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.ByteStreams;

/**
 * @author mkl
 */
public class AddImageSaveIncremental {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/56024623/pdfbox-saveincremental-after-inserting-second-image-doesnt-work">
     * PDFBox - “saveIncremental” after inserting second image doesn't work
     * </a>
     * <br/>
     * <a href="https://drive.google.com/drive/folders/1DEw2wLnVbcjijnvgtQVrZoN48DoNPFK0">
     * blank.pdf
     * </a>
     * <p>
     * Indeed, the output with two images added is damaged, the image resource
     * is missing. But see {@link #testAddImagesLikeUser11465050Improved()}.
     * </p>
     */
    @Test
    public void testAddImagesLikeUser11465050() throws IOException {
        File oneImage = new File(RESULT_FOLDER, "blank-with-Image.pdf");
        File twoImages = new File(RESULT_FOLDER, "blank-with-two-Image.pdf");

        try (   InputStream resource = getClass().getResourceAsStream("blank.pdf");
                InputStream imageResource = getClass().getResourceAsStream("Willi-1.jpg");
                OutputStream result = new FileOutputStream(oneImage)) {
            PDDocument document = PDDocument.load(resource);
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, ByteStreams.toByteArray(imageResource), "Willi");
            addImageLikeUser11465050(document, pdImage);
            document.saveIncremental(result);
        }

        try (   InputStream resource = new FileInputStream(oneImage);
                InputStream imageResource = getClass().getResourceAsStream("Willi-1.jpg");
                OutputStream result = new FileOutputStream(twoImages)) {
            PDDocument document = PDDocument.load(resource);
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, ByteStreams.toByteArray(imageResource), "Willi");
            addImageLikeUser11465050(document, pdImage);
            document.saveIncremental(result);
        }
    }

    /** @see #testAddImagesLikeUser11465050() */
    void addImageLikeUser11465050(PDDocument document, PDImageXObject image) throws IOException {
        PDPage page = document.getPage(0);
        PDRectangle pageSize = page.getMediaBox();
        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
        contentStream.drawImage(image, pageSize.getLowerLeftX(), pageSize.getLowerLeftY(), pageSize.getWidth(), pageSize.getHeight());
        contentStream.close();

        page.getCOSObject().setNeedToBeUpdated(true);
        page.getResources().getCOSObject().setNeedToBeUpdated(true);
        document.getDocumentCatalog().getPages().getCOSObject().setNeedToBeUpdated(true);
        document.getDocumentCatalog().getCOSObject().setNeedToBeUpdated(true);
    }

    /**
     * <a href="https://stackoverflow.com/questions/56024623/pdfbox-saveincremental-after-inserting-second-image-doesnt-work">
     * PDFBox - “saveIncremental” after inserting second image doesn't work
     * </a>
     * <br/>
     * <a href="https://drive.google.com/drive/folders/1DEw2wLnVbcjijnvgtQVrZoN48DoNPFK0">
     * blank.pdf
     * </a>
     * <p>
     * After also marking the XObject resources dictionary as updated, the
     * output with two images added is not damaged anymore as it was in
     * {@link #testAddImagesLikeUser11465050()}.
     * </p>
     */
    @Test
    public void testAddImagesLikeUser11465050Improved() throws IOException {
        File oneImage = new File(RESULT_FOLDER, "blank-with-Image-Improved.pdf");
        File twoImages = new File(RESULT_FOLDER, "blank-with-two-Image-Improved.pdf");

        try (   InputStream resource = getClass().getResourceAsStream("blank.pdf");
                InputStream imageResource = getClass().getResourceAsStream("Willi-1.jpg");
                OutputStream result = new FileOutputStream(oneImage)) {
            PDDocument document = PDDocument.load(resource);
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, ByteStreams.toByteArray(imageResource), "Willi");
            addImageLikeUser11465050Improved(document, pdImage);
            document.saveIncremental(result);
        }

        try (   InputStream resource = new FileInputStream(oneImage);
                InputStream imageResource = getClass().getResourceAsStream("Willi-1.jpg");
                OutputStream result = new FileOutputStream(twoImages)) {
            PDDocument document = PDDocument.load(resource);
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, ByteStreams.toByteArray(imageResource), "Willi");
            addImageLikeUser11465050Improved(document, pdImage);
            document.saveIncremental(result);
        }
    }

    /** @see #testAddImagesLikeUser11465050Improved() */
    void addImageLikeUser11465050Improved(PDDocument document, PDImageXObject image) throws IOException {
        PDPage page = document.getPage(0);
        PDRectangle pageSize = page.getMediaBox();
        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
        contentStream.drawImage(image, pageSize.getLowerLeftX(), pageSize.getLowerLeftY(), pageSize.getWidth(), pageSize.getHeight());
        contentStream.close();

        page.getCOSObject().setNeedToBeUpdated(true);
        page.getResources().getCOSObject().setNeedToBeUpdated(true);
        page.getResources().getCOSObject().getCOSDictionary(COSName.XOBJECT).setNeedToBeUpdated(true);
        document.getDocumentCatalog().getPages().getCOSObject().setNeedToBeUpdated(true);
        document.getDocumentCatalog().getCOSObject().setNeedToBeUpdated(true);
    }
}
