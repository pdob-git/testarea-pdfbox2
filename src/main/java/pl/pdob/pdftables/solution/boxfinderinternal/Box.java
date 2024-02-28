package pl.pdob.pdftables.solution.boxfinderinternal;

import javax.annotation.Nonnull;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

public class Box implements Comparable<Box>{

    private String key;
    private Rectangle2D shape;

    public Box(Point2D p0, Point2D p1, Point2D p2, Point2D p3) {
        ThinRectangle thinRectangle = new ThinRectangle(p0, p1, p2, p3);
        double maxY = ThinRectangle.getMaxY(thinRectangle);
        double minY = ThinRectangle.getMinY(thinRectangle);
        double maxX = ThinRectangle.getMaxX(thinRectangle);
        double minX = ThinRectangle.getMinX(thinRectangle);
        this.shape = new Rectangle2D.Float((float) minX, (float) minY, (float) (maxX - minX), (float) (maxY - minY));
        this.key = "";

    }

    private Box() {

    }

    public Box(String key, Rectangle2D shape) {
        this.key = key;
        this.shape = shape;
    }

    /**
     * Creates Box from borders which are ThinRectangles. Borders in pdf are usually defined not as lines but by rectangles of thin width.
     *
     * @param top    - top horizontal border
     * @param bottom - bottom horizontal border
     * @param left   - left vertical border
     * @param right  - right vertical border
     * @return box formed from borders
     */

    public static Box ofThinRectangles(@Nonnull ThinRectangle top, @Nonnull ThinRectangle bottom, @Nonnull ThinRectangle left, @Nonnull ThinRectangle right) {

        //HORIZ TOP min Y = top Y
        //VERT LEFT MAX X = min X
        //HORIZ BOTTOM MAX Y = bottom Y
        //VERT RIGTH MIN X  = max X

        double topY = ThinRectangle.getMinY(top);
        double bottomY = ThinRectangle.getMaxY(bottom);
        double leftX = ThinRectangle.getMaxX(left);
        double rightX = ThinRectangle.getMinX(right);

        double width = rightX - leftX;
        double height = topY - bottomY;

        boolean isWidthGreaterThanZero = width > 0d;
        boolean isHeightGreaterThanZero = height > 0d;

        if (isWidthGreaterThanZero && isHeightGreaterThanZero) {
            return new Box("", new Rectangle2D.Float((float) leftX, (float) bottomY, (float) width, (float) height));
        }

        return Box.newEmpty();
    }


    public static Box newEmpty() {
        Rectangle2D.Float shape = new Rectangle2D.Float();
        String key = "";
        return new Box(key, shape);
    }

    public boolean isEmpty() {
        return this.getShape().isEmpty() && this.getKey().isEmpty();
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Rectangle2D getShape() {
        return shape;
    }

    public void setShape(Rectangle2D shape) {
        this.shape = shape;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Box box = (Box) o;
        return Objects.equals(key, box.key) && Objects.equals(shape, box.shape);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, shape);
    }

    @Override
    public String toString() {
        return "Box{" +
                "key='" + key + '\'' +
                ", shape=" + shape +
                '}';
    }

    @Override
    public int compareTo(Box o) {
        return this.getKey().compareTo(o.getKey());
    }
}
