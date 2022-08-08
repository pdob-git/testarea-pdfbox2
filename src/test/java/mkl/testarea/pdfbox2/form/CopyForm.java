package mkl.testarea.pdfbox2.form;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class CopyForm
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/39260500/pdfbox-2-0-2-how-to-copy-pdtextfields-between-documents">
     * PDFBox 2.0.2 How to copy PDTextFields between documents
     * </a>
     * <p>
     * The NullPointerException can be reproduced. The problematic code line eventually
     * causing it is <code>newField.getWidgets().add(newWidget)</code>: the method
     * <code>getWidgets()</code> merely returns a list with all the current widgets
     * of the field, it is not the list in which the field object itself keeps its
     * widgets. Thus, adding to this list does not attach the widget to the field.
     * Instead one needs to collect all the widgets in a new List and eventually set
     * this list as the list of field widgets using the setWidgets method. But there
     * are more issues in this code to resolve...
     * </p>
     * <p>
     * The nomenclature for methods here is indeed misleading: You can add a page to a
     * document using <code>document.getPages().add(page)</code> but you cannot add
     * widgets to a field using <code>field.getWidgets().add(widget)</code>. IMO all
     * <code>getXXXs</code> across PDFBox should function similarly, either all allow
     * using <code>add</code> to add to the underlying document structure or none do.
     * </p>
     */
    @Test
    public void testCopyLikeLockrick() throws IOException
    {
        try (   InputStream originalStream = getClass().getResourceAsStream("FillFormField.pdf") )
        {
            PDDocument sourceDocument = PDDocument.load(originalStream);
            PDDocument targetDocument = new PDDocument();
            targetDocument.getDocumentCatalog().setAcroForm(new PDAcroForm(targetDocument));
            addPage(sourceDocument, targetDocument, 0);
            targetDocument.save(new File(RESULT_FOLDER, "FillFormField-Copied.pdf"));
            targetDocument.close();
            sourceDocument.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/39260500/pdfbox-2-0-2-how-to-copy-pdtextfields-between-documents">
     * PDFBox 2.0.2 How to copy PDTextFields between documents
     * </a>
     * <p>
     * The OP's method to add a page with a copy of the form of another
     * document which is tested in {@link #testCopyLikeLockrick()}.
     * </p>
     */
    private static void addPage(PDDocument src, PDDocument dest, int pageIndex) throws IOException
    {
        PDAcroForm srcAcro = src.getDocumentCatalog().getAcroForm();
        PDAcroForm destAcro = dest.getDocumentCatalog().getAcroForm();
        //destAcro.setDefaultResources(srcAcro.getDefaultResources()); <-- otherwise we get an exception
        PDPage page = new PDPage(src.getPage(pageIndex).getCOSObject());
        dest.addPage(page);

        if (destAcro == null)
        {
            destAcro = new PDAcroForm(dest, new COSDictionary(srcAcro.getCOSObject()));
            dest.getDocumentCatalog().setAcroForm(destAcro);

            for (PDField field : destAcro.getFieldTree())
            {
                if (field instanceof PDTextField)
                {
                    field.setPartialName(field.getPartialName() + ":" + dest.getPages().indexOf(page));
                    field.getCOSObject().setInt(COSName.PAGE, dest.getPages().indexOf(page));
                    field.setValue("TEST");
                }
            }
        }
        else
        {
            for (PDField field : srcAcro.getFieldTree())
            {
                if (field instanceof PDTextField)
                {
                    PDField newField = new PDTextField(destAcro);
                    newField.setPartialName(field.getPartialName() + ":" + dest.getPages().indexOf(page));
                    newField.getCOSObject().setInt(COSName.PAGE, dest.getPages().indexOf(page));
                    newField.getCOSObject().setString(COSName.DA, field.getCOSObject().getString(COSName.DA));

                    newField.getWidgets().clear();
                    
                    for (PDAnnotationWidget widget : field.getWidgets())
                    {
                        PDAnnotationWidget newWidget = new PDAnnotationWidget(widget.getCOSObject());
                        newWidget.setPage(page);
                        newField.getWidgets().add(newWidget);
                    }

                    destAcro.getFields().add(newField);
                    page.getAnnotations().addAll(newField.getWidgets());
                    newField.setValue("TEST");
                }
            }
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/55416144/search-used-resources-through-page-and-remove-them">
     * search used resources through page and remove them
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=15tW_o8A3inEHPRVPXyjXqqEX3zNsgwWf">
     * form.pdf
     * </a> as "formBee3.pdf".
     * <p>
     * Indeed, the result is not smaller than the source. But see
     * {@link #testCopyLikeBeeImproved()}.
     * </p>
     */
    @Test
    public void testCopyLikeBee() throws IOException
    {
        try (   InputStream originalStream = getClass().getResourceAsStream("formBee3.pdf") )
        {
            PDDocument documentSrc = PDDocument.load(originalStream);;
            PDAcroForm acroFormSrc = documentSrc.getDocumentCatalog().getAcroForm();

            PDDocument documentDest = new PDDocument();
            for (PDPage page : documentSrc.getPages()) {
                PDPage destPage  = new PDPage(PDRectangle.A4);
                destPage.setMediaBox(page.getMediaBox());
                destPage.setCropBox(page.getCropBox());
                documentDest.addPage(destPage);
            }

            PDAcroForm acroFormDest = new PDAcroForm(documentDest);


            acroFormDest.setCacheFields(true);
            acroFormDest.setFields(acroFormSrc.getFields());
            documentDest.getDocumentCatalog().setAcroForm(acroFormDest);

            int pageIndex = 0;
            for (PDPage page : documentSrc.getPages()) {
                documentDest.getPage(pageIndex).setAnnotations(page.getAnnotations());
                // after disabling this size increase
                //documentDest.getPage(pageIndex).setResources(page.getResources());
                pageIndex++;
            }

            acroFormDest.setDefaultAppearance(acroFormSrc.getDefaultAppearance());
            acroFormDest.setDefaultResources(acroFormSrc.getDefaultResources());
            acroFormDest.setQ(acroFormSrc.getQ());

            // this is disabled because setResources is disabled above
            //removeLinksInPages(documentDest);
            //removeTextInDocument(documentDest);
            documentDest.save(new File(RESULT_FOLDER, "formBee3-Copied.pdf"));
            documentDest.close();
            documentSrc.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/55416144/search-used-resources-through-page-and-remove-them">
     * search used resources through page and remove them
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=15tW_o8A3inEHPRVPXyjXqqEX3zNsgwWf">
     * form.pdf
     * </a> as "formBee3.pdf".
     * <p>
     * In contrast to the OP's code {@link #testCopyLikeBee()}
     * here we also set the page of the annotation correctly.
     * The result is much smaller
     * </p>
     */
    @Test
    public void testCopyLikeBeeImproved() throws IOException
    {
        try (   InputStream originalStream = getClass().getResourceAsStream("formBee3.pdf") )
        {
            PDDocument documentSrc = PDDocument.load(originalStream);;
            PDAcroForm acroFormSrc = documentSrc.getDocumentCatalog().getAcroForm();

            PDDocument documentDest = new PDDocument();
            for (PDPage page : documentSrc.getPages()) {
                PDPage destPage  = new PDPage(PDRectangle.A4);
                destPage.setMediaBox(page.getMediaBox());
                destPage.setCropBox(page.getCropBox());
                documentDest.addPage(destPage);
            }

            PDAcroForm acroFormDest = new PDAcroForm(documentDest);


            acroFormDest.setCacheFields(true);
            acroFormDest.setFields(acroFormSrc.getFields());
            documentDest.getDocumentCatalog().setAcroForm(acroFormDest);

            int pageIndex = 0;
            for (PDPage page : documentSrc.getPages()) {
                PDPage destPage = documentDest.getPage(pageIndex);
                destPage.setAnnotations(page.getAnnotations());
                for (PDAnnotation annotation : destPage.getAnnotations())
                    annotation.setPage(destPage);
                // after disabling this size increase
                //documentDest.getPage(pageIndex).setResources(page.getResources());
                pageIndex++;
            }

            acroFormDest.setDefaultAppearance(acroFormSrc.getDefaultAppearance());
            acroFormDest.setDefaultResources(acroFormSrc.getDefaultResources());
            acroFormDest.setQ(acroFormSrc.getQ());

            // this is disabled because setResources is disabled above
            //removeLinksInPages(documentDest);
            //removeTextInDocument(documentDest);
            documentDest.save(new File(RESULT_FOLDER, "formBee3-Copied-Improved.pdf"));
            documentDest.close();
            documentSrc.close();
        }
    }
}
