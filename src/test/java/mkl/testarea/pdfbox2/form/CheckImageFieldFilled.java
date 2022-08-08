package mkl.testarea.pdfbox2.form;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDPushButton;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author mkl
 */
public class CheckImageFieldFilled {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/56959790/how-do-i-find-which-image-field-in-pdf-has-image-inserted-and-which-one-has-no-i">
     * How do I find which image field in PDF has image inserted and which one has no images attached using PDFbox 1.8.11?
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/g2wqm8ipsp8t8l5/GSA%20500%20PDF_v4.pdf?dl=0">
     * GSA 500 PDF_v4.pdf
     * </a>
     * <p>
     * This test shows how to check in the XFA XML whether a given image
     * field is set. 
     * </p>
     * @see #isFieldFilledXfa(Document, String)
     */
    @Test
    public void testCheckXfaGsa500Pdf_v4() throws IOException, ParserConfigurationException, SAXException {
        try (   InputStream resource = getClass().getResourceAsStream("GSA 500 PDF_v4.pdf");
                PDDocument document = PDDocument.load(resource);    ) {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            Document xfaDom = acroForm.getXFA().getDocument();

            System.out.println("Filled image fields from ImageField1..ImageField105:");
            for (int i=1; i < 106; i++) {
                if (isFieldFilledXfa(xfaDom, "ImageField" + i)) {
                    System.out.printf("* ImageField%d\n", i);
                }
            }
        }
    }

    /** @see #testCheckXfaGsa500Pdf_v4() */
    boolean isFieldFilledXfa(Document xfaDom, String fieldName) {
        NodeList fieldElements = xfaDom.getElementsByTagName(fieldName);
        for (int i = 0; i < fieldElements.getLength(); i++) {
            Node node = fieldElements.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                if (element.getAttribute("xfa:contentType").startsWith("image/")) {
                    return element.getTextContent().length() > 0;
                }
            }
        }
        return false;
    }

    /**
     * <a href="https://stackoverflow.com/questions/56959790/how-do-i-find-which-image-field-in-pdf-has-image-inserted-and-which-one-has-no-i">
     * How do I find which image field in PDF has image inserted and which one has no images attached using PDFbox 1.8.11?
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/g2wqm8ipsp8t8l5/GSA%20500%20PDF_v4.pdf?dl=0">
     * GSA 500 PDF_v4.pdf
     * </a>
     * <p>
     * This test shows how to check in the AcroForm appearances whether a given image
     * field is set. 
     * </p>
     * @see #isFieldFilledAcroForm(PDAcroForm, String)
     */
    @Test
    public void testCheckAcroFormGsa500Pdf_v4() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("GSA 500 PDF_v4.pdf");
                PDDocument document = PDDocument.load(resource);    ) {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();

            System.out.println("Filled image fields (AcroForm) from ImageField1..ImageField105:");
            for (int i=1; i < 106; i++) {
                if (isFieldFilledAcroForm(acroForm, "ImageField" + i + "[0]")) {
                    System.out.printf("* ImageField%d\n", i);
                }
            }
        }
    }

    /** @see #testCheckAcroFormGsa500Pdf_v4() */
    boolean isFieldFilledAcroForm(PDAcroForm acroForm, String fieldName) throws IOException {
        for (PDField field : acroForm.getFieldTree()) {
            if (field instanceof PDPushButton && fieldName.equals(field.getPartialName())) {
                for (final PDAnnotationWidget widget : field.getWidgets()) {
                    WidgetImageChecker checker = new WidgetImageChecker(widget);
                    if (checker.hasImages())
                        return true;
                }
            }
        }
        return false;
    }

    static class WidgetImageChecker extends PDFGraphicsStreamEngine
    {
        WidgetImageChecker(PDAnnotationWidget widget) {
            super(widget.getPage());
            this.widget = widget;
        }

        boolean hasImages() throws IOException {
            count = 0;
            PDAppearanceStream normalAppearance = widget.getNormalAppearanceStream();
            processChildStream(normalAppearance, widget.getPage());
            return count != 0;
        }

        @Override
        public void drawImage(PDImage pdImage) throws IOException {
            count++;
        }

        @Override
        public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException { }

        @Override
        public void clip(int windingRule) throws IOException { }

        @Override
        public void moveTo(float x, float y) throws IOException {  }

        @Override
        public void lineTo(float x, float y) throws IOException { }

        @Override
        public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException {  }

        @Override
        public Point2D getCurrentPoint() throws IOException { return null; }

        @Override
        public void closePath() throws IOException { }

        @Override
        public void endPath() throws IOException { }

        @Override
        public void strokePath() throws IOException { }

        @Override
        public void fillPath(int windingRule) throws IOException { }

        @Override
        public void fillAndStrokePath(int windingRule) throws IOException { }

        @Override
        public void shadingFill(COSName shadingName) throws IOException { }

        final PDAnnotationWidget widget;
        int count = 0;
    } 
}
