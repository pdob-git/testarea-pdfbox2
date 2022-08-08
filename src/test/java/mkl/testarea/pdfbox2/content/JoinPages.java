package mkl.testarea.pdfbox2.content;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.util.Matrix;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class JoinPages {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/47295391/create-a-one-page-pdf-from-two-pdfs-using-pdfbox">
     * create a one page PDF from two PDFs using PDFBOX
     * </a>
     * <p>
     * This test shows how to join two pages into one putting one above the other.
     * </p>
     */
    @Test
    public void testJoinSmallAndBig() throws IOException {
        try (   PDDocument document = new PDDocument();
                PDDocument top = prepareSmallPdf();
                PDDocument bottom = prepareBiggerPdf()) {
            join(document, top, bottom);
            document.save(new File(RESULT_FOLDER, "joinedPage.pdf"));
        }
    }

    /**
     * @see #testJoinSmallAndBig()
     */
    void join(PDDocument target, PDDocument topSource, PDDocument bottomSource) throws IOException {
        LayerUtility layerUtility = new LayerUtility(target);
        PDFormXObject topForm = layerUtility.importPageAsForm(topSource, 0);
        PDFormXObject bottomForm = layerUtility.importPageAsForm(bottomSource, 0);

        float height = topForm.getBBox().getHeight() + bottomForm.getBBox().getHeight();
        float width, topMargin, bottomMargin;
        if (topForm.getBBox().getWidth() > bottomForm.getBBox().getWidth()) {
            width = topForm.getBBox().getWidth();
            topMargin = 0;
            bottomMargin = (topForm.getBBox().getWidth() - bottomForm.getBBox().getWidth()) / 2;
        } else {
            width = bottomForm.getBBox().getWidth();
            topMargin = (bottomForm.getBBox().getWidth() - topForm.getBBox().getWidth()) / 2;
            bottomMargin = 0;
        }

        PDPage targetPage = new PDPage(new PDRectangle(width, height));
        target.addPage(targetPage);


        PDPageContentStream contentStream = new PDPageContentStream(target, targetPage);
        if (bottomMargin != 0)
            contentStream.transform(Matrix.getTranslateInstance(bottomMargin, 0));
        contentStream.drawForm(bottomForm);
        contentStream.transform(Matrix.getTranslateInstance(topMargin - bottomMargin, bottomForm.getBBox().getHeight()));
        contentStream.drawForm(topForm);
        contentStream.close();
    }

    /**
     * @see #testJoinSmallAndBig()
     */
    PDDocument prepareSmallPdf() throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(new PDRectangle(72, 72));
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.setNonStrokingColor(Color.YELLOW);
        contentStream.addRect(0, 0, 72, 72);
        contentStream.fill();
        contentStream.setNonStrokingColor(Color.BLACK);
        PDFont font = PDType1Font.HELVETICA;
        contentStream.beginText();
        contentStream.setFont(font, 18);
        contentStream.newLineAtOffset(2, 54);
        contentStream.showText("small");
        contentStream.newLineAtOffset(0, -48);
        contentStream.showText("page");
        contentStream.endText();
        contentStream.close();
        return document;
    }

    /**
     * @see #testJoinSmallAndBig()
     */
    PDDocument prepareBiggerPdf() throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A5);
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.setNonStrokingColor(Color.GREEN);
        contentStream.addRect(0, 0, PDRectangle.A5.getWidth(), PDRectangle.A5.getHeight());
        contentStream.fill();
        contentStream.setNonStrokingColor(Color.BLACK);
        PDFont font = PDType1Font.HELVETICA;
        contentStream.beginText();
        contentStream.setFont(font, 18);
        contentStream.newLineAtOffset(2, PDRectangle.A5.getHeight() - 24);
        contentStream.showText("This is the Bigger page");
        contentStream.newLineAtOffset(0, -48);
        contentStream.showText("BIGGER!");
        contentStream.endText();
        contentStream.close();
        return document;
    }

}
