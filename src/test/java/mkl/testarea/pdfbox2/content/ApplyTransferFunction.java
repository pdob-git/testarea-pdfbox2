package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.function.PDFunctionType4;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.util.Matrix;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.ByteStreams;

/**
 * @author mkl
 */
public class ApplyTransferFunction {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/65665511/pdfbox-tint-transformation-of-pdf-page-contents-colors">
     * PDFBox tint transformation of PDF page contents (colors)
     * </a>
     * <p>
     * This test shows how to apply transfer functions.
     * </p>
     */
    @Test
    public void testSimpleTransferExample() throws IOException {
        try (PDDocument pdDocument = new PDDocument()) {
            PDPage pdPage = new PDPage();
            pdDocument.addPage(pdPage);

            PDImageXObject pdImage = null;
            try (InputStream imageResource = getClass().getResourceAsStream("Willi-1.jpg")) {
                pdImage = PDImageXObject.createFromByteArray(pdDocument, ByteStreams.toByteArray(imageResource), "Willi");
            }

            PDAppearanceStream pdFormXObject = new PDAppearanceStream(pdDocument);
            pdFormXObject.setResources(new PDResources());
            pdFormXObject.setBBox(new PDRectangle(150, 100));
            try (PDPageContentStream canvas = new PDPageContentStream(pdDocument, pdFormXObject)) {
                PDFont font = PDType1Font.HELVETICA;
                canvas.setFont(font, 10);
                canvas.beginText();
                canvas.newLineAtOffset(10, 85);
                canvas.setNonStrokingColor(1, 0, 0);
                canvas.showText("red");
                canvas.newLineAtOffset(0, -10);
                canvas.setNonStrokingColor(0, 1, 0);
                canvas.showText("green");
                canvas.newLineAtOffset(0, -10);
                canvas.setNonStrokingColor(0, 0, 1);
                canvas.showText("blue");
                canvas.newLineAtOffset(0, -10);
                canvas.setNonStrokingColor(0);
                canvas.showText("gray");
                canvas.newLineAtOffset(0, -10);
                canvas.setNonStrokingColor(1, 0, 0, 0);
                canvas.showText("cyan");
                canvas.newLineAtOffset(0, -10);
                canvas.setNonStrokingColor(0, 1, 0, 0);
                canvas.showText("magenta");
                canvas.newLineAtOffset(0, -10);
                canvas.setNonStrokingColor(0, 0, 1, 0);
                canvas.showText("yellow");
                canvas.newLineAtOffset(0, -10);
                canvas.setNonStrokingColor(0, 0, 0, 1);
                canvas.showText("black");
                canvas.endText();
            }

            try (PDPageContentStream canvas = new PDPageContentStream(pdDocument, pdPage)) {
                canvas.drawImage(pdImage, 0, 600, 150, 150);
                drawForm(canvas, pdFormXObject, 0, 500);
                canvas.setGraphicsStateParameters(createTransferedState("{ neg 1 add }"));
                canvas.drawImage(pdImage, 150, 600, 150, 150);
                drawForm(canvas, pdFormXObject, 150, 500);
                canvas.setGraphicsStateParameters(createTransferedState("{ 90 mul cos }"));
                canvas.drawImage(pdImage, 300, 600, 150, 150);
                drawForm(canvas, pdFormXObject, 300, 500);
                canvas.setGraphicsStateParameters(createTransferedState("{ 90 mul sin neg 1 add }"));
                canvas.drawImage(pdImage, 450, 600, 150, 150);
                drawForm(canvas, pdFormXObject, 450, 500);
                canvas.setGraphicsStateParameters(createTransferedState("{ .5 mul }"));
                canvas.drawImage(pdImage, 0, 350, 150, 150);
                drawForm(canvas, pdFormXObject, 0, 250);
                canvas.setGraphicsStateParameters(createTransferedState("{ 90 mul cos neg 1 add }"));
                canvas.drawImage(pdImage, 150, 350, 150, 150);
                drawForm(canvas, pdFormXObject, 150, 250);
                canvas.setGraphicsStateParameters(createTransferedState("{ 90 mul sin }"));
                canvas.drawImage(pdImage, 300, 350, 150, 150);
                drawForm(canvas, pdFormXObject, 300, 250);
                canvas.setGraphicsStateParameters(createTransferedState("{ .5 mul .5 add }"));
                canvas.drawImage(pdImage, 450, 350, 150, 150);
                drawForm(canvas, pdFormXObject, 450, 250);
                canvas.setGraphicsStateParameters(createTransferedState("{ }", "{ pop 1 }", "{ pop 1 }", "{ pop 1 }"));
                canvas.drawImage(pdImage, 0, 100, 150, 150);
                drawForm(canvas, pdFormXObject, 0, 0);
                canvas.setGraphicsStateParameters(createTransferedState("{ pop 1 }", "{ }", "{ pop 1 }", "{ pop 1 }"));
                canvas.drawImage(pdImage, 150, 100, 150, 150);
                drawForm(canvas, pdFormXObject, 150, 0);
                canvas.setGraphicsStateParameters(createTransferedState("{ pop 1 }", "{ pop 1 }", "{ }", "{ pop 1 }"));
                canvas.drawImage(pdImage, 300, 100, 150, 150);
                drawForm(canvas, pdFormXObject, 300, 0);
                canvas.setGraphicsStateParameters(createTransferedState("{ pop 1 }", "{ pop 1 }", "{ pop 1 }", "{ }"));
                canvas.drawImage(pdImage, 450, 100, 150, 150);
                drawForm(canvas, pdFormXObject, 450, 0);
            }

            pdDocument.save(new File(RESULT_FOLDER, "SimpleTransferExample.pdf"));
        }
    }

