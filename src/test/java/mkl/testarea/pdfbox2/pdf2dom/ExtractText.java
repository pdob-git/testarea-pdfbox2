package mkl.testarea.pdfbox2.pdf2dom;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.TextPosition;
import org.fit.pdfdom.BoxStyle;
import org.fit.pdfdom.PDFDomTree;
import org.fit.pdfdom.PDFDomTreeConfig;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ExtractText {
    final static File RESULT_FOLDER = new File("target/test-outputs", "pdf2dom");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/51672080/pdfdomtree-does-not-detecting-white-spaces-while-converting-a-pdf-file-to-html">
     * PDFDomTree does not detecting white spaces while converting a pdf file to html
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1SZNFCvGVbQzCxJiRr8HlW99ravC_Cm71/view?usp=sharing">
     * demo.pdf
     * </a>
     * <p>
     * Indeed, Pdf2Dom drops the narrow word gaps even though they are
     * created by space glyphs. The issues: white space glyphs are dropped
     * instead of being interpreted as large-enough gap; and gap analysis
     * by distance uses hard coded values without taking font size etc.
     * into account.
     * </p>
     * @see #testDemoImproved()
     */
    @Test
    public void testDemo() throws IOException, ParserConfigurationException
    {
        System.out.printf("\n*\n* demo.pdf\n*\n");
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/pdfbox2/extract/demo.pdf")    ) {
            PDDocument document = PDDocument.load(resource);

            PDFDomTree parser = new PDFDomTree(PDFDomTreeConfig.createDefaultConfig());
            Writer output = new PrintWriter(new File(RESULT_FOLDER, "demo.html"), "utf-8");

            parser.writeText(document, output);
            output.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/51672080/pdfdomtree-does-not-detecting-white-spaces-while-converting-a-pdf-file-to-html">
     * PDFDomTree does not detecting white spaces while converting a pdf file to html
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1SZNFCvGVbQzCxJiRr8HlW99ravC_Cm71/view?usp=sharing">
     * demo.pdf
     * </a>
     * <p>
     * This improved version does not ignore white space glyphs but
     * instead translates them into gaps. This is a work-around and
     * not a fix, different kinds of white spaces need to be handled
     * differently.
     * </p>
     * @see #testDemo()
     */
    @Test
    public void testDemoImproved() throws IOException, ParserConfigurationException
    {
        System.out.printf("\n*\n* demo.pdf improved\n*\n");
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/pdfbox2/extract/demo.pdf")    ) {
            PDDocument document = PDDocument.load(resource);

            PDFDomTree parser = new PDFDomTree(PDFDomTreeConfig.createDefaultConfig()) {
                @Override
                protected void processTextPosition(TextPosition text) {
                    if (text.getUnicode().trim().isEmpty()) {
                        //finish current box (if any)
                        if (lastText != null)
                        {
                            finishBox();
                        }
                        //start a new box
                        curstyle = new BoxStyle(style);
                        lastText = null;
                    } else {
                        super.processTextPosition(text);
                    }
                }
            };
            Writer output = new PrintWriter(new File(RESULT_FOLDER, "demo-improved.html"), "utf-8");

            parser.writeText(document, output);
            output.close();
        }
    }
}
