package mkl.testarea.pdfbox2.content;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSString;
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

    /**
     * <a href="https://stackoverflow.com/questions/77992635/how-to-remove-specific-pattern-text-from-pdf-using-pdfbox">
     * How to remove specific pattern text from PDF using PDFBox?
     * </a>
     * <br/>
     * <a href="https://github.com/nofelkad/pdf-sample/blob/main/sample_tag.pdf">
     * sample_tag.pdf
     * </a>
     * <p>
     * As the OP already said, his code posted as "Update 1" works as expected
     * for this file, even after the corrected decoding and encoding.
     * </p>
     * @see #removeTagsLikeVasK(PDDocument)
     */
    @Test
    public void testRemoveTagsFromSampleTag() throws IOException {
        try (
            InputStream resource = getClass().getResourceAsStream("sample_tag.pdf");
            PDDocument document = PDDocument.load(resource)
        ) {
            removeTagsLikeVasK(document);
            document.save(new File(RESULT_FOLDER, "sample_tag-RemoveTags.pdf"));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/77992635/how-to-remove-specific-pattern-text-from-pdf-using-pdfbox">
     * How to remove specific pattern text from PDF using PDFBox?
     * </a>
     * <br/>
     * <a href="https://github.com/nofelkad/pdf-sample/blob/main/text_tag.pdf">
     * text_tag.pdf
     * </a>
     * <p>
     * As the OP already said, his code posted as "Update 1" works does not work
     * for this file, instead it scrambled the displayed text. After correcting the
     * decoding and encoding of COS string data, this problem stopped. It became
     * apparent, though, that the OP's routines to filter the COS string contents
     * are deficient.
     * </p>
     * @see #removeTagsLikeVasK(PDDocument)
     * @see #extractStringsBetweenCurlyBraces(String)
     */
    @Test
    public void testRemoveTagsFromTextTag() throws IOException {
        try (
            InputStream resource = getClass().getResourceAsStream("text_tag.pdf");
            PDDocument document = PDDocument.load(resource)
        ) {
            removeTagsLikeVasK(document);
            document.save(new File(RESULT_FOLDER, "text_tag-RemoveTags.pdf"));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/77992635/how-to-remove-specific-pattern-text-from-pdf-using-pdfbox">
     * How to remove specific pattern text from PDF using PDFBox?
     * </a>
     * <p>
     * This essentially is the code provided by the OP with only COSString
     * decoding and encoding fixed.
     * </p>
     * @see #testRemoveTagsFromSampleTag()
     * @see #testRemoveTagsFromTextTag()
     */
    public void removeTagsLikeVasK(PDDocument document) throws IOException {
        for (PDPage page : document.getDocumentCatalog().getPages()) {
            PdfContentStreamEditor editor = new PdfContentStreamEditor(document, page) {
                @Override
                protected void write(ContentStreamWriter contentStreamWriter, Operator operator, List<COSBase> operands) throws IOException {
                    String operatorString = operator.getName();
                    if (TEXT_SHOWING_OPERATORS.contains(operatorString))
                    {
                        PDFont font = getGraphicsState().getTextState().getFont();
                        if (operands.get(0) instanceof COSString) {
                            COSString str = (COSString) operands.get(0);
                            String text = decode(str, font);// str.getString();
                            String updated = extractStringsBetweenCurlyBraces(text);
                            if(!text.equals(updated)){
                                str.setValue(font.encode(updated));//(updated.getBytes());
                            }
                        }
                        if (operands.get(0) instanceof COSArray) {
                            Iterator<?> var7 = ((COSArray) operands.get(0)).iterator();
                            while (var7.hasNext()) {
                                COSBase obj = (COSBase) var7.next();
                                if (obj instanceof COSString) {
                                    COSString str = (COSString) obj;
                                    String text = decode(str, font);//str.getString();
                                    String updated = extractStringsBetweenCurlyBraces(text);
                                    str.setValue(font.encode(updated));//(updated.getBytes());
                                }
                            }
                        }
                    }
                    super.write(contentStreamWriter, operator, operands);
                }

                String decode(COSString string, PDFont font) throws IOException {
                    StringBuilder builder = new StringBuilder();
                    try (InputStream in = new ByteArrayInputStream(string.getBytes())) {
                        while (in.available() > 0) {
                            int code = font.readCode(in);
                            String chars = font.toUnicode(code);
                            builder.append(chars);
                        }
                    }
                    return builder.toString();
                }

                final List<String> TEXT_SHOWING_OPERATORS = Arrays.asList("Tj", "'", "\"", "TJ");
            };
            editor.processPage(page);
        }
    }

    /** @see #removeTagsLikeVasK(PDDocument) */
    public static String extractStringsBetweenCurlyBraces(String input) {
        Pattern pattern = Pattern.compile("\\{\\{[^}]*\\}\\}|\\{\\{.*$");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String match = matcher.group();
            String replacement = " ".repeat(match.length()+7);
            input= input.replace(match,replacement);
        }

        pattern = Pattern.compile("^.*?\\}\\}");
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String match = matcher.group();
            String replacement = " ".repeat(match.length()+7);
            input= input.replace(match,replacement);
        }
        return input;
    }
}
