package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class RemoveText {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/66809923/remove-a-sentence-in-pdf-by-pdfbox">
     * Remove a sentence in pdf by pdfbox
     * </a>
     * <br/>
     * <a href="https://github.com/zhongguogu/PDFBOX/blob/master/pdf/watermark.pdf">
     * watermark.pdf
     * </a>
     * <p>
     * This test shows how to remove the header line in question. It essentially
     * is a copy of {@link EditPageContent#testRemoveQrTextNuevo()} where "[QR]"
     * texts were to be removed from underneath QR codes. The situation becomes
     * a bit less simple, though, if your text is not drawn using a single text
     * showing instruction only.
     * </p>
     */
    @Test
    public void testRemoveByText() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("watermark.pdf");
                PDDocument document = PDDocument.load(resource)) {
            for (PDPage page : document.getDocumentCatalog().getPages()) {
                PdfContentStreamEditor editor = new PdfContentStreamEditor(document, page) {
                    final StringBuilder recentChars = new StringBuilder();

                    @Override
                    protected void showGlyph(Matrix textRenderingMatrix, PDFont font, int code, Vector displacement)
                            throws IOException {
                        String string = font.toUnicode(code);
                        if (string != null)
                            recentChars.append(string);

                        super.showGlyph(textRenderingMatrix, font, code, displacement);
                    }

                    @Override
                    protected void write(ContentStreamWriter contentStreamWriter, Operator operator, List<COSBase> operands) throws IOException {
                        String recentText = recentChars.toString();
                        recentChars.setLength(0);
                        String operatorString = operator.getName();

                        if (TEXT_SHOWING_OPERATORS.contains(operatorString) && "本报告仅供-中庚基金管理有限公司-中庚报告邮箱使用 p2".equals(recentText))
                        {
                            return;
                        }

                        super.write(contentStreamWriter, operator, operands);
                    }

                    final List<String> TEXT_SHOWING_OPERATORS = Arrays.asList("Tj", "'", "\"", "TJ");
                };
                editor.processPage(page);
            }
            document.save(new File(RESULT_FOLDER, "watermark-RemoveByText.pdf"));
        }
    }

}
