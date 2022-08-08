package mkl.testarea.pdfbox2.sign;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.action.PDFormFieldAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDListBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class CreateUnsignablePdf {
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/56393909/why-form-cannot-be-signed-error-message-due-to-current-status">
     * why form cannot be signed (error message “due to current status”)
     * </a>
     * <p>
     * Indeed, the PDF created by the OP triggers a "The document cannot be signed
     * in its current state. Please save the document, close it, reopen it, and then
     * attempt to sign again." The "current state" might be one where JavaScript
     * replaced event handlers. In the document at hand, for example, the domicilation
     * event handler defines a function fft() and sets it as the new event handler.
     * But this causes the definition of the function fft() to be only present in
     * the PDF viewer memory and nowhere in the PDF anymore. A signed (and in the
     * process of signing saved) PDF, therefore, would behave differently than the
     * PDF present for signing.
     * </p>
     * 
     * @see #testCreateLikeJSImproved()
     */
    @Test
    public void testCreateLikeJS() throws IOException {
     // Create a new document with an empty page.
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);



        String javaScript = "var now = util.printd('yyyy-mm-dd', new Date());"
                + "var ndf = this.getField('newDateField');"
                + "ndf.value = now;"
        //        + "this.getField('signatureField').display=display.hidden;"
        //        + "var formReady = false;"
                + "var anacredit = { '-': [['-', '-']], "
                               + "  'Luxembourg': [[ '-', '-'], ['LU01 Entreprise individuelle', 'LU01'],[ 'LU06 Société anonyme', 'LU06'] ,['LU14 Société civile','LU14']] , "
                               + " 'Germany': [[ '-', '-'], ['DE201 Aktiengesellschaft', 'DE201'], ['DE602 Eingetragener Verein', 'DE602'], ['DE205 Investmentaktiengesellschaft', 'DE205']],  "
                               + " 'Greece': [[ '-', '-'], ['GR906 Εταιρία Περιορισμένης Ευθύνης/Etería Periorisménis Euthínis', 'GR906'], ['GR912 Κοινοπραξία/Kinopraxia', 'GR912'], ['GR999 Λοιπά/Lipa', 'GR999']]  };";


        // Create an action as JavaScript action
        PDActionJavaScript jsAction = new PDActionJavaScript();
        jsAction.setAction(javaScript);

        // Set the action to be executed when the document is opened
        document.getDocumentCatalog().setOpenAction(jsAction);


        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), font);


        PDDocumentCatalog pdCatalog = document.getDocumentCatalog();

        PDAcroForm pdAcroForm = new PDAcroForm(document);
        pdCatalog.setAcroForm(pdAcroForm);

        pdAcroForm.setDefaultResources(resources);

        String defaultAppearanceString = "/Helv 0 Tf 0 g";
        pdAcroForm.setDefaultAppearance(defaultAppearanceString);


        PDTextField newDateField = new PDTextField(pdAcroForm);
        newDateField.setPartialName("newDateField");

        defaultAppearanceString = "/Helv 12 Tf 0 g";
        newDateField.setDefaultAppearance(defaultAppearanceString);
        pdAcroForm.getFields().add(newDateField);

        PDAnnotationWidget widget = newDateField.getWidgets().get(0);
        PDRectangle rect = new PDRectangle(50, 450, 500, 15);
        widget.setRectangle(rect);
        widget.setPage(page);

        // make sure the annotation is visible on screen and paper
        widget.setPrinted(true);

        // Add the annotation to the page
        page.getAnnotations().add(widget);
        //newDateField.setValue("value in newly created text field");

        //textBox.setActions(fieldActions);


        PDListBox domicilation = new PDListBox(pdAcroForm);
        domicilation.setPartialName("domicilation");

        List<String> displayList = Arrays.asList("-", "Germany", "Luxembourg", "Greece");
        List<String> exportList = Arrays.asList("-", "Germany", "Luxembourg", "Greece");

        domicilation.setOptions(exportList, displayList);
        defaultAppearanceString = "/Helv 12 Tf 0 g";
        domicilation.setDefaultAppearance(defaultAppearanceString);

        pdAcroForm.getFields().add(domicilation);

        String jsListBox0 =
                "var f = this.getField('domicilation');"
                        + "var r = this.getField('legalForm');"
                        + " console.println('domicilation ' + f.value + 'legalForm' + r.value);"
                        + "f.setAction('Keystroke', 'fft();');"
                        + "function fft() { if (event.willCommit)"
                        + "{  console.println('domiciliation' + event.change + ' ' + event.value); "
                        + "r.setItems( anacredit[event.value] );"
                        + "f.value=event.value) ; ndf.value= event.value;"
                        + " }}";
                      //  + "r.value='-'; formReady=false; }}";


        PDFormFieldAdditionalActions fieldActions = new PDFormFieldAdditionalActions();
        PDActionJavaScript jsKeystrokeAction = new PDActionJavaScript();
        //jsKeystrokeAction.setAction("app.alert(\"On 'keystroke' action\")");
        jsKeystrokeAction.setAction(jsListBox0);
        fieldActions.setK(jsKeystrokeAction);

        domicilation.setActions(fieldActions);


        PDAnnotationWidget widget2 = domicilation.getWidgets().get(0);
        PDRectangle rect2 = new PDRectangle(50, 380, 500, 50);
        widget2.setRectangle(rect2);
        widget2.setPage(page);

        // make sure the annotation is visible on screen and paper
        widget2.setPrinted(true);

        //PDAnnotationAdditionalActions annotationActions = new PDAnnotationAdditionalActions();

        // Add the annotation to the page
        page.getAnnotations().add(widget2);

        domicilation.setValue("-");


        PDListBox legalForm = new PDListBox(pdAcroForm);
        legalForm.setPartialName("legalForm");

        List<String> displayList2 = Arrays.asList("-");
        List<String> exportList2 = Arrays.asList(" ");

        legalForm.setOptions(exportList2, displayList2);
        defaultAppearanceString = "/Helv 12 Tf 0 g";
        legalForm.setDefaultAppearance(defaultAppearanceString);

        pdAcroForm.getFields().add(legalForm);



        PDAnnotationWidget widget3 = legalForm.getWidgets().get(0);
        PDRectangle rect3 = new PDRectangle(50, 310, 500, 50);
        widget3.setRectangle(rect3);
        widget3.setPage(page);

        // make sure the annotation is visible on screen and paper
        widget3.setPrinted(true);

        String jsListBox2 = "var lb = this.getField('legalForm'); "
                + "console.println('in legalForm action ' + lb.value);"

                + "lb.setAction('Keystroke', 'fft2();');"
                + "function fft2() { if (event.willCommit)"
                + "{ console.println('in legalForm action ' + event.change + ' ' + event.value);"
                + "lb.value=event.value; ndf.value= event.value;}}";

        //  + "console.println(formReady);"
          //      + "lb.setAction('Keystroke', 'flb();');"
            //    + "function flb() { if (event.willCommit)"
              //  + "{ console.println('in listbox action'); console.println(event.value); "
           //     + "if (lb.value == '-')  formReady= false; else formReady=true; "
             //   + "if (formReady) this.getField('signatureField').display=display.visible; "
               // + "else this.getField('signatureField').display=display.hidden; }}" +
            //   + " lb.value=event.value; ndf.value=event.value; }}" ;
        // "f2.setAction('Keystroke', 'fft2();');function fft2() { if (!event.willCommit) { console.println(event.change); r2.value = event.change; }}";

         PDFormFieldAdditionalActions fieldActions2 = new PDFormFieldAdditionalActions();  // usable only for .setK, not for .setU
        //PDAnnotationAdditionalActions annotationActions = new PDAnnotationAdditionalActions();
        PDActionJavaScript jsKeyStrokeAction = new PDActionJavaScript();
        //jsKeystrokeAction.setAction("app.alert(\"On 'keystroke' action\")");
        jsKeyStrokeAction.setAction(jsListBox2);
        fieldActions2.setK(jsKeyStrokeAction);

        legalForm.setActions(fieldActions2);

        //widget3.setActions(annotationActions);*/

        //PDAnnotationAdditionalActions annotationActions = new PDAnnotationAdditionalActions();

        PDFormFieldAdditionalActions listboxAction2 = new PDFormFieldAdditionalActions();


        // Add the annotation to the page
        page.getAnnotations().add(widget3);

        legalForm.setValue("-");


        PDRectangle rect4 = new PDRectangle(50, 150, 200, 50);

        PDAppearanceDictionary appearanceDictionary = new PDAppearanceDictionary();
        PDAppearanceStream appearanceStream = new PDAppearanceStream(document);
        appearanceStream.setBBox(rect4.createRetranslatedRectangle());
        appearanceStream.setResources(resources);
        appearanceDictionary.setNormalAppearance(appearanceStream);
        PDPageContentStream contentStream = new PDPageContentStream(document, appearanceStream);
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setNonStrokingColor(Color.LIGHT_GRAY);
        contentStream.setLineWidth(2);
        contentStream.addRect(0, 0, rect4.getWidth(), rect4.getHeight());
        contentStream.fill();
        contentStream.moveTo(1 * rect4.getHeight() / 4, 1 * rect4.getHeight() / 4);
        contentStream.lineTo(2 * rect4.getHeight() / 4, 3 * rect4.getHeight() / 4);
        contentStream.moveTo(1 * rect4.getHeight() / 4, 3 * rect4.getHeight() / 4);
        contentStream.lineTo(2 * rect4.getHeight() / 4, 1 * rect4.getHeight() / 4);
        contentStream.moveTo(3 * rect4.getHeight() / 4, 1 * rect4.getHeight() / 4);
        contentStream.lineTo(rect4.getWidth() - rect4.getHeight() / 4, 1 * rect4.getHeight() / 4);
        contentStream.stroke();
        contentStream.setNonStrokingColor(Color.DARK_GRAY);
        contentStream.beginText();
        contentStream.setFont(font, rect4.getHeight() / 5);
        contentStream.newLineAtOffset(3 * rect4.getHeight() / 4, -font.getBoundingBox().getLowerLeftY() * rect4.getHeight() / 5000);
        contentStream.showText("Customer");
        contentStream.endText();
        contentStream.close();

        PDSignatureField signatureField = new PDSignatureField(pdAcroForm);
        signatureField.setPartialName("signatureField");


        PDAnnotationWidget widget4 = signatureField.getWidgets().get(0);
        widget4.setAppearance(appearanceDictionary);
        widget4.setRectangle(rect4);
        widget4.setPage(page);



        page.getAnnotations().add(widget4);
        pdAcroForm.getFields().add(signatureField);


        document.save(new File(RESULT_FOLDER, "anacreditForm.pdf"));

        for (PDField pdField : pdAcroForm.getFields()) {
            System.out.println(pdField.getFullyQualifiedName() + " " + pdField.getFieldType() + " " + pdField.getValueAsString());
        }
        document.close();
    }

    /**
     * <a href="https://stackoverflow.com/questions/56393909/why-form-cannot-be-signed-error-message-due-to-current-status">
     * why form cannot be signed (error message “due to current status”)
     * </a>
     * <p>
     * In contrast to the OP's original code the JavaScript code here defines
     * the functions fft() and fft2() outside the original event handler. Thus,
     * even after triggering the original event handlers the definition of these
     * functions is still present in the PDF. Nonetheless this document also
     * triggers a "The document cannot be signed in its current state. Please
     * save the document, close it, reopen it, and then attempt to sign again."
     * message. Probably Adobe Reader does not check whether all code in question
     * is still defined somewhere in the PDF but automatically considers a PDF
     * with manipulated event listeners dirty...
     * </p>
     * 
     * @see #testCreateLikeJS()
     */
    @Test
    public void testCreateLikeJSImproved() throws IOException {
     // Create a new document with an empty page.
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);



        String javaScript = "var now = util.printd('yyyy-mm-dd', new Date());"
                + "var ndf = this.getField('newDateField');"
                + "ndf.value = now;"
        //        + "this.getField('signatureField').display=display.hidden;"
        //        + "var formReady = false;"
                + "var anacredit = { '-': [['-', '-']], "
                               + "  'Luxembourg': [[ '-', '-'], ['LU01 Entreprise individuelle', 'LU01'],[ 'LU06 Société anonyme', 'LU06'] ,['LU14 Société civile','LU14']] , "
                               + " 'Germany': [[ '-', '-'], ['DE201 Aktiengesellschaft', 'DE201'], ['DE602 Eingetragener Verein', 'DE602'], ['DE205 Investmentaktiengesellschaft', 'DE205']],  "
                               + " 'Greece': [[ '-', '-'], ['GR906 Εταιρία Περιορισμένης Ευθύνης/Etería Periorisménis Euthínis', 'GR906'], ['GR912 Κοινοπραξία/Kinopraxia', 'GR912'], ['GR999 Λοιπά/Lipa', 'GR999']]  };"
               + "function fft() { if (event.willCommit)"
               + "{  console.println('domiciliation' + event.change + ' ' + event.value); "
               + "r.setItems( anacredit[event.value] );"
               + "f.value=event.value ; ndf.value= event.value;"
               + " }}"
               + "function fft2() { if (event.willCommit)"
               + "{ console.println('in legalForm action ' + event.change + ' ' + event.value);"
               + "lb.value=event.value; ndf.value= event.value;}}";


        // Create an action as JavaScript action
        PDActionJavaScript jsAction = new PDActionJavaScript();
        jsAction.setAction(javaScript);

        // Set the action to be executed when the document is opened
        document.getDocumentCatalog().setOpenAction(jsAction);


        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), font);


        PDDocumentCatalog pdCatalog = document.getDocumentCatalog();

        PDAcroForm pdAcroForm = new PDAcroForm(document);
        pdCatalog.setAcroForm(pdAcroForm);

        pdAcroForm.setDefaultResources(resources);

        String defaultAppearanceString = "/Helv 0 Tf 0 g";
        pdAcroForm.setDefaultAppearance(defaultAppearanceString);


        PDTextField newDateField = new PDTextField(pdAcroForm);
        newDateField.setPartialName("newDateField");

        defaultAppearanceString = "/Helv 12 Tf 0 g";
        newDateField.setDefaultAppearance(defaultAppearanceString);
        pdAcroForm.getFields().add(newDateField);

        PDAnnotationWidget widget = newDateField.getWidgets().get(0);
        PDRectangle rect = new PDRectangle(50, 450, 500, 15);
        widget.setRectangle(rect);
        widget.setPage(page);

        // make sure the annotation is visible on screen and paper
        widget.setPrinted(true);

        // Add the annotation to the page
        page.getAnnotations().add(widget);
        //newDateField.setValue("value in newly created text field");

        //textBox.setActions(fieldActions);


        PDListBox domicilation = new PDListBox(pdAcroForm);
        domicilation.setPartialName("domicilation");

        List<String> displayList = Arrays.asList("-", "Germany", "Luxembourg", "Greece");
        List<String> exportList = Arrays.asList("-", "Germany", "Luxembourg", "Greece");

        domicilation.setOptions(exportList, displayList);
        defaultAppearanceString = "/Helv 12 Tf 0 g";
        domicilation.setDefaultAppearance(defaultAppearanceString);

        pdAcroForm.getFields().add(domicilation);

        String jsListBox0 =
                "var f = this.getField('domicilation');"
                        + "var r = this.getField('legalForm');"
                        + " console.println('domicilation ' + f.value + 'legalForm' + r.value);"
                        + "f.setAction('Keystroke', 'fft();');";
                      //  + "r.value='-'; formReady=false; }}";


        PDFormFieldAdditionalActions fieldActions = new PDFormFieldAdditionalActions();
        PDActionJavaScript jsKeystrokeAction = new PDActionJavaScript();
        //jsKeystrokeAction.setAction("app.alert(\"On 'keystroke' action\")");
        jsKeystrokeAction.setAction(jsListBox0);
        fieldActions.setK(jsKeystrokeAction);

        domicilation.setActions(fieldActions);


        PDAnnotationWidget widget2 = domicilation.getWidgets().get(0);
        PDRectangle rect2 = new PDRectangle(50, 380, 500, 50);
        widget2.setRectangle(rect2);
        widget2.setPage(page);

        // make sure the annotation is visible on screen and paper
        widget2.setPrinted(true);

        //PDAnnotationAdditionalActions annotationActions = new PDAnnotationAdditionalActions();

        // Add the annotation to the page
        page.getAnnotations().add(widget2);

        domicilation.setValue("-");


        PDListBox legalForm = new PDListBox(pdAcroForm);
        legalForm.setPartialName("legalForm");

        List<String> displayList2 = Arrays.asList("-");
        List<String> exportList2 = Arrays.asList(" ");

        legalForm.setOptions(exportList2, displayList2);
        defaultAppearanceString = "/Helv 12 Tf 0 g";
        legalForm.setDefaultAppearance(defaultAppearanceString);

        pdAcroForm.getFields().add(legalForm);



        PDAnnotationWidget widget3 = legalForm.getWidgets().get(0);
        PDRectangle rect3 = new PDRectangle(50, 310, 500, 50);
        widget3.setRectangle(rect3);
        widget3.setPage(page);

        // make sure the annotation is visible on screen and paper
        widget3.setPrinted(true);

        String jsListBox2 = "var lb = this.getField('legalForm'); "
                + "console.println('in legalForm action ' + lb.value);"

                + "lb.setAction('Keystroke', 'fft2();');";

        //  + "console.println(formReady);"
          //      + "lb.setAction('Keystroke', 'flb();');"
            //    + "function flb() { if (event.willCommit)"
              //  + "{ console.println('in listbox action'); console.println(event.value); "
           //     + "if (lb.value == '-')  formReady= false; else formReady=true; "
             //   + "if (formReady) this.getField('signatureField').display=display.visible; "
               // + "else this.getField('signatureField').display=display.hidden; }}" +
            //   + " lb.value=event.value; ndf.value=event.value; }}" ;
        // "f2.setAction('Keystroke', 'fft2();');function fft2() { if (!event.willCommit) { console.println(event.change); r2.value = event.change; }}";

         PDFormFieldAdditionalActions fieldActions2 = new PDFormFieldAdditionalActions();  // usable only for .setK, not for .setU
        //PDAnnotationAdditionalActions annotationActions = new PDAnnotationAdditionalActions();
        PDActionJavaScript jsKeyStrokeAction = new PDActionJavaScript();
        //jsKeystrokeAction.setAction("app.alert(\"On 'keystroke' action\")");
        jsKeyStrokeAction.setAction(jsListBox2);
        fieldActions2.setK(jsKeyStrokeAction);

        legalForm.setActions(fieldActions2);

        //widget3.setActions(annotationActions);*/

        //PDAnnotationAdditionalActions annotationActions = new PDAnnotationAdditionalActions();

        PDFormFieldAdditionalActions listboxAction2 = new PDFormFieldAdditionalActions();


        // Add the annotation to the page
        page.getAnnotations().add(widget3);

        legalForm.setValue("-");


        PDRectangle rect4 = new PDRectangle(50, 150, 200, 50);

        PDAppearanceDictionary appearanceDictionary = new PDAppearanceDictionary();
        PDAppearanceStream appearanceStream = new PDAppearanceStream(document);
        appearanceStream.setBBox(rect4.createRetranslatedRectangle());
        appearanceStream.setResources(resources);
        appearanceDictionary.setNormalAppearance(appearanceStream);
        PDPageContentStream contentStream = new PDPageContentStream(document, appearanceStream);
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setNonStrokingColor(Color.LIGHT_GRAY);
        contentStream.setLineWidth(2);
        contentStream.addRect(0, 0, rect4.getWidth(), rect4.getHeight());
        contentStream.fill();
        contentStream.moveTo(1 * rect4.getHeight() / 4, 1 * rect4.getHeight() / 4);
        contentStream.lineTo(2 * rect4.getHeight() / 4, 3 * rect4.getHeight() / 4);
        contentStream.moveTo(1 * rect4.getHeight() / 4, 3 * rect4.getHeight() / 4);
        contentStream.lineTo(2 * rect4.getHeight() / 4, 1 * rect4.getHeight() / 4);
        contentStream.moveTo(3 * rect4.getHeight() / 4, 1 * rect4.getHeight() / 4);
        contentStream.lineTo(rect4.getWidth() - rect4.getHeight() / 4, 1 * rect4.getHeight() / 4);
        contentStream.stroke();
        contentStream.setNonStrokingColor(Color.DARK_GRAY);
        contentStream.beginText();
        contentStream.setFont(font, rect4.getHeight() / 5);
        contentStream.newLineAtOffset(3 * rect4.getHeight() / 4, -font.getBoundingBox().getLowerLeftY() * rect4.getHeight() / 5000);
        contentStream.showText("Customer");
        contentStream.endText();
        contentStream.close();

        PDSignatureField signatureField = new PDSignatureField(pdAcroForm);
        signatureField.setPartialName("signatureField");


        PDAnnotationWidget widget4 = signatureField.getWidgets().get(0);
        widget4.setAppearance(appearanceDictionary);
        widget4.setRectangle(rect4);
        widget4.setPage(page);



        page.getAnnotations().add(widget4);
        pdAcroForm.getFields().add(signatureField);


        document.save(new File(RESULT_FOLDER, "anacreditForm-Improved.pdf"));

        for (PDField pdField : pdAcroForm.getFields()) {
            System.out.println(pdField.getFullyQualifiedName() + " " + pdField.getFieldType() + " " + pdField.getValueAsString());
        }
        document.close();
    }
}
