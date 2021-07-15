package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.function.PDFunctionType2;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDShadingPattern;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType2;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class DrawGradient {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/68272759/gradient-stroke-in-pdfbox">
     * Gradient stroke in PDFBox
     * </a>
     * <p>
     * This test illustrate how to draw using a shading pattern color.
     * </p>
     */
    @Test
    public void testDrawWithGradientColor() throws IOException {
        try (   PDDocument document = new PDDocument(); ) {
            PDPage page = new PDPage(new PDRectangle(-10, -10, 520, 520));
            page.setResources(new PDResources());
            document.addPage(page);

            PDShadingType2 shading = createGradientColor(arrayOf(1, 0, 0), arrayOf(0, 1, 0), arrayOf(0, 0, 500, 500));
            PDShadingPattern pattern = new PDShadingPattern();
            pattern.setShading(shading);
            COSName name = page.getResources().add(pattern);
            PDColor color = new PDColor(name, new PDPattern(null));

            try (   PDPageContentStream canvas = new PDPageContentStream(document, page)) {
                canvas.setStrokingColor(color);
                canvas.setLineWidth(5);
                canvas.moveTo(0, 0);
                canvas.lineTo(500, 500);
                canvas.curveTo2(500, 250, 250, 250);
                canvas.curveTo1(0, 250, 0, 0);
                canvas.stroke();
            }

            document.save(new File(RESULT_FOLDER, "DrawWithGradientColor.pdf"));
        }
    }

    /** @see #testDrawWithGradientColor() */
    static PDShadingType2 createGradientColor(COSArray c0, COSArray c1, COSArray coords) throws IOException {
        COSDictionary fdict = new COSDictionary();

        fdict.setInt(COSName.FUNCTION_TYPE, 2);

        COSArray domain = new COSArray();
        domain.add(COSInteger.get(0));
        domain.add(COSInteger.get(1));

        fdict.setItem(COSName.DOMAIN, domain);
        fdict.setItem(COSName.C0, c0);
        fdict.setItem(COSName.C1, c1);
        fdict.setInt(COSName.N, 1);

        PDFunctionType2 func = new PDFunctionType2(fdict);

        PDShadingType2 axialShading = new PDShadingType2(new COSDictionary());

        axialShading.setColorSpace(PDDeviceRGB.INSTANCE);
        axialShading.setShadingType(PDShading.SHADING_TYPE2);

        axialShading.setCoords(coords);
        axialShading.setFunction(func);

        return axialShading;
    }

    /** @see #testDrawWithGradientColor() */
    static COSArray arrayOf(float... entries) {
        COSArray array = new COSArray();
        for (float entry : entries) {
            array.add(new COSFloat(entry));
        }
        return array;
    }
}
