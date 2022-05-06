package mkl.testarea.pdfbox2.extract;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <a href="https://stackoverflow.com/questions/72086776/find-blank-spacerectangle-for-signature-field-using-pdfbox">
 * Find blank space(rectangle) for signature field using PDFBox
 * </a>
 * <p>
 * This tests the {@link PDFGraphicsStreamEngine} subclass {@link FreeSpaceFinder}
 * which is a port of the iText <code>FreeSpaceFinderExt</code> render listener
 * to PDFBox as a special {@link PDFGraphicsStreamEngine}. The iText render
 * listener has been developed in response to the stack overflow question
 * </p>
 * <a href="http://stackoverflow.com/questions/26464324/how-can-i-can-insert-an-image-or-stamp-on-a-pdf-where-there-is-free-space-availa">
 * How can I can insert an image or stamp on a pdf where there is free space available like a density scanner
 * </a>
 * 
 * @author mkl
 */
public class DetermineFreeSpaces {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void testZugferd_20x20() throws IOException {
        test("zugferd_add_xml_to_pdf.pdf", 20, 20);
    }

    @Test
    public void testZugferd_200x50() throws IOException {
        test("zugferd_add_xml_to_pdf.pdf", 200, 50);
    }

    @Test
    public void testZugferd_200x200() throws IOException {
        test("zugferd_add_xml_to_pdf.pdf", 200, 200);
    }

    void test(String resource, float minWidth, float minHeight) throws IOException {
        String name = new File(resource).getName();
        String target = String.format("%s-freeSpace%.0fx%.0f.pdf", name, minWidth, minHeight);
        
        try (
            InputStream resourceStream = getClass().getResourceAsStream(resource);
            PDDocument pdDocument = Loader.loadPDF(resourceStream);
        ) {
            System.out.printf("\nFree %.0fx%.0f regions in %s\n", minWidth, minHeight, name);

            PDPage pdPage = pdDocument.getPage(0);
            Collection<Rectangle2D> rectangles = find(pdDocument, pdPage, minWidth, minHeight);
            print(rectangles);

            try (   PDPageContentStream canvas = new PDPageContentStream(pdDocument, pdPage, AppendMode.APPEND, true, true) ) {
                enhance(canvas, rectangles);
            }

            pdDocument.save(new File(RESULT_FOLDER, target));
        }
    }

    public Collection<Rectangle2D> find(PDDocument pdDocument, PDPage pdPage, float minWidth, float minHeight) throws IOException {
        FreeSpaceFinder finder = new FreeSpaceFinder(pdPage, minWidth, minHeight);
        finder.processPage(pdPage);
        return finder.getFreeSpaces();
    }

    void print(Collection<Rectangle2D> rectangles) {
        System.out.println("  x       y       w      h");
        for (Rectangle2D rectangle : rectangles) {
            System.out.printf("  %07.3f %07.3f %07.3f %07.3f\n", rectangle.getMinX(), rectangle.getMinY(), rectangle.getWidth(), rectangle.getHeight());
        }
    }

    void enhance(PDPageContentStream canvas, Collection<Rectangle2D> rectangles) throws IOException
    {
        for (Rectangle2D rectangle : rectangles)
        {
            canvas.setStrokingColor(pickColor());
            canvas.addRect((float) rectangle.getMinX(), (float) rectangle.getMinY(), (float) rectangle.getWidth(), (float) rectangle.getHeight());
            canvas.stroke();
        }
    }

    final static Color[] colors = new Color[] { Color.RED, Color.PINK, Color.ORANGE, Color.GREEN, Color.MAGENTA, Color.CYAN, Color.BLUE };
    static int colorIndex = 0;

    static Color pickColor() {
        colorIndex++;
        if (colorIndex >= colors.length)
        colorIndex = 0;
        return colors[colorIndex];
    }
}
