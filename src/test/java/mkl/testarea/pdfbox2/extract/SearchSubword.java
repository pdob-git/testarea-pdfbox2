// $Id$
package mkl.testarea.pdfbox2.extract;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.junit.Test;

/**
 * @author mkl
 */
public class SearchSubword
{
    /**
     * <a href="http://stackoverflow.com/questions/35937774/how-to-search-some-specific-string-or-a-word-and-there-coordinates-from-a-pdf-do">
     * How to search some specific string or a word and there coordinates from a pdf document in java
     * </a>
     * <br/>
     * <a href="https://stackoverflow.com/questions/62792228/search-texts-and-get-position-in-pdf-with-java">
     * Search texts and get position in pdf with java
     * </a>
     * <br/>
     * Variables.pdf
     * <p>
     * This test demonstrates how one can search for text parts with positions in a PDF.
     * This method actually is quite crude as it expects the whole search term to be
     * forwarded to {@link PDFTextStripper#writeString(String, List<TextPosition>)}
     * in the same call. This is why the methods here are called
     * {@link #printSubwords(PDDocument, String)} and
     * {@link #findSubwords(PDDocument, int, String)} as internally PDFBox calls
     * the portions forwarded here "words", cf. the calling method
     * {@link PDFTextStripper#writeLine(List<WordWithTextPositions>)}.
     * </p>
     * <p>
     * The third run executing {@link #findSubwordsImproved(PDDocument, int, String)}
     * in contrast collects all text positions and adds virtual space text positions
     * when <code>writeLineSeparator</code> is called. Thereafter, it looks for matches
     * in the whole collection of text positions. Thus, also the matches spanning lines
     * are found.
     * </p>
     */
    @Test
    public void testVariables() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Variables.pdf");
                PDDocument document = PDDocument.load(resource);    )
        {
            System.out.println("\nVariables.pdf\n-------------\n");
            printSubwords(document, "${var1}");
            printSubwords(document, "${var 2}");
            printSubwordsImproved(document, "${var 2}");
        }
    }
    
    void printSubwords(PDDocument document, String searchTerm) throws IOException
    {
        System.out.printf("* Looking for '%s'\n", searchTerm);
        for (int page = 1; page <= document.getNumberOfPages(); page++)
        {
            List<TextPositionSequence> hits = findSubwords(document, page, searchTerm);
            for (TextPositionSequence hit : hits)
            {
                if (!searchTerm.equals(hit.toString()))
                    System.out.printf("  Invalid (%s) ", hit.toString());
                TextPosition lastPosition = hit.textPositionAt(hit.length() - 1);
                System.out.printf("  Page %s at %s, %s with width %s and last letter '%s' at %s, %s\n",
                        page, hit.getX(), hit.getY(), hit.getWidth(),
                        lastPosition.getUnicode(), lastPosition.getXDirAdj(), lastPosition.getYDirAdj());
            }
        }
    }

    void printSubwordsImproved(PDDocument document, String searchTerm) throws IOException
    {
        System.out.printf("* Looking for '%s' (improved)\n", searchTerm);
        for (int page = 1; page <= document.getNumberOfPages(); page++)
        {
            List<TextPositionSequence> hits = findSubwordsImproved(document, page, searchTerm);
            for (TextPositionSequence hit : hits)
            {
                if (!searchTerm.equals(hit.toString()))
                    System.out.printf("  Invalid (%s) ", hit.toString());
                TextPosition lastPosition = hit.textPositionAt(hit.length() - 1);
                System.out.printf("  Page %s at %s, %s with width %s and last letter '%s' at %s, %s\n",
                        page, hit.getX(), hit.getY(), hit.getWidth(),
                        lastPosition.getUnicode(), lastPosition.getXDirAdj(), lastPosition.getYDirAdj());
            }
        }
    }

    List<TextPositionSequence> findSubwords(PDDocument document, int page, String searchTerm) throws IOException
    {
        final List<TextPositionSequence> hits = new ArrayList<TextPositionSequence>();
        PDFTextStripper stripper = new PDFTextStripper()
        {
            @Override
            protected void writeString(String text, List<TextPosition> textPositions) throws IOException
            {
                System.out.printf("  -- %s\n", text);

                TextPositionSequence word = new TextPositionSequence(textPositions);
                String string = word.toString();

                int fromIndex = 0;
                int index;
                while ((index = string.indexOf(searchTerm, fromIndex)) > -1)
                {
                    hits.add(word.subSequence(index, index + searchTerm.length()));
                    fromIndex = index + 1;
                }
                super.writeString(text, textPositions);
            }
        };
        
        stripper.setSortByPosition(true);
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        stripper.getText(document);
        return hits;
    }

    List<TextPositionSequence> findSubwordsImproved(PDDocument document, int page, String searchTerm) throws IOException
    {
        final List<TextPosition> allTextPositions = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper()
        {
            @Override
            protected void writeString(String text, List<TextPosition> textPositions) throws IOException
            {
                allTextPositions.addAll(textPositions);
                super.writeString(text, textPositions);
            }

            @Override
            protected void writeLineSeparator() throws IOException {
                if (!allTextPositions.isEmpty()) {
                    TextPosition last = allTextPositions.get(allTextPositions.size() - 1);
                    if (!" ".equals(last.getUnicode())) {
                        Matrix textMatrix = last.getTextMatrix().clone();
                        textMatrix.setValue(2, 0, last.getEndX());
                        textMatrix.setValue(2, 1, last.getEndY());
                        TextPosition separatorSpace = new TextPosition(last.getRotation(), last.getPageWidth(), last.getPageHeight(),
                                textMatrix, last.getEndX(), last.getEndY(), last.getHeight(), 0, last.getWidthOfSpace(), " ",
                                new int[] {' '}, last.getFont(), last.getFontSize(), (int) last.getFontSizeInPt());
                        allTextPositions.add(separatorSpace);
                    }
                }
                super.writeLineSeparator();
            }
        };
        
        stripper.setSortByPosition(true);
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        stripper.getText(document);

        final List<TextPositionSequence> hits = new ArrayList<TextPositionSequence>();
        TextPositionSequence word = new TextPositionSequence(allTextPositions);
        String string = word.toString();
        System.out.printf("  -- %s\n", string);

        int fromIndex = 0;
        int index;
        while ((index = string.indexOf(searchTerm, fromIndex)) > -1)
        {
            hits.add(word.subSequence(index, index + searchTerm.length()));
            fromIndex = index + 1;
        }

        return hits;
    }

}
