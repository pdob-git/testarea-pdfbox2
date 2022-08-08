package mkl.testarea.pdfbox2.content;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.Matrix;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class RotatePageContent
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/40611736/rotate-pdf-around-its-center-using-pdfbox-in-java">
     * Rotate PDF around its center using PDFBox in java
     * </a>
     * <p>
     * Indeed, using the code of the OP the image mostly is rotated out of the page area.
     * </p>
     */
    @Test
    public void testRotateLikeSagar() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("IRJET_Copy_Right_form.pdf")  )
        {
            PDDocument document = PDDocument.load(resource);
            PDPage page = document.getDocumentCatalog().getPages().get(0);
            PDPageContentStream cs = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.PREPEND, false, false); 
            cs.transform(Matrix.getRotateInstance(Math.toRadians(45), 0, 0));
            cs.close();
            document.save(new File(RESULT_FOLDER, "IRJET_Copy_Right_form-rotated-like-sagar.pdf"));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/40611736/rotate-pdf-around-its-center-using-pdfbox-in-java">
     * Rotate PDF around its center using PDFBox in java
     * </a>
     * <p>
     * This test shows how to rotate the page content around the center of its crop box.
     * </p>
     */
    @Test
    public void testRotateCenter() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("IRJET_Copy_Right_form.pdf")  )
        {
            PDDocument document = PDDocument.load(resource);
            PDPage page = document.getDocumentCatalog().getPages().get(0);
            PDPageContentStream cs = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.PREPEND, false, false); 
            PDRectangle cropBox = page.getCropBox();
            float tx = (cropBox.getLowerLeftX() + cropBox.getUpperRightX()) / 2;
            float ty = (cropBox.getLowerLeftY() + cropBox.getUpperRightY()) / 2;
            cs.transform(Matrix.getTranslateInstance(tx, ty));
            cs.transform(Matrix.getRotateInstance(Math.toRadians(45), 0, 0));
            cs.transform(Matrix.getTranslateInstance(-tx, -ty));
            cs.close();
            document.save(new File(RESULT_FOLDER, "IRJET_Copy_Right_form-rotated-center.pdf"));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/40611736/rotate-pdf-around-its-center-using-pdfbox-in-java">
     * Rotate PDF around its center using PDFBox in java
     * </a>
     * <p>
     * This test shows how to rotate the page content around the center of its crop box
     * and then crop it to make all previously visible content fit.
     * </p>
     */
    @Test
    public void testRotateCenterScale() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("IRJET_Copy_Right_form.pdf")  )
        {
            PDDocument document = PDDocument.load(resource);
            PDPage page = document.getDocumentCatalog().getPages().get(0);
            PDPageContentStream cs = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.PREPEND, false, false);

            Matrix matrix = Matrix.getRotateInstance(Math.toRadians(45), 0, 0);
            PDRectangle cropBox = page.getCropBox();
            float tx = (cropBox.getLowerLeftX() + cropBox.getUpperRightX()) / 2;
            float ty = (cropBox.getLowerLeftY() + cropBox.getUpperRightY()) / 2;

            Rectangle rectangle = cropBox.transform(matrix).getBounds();
            float scale = Math.min(cropBox.getWidth() / (float)rectangle.getWidth(), cropBox.getHeight() / (float)rectangle.getHeight());

            cs.transform(Matrix.getTranslateInstance(tx, ty));
            cs.transform(matrix);
            cs.transform(Matrix.getScaleInstance(scale, scale));
            cs.transform(Matrix.getTranslateInstance(-tx, -ty));
            cs.close();
            document.save(new File(RESULT_FOLDER, "IRJET_Copy_Right_form-rotated-center-scale.pdf"));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/40611736/rotate-pdf-around-its-center-using-pdfbox-in-java">
     * Rotate PDF around its center using PDFBox in java
     * </a>
     * <p>
     * This test shows how to rotate the page content and then move its crop box and
     * media box accordingly to make it appear as if the content was rotated around
     * the center of the crop box.
     * </p>
     */
    @Test
    public void testRotateMoveBox() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("IRJET_Copy_Right_form.pdf")  )
        {
            PDDocument document = PDDocument.load(resource);
            PDPage page = document.getDocumentCatalog().getPages().get(0);
            PDPageContentStream cs = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.PREPEND, false, false);
            Matrix matrix = Matrix.getRotateInstance(Math.toRadians(45), 0, 0);
            cs.transform(matrix);
            cs.close();

            PDRectangle cropBox = page.getCropBox();
            float cx = (cropBox.getLowerLeftX() + cropBox.getUpperRightX()) / 2;
            float cy = (cropBox.getLowerLeftY() + cropBox.getUpperRightY()) / 2;
            Point2D.Float newC = matrix.transformPoint(cx, cy);
            float tx = (float)newC.getX() - cx;
            float ty = (float)newC.getY() - cy;
            page.setCropBox(new PDRectangle(cropBox.getLowerLeftX() + tx, cropBox.getLowerLeftY() + ty, cropBox.getWidth(), cropBox.getHeight()));
            PDRectangle mediaBox = page.getMediaBox();
            page.setMediaBox(new PDRectangle(mediaBox.getLowerLeftX() + tx, mediaBox.getLowerLeftY() + ty, mediaBox.getWidth(), mediaBox.getHeight()));

            document.save(new File(RESULT_FOLDER, "IRJET_Copy_Right_form-rotated-move-box.pdf"));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/40611736/rotate-pdf-around-its-center-using-pdfbox-in-java">
     * Rotate PDF around its center using PDFBox in java
     * </a>
     * <p>
     * This test shows how to rotate the page content and then set the crop
     * box and media box to the bounding rectangle of the rotated page area.
     * </p>
     */
    @Test
    public void testRotateExpandBox() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("IRJET_Copy_Right_form.pdf")  )
        {
            PDDocument document = PDDocument.load(resource);
            PDPage page = document.getDocumentCatalog().getPages().get(0);
            PDPageContentStream cs = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.PREPEND, false, false);
            Matrix matrix = Matrix.getRotateInstance(Math.toRadians(45), 0, 0);
            cs.transform(matrix);
            cs.close();

            PDRectangle cropBox = page.getCropBox();
            Rectangle rectangle = cropBox.transform(matrix).getBounds();
            PDRectangle newBox = new PDRectangle((float)rectangle.getX(), (float)rectangle.getY(), (float)rectangle.getWidth(), (float)rectangle.getHeight());
            page.setCropBox(newBox);
            page.setMediaBox(newBox);

            document.save(new File(RESULT_FOLDER, "IRJET_Copy_Right_form-rotated-expand-box.pdf"));
        }
    }
}
