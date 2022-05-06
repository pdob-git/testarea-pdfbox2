package mkl.testarea.pdfbox2.extract;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType3CharProc;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.font.PDVectorFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

/**
 * <a href="https://stackoverflow.com/questions/72086776/find-blank-spacerectangle-for-signature-field-using-pdfbox">
 * Find blank space(rectangle) for signature field using PDFBox
 * </a>
 * <p>
 * This is a port of the iText <code>FreeSpaceFinderExt</code> render listener
 * to PDFBox as a special {@link PDFGraphicsStreamEngine}. The iText render
 * listener has been developed in response to the stack overflow question
 * </p>
 * <a href="http://stackoverflow.com/questions/26464324/how-can-i-can-insert-an-image-or-stamp-on-a-pdf-where-there-is-free-space-availa">
 * How can I can insert an image or stamp on a pdf where there is free space available like a density scanner
 * </a>
 * 
 * @author mkl
 */
public class FreeSpaceFinder extends PDFGraphicsStreamEngine {
    //
    // constructors
    //
    public FreeSpaceFinder(PDPage page, float minWidth, float minHeight) {
        this(page, page.getCropBox().toGeneralPath().getBounds2D(), minWidth, minHeight);
    }

    public FreeSpaceFinder(PDPage page, Rectangle2D initialBox, float minWidth, float minHeight) {
        this(page, Collections.singleton(initialBox), minWidth, minHeight);
    }

    public FreeSpaceFinder(PDPage page, Collection<Rectangle2D> initialBoxes, float minWidth, float minHeight) {
        super(page);

        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.freeSpaces = initialBoxes;
    }

    //
    // Result
    //
    public Collection<Rectangle2D> getFreeSpaces() {
        return freeSpaces;
    }

    //
    // Text
    //
    @Override
    protected void showGlyph(Matrix textRenderingMatrix, PDFont font, int code, Vector displacement)
            throws IOException {
        super.showGlyph(textRenderingMatrix, font, code, displacement);
        Shape shape = calculateGlyphBounds(textRenderingMatrix, font, code);
        if (shape != null) {
            Rectangle2D rect = shape.getBounds2D();
            remove(rect);
        }
    }

    /**
     * Copy of <code>org.apache.pdfbox.examples.util.DrawPrintTextLocations.calculateGlyphBounds(Matrix, PDFont, int)</code>.
     */
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

    //
    // Bitmaps
    //
    @Override
    public void drawImage(PDImage pdImage) throws IOException {
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        Rectangle2D unitSquare = new Rectangle2D.Float(0, 0, 1, 1);
        Path2D path = new Path2D.Float(unitSquare);
        path.transform(ctm.createAffineTransform());
        remove(path.getBounds2D());
    }

    //
    // Paths
    //
    @Override
    public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException {
        currentPath.moveTo(p0.getX(), p0.getY());
        currentPath.lineTo(p1.getX(), p1.getY());
        currentPath.lineTo(p2.getX(), p2.getY());
        currentPath.lineTo(p3.getX(), p3.getY());
        currentPath.closePath();
    }

    @Override
    public void clip(int windingRule) throws IOException {
        // ignore
    }

    @Override
    public void moveTo(float x, float y) throws IOException {
        currentPath.moveTo(x, y);
    }

    @Override
    public void lineTo(float x, float y) throws IOException {
        currentPath.lineTo(x, y);
    }

    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException {
        currentPath.curveTo(x1, y1, x2, y2, x3, y3);
    }

    @Override
    public Point2D getCurrentPoint() throws IOException {
        // To prevent many warnings...
        return new Point2D.Float();
    }

    @Override
    public void closePath() throws IOException {
        currentPath.closePath();
    }

    @Override
    public void endPath() throws IOException {
        currentPath = new Path2D.Float();
    }

    @Override
    public void strokePath() throws IOException {
        // Better only remove the bounding boxes of the constituting strokes
        remove(currentPath.getBounds2D());
        currentPath = new Path2D.Float();
    }

    @Override
    public void fillPath(int windingRule) throws IOException {
        // Better only remove the bounding boxes of the constituting subpaths
        remove(currentPath.getBounds2D());
        currentPath = new Path2D.Float();
    }

    @Override
    public void fillAndStrokePath(int windingRule) throws IOException {
        // Better only remove the bounding boxes of the constituting subpaths
        remove(currentPath.getBounds2D());
        currentPath = new Path2D.Float();
    }

    @Override
    public void shadingFill(COSName shadingName) throws IOException {
        // ignore
    }

    //
    // helpers
    //
    void remove(Rectangle2D usedSpace)
    {
        final double minX = usedSpace.getMinX();
        final double maxX = usedSpace.getMaxX();
        final double minY = usedSpace.getMinY();
        final double maxY = usedSpace.getMaxY();

        final Collection<Rectangle2D> newFreeSpaces = new ArrayList<Rectangle2D>();

        for (Rectangle2D freeSpace: freeSpaces)
        {
            final Collection<Rectangle2D> newFragments = new ArrayList<Rectangle2D>();
            if (freeSpace.intersectsLine(minX, minY, maxX, minY))
                newFragments.add(new Rectangle2D.Double(freeSpace.getMinX(), freeSpace.getMinY(), freeSpace.getWidth(), minY-freeSpace.getMinY()));
            if (freeSpace.intersectsLine(minX, maxY, maxX, maxY))
                newFragments.add(new Rectangle2D.Double(freeSpace.getMinX(), maxY, freeSpace.getWidth(), freeSpace.getMaxY() - maxY));
            if (freeSpace.intersectsLine(minX, minY, minX, maxY))
                newFragments.add(new Rectangle2D.Double(freeSpace.getMinX(), freeSpace.getMinY(), minX - freeSpace.getMinX(), freeSpace.getHeight()));
            if (freeSpace.intersectsLine(maxX, minY, maxX, maxY))
                newFragments.add(new Rectangle2D.Double(maxX, freeSpace.getMinY(), freeSpace.getMaxX() - maxX, freeSpace.getHeight()));
            if (newFragments.isEmpty())
            {
                add(newFreeSpaces, freeSpace);
            }
            else
            {
                for (Rectangle2D fragment: newFragments)
                {
                    if (fragment.getHeight() >= minHeight && fragment.getWidth() >= minWidth)
                    {
                        add(newFreeSpaces, fragment);
                    }
                }
            }
        }

        freeSpaces = newFreeSpaces;
    }

    void add(Collection<Rectangle2D> rectangles, Rectangle2D addition)
    {
        final Collection<Rectangle2D> toRemove = new ArrayList<Rectangle2D>();
        boolean isContained = false;
        for (Rectangle2D rectangle: rectangles)
        {
            if (rectangle.contains(addition))
            {
                isContained = true;
                break;
            }
            if (addition.contains(rectangle))
                toRemove.add(rectangle);
        }
        rectangles.removeAll(toRemove);
        if (!isContained)
            rectangles.add(addition);
    }

    //
    // hidden members
    //
    Path2D currentPath = new Path2D.Float();
    Collection<Rectangle2D> freeSpaces = null;
    final float minWidth;
    final float minHeight;
}
