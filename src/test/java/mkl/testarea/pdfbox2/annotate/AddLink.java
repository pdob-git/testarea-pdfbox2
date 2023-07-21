package mkl.testarea.pdfbox2.annotate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class AddLink {
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/54986135/how-to-use-pdfbox-to-create-a-link-i-can-click-to-go-to-another-page-in-the-same">
     * How to use PDFBox to create a link i can click to go to another page in the same document
     * </a>
     * <p>
     * The OP used destination.setPageNumber which is not ok for local
     * links. Furthermore, he forgot to add the link to the page and
     * to give it a rectangle.
     * </p>
     */
    @Test
    public void testAddLinkToMwb_I_201711() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/pdfbox2/content/mwb_I_201711.pdf")) {
            PDDocument document = PDDocument.load(resource);

            PDPage page = document.getPage(1);

            PDAnnotationLink link         = new PDAnnotationLink();
            PDPageDestination destination = new PDPageFitWidthDestination();
            PDActionGoTo action           = new PDActionGoTo();

            //destination.setPageNumber(2);
            destination.setPage(document.getPage(2));
            action.setDestination(destination);
            link.setAction(action);
            link.setPage(page);

            link.setRectangle(page.getMediaBox());
            page.getAnnotations().add(link);

            document.save(new File(RESULT_FOLDER, "mwb_I_201711-with-link.pdf"));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/76738878/add-pdannotationlink-to-a-pdf-with-pdfbox">
     * Add PDAnnotationLink to a PDF With pdfbox
     * </a>
     * <p>
     * Indeed, the text under the link annotation is half covered by a black rectangle.
     * Looking closely at the source code one can see what this black rectangle is: The
     * code requests an underline of width 10! Considering that the font size of the text
     * underneath also is 10, it is not surprising to see half covered...
     * </p>
     */
    @Test
    public void testAddLinkLikeMarioRovelli() throws IOException {
        try (PDDocument pdDocument = new PDDocument()){
            PDPage pdPage = new PDPage();
            pdDocument.addPage(pdPage);

            PDPageContentStream contentStream = new PDPageContentStream(pdDocument, pdPage, true, true);
            
            final PDAnnotationLink txtLink = new PDAnnotationLink ();

            // border style
            final PDBorderStyleDictionary linkBorder = new PDBorderStyleDictionary ();
            linkBorder.setStyle (PDBorderStyleDictionary.STYLE_UNDERLINE);
            linkBorder.setWidth (10);
            txtLink.setBorderStyle (linkBorder);
            
            // Destination URI
            final PDActionURI action = new PDActionURI ();
            action.setURI ("https://www.test.com");
            txtLink.setAction (action); 

            // Position
            final PDRectangle position = new PDRectangle (200,302,100,11);
            txtLink.setRectangle (position);
            pdPage.getAnnotations ().add (txtLink);

            // Main page content
            contentStream.beginText ();
            contentStream.newLineAtOffset (102, 302);
            contentStream.setFont (PDType1Font.COURIER_BOLD, 10);
            contentStream.showText ("This is linked to the outside world");
            contentStream.endText ();
            contentStream.close(); 

            pdDocument.save(new File(RESULT_FOLDER, "AddLinkLikeMarioRovelli.pdf"));
        }
    }
}
