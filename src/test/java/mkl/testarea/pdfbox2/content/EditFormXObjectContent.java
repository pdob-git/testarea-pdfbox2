package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class EditFormXObjectContent {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/77696598/how-to-adjust-operators-and-operands-from-xobjects-with-pdfbox">
     * How to adjust Operators and Operands from XObjects with PDFBox
     * </a>
     * <p>
     * This test replaces (some) fill color values in the form XObjects in the
     * immediate page resources of the test document. 
     * </p>
     */
    @Test
    public void testInvertColorsHighPioneerFallNewsletterADApdf_2() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("HighPioneerFallNewsletterADApdf_2.pdf");
                PDDocument document = PDDocument.load(resource)) {
            for (PDPage page : document.getDocumentCatalog().getPages()) {
                PdfContentStreamEditor editor = new PdfContentStreamEditor(document, page) {
                    @Override
                    protected void write(ContentStreamWriter contentStreamWriter, Operator operator, List<COSBase> operands) throws IOException {
                        String operatorString = operator.getName();

                        if (RGB_FILL_COLOR_OPERATORS.contains(operatorString))
                        {
                            for (int i = 0; i < operands.size(); i++) {
                                COSBase number = operands.get(i);
                                if (number instanceof COSNumber) {
                                    operands.set(i, new COSFloat(1.0f - ((COSNumber)number).floatValue()));
                                }
                            }
                        }

                        super.write(contentStreamWriter, operator, operands);
                    }

                    final List<String> RGB_FILL_COLOR_OPERATORS = Arrays.asList("rg", "sc", "scn");
                };
                PDResources resources = page.getResources();
                for (COSName name : resources.getXObjectNames()) {
                    PDXObject xObject = resources.getXObject(name);
                    if (xObject instanceof PDFormXObject) {
                        System.out.printf("Editing HighPioneerFallNewsletterADApdf_2 form XObject %s.\n", name.toString());
                        editor.processFormXObject((PDFormXObject) xObject, page);
                    }
                }
            }
            document.save(new File(RESULT_FOLDER, "HighPioneerFallNewsletterADApdf_2-formColorsInverted.pdf"));
        }
    }

}
