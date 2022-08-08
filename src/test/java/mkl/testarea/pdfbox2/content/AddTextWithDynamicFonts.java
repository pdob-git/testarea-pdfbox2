package mkl.testarea.pdfbox2.content;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class AddTextWithDynamicFonts {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/51481600/handle-many-unicode-caracters-with-pdfbox">
     * Handle many unicode caracters with PDFBox
     * </a>
     * <p>
     * This (including {@link #generatePdfFromString(String)}) is the OP's
     * original code with minor changes to allow for better testability.
     * Indeed, for obvious reasons using the built-in Helvetica for Japanese
     * characters fails.
     * </p>
     */
    @Test
    public void testAddLikeCccompany() throws IOException {
        String latinText = "This is latin text";
        String japaneseText = "これは日本語です";

        // This works good
        generatePdfFromString(latinText).writeTo(new FileOutputStream(new File(RESULT_FOLDER, "Cccompany-Latin.pdf")));

        try {
            // This generate an error
            generatePdfFromString(japaneseText).writeTo(new FileOutputStream(new File(RESULT_FOLDER, "Cccompany-Japanese.pdf")));
            fail("expected: java.lang.IllegalArgumentException: U+3053 ('kohiragana') is not available in this font Helvetica encoding: WinAnsiEncoding");
        } catch (IllegalArgumentException iae) {
            
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/51481600/handle-many-unicode-caracters-with-pdfbox">
     * Handle many unicode caracters with PDFBox
     * </a>
     * <br/>
     * NotoSans-Regular.ttf from
     * <a href="https://www.google.com/get/noto/">
     * Google Noto Fonts
     * </a>
     * <br/>
     * NotoSansCJKtc-Regular.ttf from
     * <a href="https://djmilch.wordpress.com/2016/01/19/free-font-noto-sans-cjk-in-ttf/">
     * FREE FONT NOTO SANS CJK IN TTF
     * </a>
     * <p>
     * This (including {@link #generatePdfFromStringImproved(String)}) is an
     * improved version of the OP's original code using multiple fonts and
     * font selection. This option works.
     * </p>
     */
    @Test
    public void testAddLikeCccompanyImproved() throws IOException {
        String latinText = "This is latin text";
        String japaneseText = "これは日本語です";
        String mixedText = "Tこhれiはs日 本i語sで すlatin text";

        generatePdfFromStringImproved(latinText).writeTo(new FileOutputStream(new File(RESULT_FOLDER, "Cccompany-Latin-Improved.pdf")));
        generatePdfFromStringImproved(japaneseText).writeTo(new FileOutputStream(new File(RESULT_FOLDER, "Cccompany-Japanese-Improved.pdf")));
        generatePdfFromStringImproved(mixedText).writeTo(new FileOutputStream(new File(RESULT_FOLDER, "Cccompany-Mixed-Improved.pdf")));
    }

    /**
     * @see #testAddLikeCccompany()
     */
    private static ByteArrayOutputStream generatePdfFromString(String content) throws IOException {
        PDPage page = new PDPage();

        try (PDDocument doc = new PDDocument();
             PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
            doc.addPage(page);
            contentStream.setFont(PDType1Font.HELVETICA, 12);

            // Or load a specific font from a file
            // contentStream.setFont(PDType0Font.load(this.doc, new File("/fontPath.ttf")), 12);

            contentStream.beginText();
            contentStream.showText(content);
            contentStream.endText();
            contentStream.close();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            doc.save(os);
            return os;
        }
    }

    /**
     * @see #testAddLikeCccompanyImproved()
     */
    private static ByteArrayOutputStream generatePdfFromStringImproved(String content) throws IOException {
        try (   PDDocument doc = new PDDocument();
                InputStream notoSansRegularResource = AddTextWithDynamicFonts.class.getResourceAsStream("NotoSans-Regular.ttf");
                InputStream notoSansCjkRegularResource = AddTextWithDynamicFonts.class.getResourceAsStream("NotoSansCJKtc-Regular.ttf")   ) {
            PDType0Font notoSansRegular = PDType0Font.load(doc, notoSansRegularResource);
            PDType0Font notoSansCjkRegular = PDType0Font.load(doc, notoSansCjkRegularResource);
            List<PDFont> fonts = Arrays.asList(notoSansRegular, notoSansCjkRegular);

            List<TextWithFont> fontifiedContent = fontify(fonts, content);

            PDPage page = new PDPage();
            doc.addPage(page);
            try (   PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.beginText();
                for (TextWithFont textWithFont : fontifiedContent) {
                    textWithFont.show(contentStream, 12);
                }
                contentStream.endText();
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            doc.save(os);
            return os;
        }
    }

    /**
     * @see #testAddLikeCccompanyImproved()
     */
    static List<TextWithFont> fontify(List<PDFont> fonts, String text) throws IOException {
        List<TextWithFont> result = new ArrayList<>();
        if (text.length() > 0) {
            PDFont currentFont = null;
            int start = 0;
            for (int i = 0; i < text.length(); ) {
                int codePoint = text.codePointAt(i);
                int codeChars = Character.charCount(codePoint);
                String codePointString = text.substring(i, i + codeChars);
                boolean canEncode = false;
                for (PDFont font : fonts) {
                    try {
                        font.encode(codePointString);
                        canEncode = true;
                        if (font != currentFont) {
                            if (currentFont != null) {
                                result.add(new TextWithFont(text.substring(start, i), currentFont));
                            }
                            currentFont = font;
                            start = i;
                        }
                        break;
                    } catch (Exception ioe) {
                        // font cannot encode codepoint
                    }
                }
                if (!canEncode) {
                    throw new IOException("Cannot encode '" + codePointString + "'.");
                }
                i += codeChars;
            }
            result.add(new TextWithFont(text.substring(start, text.length()), currentFont));
        }
        return result;
    }

    /**
     * @see #testAddLikeCccompanyImproved()
     */
    static class TextWithFont {
        final String text;
        final PDFont font;

        TextWithFont(String text, PDFont font) {
            this.text = text;
            this.font = font;
        }

        public void show(PDPageContentStream canvas, float fontSize) throws IOException {
            canvas.setFont(font, fontSize);
            canvas.showText(text);
        }
    }
}
