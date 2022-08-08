package mkl.testarea.pdfbox2.extract;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ExtractWordCoordinates {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/50330484/could-someone-give-me-an-example-of-how-to-extract-coordinates-for-a-word-usin">
     * Could someone give me an example of how to extract coordinates for a 'word' using PDFBox
     * </a>
     * <br/>
     * <a href="https://www.tutorialkart.com/pdfbox/how-to-get-location-and-size-of-images-in-pdf/attachment/apache-pdf/">
     * apache.pdf
     * </a>
     * <p>
     * This test shows how to extract word coordinates combining the ideas of
     * the two tutorials referenced by the OP.
     * </p>
     */
    @Test
    public void testExtractWordsForGoodJuJu() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("apache.pdf")) {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new GetWordLocationAndSize();
            stripper.setSortByPosition( true );
            stripper.setStartPage( 0 );
            stripper.setEndPage( document.getNumberOfPages() );
 
            Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
            stripper.writeText(document, dummy);
        }
    }

    /**
     * Combining ideas from 
     * <a href="https://www.tutorialkart.com/pdfbox/how-to-extract-coordinates-or-position-of-characters-in-pdf/">
     * How to extract coordinates or position of characters in PDF – PDFBox
     * </a>
     * and
     * <a href="https://www.tutorialkart.com/pdfbox/extract-words-from-pdf-document/">
     * How to extract words from PDF document
     * </a>
     * for
     * {@link ExtractWordCoordinates#testExtractWordsForGoodJuJu()}.
     */
    public class GetWordLocationAndSize extends PDFTextStripper {
        public GetWordLocationAndSize() throws IOException {
        }

        @Override
        protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
            String wordSeparator = getWordSeparator();
            List<TextPosition> word = new ArrayList<>();
            for (TextPosition text : textPositions) {
                String thisChar = text.getUnicode();
                if (thisChar != null) {
                    if (thisChar.length() >= 1) {
                        if (!thisChar.equals(wordSeparator)) {
                            word.add(text);
                        } else if (!word.isEmpty()) {
                            printWord(word);
                            word.clear();
                        }
                    }
                }
            }
            if (!word.isEmpty()) {
                printWord(word);
                word.clear();
            }
        }

        void printWord(List<TextPosition> word) {
            Rectangle2D boundingBox = null;
            StringBuilder builder = new StringBuilder();
            for (TextPosition text : word) {
                Rectangle2D box = new Rectangle2D.Float(text.getXDirAdj(), text.getYDirAdj(), text.getWidthDirAdj(), text.getHeightDir());
                if (boundingBox == null)
                    boundingBox = box;
                else
                    boundingBox.add(box);
                builder.append(text.getUnicode());
            }
            System.out.println(builder.toString() + " [(X=" + boundingBox.getX() + ",Y=" + boundingBox.getY()
                     + ") height=" + boundingBox.getHeight() + " width=" + boundingBox.getWidth() + "]");
        }
    }
}
