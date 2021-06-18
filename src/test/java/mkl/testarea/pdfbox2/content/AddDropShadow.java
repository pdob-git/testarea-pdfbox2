package mkl.testarea.pdfbox2.content;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDFormContentStream;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.function.PDFunctionType2;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroupAttributes;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType2;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.ByteStreams;

/**
 * @author mkl
 */
public class AddDropShadow {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/68020506/need-help-on-applying-drop-shadow-effects-to-a-pdf-box-components-in-java">
     * Need help on applying drop shadow effects to a pdf box components in java
     * </a>
     * <p>
     * This test shows how to apply a simply drop shadow to a rectangle. It may
     * look fancier if one applied a bit of blur to the shadow.
     * </p>
     */
    @Test
    public void testRectangleWithDropShadow() throws IOException {
        PDDocument doc = new PDDocument();
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, ByteStreams.toByteArray(getClass().getResourceAsStream("Willi-1.jpg")), "Willi-1.jpg");
        PDPage page = new PDPage();
        doc.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(doc, page);

        int left = -60;
        int top = 96;
        int width = 471;
        int height = 365;

        contentStream.setStrokingColor(1, 0, 0);
        contentStream.setLineWidth(4);

        int imageOriginalWidth = 633;
        int imageOriginalHeight = 422;

        float scaleX = 0.99f;
        float scaleY = 0.99f;

        float imageWidth = imageOriginalWidth*scaleX;
        float imageHeight = imageOriginalHeight*scaleY;

        float imageY = page.getMediaBox().getHeight() - (top + imageHeight-58); 
        float imageX = -104; 

        // vvv--- code added for shadow
        PDExtendedGraphicsState extGS = new PDExtendedGraphicsState();
        extGS.setNonStrokingAlphaConstant(.2f);
        contentStream.saveGraphicsState();
        contentStream.setGraphicsStateParameters(extGS);
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.addRect(left + 5, page.getMediaBox().getHeight() - top - height - 5, width, height);
        contentStream.fill();
        contentStream.restoreGraphicsState();
        // ^^^--- code added for shadow

        contentStream.addRect(left, page.getMediaBox().getHeight() - top - height, width, height);
        contentStream.clip();
        contentStream.drawImage(pdImage, imageX, imageY, imageWidth, imageHeight);
        contentStream.close();

