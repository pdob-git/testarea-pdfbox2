package mkl.testarea.pdfbox2.merge;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.util.Matrix;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class DenseMerging {
    final static File RESULT_FOLDER = new File("target/test-outputs", "merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/54283827/pdf-files-merge-remove-blank-at-end-of-page-i-am-using-pdfbox-v2-0-13-to-achi">
     * PDF files merge : remove blank at end of page. I am using PDFBox v2.0.13 to achieve that
     * </a>
     * <p>
     * This test checks the {@link PdfDenseMergeTool} which allows
     * a dense merging of multiple input PDFs.
     * </p>
     */
    @Test
    public void testWithText() throws IOException {
        PDDocument document1 = createTextDocument(new PDRectangle(0, 0, 400, 600), 
                Matrix.getTranslateInstance(30, 300),
                "Doc 1 line 1", "Doc 1 line 2", "Doc 1 line 3");
        document1.save(new File(RESULT_FOLDER, "Test Text 1.pdf"));
        PDDocument document2 = createTextDocument(new PDRectangle(0, 0, 400, 600), 
                Matrix.getTranslateInstance(40, 400),
                "Doc 2 line 1", "Doc 2 line 2", "Doc 2 line 3");
        document2.save(new File(RESULT_FOLDER, "Test Text 2.pdf"));
        PDDocument document3 = createTextDocument(new PDRectangle(0, -300, 400, 600), 
                Matrix.getTranslateInstance(50, -100),
                "Doc 3 line 1", "Doc 3 line 2", "Doc 3 line 3");
        document3.save(new File(RESULT_FOLDER, "Test Text 3.pdf"));
        PDDocument document4 = createTextDocument(new PDRectangle(-200, -300, 400, 600), 
                Matrix.getTranslateInstance(-140, -100),
                "Doc 4 line 1", "Doc 4 line 2", "Doc 4 line 3");
        document4.save(new File(RESULT_FOLDER, "Test Text 4.pdf"));
        PDDocument document5 = createTextDocument(new PDRectangle(-200, -300, 400, 600), 
                Matrix.getRotateInstance(Math.PI / 4, -120, 0),
                "Doc 5 line 1", "Doc 5 line 2", "Doc 5 line 3");
        document5.save(new File(RESULT_FOLDER, "Test Text 5.pdf"));

        PdfDenseMergeTool tool = new PdfDenseMergeTool(PDRectangle.A4, 30, 30, 10);
        tool.merge(new FileOutputStream(new File(RESULT_FOLDER, "Merge with Text.pdf")),
                Arrays.asList(document1, document2, document3, document4, document5,
                        document1, document2, document3, document4, document5,
                        document1, document2, document3, document4, document5));
    }

    PDDocument createTextDocument(PDRectangle size, Matrix textMatrix, String... lines) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(size);
        document.addPage(page);

        try (PDPageContentStream canvas = new PDPageContentStream(document, page)) {
            canvas.beginText();
            canvas.setTextMatrix(textMatrix);
            canvas.setFont(new PDType1Font(FontName.HELVETICA_BOLD), 12);
            canvas.setLeading(14);
            for (String line : lines) {
                canvas.showText(line);
                canvas.newLine();
            }
            canvas.endText();
        }

        return document;
    }

    /**
     * <a href="https://stackoverflow.com/questions/60052967/how-to-dense-merge-pdf-files-using-pdfbox-2-without-whitespace-near-page-breaks">
     * How to dense merge PDF files using PDFBox 2 without whitespace near page breaks?
     * </a>
     * <p>
     * This test checks the {@link PageVerticalAnalyzer} functionality.
     * </p>
     * <p>
     * Beware, as mentioned in the {@link PageVerticalAnalyzer} comments,
     * the processing in particular of curves is incorrect. The curve
     * used in this test is chosen not to create wrong results due to
     * this known issue.
     * </p>
     */
    @Test
    public void testVerticalAnalyzer() throws IOException {
        PDDocument document = createTextDocument(new PDRectangle(0, 0, 400, 600), 
                Matrix.getTranslateInstance(30, 300),
                "Document line 1", "Document line 2", "Document line 3");

        PDPage page = document.getPage(0);

        try (   PDPageContentStream content = new PDPageContentStream(document, page, AppendMode.APPEND, false, true)) {
            content.setStrokingColor(Color.BLACK);
            content.moveTo(40, 40);
            content.lineTo(80, 80);
            content.lineTo(120, 100);
            content.stroke();

            content.moveTo(40, 140);
            content.curveTo(80, 140, 160, 140, 80, 180);
            content.closeAndFillAndStroke();
        }

        PageVerticalAnalyzer analyzer = new PageVerticalAnalyzer(page);
        analyzer.processPage(page);
        System.out.println(analyzer.getVerticalFlips());
        
        try (   PDPageContentStream content = new PDPageContentStream(document, page, AppendMode.APPEND, false, true)) {
            content.setStrokingColor(Color.RED);
            content.setLineWidth(3);
            List<Float> flips = analyzer.getVerticalFlips();
            float x = page.getCropBox().getLowerLeftX() + 20;
            for (int i = 0; i < flips.size() - 1; i+=2) {
                content.moveTo(x, flips.get(i));
                content.lineTo(x, flips.get(i+1));
            }
            content.stroke();
        }

        document.save(new File(RESULT_FOLDER, "Test Document Vertically Marked.pdf"));
    }

    /**
     * <a href="https://stackoverflow.com/questions/60052967/how-to-dense-merge-pdf-files-using-pdfbox-2-without-whitespace-near-page-breaks">
     * How to dense merge PDF files using PDFBox 2 without whitespace near page breaks?
     * </a>
     * <p>
     * This test checks the {@link PdfVeryDenseMergeTool} which allows
     * a very dense merging of multiple input PDFs.
     * </p>
     * <p>
     * Beware, as mentioned in the {@link PageVerticalAnalyzer} comments,
     * the processing in particular of curves is incorrect. The curve
     * used in this test is chosen not to create wrong results due to
     * this known issue.
     * </p>
     */
    @Test
    public void testVeryDenseMerging() throws IOException {
        PDDocument document1 = createTextDocument(new PDRectangle(0, 0, 400, 600), 
                Matrix.getTranslateInstance(30, 300),
                "Doc 1 line 1", "Doc 1 line 2", "Doc 1 line 3");
        PDDocument document2 = createTextDocument(new PDRectangle(0, 0, 400, 600), 
                Matrix.getTranslateInstance(40, 400),
                "Doc 2 line 1", "Doc 2 line 2", "Doc 2 line 3");
        PDDocument document3 = createTextDocument(new PDRectangle(0, -300, 400, 600), 
                Matrix.getTranslateInstance(50, -100),
                "Doc 3 line 1", "Doc 3 line 2", "Doc 3 line 3");
        PDDocument document4 = createTextDocument(new PDRectangle(-200, -300, 400, 600), 
                Matrix.getTranslateInstance(-140, -100),
                "Doc 4 line 1", "Doc 4 line 2", "Doc 4 line 3");
        PDDocument document5 = createTextDocument(new PDRectangle(-200, -300, 400, 600), 
                Matrix.getTranslateInstance(-140, -100),
                "Doc 5 line 1", "Doc 5 line 2", "Doc 5 line 3");
        PDDocument document6 = createTextDocument(new PDRectangle(-200, -300, 400, 600), 
                Matrix.getRotateInstance(Math.PI / 4, -120, 0),
                "Doc 6 line 1", "Doc 6 line 2", "Doc 6 line 3");
        try (   PDPageContentStream content = new PDPageContentStream(document6, document6.getPage(0), AppendMode.APPEND, false, true)) {
            content.setStrokingColor(Color.BLACK);
            content.moveTo(40, 40);
            content.lineTo(80, 80);
            content.lineTo(120, 100);
            content.stroke();

            content.moveTo(40, 140);
            content.curveTo(80, 140, 160, 140, 80, 180);
            content.closeAndFillAndStroke();
        }
        document6.save(new File(RESULT_FOLDER, "Test Text and Graphics.pdf"));

        PdfVeryDenseMergeTool tool = new PdfVeryDenseMergeTool(PDRectangle.A4, 30, 30, 10);
        tool.merge(new FileOutputStream(new File(RESULT_FOLDER, "Merge with Text and Graphics, very dense.pdf")),
                Arrays.asList(document1, document2, document3, document4, document5, document6,
                        document1, document2, document3, document4, document5, document6,
                        document1, document2, document3, document4, document5, document6,
                        document1, document2, document3, document4, document5, document6,
                        document1, document2, document3, document4, document5, document6));
    }
}
