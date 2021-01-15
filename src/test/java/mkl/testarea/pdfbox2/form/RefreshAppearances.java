package mkl.testarea.pdfbox2.form;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class RefreshAppearances {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/65701597/added-font-not-loading-correct-pdfbox-for-acroform">
     * Added font not loading correct pdfbox for acroform
     * </a>
     * <br/>
     * <a href="https://www.fontsquirrel.com/fonts/DejaVu-Sans">
     * DejaVuSans.ttf
     * </a>
     * <br/>
     * <a href="https://pdfhost.io/v/Oh9~qVoG5_sample5pdf.pdf">
     * sample5.pdf
     * </a>
     * <p>
     * This test essentially calls the OP's code. Debugging made clear that
     * the current AcroForm form field refreshing mechanism in PDFBox is not
     * really usable in combination with fonts yet to be subsetted. The
     * underlying problem can be reduced to what is shown in the test
     * {@link #testIllustrateType0Issue()}.
     * </p>
     */
    @Test
    public void testRefreshLikeLuckydonald() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("sample5.pdf");
                PDDocument pdDocument = Loader.loadPDF(resource)) {
            refresh(pdDocument, pdDocument.getDocumentCatalog().getAcroForm());

            pdDocument.save(new File(RESULT_FOLDER, "sample5-RefreshLikeLuckydonald.pdf"));
        }
    }

    void refresh(PDDocument document, PDAcroForm acroForm) throws IOException {
        InputStream font_file = getClass().getResourceAsStream("DejaVuSans.ttf");
        PDType0Font font = PDType0Font.load(document, font_file, false);
        if (font_file != null) {
            font_file.close();
        }
        System.err.println("Embedded font 'DejaVuSans.ttf' loaded.");

        PDResources resources = acroForm.getDefaultResources();
        if (resources == null) {
            resources = new PDResources();
        }

        resources.put(COSName.getPDFName("Helv"), font);
        resources.put(COSName.getPDFName("Helvetica"), font);
        // Also use "DejaVuSans.ttf" for "HeBo", "HelveticaBold" and "Helvetica-Bold" in a similar way, but this is left out to keep this short.

        acroForm.setDefaultResources(resources);

        // let pdfbox handle refreshing the values, now that all the fonts should be there.
        acroForm.refreshAppearances();
    }

    /**
     * <a href="https://stackoverflow.com/questions/65701597/added-font-not-loading-correct-pdfbox-for-acroform">
     * Added font not loading correct pdfbox for acroform
     * </a>
     * <br/>
     * <a href="https://www.fontsquirrel.com/fonts/DejaVu-Sans">
     * DejaVuSans.ttf
     * </a>
     * <p>
     * This test illustrates the problem one can run into when using a PDFBox
     * {@link PDType0Font} with subsetting activated: If one does not always
     * use the font directly but instead looks it up in the resources, a new
     * {@link PDType0Font} is generated from the preliminary PDF objects
     * created from the original instance, but the contents thereof make this
     * new instance appear non-embedded, so no information are given to the
     * original instance which glyphs are to be embedded and need to be in
     * the ToUnicode mapping.
     * </p>
     * <p>
     * This is exactly the problem the PDFBox code for refreshing AcroForm
     * appearances runs into in {@link #testRefreshLikeLuckydonald()}.
     * </p>
     */
    @Test
    public void testIllustrateType0Issue() throws IOException {
        try (   PDDocument pdDocument = new PDDocument();
                InputStream font_file = getClass().getResourceAsStream("DejaVuSans.ttf")    ) {
            PDType0Font font = PDType0Font.load(pdDocument, font_file);

            PDResources pdResources = new PDResources();
            COSName name = pdResources.add(font);
            PDPage pdPage = new PDPage();
            pdPage.setResources(pdResources);
            pdDocument.addPage(pdPage);

            try (   PDPageContentStream canvas = new PDPageContentStream(pdDocument, pdPage)    ) {
                canvas.setFont(pdResources.getFont(name), 12);
                canvas.beginText();
                canvas.newLineAtOffset(30, 700);
                canvas.showText("Some test text.");
                canvas.endText();
            }

            font.addToSubset('t');
            font.subset();
            pdDocument.save(new File(RESULT_FOLDER, "sampleOfType0Issue.pdf"));
        }
    }
}