        doc.save(new File(RESULT_FOLDER, "RectangleWithDropShadow.pdf"));
        doc.close();
    }

    /**
     * <a href="https://stackoverflow.com/questions/68020506/need-help-on-applying-drop-shadow-effects-to-a-pdf-box-components-in-java">
     * Need help on applying drop shadow effects to a pdf box components in java
     * </a>
     * <p>
     * This test shows how to apply a drop shadow with fading edges to a rectangle.
     * </p>
     */
    @Test
    public void testRectangleWithDropShadowFade() throws IOException {
        PDDocument doc = new PDDocument();
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, ByteStreams.toByteArray(getClass().getResourceAsStream("Willi-1.jpg")), "Willi-1.jpg");
        PDPage page = new PDPage();
        doc.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(doc, page);

        int left = -60;
        int top = 96;
        int width = 471;
        int height = 365;

        contentStream.setStrokingColor(1, 0, 0);
        contentStream.setLineWidth(4);

        int imageOriginalWidth = 633;
        int imageOriginalHeight = 422;

        float scaleX = 0.99f;
        float scaleY = 0.99f;

        float imageWidth = imageOriginalWidth*scaleX;
        float imageHeight = imageOriginalHeight*scaleY;

        float imageY = page.getMediaBox().getHeight() - (top + imageHeight-58); 
        float imageX = -104; 

        // vvv--- code added for fading shadow
        PDExtendedGraphicsState extGS = createStateWithSMask(doc, left, page.getMediaBox().getHeight() - top - height, width, height);
        contentStream.saveGraphicsState();
        contentStream.setGraphicsStateParameters(extGS);
        contentStream.setNonStrokingColor(Color.BLACK);
        PDRectangle cropBox = page.getCropBox();
        contentStream.addRect(cropBox.getLowerLeftX(), cropBox.getLowerLeftY(), cropBox.getWidth(), cropBox.getHeight());
        contentStream.fill();
        contentStream.restoreGraphicsState();
        // ^^^--- code added for fading shadow

        contentStream.addRect(left, page.getMediaBox().getHeight() - top - height, width, height);
        contentStream.clip();
        contentStream.drawImage(pdImage, imageX, imageY, imageWidth, imageHeight);
        contentStream.close();

        doc.save(new File(RESULT_FOLDER, "RectangleWithDropShadowFade.pdf"));
        doc.close();
    }

    /**
     * Creates a {@link PDExtendedGraphicsState} with a soft mask for a fading drop shadow.
     * 
     * @see #testRectangleWithDropShadowFade()
     * @see UseSoftMask#testSoftMaskedImageAndRectangle()
     */
    PDExtendedGraphicsState createStateWithSMask(PDDocument document, float x, float y, float width, float height) throws IOException {
        float offset = 15;
        float fade = 15;
        float opacityFrom = .2f;
        float opacityTo = 0f;

        x += offset;
        y -= offset;
        float xCenter = x + width/2;
        float yCenter = y + height/2;
        float innerWidth = width - 2*fade;
        float innerHeight = height - 2*fade;

        PDFunctionType2 func = createFadingFunction(opacityFrom, opacityTo);
        PDShading axialShading = createShading(-fade, 0);
        axialShading.setFunction(func);

        PDTransparencyGroupAttributes transparencyGroupAttributes = new PDTransparencyGroupAttributes();
        transparencyGroupAttributes.getCOSObject().setItem(COSName.CS, COSName.DEVICEGRAY);

        PDTransparencyGroup transparencyGroup = new PDTransparencyGroup(document);
        transparencyGroup.setBBox(PDRectangle.A4);
        transparencyGroup.setResources(new PDResources());
        transparencyGroup.getCOSObject().setItem(COSName.GROUP, transparencyGroupAttributes);
        try (   PDFormContentStream canvas = new PDFormContentStream(transparencyGroup)   ) {
            canvas.saveGraphicsState();
            clip(canvas, xCenter, yCenter, innerWidth, innerHeight, innerWidth, -innerHeight);
            canvas.transform(Matrix.getTranslateInstance(x + width, 0));
            canvas.shadingFill(axialShading);
            canvas.restoreGraphicsState();

            canvas.saveGraphicsState();
            clip(canvas, xCenter, yCenter, -innerWidth, innerHeight, innerWidth, innerHeight);
            canvas.transform(Matrix.getRotateInstance((float)Math.PI / 2, 0, y + height));
            canvas.shadingFill(axialShading);
            canvas.restoreGraphicsState();

            canvas.saveGraphicsState();
            clip(canvas, xCenter, yCenter, -innerWidth, innerHeight, -innerWidth, -innerHeight);
            canvas.transform(Matrix.getRotateInstance((float)Math.PI, x, 0));
            canvas.shadingFill(axialShading);
            canvas.restoreGraphicsState();

            canvas.saveGraphicsState();
            clip(canvas, xCenter, yCenter, innerWidth, -innerHeight, -innerWidth, -innerHeight);
            canvas.transform(Matrix.getRotateInstance(-(float)Math.PI / 2, 0, y));
            canvas.shadingFill(axialShading);
            canvas.restoreGraphicsState();
        }

        COSDictionary softMaskDictionary = new COSDictionary();
        softMaskDictionary.setItem(COSName.TYPE, COSName.MASK);
        softMaskDictionary.setItem(COSName.S, COSName.LUMINOSITY);
        softMaskDictionary.setItem(COSName.G, transparencyGroup);

        PDExtendedGraphicsState extendedGraphicsState = new PDExtendedGraphicsState();
        extendedGraphicsState.getCOSObject().setItem(COSName.SMASK, softMaskDictionary);

        return extendedGraphicsState;
    }

    /**
     * @see #testRectangleWithDropShadowFade()
     * @see #createStateWithSMask(PDDocument, float, float, float, float)
     */
    PDFunctionType2 createFadingFunction(float opacityFrom, float opacityTo) {
        COSDictionary function = new COSDictionary();
        function.setInt(COSName.FUNCTION_TYPE, 2);

        COSArray domain = new COSArray();
        domain.add(COSInteger.get(0));
        domain.add(COSInteger.get(1));

        COSArray c0 = new COSArray();
        c0.add(new COSFloat(opacityFrom));

        COSArray c1 = new COSArray();
        c1.add(new COSFloat(opacityTo));

        function.setItem(COSName.DOMAIN, domain);
        function.setItem(COSName.C0, c0);
        function.setItem(COSName.C1, c1);
        function.setInt(COSName.N, 1);

        return new PDFunctionType2(function);
    }

    /**
     * @see #testRectangleWithDropShadowFade()
     * @see #createStateWithSMask(PDDocument, float, float, float, float)
     */
    PDShading createShading(float xFrom, float xTo) {
        PDShadingType2 axialShading = new PDShadingType2(new COSDictionary());

        axialShading.setColorSpace(PDDeviceGray.INSTANCE);
        axialShading.setShadingType(PDShading.SHADING_TYPE2);

        COSArray coords = new COSArray();
        coords.add(new COSFloat(xFrom));
        coords.add(new COSFloat(0));
        coords.add(new COSFloat(xTo));
        coords.add(new COSFloat(0));
        axialShading.setCoords(coords);

        COSArray extend = new COSArray();
        extend.add(COSBoolean.TRUE);
        extend.add(COSBoolean.TRUE);
        axialShading.setExtend(extend);

        return axialShading;
    }

    /**
     * @see #testRectangleWithDropShadowFade()
     * @see #createStateWithSMask(PDDocument, float, float, float, float)
     */
    void clip(PDFormContentStream canvas, float x0, float y0, float deltaX1, float deltaY1, float deltaX2, float deltaY2) throws IOException {
        canvas.moveTo(x0, y0);
        canvas.lineTo(x0 + deltaX1, y0 + deltaY1);
        canvas.lineTo(x0 + deltaX2, y0 + deltaY2);
        canvas.close();
        canvas.clip();
    }
}
