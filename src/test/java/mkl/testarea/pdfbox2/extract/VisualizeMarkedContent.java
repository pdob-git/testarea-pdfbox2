package mkl.testarea.pdfbox2.extract;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureNode;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDType3CharProc;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.font.PDVectorFont;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.text.PDFMarkedContentExtractor;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class VisualizeMarkedContent {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/59192443/get-tags-related-bboxs-even-though-there-is-no-attributes-a-in-document-cata">
     * Get tag's related BBox's even though there is no attributes (/A in document catalog structure) related to Layout in PDFBox?
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1_-tuWuReaTvrDsqQwldTnPYrMHSpXIWp/view?usp=sharing">
     * res_multipage.pdf
     * </a>
     * <p>
     * This test shows how to determine the bounding box of text content in
     * structure elements.
     * </p>
     */
    @Test
    public void testVisualizeResMultipage() throws IOException {
        visualize("res_multipage.pdf", "res_multipage-withBoxes.pdf");
    }

    /**
     * <a href="https://stackoverflow.com/questions/54956720/how-to-replace-a-space-with-a-word-while-extract-the-data-from-pdf-using-pdfbox">
     * How to replace a space with a word while extract the data from PDF using PDFBox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=10ZkdPlGWzMJeahwnQPzE6V7s09d1nvwq">
     * test.pdf
     * </a> as "testWPhromma.pdf"
     * <p>
     * This test shows how to determine the bounding box of text content in
     * structure elements.
     * </p>
     */
    @Test
    public void testVisualizeTestWPhromma() throws IOException {
        visualize("testWPhromma.pdf", "testWPhromma-withBoxes.pdf");
    }

    /**
     * This method outputs an XML'ish representation of the structure
     * tree plus text extracted for it and additionally creates a PDF
     * with frames representing the bounding boxes of the text inside
     * the structure elements.
     */
    public void visualize(String resourceName, String resultName) throws IOException {
        System.out.printf("\n\n===\n%s\n===\n", resourceName);
        try (   InputStream resource = getClass().getResourceAsStream(resourceName)) {
            PDDocument document = Loader.loadPDF(resource);

            Map<PDPage, Map<Integer, PDMarkedContent>> markedContents = new HashMap<>();

            for (PDPage page : document.getPages()) {
                PDFMarkedContentExtractor extractor = new PDFMarkedContentExtractor();
                extractor.processPage(page);

                Map<Integer, PDMarkedContent> theseMarkedContents = new HashMap<>();
                markedContents.put(page, theseMarkedContents);
                for (PDMarkedContent markedContent : extractor.getMarkedContents()) {
                    addToMap(theseMarkedContents, markedContent);
                }
            }

            PDStructureNode root = document.getDocumentCatalog().getStructureTreeRoot();
            Map<PDPage, PDPageContentStream> visualizations = new HashMap<>();
            showStructure(document, root, markedContents, visualizations);
            for (PDPageContentStream canvas : visualizations.values())
                canvas.close();

            document.save(new File(RESULT_FOLDER, resultName));
        }
    }

    /** Helper for {@link #visualize(String, String)} */
    void addToMap(Map<Integer, PDMarkedContent> theseMarkedContents, PDMarkedContent markedContent) {
        theseMarkedContents.put(markedContent.getMCID(), markedContent);
        for (Object object : markedContent.getContents()) {
            if (object instanceof PDMarkedContent) {
                addToMap(theseMarkedContents, (PDMarkedContent)object);
            }
        }
    }

    int index = 0;

    /**
     * This method prints and visualizes the given structure element
     * node and recursively also its descendants. It is used by
     * {@link #visualize(String, String)}.
     */
    Map<PDPage, Rectangle2D> showStructure(PDDocument document, PDStructureNode node, Map<PDPage, Map<Integer, PDMarkedContent>> markedContents, Map<PDPage, PDPageContentStream> visualizations) throws IOException {
        Map<PDPage, Rectangle2D> boxes = null;
        String structType = null;
        PDPage page = null;
        if (node instanceof PDStructureElement) {
            PDStructureElement element = (PDStructureElement) node;
            structType = element.getStructureType();
            page = element.getPage();
        }
        Map<Integer, PDMarkedContent> theseMarkedContents = markedContents.get(page);
        int indexHere = index++;
        System.out.printf("<%s index=%s>\n", structType, indexHere);
        for (Object object : node.getKids()) {
            if (object instanceof COSArray) {
                for (COSBase base : (COSArray) object) {
                    if (base instanceof COSDictionary) {
                        boxes = union(boxes, showStructure(document, PDStructureNode.create((COSDictionary) base), markedContents, visualizations));
                    } else if (base instanceof COSNumber) {
                        boxes = union(boxes, page, showContent(((COSNumber)base).intValue(), theseMarkedContents));
                    } else {
                        System.out.printf("?%s\n", base);
                    }
                }
            } else if (object instanceof PDStructureNode) {
                boxes = union(boxes, showStructure(document, (PDStructureNode) object, markedContents, visualizations));
            } else if (object instanceof Integer) {
                boxes = union(boxes, page, showContent((Integer)object, theseMarkedContents));
            } else {
                System.out.printf("?%s\n", object);
            }

        }
        System.out.printf("</%s>\n", structType);
        if (boxes != null) {
            Color color = new Color((int)(Math.random() * 256), (int)(Math.random() * 256), (int)(Math.random() * 256));

            for (Map.Entry<PDPage, Rectangle2D> entry : boxes.entrySet()) {
                page = entry.getKey();
                Rectangle2D box = entry.getValue();
                if (box == null)
                    continue;

                PDPageContentStream canvas = visualizations.get(page);
                if (canvas == null) {
                    canvas = new PDPageContentStream(document, page, AppendMode.APPEND, false, true);
                    visualizations.put(page, canvas);
                    canvas.setFont(new PDType1Font(FontName.HELVETICA), 11);
                }
                canvas.saveGraphicsState();
                canvas.setStrokingColor(color);
                canvas.addRect((float)box.getMinX(), (float)box.getMinY(), (float)box.getWidth(), (float)box.getHeight());
                canvas.stroke();
                canvas.setNonStrokingColor(color);
                canvas.beginText();
                canvas.newLineAtOffset((float)((box.getMinX() + box.getMaxX())/2), (float)box.getMaxY());
                canvas.showText(String.format("<%s index=%s>", structType, indexHere));
                canvas.endText();
                canvas.restoreGraphicsState();
            }
        }
        return boxes;
    }

    /**
     * This method shows the text content for a MCID and determines its
     * bounding box. It also recurses.
     */
    Rectangle2D showContent(int mcid, Map<Integer, PDMarkedContent> theseMarkedContents) throws IOException {
        Rectangle2D box = null;
        PDMarkedContent markedContent = theseMarkedContents != null ? theseMarkedContents.get(mcid) : null;
        List<Object> contents = markedContent != null ? markedContent.getContents() : Collections.emptyList();
        StringBuilder textContent =  new StringBuilder();
        for (Object object : contents) {
            if (object instanceof TextPosition) {
                TextPosition textPosition = (TextPosition)object;
                textContent.append(textPosition.getUnicode());

                int[] codes = textPosition.getCharacterCodes();
                if (codes.length != 1) {
                    System.out.printf("<!-- text position with unexpected number of codes: %d -->", codes.length);
                } else {
                    box = union(box, calculateGlyphBounds(textPosition.getTextMatrix(), textPosition.getFont(), codes[0]).getBounds2D());
                }
            } else if (object instanceof PDMarkedContent) {
                PDMarkedContent thisMarkedContent = (PDMarkedContent) object;
                box = union(box, showContent(thisMarkedContent.getMCID(), theseMarkedContents));
            } else {
                textContent.append("?" + object);
            }
        }
        System.out.printf("%s\n", textContent);
        return box;
    }

    /**
     * This method determines per page the union of the rectangles in the
     * given maps.
     */
    @SafeVarargs
    final Map<PDPage, Rectangle2D> union(Map<PDPage, Rectangle2D>... maps) {
        Map<PDPage, Rectangle2D> result = null;
        for (Map<PDPage, Rectangle2D> map : maps) {
            if (map != null) {
                if (result != null) {
                    for (Map.Entry<PDPage, Rectangle2D> entry : map.entrySet()) {
                        PDPage page = entry.getKey();
                        Rectangle2D rectangle = union(result.get(page), entry.getValue());
                        if (rectangle != null)
                            result.put(page, rectangle);
                    }
                } else {
                    result = map;
                }
            }
        }
        return result;
    }

    /**
     * This method determines the union of the current rectangle on the
     * given map and the given rectangle.
     */
    Map<PDPage, Rectangle2D> union(Map<PDPage, Rectangle2D> map, PDPage page, Rectangle2D rectangle) {
        if (map == null)
            map = new HashMap<>();
        map.put(page, union(map.get(page), rectangle));
        return map;
    }

    /**
     * This method determines the union of the given rectangles.
     */
    Rectangle2D union(Rectangle2D... rectangles)
    {
        Rectangle2D box = null;
        for (Rectangle2D rectangle : rectangles) {
            if (rectangle != null) {
                if (box != null)
                    box.add(rectangle);
                else
                    box = rectangle;
            }
        }
        return box;
    }

    /** @see org.apache.pdfbox.examples.util.DrawPrintTextLocations#calculateGlyphBounds(Matrix, PDFont, int) */
    // this calculates the real (except for type 3 fonts) individual glyph bounds
    private Shape calculateGlyphBounds(Matrix textRenderingMatrix, PDFont font, int code) throws IOException
    {
        GeneralPath path = null;
        AffineTransform at = textRenderingMatrix.createAffineTransform();
        at.concatenate(font.getFontMatrix().createAffineTransform());
        if (font instanceof PDType3Font)
        {
            // It is difficult to calculate the real individual glyph bounds for type 3 fonts
            // because these are not vector fonts, the content stream could contain almost anything
            // that is found in page content streams.
            PDType3Font t3Font = (PDType3Font) font;
            PDType3CharProc charProc = t3Font.getCharProc(code);
            if (charProc != null)
            {
                BoundingBox fontBBox = t3Font.getBoundingBox();
                PDRectangle glyphBBox = charProc.getGlyphBBox();
                if (glyphBBox != null)
                {
                    // PDFBOX-3850: glyph bbox could be larger than the font bbox
                    glyphBBox.setLowerLeftX(Math.max(fontBBox.getLowerLeftX(), glyphBBox.getLowerLeftX()));
                    glyphBBox.setLowerLeftY(Math.max(fontBBox.getLowerLeftY(), glyphBBox.getLowerLeftY()));
                    glyphBBox.setUpperRightX(Math.min(fontBBox.getUpperRightX(), glyphBBox.getUpperRightX()));
                    glyphBBox.setUpperRightY(Math.min(fontBBox.getUpperRightY(), glyphBBox.getUpperRightY()));
                    path = glyphBBox.toGeneralPath();
                }
            }
        }
        else if (font instanceof PDVectorFont)
        {
            PDVectorFont vectorFont = (PDVectorFont) font;
            path = vectorFont.getPath(code);

            if (font instanceof PDTrueTypeFont)
            {
                PDTrueTypeFont ttFont = (PDTrueTypeFont) font;
                int unitsPerEm = ttFont.getTrueTypeFont().getHeader().getUnitsPerEm();
                at.scale(1000d / unitsPerEm, 1000d / unitsPerEm);
            }
            if (font instanceof PDType0Font)
            {
                PDType0Font t0font = (PDType0Font) font;
                if (t0font.getDescendantFont() instanceof PDCIDFontType2)
                {
                    int unitsPerEm = ((PDCIDFontType2) t0font.getDescendantFont()).getTrueTypeFont().getHeader().getUnitsPerEm();
                    at.scale(1000d / unitsPerEm, 1000d / unitsPerEm);
                }
            }
        }
        else if (font instanceof PDSimpleFont)
        {
            PDSimpleFont simpleFont = (PDSimpleFont) font;

            // these two lines do not always work, e.g. for the TT fonts in file 032431.pdf
            // which is why PDVectorFont is tried first.
            String name = simpleFont.getEncoding().getName(code);
            path = simpleFont.getPath(name);
        }
        else
        {
            // shouldn't happen, please open issue in JIRA
            System.out.println("Unknown font class: " + font.getClass());
        }
        if (path == null)
        {
            return null;
        }
        return at.createTransformedShape(path.getBounds2D());
    }
}
