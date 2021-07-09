package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ChangeTextColor {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/68299177/pdfbox-text-displayed-in-blue-although-pdannotation-removed">
     * PDFBox - Text displayed in blue although PDAnnotation removed
     * </a>
     * <br/>
     * <a href="https://github.com/sureshbabukatta/pdfbox-remove-annotation">
     * test-after-removing-annotation.pdf
     * </a>
     * <p>
     * In addition to removing the annotations one has to edit the static page content.
     * This test method shows how to do so.
     * </p>
     */
    @Test
    public void testMakeTextBlackTestAfterRemovingAnnotation() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("test-after-removing-annotation.pdf");
                PDDocument document = Loader.loadPDF(resource)) {
            for (PDPage page : document.getDocumentCatalog().getPages()) {
                PdfContentStreamEditor editor = new PdfContentStreamEditor(document, page) {
                    @Override
                    protected void write(ContentStreamWriter contentStreamWriter, Operator operator, List<COSBase> operands) throws IOException {
                        String operatorString = operator.getName();

                        if (TEXT_SHOWING_OPERATORS.contains(operatorString)) {
                            if (currentlyReplacedColor == null)
                            {
                                PDColor currentFillColor = getGraphicsState().getNonStrokingColor();
                                if (!isBlack(currentFillColor))
                                {
                                    currentlyReplacedColor = currentFillColor;
                                    super.write(contentStreamWriter, SET_NON_STROKING_GRAY, GRAY_BLACK_VALUES);
                                }
                            }
                        } else if (currentlyReplacedColor != null) {
                            PDColorSpace replacedColorSpace = currentlyReplacedColor.getColorSpace();
                            List<COSBase> replacedColorValues = new ArrayList<>();
                            for (float f : currentlyReplacedColor.getComponents())
                                replacedColorValues.add(new COSFloat(f));
                            if (replacedColorSpace instanceof PDDeviceCMYK)
                                super.write(contentStreamWriter, SET_NON_STROKING_CMYK, replacedColorValues);
                            else if (replacedColorSpace instanceof PDDeviceGray)
                                super.write(contentStreamWriter, SET_NON_STROKING_GRAY, replacedColorValues);
                            else if (replacedColorSpace instanceof PDDeviceRGB)
                                super.write(contentStreamWriter, SET_NON_STROKING_RGB, replacedColorValues);
                            else {
                                //TODO
                            }
                            currentlyReplacedColor = null;
                        }

                        super.write(contentStreamWriter, operator, operands);
                    }

                    PDColor currentlyReplacedColor = null;

                    final List<String> TEXT_SHOWING_OPERATORS = Arrays.asList("Tj", "'", "\"", "TJ");
                    final Operator SET_NON_STROKING_CMYK = Operator.getOperator("k");
                    final Operator SET_NON_STROKING_RGB = Operator.getOperator("rg");
                    final Operator SET_NON_STROKING_GRAY = Operator.getOperator("g");
                    final List<COSBase> GRAY_BLACK_VALUES = Arrays.asList(COSInteger.ZERO);
                };
                editor.processPage(page);
            }
            document.save(new File(RESULT_FOLDER, "test-after-removing-annotation-withBlackText.pdf"));
        }
    }

    static boolean isBlack(PDColor pdColor) {
        PDColorSpace pdColorSpace = pdColor.getColorSpace();
        float[] components = pdColor.getComponents();
        if (pdColorSpace instanceof PDDeviceCMYK)
            return (components[0] > .9f && components[1] > .9f && components[2] > .9f) || components[3] > .9f;
        else if (pdColorSpace instanceof PDDeviceGray)
            return components[0] < .1f;
        else if (pdColorSpace instanceof PDDeviceRGB)
            return components[0] < .1f && components[1] < .1f && components[2] < .1f;
        else
            return false;
    }
}
