package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.util.Matrix;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ReuseExistingFont {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/61934819/pdfbox-no-glyph-for-u0050-in-extracted-font">
     * PDFBox: No glyph for U+0050 in extracted font
     * </a>
     * <br/>
     * <a href="https://github.com/sutr90/pdfbox-font-problem/raw/master/protokol.pdf">
     * protokol.pdf
     * </a>
     * <p>
     * Indeed, the font in question cannot be reused. One cause is that
     * {@link PDCIDFontType2#encode(int)} has an open TODO (to reverse
     * map ToUnicode), another that the font has no cmap.
     * </p>
     */
    @Test
    public void testReuseProtokolLikeJnovacho() throws IOException {
        try (PDDocument document = PDDocument.load(getClass().getResourceAsStream("protokol.pdf"))) {
            PDPage page = document.getPage(0);
            PDResources res = page.getResources();

            List<PDFont> fonts = new ArrayList<>();

            for (COSName fontName : res.getFontNames()) {
                PDFont font = res.getFont(fontName);
                System.out.println(font);
                fonts.add(font);
            }

            PDPage testPage = new PDPage();
            try (PDPageContentStream stream = new PDPageContentStream(document, testPage, PDPageContentStream.AppendMode.OVERWRITE, true)) {
                stream.beginText();
                int yPos = 200;
                for (int i = 0; i < fonts.size(); i++) {
                    stream.setFont(fonts.get(i), 12);
                    stream.setTextMatrix(Matrix.getTranslateInstance(20, yPos - 50 * i));
                    stream.showText("Protokol");

                }
                stream.endText();
            }
            document.addPage(testPage);

            document.save(new File(RESULT_FOLDER, "protokol-reusedFont.pdf"));
        } 
    }
}
