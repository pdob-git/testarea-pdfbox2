package mkl.testarea.pdfbox2.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorN;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorSpace;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceCMYKColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceGrayColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceRGBColor;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.examples.util.PrintImageLocations;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ExtractImageLocations {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/50576614/why-does-pdfbox-return-image-dimension-of-size-0-x-0">
     * Why does PDFBox return image dimension of size 0 x 0
     * </a>
     * <br/>
     * <a href="https://files.fm/u/udvgszyv#_">
     * TopSecret.pdf
     * </a>
     * <p>
     * This effectively is the code used by the OP. Indeed, the image is not
     * found by the {@link PrintImageLocations} class. This is due to the
     * image being drawn in a tiling pattern which then is used to fill an
     * area in the page content and {@link PrintImageLocations} not descending
     * into patterns.
     * </p>
     * @see #testExtractLikeHelloWorldImprovedFromTopSecret()
     */
    @Test
    public void testExtractLikeHelloWorldFromTopSecret() throws IOException {
        System.out.println( "Processing TopSecret.pdf using PrintImageLocations");
        try (   InputStream resource = getClass().getResourceAsStream("TopSecret.pdf");
                PDDocument document = PDDocument.load(resource)    )
        {
            PrintImageLocations printer = new PrintImageLocations();
            int pageNum = 0;
            for( PDPage page : document.getPages() )
            {
                pageNum++;
                System.out.println( "Processing page: " + pageNum );
                printer.processPage(page);
            }
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/50576614/why-does-pdfbox-return-image-dimension-of-size-0-x-0">
     * Why does PDFBox return image dimension of size 0 x 0
     * </a>
     * <br/>
     * <a href="https://files.fm/u/udvgszyv#_">
     * TopSecret.pdf
     * </a>
     * <p>
     * This test uses an improved version of the {@link PrintImageLocations}
     * class: {@link PrintImageLocationsImproved}. The improvement makes the
     * class descent into a pattern definition whenever a filling operation
     * is encountered with a pattern color.
     * </p>
     * <p>
     * This is not perfect as it does neither properly restrict to the area
     * actually filled nor return multiple finds for an area large enough to
     * require multiple pattern tiles to fill. Nonetheless, it returns an
     * image match.
     * </p>
     * @see #testExtractLikeHelloWorldFromTopSecret()
     * @see PrintImageLocationsImproved
     */
    @Test
    public void testExtractLikeHelloWorldImprovedFromTopSecret() throws IOException {
        System.out.println( "Processing TopSecret.pdf using PrintImageLocationsImproved");
        try (   InputStream resource = getClass().getResourceAsStream("TopSecret.pdf");
                PDDocument document = PDDocument.load(resource)    )
        {
            PrintImageLocations printer = new PrintImageLocationsImproved();
            int pageNum = 0;
            for( PDPage page : document.getPages() )
            {
                pageNum++;
                System.out.println( "Processing page: " + pageNum );
                printer.processPage(page);
            }
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/50576614/why-does-pdfbox-return-image-dimension-of-size-0-x-0">
     * Why does PDFBox return image dimension of size 0 x 0
     * </a>
     * <br/>
     * <a href="https://files.fm/u/udvgszyv#_">
     * TopSecret.pdf
     * </a>
     * <p>
     * This is an improved version of the {@link PrintImageLocations} class.
     * The improvement makes it descent into a pattern definition whenever a
     * filling operation is encountered with a pattern color.
     * </p>
     * <p>
     * This is not perfect as it does neither properly restrict to the area
     * actually filled nor return multiple finds for an area large enough to
     * require multiple pattern tiles to fill. Nonetheless, it returns an
     * image match for the PDF at hand.
     * </p>
     * @see ExtractImageLocations#testExtractLikeHelloWorldImprovedFromTopSecret()
     */
    class PrintImageLocationsImproved extends PrintImageLocations {
        public PrintImageLocationsImproved() throws IOException {
            super();

            addOperator(new SetNonStrokingColor());
            addOperator(new SetNonStrokingColorN());
            addOperator(new SetNonStrokingDeviceCMYKColor());
            addOperator(new SetNonStrokingDeviceGrayColor());
            addOperator(new SetNonStrokingDeviceRGBColor());
            addOperator(new SetNonStrokingColorSpace());
        }

        @Override
        protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
            String operation = operator.getName();
            if (fillOperations.contains(operation)) {
                PDColor color = getGraphicsState().getNonStrokingColor();
                PDAbstractPattern pattern = getResources().getPattern(color.getPatternName());
                if (pattern instanceof PDTilingPattern) {
                    processTilingPattern((PDTilingPattern) pattern, null, null);
                }
            }
            super.processOperator(operator, operands);
        }

        final List<String> fillOperations = Arrays.asList("f", "F", "f*", "b", "b*", "B", "B*");
    }
}
