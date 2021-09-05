/**
 * 
 */
package mkl.testarea.pdfbox2.form;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTerminalField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mklink
 *
 */
public class DuplicateFields
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <p>
     * This test attempts to create duplicate fields in different ways
     * </p>
     * <ul>
     * <li>A single field with multiple widget annotations, one per use;
     *  this is the "official" way. 
     * <li>A single field with a single, merged widget annotation which
     *  is referenced from different pages; this is not valid per se, cf.
     *  ISO 32000-1, section 12.5.2: "A given annotation dictionary shall
     *  be referenced from the Annots array of only one page". Unless
     *  simultaneously visible (e.g. in "Two Page View" page display),
     *  though, this works well in Adobe Reader. 
     * <li>Two separate fields, either both in the AcroForm Fields or
     *  not; this is not explicitly prohibited by the specification but
     *  seems not to be supported by Adobe. One might consider this an
     *  implicit requirement, there shall not be two fields with the
     *  same name unless these two fields are descendants of a single
     *  field which already also has this name.
     * <li>One named field with two anonymous descendant fields each
     *  with two widgets. While this should work, it doesn't: Adobe
     *  ignores one of the four widgets.  
     * </ul> 
     */
    @Test
    public void testCreateDuplicateFields() throws IOException
    {
        try (PDDocument document = new PDDocument())
        {
            PDPage page1 = new PDPage(PDRectangle.A4);
            document.addPage(page1);
            PDPage page2 = new PDPage(PDRectangle.A4);
            document.addPage(page2);

            PDFont font = new PDType1Font(FontName.HELVETICA);
            PDResources resources = new PDResources();
            resources.put(COSName.getPDFName("Helv"), font);

            PDAcroForm acroForm = new PDAcroForm(document);
            acroForm.setDefaultResources(resources);
            //acroForm.setNeedAppearances(true);
            document.getDocumentCatalog().setAcroForm(acroForm);

            //
            // SampleFieldA: single field with separate widgets.
            //
            PDTextField textBox = new PDTextField(acroForm);
            textBox.setPartialName("SampleFieldA");
            textBox.setDefaultAppearance("/Helv 12 Tf 0 0 1 rg");
            acroForm.getFields().add(textBox);

            PDAnnotationWidget widget1 = new PDAnnotationWidget();
            PDRectangle rectA = new PDRectangle(50, 750, 250, 50);
            widget1.setRectangle(rectA);
            widget1.setPage(page1);
            widget1.setParent(textBox);
            page1.getAnnotations().add(widget1);

            PDAnnotationWidget widget2 = new PDAnnotationWidget();
            PDRectangle rect2 = new PDRectangle(50, 750, 250, 50);
            widget2.setRectangle(rect2);
            widget2.setPage(page2);
            widget2.setParent(textBox);
            page2.getAnnotations().add(widget2);

            textBox.setWidgets(Arrays.asList(widget1, widget2));
            textBox.setValue("A");

            //
            // SampleFieldB: single field with merged single widget.
            //
            textBox = new PDTextField(acroForm);
            textBox.setPartialName("SampleFieldB");
            textBox.setDefaultAppearance("/Helv 12 Tf 0 0 1 rg");
            acroForm.getFields().add(textBox);

            PDAnnotationWidget widget = textBox.getWidgets().get(0);
            PDRectangle rectB = new PDRectangle(50, 650, 250, 50);
            widget.setRectangle(rectB);
            page1.getAnnotations().add(widget);
            page2.getAnnotations().add(widget);

            textBox.setValue("B");

            //
            // SampleFieldC: separate fields
            //
            textBox = new PDTextField(acroForm);
            textBox.setPartialName("SampleFieldC");
            textBox.setDefaultAppearance("/Helv 12 Tf 0 0 1 rg");
            acroForm.getFields().add(textBox);

            widget = textBox.getWidgets().get(0);
            PDRectangle rectC = new PDRectangle(50, 550, 250, 50);
            widget.setRectangle(rectC);
            page1.getAnnotations().add(widget);

            textBox.setValue("C1");

            textBox = new PDTextField(acroForm);
            textBox.setPartialName("SampleFieldC");
            textBox.setDefaultAppearance("/Helv 12 Tf 0 0 1 rg");
            acroForm.getFields().add(textBox);

            widget = textBox.getWidgets().get(0);
            widget.setRectangle(rectC);
            page2.getAnnotations().add(widget);

            textBox.setValue("C2");

            //
            // SampleFieldD: separate fields not in AcroForm
            //
            textBox = new PDTextField(acroForm);
            textBox.setPartialName("SampleFieldD");
            textBox.setDefaultAppearance("/Helv 12 Tf 0 0 1 rg");

            widget = textBox.getWidgets().get(0);
            PDRectangle rectD = new PDRectangle(50, 450, 250, 50);
            widget.setRectangle(rectD);
            page1.getAnnotations().add(widget);

            textBox.setValue("D1");

            textBox = new PDTextField(acroForm);
            textBox.setPartialName("SampleFieldD");
            textBox.setDefaultAppearance("/Helv 12 Tf 0 0 1 rg");

            widget = textBox.getWidgets().get(0);
            widget.setRectangle(rectD);
            page2.getAnnotations().add(widget);

            textBox.setValue("D2");

            //
            // SampleFieldE: one ancestor field with the name,
            // separate anonymous child fields, separate widgets.
            //
            textBox = new PDTextField(acroForm);
            textBox.setPartialName("SampleFieldE");
            textBox.setDefaultAppearance("/Helv 12 Tf 0 1 0 rg");
            acroForm.getFields().add(textBox);


            COSDictionary anonTextBox1 = new COSDictionary();
            anonTextBox1.setItem(COSName.PARENT, textBox);
            anonTextBox1.setString(COSName.DA, "/Helv 12 Tf 0 0 1 rg");

            widget1 = new PDAnnotationWidget();
            PDRectangle rectE1 = new PDRectangle(50, 350, 250, 50);
            widget1.setRectangle(rectE1);
            widget1.setPage(page1);
            widget1.getCOSObject().setItem(COSName.PARENT, anonTextBox1);
            page1.getAnnotations().add(widget1);

            widget2 = new PDAnnotationWidget();
            widget2.setRectangle(rectE1);
            widget2.setPage(page2);
            widget2.getCOSObject().setItem(COSName.PARENT, anonTextBox1);
            page2.getAnnotations().add(widget2);

            COSArray kids = new COSArray();
            kids.add(widget1.getCOSObject());
            kids.add(widget2.getCOSObject());
            anonTextBox1.setItem(COSName.KIDS, kids);

            PDTextField tempField = new PDTextField(acroForm);
            tempField.setDefaultAppearance("/Helv 12 Tf 0 0 1 rg");
            tempField.getWidgets().get(0).setRectangle(rectE1);
            tempField.setValue("E");
            PDAppearanceDictionary appearance = tempField.getWidgets().get(0).getAppearance();
            widget1.setAppearance(appearance);
            widget2.setAppearance(appearance);


            COSDictionary anonTextBox2 = new COSDictionary();
            anonTextBox2.setItem(COSName.PARENT, textBox);
            anonTextBox2.setString(COSName.DA, "/Helv 12 Tf 1 0 0 rg");

            widget1 = new PDAnnotationWidget();
            PDRectangle rectE2 = new PDRectangle(350, 350, 200, 50);
            widget1.setRectangle(rectE2);
            widget1.setPage(page2);
            widget1.getCOSObject().setItem(COSName.PARENT, anonTextBox2);
            page2.getAnnotations().add(widget1);

            widget2 = new PDAnnotationWidget();
            widget2.setRectangle(rectE2);
            widget2.setPage(page1);
            widget2.getCOSObject().setItem(COSName.PARENT, anonTextBox2);
            page1.getAnnotations().add(widget2);

            kids = new COSArray();
            kids.add(widget2.getCOSObject());
            kids.add(widget1.getCOSObject());
            anonTextBox2.setItem(COSName.KIDS, kids);

            kids = new COSArray();
            kids.add(anonTextBox1);
            kids.add(anonTextBox2);
            textBox.getCOSObject().setItem(COSName.KIDS, kids);

            tempField = new PDTextField(acroForm);
            tempField.setDefaultAppearance("/Helv 12 Tf 1 0 0 rg");
            tempField.getWidgets().get(0).setRectangle(rectE2);
            tempField.setValue("E");
            appearance = tempField.getWidgets().get(0).getAppearance();
            widget1.setAppearance(appearance);
            widget2.setAppearance(appearance);


            textBox.getCOSObject().setString(COSName.V, "E");


            document.save(new File(RESULT_FOLDER, "duplicateFields.pdf"));
        }

        try (   InputStream stream = new FileInputStream(new File(RESULT_FOLDER, "duplicateFields.pdf"));
                PDDocument document = Loader.loadPDF(stream)  )
        {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            for (PDField field : acroForm.getFieldTree())
            {
                System.out.println(field.getFullyQualifiedName() + " (" + field.getClass().getSimpleName() + ")");
                if (field instanceof PDTerminalField)
                {
                    for (PDAnnotationWidget widget: ((PDTerminalField)field).getWidgets())
                    {
                        System.out.println("\t" + widget.getAnnotationName());
                    }
                    field.setValue("changed " + field.getValueAsString());
                }
            }

            document.save(new File(RESULT_FOLDER, "duplicateFieldsChanged.pdf"));
        }
    }

}
