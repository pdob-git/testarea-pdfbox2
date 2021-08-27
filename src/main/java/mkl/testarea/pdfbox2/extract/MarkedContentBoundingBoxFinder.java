package mkl.testarea.pdfbox2.extract;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 * <a href="https://stackoverflow.com/questions/68565744/pdfbox-how-to-determine-bounding-box-of-vector-figure-path-shape">
 * PDFBox: how to determine bounding box of vector figure (path shape)
 * </a>
 * <p>
 * This {@link PDFGraphicsStreamEngine} subclass allows determining the
 * bounding boxes of marked content according to their instructions
 * inside.
 * </p>
 * <p>
 * It is derived from the {@link BoundingBoxFinder} and, therefore, also
 * is subject to the restrictions and limitations of that class.
 * </p>
 * 
 * @author mkl
 */
public class MarkedContentBoundingBoxFinder extends BoundingBoxFinder {
    public MarkedContentBoundingBoxFinder(PDPage page) {
        super(page);
        contents.add(content);
    }

    @Override
    public void processPage(PDPage page) throws IOException {
        super.processPage(page);
        endMarkedContentSequence();
    }

    @Override
    public void beginMarkedContentSequence(COSName tag, COSDictionary properties) {
        MarkedContent current = contents.getLast();
        if (rectangle != null) {
            if (current.boundingBox != null)
                add(current.boundingBox);
            current.boundingBox = rectangle;
        }
        rectangle = null;
        MarkedContent newContent = new MarkedContent(tag, properties);
        contents.addLast(newContent);
        current.children.add(newContent);

        super.beginMarkedContentSequence(tag, properties);
    }

    @Override
    public void endMarkedContentSequence() {
        MarkedContent current = contents.removeLast();
        if (rectangle != null) {
            if (current.boundingBox != null)
                add(current.boundingBox);
            current.boundingBox = (Rectangle2D) rectangle.clone();
        } else if (current.boundingBox != null)
            rectangle = (Rectangle2D) current.boundingBox.clone();

        super.endMarkedContentSequence();
    }

    public static class MarkedContent {
        public MarkedContent(COSName tag, COSDictionary properties) {
            this.tag = tag;
            this.properties = properties;
        }

        public final COSName tag;
        public final COSDictionary properties;
        public final List<MarkedContent> children = new ArrayList<>();
        public Rectangle2D boundingBox = null;
    }

    public final MarkedContent content = new MarkedContent(COSName.DOCUMENT, null);
    public final Deque<MarkedContent> contents = new ArrayDeque<>();
}