    void drawForm(PDPageContentStream canvas, PDFormXObject pdFormXObject, float x, float y) throws IOException {
        canvas.saveGraphicsState();
        canvas.transform(Matrix.getTranslateInstance(x, y));
        canvas.drawForm(pdFormXObject);
        canvas.restoreGraphicsState();
    }

    PDExtendedGraphicsState createTransferedState(String function) throws IOException {
        PDExtendedGraphicsState transferedState = new PDExtendedGraphicsState();
        transferedState.setTransfer(createTransferFunction(function).getCOSObject());
        return transferedState;
    }

    PDExtendedGraphicsState createTransferedState(String function1, String function2, String function3, String function4) throws IOException {
        COSArray array = new COSArray();
        array.add(createTransferFunction(function1).getCOSObject());
        array.add(createTransferFunction(function2).getCOSObject());
        array.add(createTransferFunction(function3).getCOSObject());
        array.add(createTransferFunction(function4).getCOSObject());
        PDExtendedGraphicsState transferedState = new PDExtendedGraphicsState();
        transferedState.setTransfer(array);
        return transferedState;
    }

    PDFunctionType4 createTransferFunction(String function) throws IOException {
        return createFunction(function, new float[] {0f, 1.0f}, new float[] {0f, 1.0f});
    }

    /** @see org.apache.pdfbox.pdmodel.common.function.TestPDFunctionType4 */
    PDFunctionType4 createFunction(String function, float[] domain, float[] range) throws IOException {
        COSStream stream = new COSStream();
        stream.setInt("FunctionType", 4);
        COSArray domainArray = new COSArray();
        domainArray.setFloatArray(domain);
        stream.setItem("Domain", domainArray);
        COSArray rangeArray = new COSArray();
        rangeArray.setFloatArray(range);
        stream.setItem("Range", rangeArray);
        
        try (OutputStream out = stream.createOutputStream())
        {
            byte[] data = function.getBytes(StandardCharsets.US_ASCII);
            out.write(data, 0, data.length);
        }

        return new PDFunctionType4(stream);
    }
}
