package pl.pdob.pdfTables;

import mkl.testarea.pdfbox2.extract.PdfBoxFinder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class to demonstrate issue with tables with merged cells<br>
 * <a href="https://stackoverflow.com/questions/78001237/extracting-text-from-pdf-java-using-pdfbox-library-from-a-tables-with-merged-c">
 * Extracting text from pdf (java using pdfbox library) from a tables with merged cells
 * </a>
 * <br>
 * Method Drawing found rectangles taken from {@link mkl.testarea.pdfbox2.extract.ExtractBoxedText}<br>
 * and modified
 */
public class Stack78001237 {


    private static final File RESULT_FOLDER = new File("pl.pdob.results");

    private static final File INPUT_FOLDER = new File("pl.pdob.input");

    private static final String EXAMPLE_PDF = "regular_table.pdf";
//    private static final String EXAMPLE_PDF = "merged_cells_example.pdf";


    static {

        if (!INPUT_FOLDER.exists()) {
            //noinspection ResultOfMethodCallIgnored
            INPUT_FOLDER.mkdirs();
        }

        if (!RESULT_FOLDER.exists()) {
            //noinspection ResultOfMethodCallIgnored
            RESULT_FOLDER.mkdirs();
        }
    }

    public static void main(String[] args) throws IOException {
        Stack78001237 stack78001237 = new Stack78001237();
        stack78001237.drawBoxes(EXAMPLE_PDF);
    }

    @SuppressWarnings("SameParameterValue")
    private void drawBoxes(String fileName) throws IOException {
        File file = new File(INPUT_FOLDER, fileName);

        try (
             PDDocument document = PDDocument.load(file) ) {
            for (PDPage page : document.getDocumentCatalog().getPages()) {
                PdfBoxFinder boxFinder = new PdfBoxFinder(page);
                boxFinder.processPage(page);

                try (PDPageContentStream canvas = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    canvas.setStrokingColor(Color.RED);
                    for (Rectangle2D rectangle : boxFinder.getBoxes().values()) {
                        canvas.addRect((float)rectangle.getX(), (float)rectangle.getY(), (float)rectangle.getWidth(), (float)rectangle.getHeight());
                    }
                    canvas.stroke();
                }
            }
            document.save(new File(RESULT_FOLDER, fileName + "-rectangles.pdf"));
        }
    }
}
