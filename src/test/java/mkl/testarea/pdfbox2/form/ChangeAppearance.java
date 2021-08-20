package mkl.testarea.pdfbox2.form;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTerminalField;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ChangeAppearance {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/68632495/can-java-use-pdfbox-to-turn-the-red-stamp-into-black">
     * Can java use pdfbox to turn the red stamp into black
     * </a>
     * <br/>
     * 222-color.pdf
     * <p>
     * This test shows how to change the signature annotation normal appearance
     * and remove all color saturation.
     * </p>
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testRemoveAppearanceSaturation() throws IOException {
        try (   InputStream originalStream = getClass().getResourceAsStream("222-color.pdf");
                PDDocument pdf = Loader.loadPDF(originalStream) ) {
            PDAcroForm acroForm = pdf.getDocumentCatalog().getAcroForm();
            PDTerminalField acroField = (PDTerminalField) acroForm.getField("Signature1");
            PDAnnotationWidget widget = acroField.getWidgets().get(0);

            PDAppearanceStream appearance = widget.getAppearance().getNormalAppearance().getAppearanceStream();
            byte[] originalBytes;
            try (   InputStream oldContent = appearance.getContents()   ) {
                originalBytes = IOUtils.toByteArray(oldContent);
            }
            try (   PDPageContentStream canvas = new PDPageContentStream(pdf, appearance)   ) {
                canvas.appendRawCommands(originalBytes);
                PDExtendedGraphicsState r01 = new PDExtendedGraphicsState();
                r01.setBlendMode(BlendMode.SATURATION);
                canvas.setGraphicsStateParameters(r01);
                canvas.setNonStrokingColor(Color.DARK_GRAY);
                PDRectangle bbox = appearance.getBBox();
                canvas.addRect(bbox.getLowerLeftX(), bbox.getLowerLeftY(), bbox.getWidth(), bbox.getHeight());
                canvas.fill();
            }
            pdf.save(new File(RESULT_FOLDER, "222-desaturated.pdf"));
        }
    }
}
