package mkl.testarea.pdfbox2.form;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class AddFormField {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/46433388/pdfbox-could-not-find-font-helv">
     * PDFbox Could not find font: /Helv
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B2--NSDOiujoR3hOZFYteUl2UE0/view?usp=sharing">
     * 4.pdf
     * </a>
     * <p>
     * The cause is a combination of the OP and the source PDF not providing
     * a default appearance for the text field and PDFBox providing defaults
     * inconsequentially.
     * </p>
     * <p>
     * This is fixed here by setting the default appearance explicitly.
     * </p>
     */
    @Test
    public void testAddFieldLikeEugenePodoliako() throws IOException {
        try (   InputStream originalStream = getClass().getResourceAsStream("4.pdf") )
        {
            PDDocument pdf = PDDocument.load(originalStream);
            PDDocumentCatalog docCatalog = pdf.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();
            PDPage page = pdf.getPage(0);

            PDTextField textBox = new PDTextField(acroForm);
            textBox.setPartialName("SampleField");
            acroForm.getFields().add(textBox);
            PDAnnotationWidget widget = textBox.getWidgets().get(0);
            PDRectangle rect = new PDRectangle(0, 0, 0, 0);
            widget.setRectangle(rect);
            widget.setPage(page);
//  Unnecessary code from OP
//            widget.setAppearance(acroForm.getFields().get(0).getWidgets().get(0).getAppearance());
//  Fix added to set default appearance accordingly
            textBox.setDefaultAppearance(acroForm.getFields().get(0).getCOSObject().getString("DA"));

            widget.setPrinted(false);

            page.getAnnotations().add(widget);

            acroForm.refreshAppearances();
            acroForm.flatten();
            pdf.save(new File(RESULT_FOLDER, "4-add-field.pdf"));
            pdf.close();
        }
    }

}
