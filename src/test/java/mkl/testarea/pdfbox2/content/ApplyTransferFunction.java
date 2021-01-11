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
import org.apache.pdfbox.pdmodel.common.function.PDFunctionType4;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
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

            try (PDPageContentStream canvas = new PDPageContentStream(pdDocument, pdPage)) {
                canvas.drawImage(pdImage, 0, 600, 150, 150);
                canvas.setGraphicsStateParameters(createTransferedState("{ neg 1 add }"));
                canvas.drawImage(pdImage, 150, 600, 150, 150);
                canvas.setGraphicsStateParameters(createTransferedState("{ 90 mul cos }"));
                canvas.drawImage(pdImage, 300, 600, 150, 150);
                canvas.setGraphicsStateParameters(createTransferedState("{ 90 mul sin neg 1 add }"));
                canvas.drawImage(pdImage, 450, 600, 150, 150);
                canvas.setGraphicsStateParameters(createTransferedState("{ .5 mul }"));
                canvas.drawImage(pdImage, 0, 450, 150, 150);
                canvas.setGraphicsStateParameters(createTransferedState("{ 90 mul cos neg 1 add }"));
                canvas.drawImage(pdImage, 150, 450, 150, 150);
                canvas.setGraphicsStateParameters(createTransferedState("{ 90 mul sin }"));
                canvas.drawImage(pdImage, 300, 450, 150, 150);
                canvas.setGraphicsStateParameters(createTransferedState("{ .5 mul .5 add }"));
                canvas.drawImage(pdImage, 450, 450, 150, 150);
                canvas.setGraphicsStateParameters(createTransferedState("{ }", "{ pop 1 }", "{ pop 1 }", "{ pop 1 }"));
                canvas.drawImage(pdImage, 0, 300, 150, 150);
                canvas.setGraphicsStateParameters(createTransferedState("{ pop 1 }", "{ }", "{ pop 1 }", "{ pop 1 }"));
                canvas.drawImage(pdImage, 150, 300, 150, 150);
                canvas.setGraphicsStateParameters(createTransferedState("{ pop 1 }", "{ pop 1 }", "{ }", "{ pop 1 }"));
                canvas.drawImage(pdImage, 300, 300, 150, 150);
                canvas.setGraphicsStateParameters(createTransferedState("{ pop 1 }", "{ pop 1 }", "{ pop 1 }", "{ }"));
                canvas.drawImage(pdImage, 450, 300, 150, 150);
            }

            pdDocument.save(new File(RESULT_FOLDER, "SimpleTransferExample.pdf"));
        }
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
