package pl.pdob.pdftables.solution.boxfinderinternal;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import pl.pdob.pdftables.MainDemo;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class BoxProcessor {


    private final RectanglesProcessor rectanglesProcessor;

    public BoxProcessor() {
        this.rectanglesProcessor = new RectanglesProcessor();
    }

    public SortedMap<String,Box> generateBoxes(AllRectangles allRectangles){
        rectanglesProcessor.consolidateLists(allRectangles);
        SortedMap<String, Box> boxes = new TreeMap<>();
        List<ThinRectangle> horizontalRectangles = allRectangles.getHorizontalRectangles();
        List<ThinRectangle> verticalRectangles = allRectangles.getVerticalRectangles();

        if (!horizontalRectangles.isEmpty() && !verticalRectangles.isEmpty()){
            ThinRectangle top = horizontalRectangles.get(0);
            char rowLetter = 'A';
            for (int i = 1; i < horizontalRectangles.size(); i++,rowLetter++) {
                ThinRectangle bottom = horizontalRectangles.get(i);
                ThinRectangle left = verticalRectangles.get(0);
                int column = 1;
                for (int j = 1; j < verticalRectangles.size(); j++) {
                    ThinRectangle right = verticalRectangles.get(j);
                    String name = String.format("%s%s", rowLetter, column);
                    Box box = Box.ofThinRectangles(top, bottom, left, right);
                    boolean b = areValidBorders(top, bottom, left, right);
                    if (!box.isEmpty() && b) {
                        box.setKey(name);
                        boxes.put(box.getKey(),box);
                        left = right;
                        column++;
                    }
                }
                top = bottom;
            }
        }
        return boxes;

    }

    private boolean areValidBorders(ThinRectangle top, ThinRectangle bottom, ThinRectangle left, ThinRectangle right) {

        if (!isBordersGoodOrientation(top, bottom, left, right)) {
            return false;
        }

        //Top Values
        double topMinX = ThinRectangle.getMinX(top);
        double topMaxX = ThinRectangle.getMaxX(top);
        double topMinY = ThinRectangle.getMinY(top);
        double topMaxY = ThinRectangle.getMaxY(top);

        //Bottom Values
        double bottomMinX = ThinRectangle.getMinX(bottom);
        double bottomMaxX = ThinRectangle.getMaxX(bottom);
        double bottomMinY = ThinRectangle.getMinY(bottom);
        double bottomMaxY = ThinRectangle.getMaxY(bottom);

        //Left Values
        double leftMinX = ThinRectangle.getMinX(left);
        double leftMaxX = ThinRectangle.getMaxX(left);
        double leftMinY = ThinRectangle.getMinY(left);
        double leftMaxY = ThinRectangle.getMaxY(left);

        //Right Values
        double rightMinX = ThinRectangle.getMinX(right);
        double rightMaxX = ThinRectangle.getMaxX(right);
        double rightMinY = ThinRectangle.getMinY(right);
        double rightMaxY = ThinRectangle.getMaxY(right);

        boolean isLeftXInTopRange = RangeOperator.isInClosedRange(leftMaxX,topMinX,topMaxX);
        boolean isBottomYInLeftRange = RangeOperator.isInClosedRange(bottomMaxY, leftMinY, leftMaxY);
        boolean isRightXInTopRange = RangeOperator.isInClosedRange(rightMinX, topMinX, topMaxX);
        boolean isBottomYInRightRange = RangeOperator.isInClosedRange(bottomMaxY, rightMinY, rightMaxY);
        boolean isTopYInLeftRange = RangeOperator.isInClosedRange(topMinY, leftMinY, leftMaxY);
        boolean isTopYInRightRange = RangeOperator.isInClosedRange(topMinY, rightMinY, rightMaxY);
        boolean isRightXInBottomRange =  RangeOperator.isInClosedRange(rightMinX, bottomMinX, bottomMaxX);
        boolean isLeftXInBottomRange =  RangeOperator.isInClosedRange(leftMaxX, topMinX, topMaxX);


        return isBottomYInLeftRange && isBottomYInRightRange && isTopYInLeftRange && isTopYInRightRange
                && isLeftXInTopRange && isRightXInTopRange && isRightXInBottomRange && isLeftXInBottomRange;

    }

    private static boolean isBordersGoodOrientation(ThinRectangle top, ThinRectangle bottom, ThinRectangle left, ThinRectangle right) {
        boolean topHorizontal = ThinRectangle.isHorizontal(top);
        boolean bottomHorizontal = ThinRectangle.isHorizontal(bottom);
        boolean leftVertical = ThinRectangle.isVertical(left);
        boolean rightVertical = ThinRectangle.isVertical(right);

        return topHorizontal && bottomHorizontal && leftVertical && rightVertical;
    }

    public void draw(SortedMap<String, Box> rectangleMap, Color color) throws IOException {

        PDFont font = PDType1Font.HELVETICA;
        float fontSize = 14;

        File file = new File(MainDemo.RESULT_FOLDER, "Rectangles.pdf");
        PDDocument doc = getPdDocument(file);

        PDPageTree pages = doc.getPages();
        int count = pages.getCount();
        PDPage page;
        if (count == 0) {
            page = new PDPage();
            doc.addPage(page);
        } else {
            page = doc.getPage(0);
        }


        PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true);
        contentStream.setFont(font, fontSize);
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setNonStrokingColor(Color.BLACK);


        for (Map.Entry<String, Box> entry : rectangleMap.entrySet()) {

            contentStream.setStrokingColor(color);
            drawBox(entry.getValue(), contentStream);

        }

        contentStream.stroke();

        contentStream.close();
        doc.save(file);
        doc.close();

    }

    private void drawBox(Box box, PDPageContentStream contentStream) throws IOException {

        Rectangle2D shape = box.getShape();
        double x = shape.getX();
        double y = shape.getY();
        double height = shape.getHeight();
        double width = shape.getWidth();

        contentStream.addRect((float) x, (float) y, (float) width, (float) height);

    }

    //Duplicate from Rectangles
    private PDDocument getPdDocument(File file) throws IOException {
        PDDocument doc;

        if (file.exists()) {
            doc = PDDocument.load(file);
        } else {
            doc = new PDDocument();
        }
        return doc;
    }

}
