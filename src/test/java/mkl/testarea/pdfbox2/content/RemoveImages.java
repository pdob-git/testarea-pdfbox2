package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class RemoveImages {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/69036429/how-to-remove-images-from-pdf-file">
     * How to remove Images from PDF File?
     * </a>
     * <p>
     * This test shows how to remove images from a PDF by replacing them
     * with empty form XObjects.
     * </p>
     */
    @Test
    public void testReplaceResourcesES1315248() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/pdfbox2/extract/ES1315248.pdf");
                PDDocument pdDocument = Loader.loadPDF(resource)    ) {
            replaceBitmapImagesResources(pdDocument);
            pdDocument.save(new File(RESULT_FOLDER, "ES1315248-noImages.pdf"));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/69036429/how-to-remove-images-from-pdf-file">
     * How to remove Images from PDF File?
     * </a>
     * <br/>
     * <a href="https://dl.dropboxusercontent.com/u/2815529/test.pdf">
     * test.pdf
     * </a> as "testDrJorge.pdf"
     * <p>
     * This test shows how to remove images from a PDF by replacing them
     * with empty form XObjects. The test file here (taken from the stack
     * overflow question https://stackoverflow.com/q/69036429/1729265 )
     * contains bitmap images in patterns.
     * </p>
     */
    @Test
    public void testReplaceResourcesTestDrJorge() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("testDrJorge.pdf");
                PDDocument pdDocument = Loader.loadPDF(resource)    ) {
            replaceBitmapImagesResources(pdDocument);
            pdDocument.save(new File(RESULT_FOLDER, "testDrJorge-noImages.pdf"));
        }
    }

    void replaceBitmapImagesResources(PDDocument document) throws IOException {
        PDFormXObject pdFormXObject = new PDFormXObject(document);
        pdFormXObject.setBBox(new PDRectangle(1, 1));
        for (PDPage pdPage : document.getPages()) {
            replaceBitmapImagesResources(pdPage.getResources(), pdFormXObject);
        }
    }

    void replaceBitmapImagesResources(PDResources resources, PDFormXObject formXObject) throws IOException {
        if (resources == null)
            return;

        for (COSName cosName : resources.getPatternNames()) {
            PDAbstractPattern pdAbstractPattern = resources.getPattern(cosName);
            if (pdAbstractPattern instanceof PDTilingPattern) {
                PDTilingPattern pdTilingPattern = (PDTilingPattern) pdAbstractPattern;
                replaceBitmapImagesResources(pdTilingPattern.getResources(), formXObject);
            }
        }

        List<COSName> xobjectsToReplace = new ArrayList<>();
        for (COSName cosName : resources.getXObjectNames()) {
            PDXObject pdxObject = resources.getXObject(cosName);
            if (pdxObject instanceof PDImageXObject) {
                xobjectsToReplace.add(cosName);
            } else if (pdxObject instanceof PDFormXObject) {
                PDFormXObject pdFormXObject = (PDFormXObject) pdxObject;
                replaceBitmapImagesResources(pdFormXObject.getResources(), formXObject);
            }
        }

        for (COSName cosName : xobjectsToReplace) {
            resources.put(cosName, formXObject);
        }
    }
}
