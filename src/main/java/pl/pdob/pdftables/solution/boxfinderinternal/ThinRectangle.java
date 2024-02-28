package pl.pdob.pdftables.solution.boxfinderinternal;

import javax.annotation.Nonnull;
import java.awt.geom.Point2D;
import java.util.Comparator;
import java.util.Objects;

public class ThinRectangle implements PathElement, Comparable<ThinRectangle> {


    final Point2D p0;
    final Point2D p1;
    final Point2D p2;
    final Point2D p3;

    public ThinRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    public Point2D getP0() {
        return p0;
    }

    public Point2D getP1() {
        return p1;
    }

    public Point2D getP2() {
        return p2;
    }

    public Point2D getP3() {
        return p3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThinRectangle thinRectangle = (ThinRectangle) o;
        return Objects.equals(p0, thinRectangle.p0) && Objects.equals(p1, thinRectangle.p1) && Objects.equals(p2, thinRectangle.p2) && Objects.equals(p3, thinRectangle.p3);
    }

    @Override
    public int hashCode() {
        return Objects.hash(p0, p1, p2, p3);
    }

    @Override
    public String toString() {
        return "Rectangle{" +
                "p0=" + p0 +
                ", p1=" + p1 +
                ", p2=" + p2 +
                ", p3=" + p3 +
                '}';
    }


    @Override
    public int compareTo(@Nonnull ThinRectangle other) {
        double thisMaxX = getMaxX(this);
        double thisMaxY = getMaxY(this);

        double oMaxX = getMaxX(other);
        double oMaxY = getMaxY(other);
        if (this.equals(other)) {
            return 0;
        } else {
            return compareMaxYThenX(thisMaxY, oMaxY, thisMaxX, oMaxX);
        }
    }

    protected static int compareMaxYThenX(double thisMaxY, double oMaxY, double thisMaxX, double oMaxX) {
        int compare = Double.compare(oMaxY, thisMaxY);
        if (compare == 0) {
            return Double.compare(thisMaxX,oMaxX);
        } else {
            return compare;
        }
    }

    protected static int compareMaxXThenY(double thisMaxY, double oMaxY, double thisMaxX, double oMaxX) {
        int compare = Double.compare(thisMaxX,oMaxX);
        if (compare == 0) {
            return Double.compare(oMaxY, thisMaxY);
        } else {
            return compare;
        }
    }

    protected static double getMaxX(ThinRectangle thinRectangle) {
        Point2D p00 = thinRectangle.getP0();
        Point2D p01 = thinRectangle.getP1();
        Point2D p02 = thinRectangle.getP2();
        Point2D p03 = thinRectangle.getP3();

        double thisXFrom = Math.max(p00.getX(), p01.getX());
        double thisXTo = Math.max(p02.getX(), p03.getX());
        return Math.max(thisXFrom, thisXTo);
    }

    protected static double getMaxY(ThinRectangle thinRectangle) {
        Point2D p00 = thinRectangle.getP0();
        Point2D p01 = thinRectangle.getP1();
        Point2D p02 = thinRectangle.getP2();
        Point2D p03 = thinRectangle.getP3();

        double thisXFrom = Math.max(p00.getY(), p01.getY());
        double thisXTo = Math.max(p02.getY(), p03.getY());
        return Math.max(thisXFrom, thisXTo);
    }

    protected static double getMinX(ThinRectangle thinRectangle) {
        Point2D p00 = thinRectangle.getP0();
        Point2D p01 = thinRectangle.getP1();
        Point2D p02 = thinRectangle.getP2();
        Point2D p03 = thinRectangle.getP3();

        double thisXFrom = Math.min(p00.getX(), p01.getX());
        double thisXTo = Math.min(p02.getX(), p03.getX());
        return Math.min(thisXFrom, thisXTo);
    }

    protected static double getMinY(ThinRectangle thinRectangle) {
        Point2D p00 = thinRectangle.getP0();
        Point2D p01 = thinRectangle.getP1();
        Point2D p02 = thinRectangle.getP2();
        Point2D p03 = thinRectangle.getP3();

        double thisXFrom = Math.min(p00.getY(), p01.getY());
        double thisXTo = Math.min(p02.getY(), p03.getY());
        return Math.min(thisXFrom, thisXTo);
    }

    public static boolean isHorizontal(ThinRectangle thinRectangle) {
        return Math.abs(thinRectangle.getP2().getY() - thinRectangle.getP1().getY()) < 3f;
    }

    public static boolean isVertical(ThinRectangle thinRectangle) {
        return Math.abs(thinRectangle.getP2().getY() - thinRectangle.getP1().getY()) >= 3f;
    }

    public boolean combinableWith(@Nonnull ThinRectangle other) {
        if(ThinRectangle.isHorizontal(this) && ThinRectangle.isHorizontal(other)){
            return horizontalCombinableWith(other);
        } else if(ThinRectangle.isVertical(this) && ThinRectangle.isVertical(other)){
            return verticalCombinableWith(other);
        }
        return false;
    }

    private boolean verticalCombinableWith(ThinRectangle other) {

        double maxX = getMaxX(this);
        double minX = getMinX(this);
        double otherMaxX = getMaxX(other);
        double otherMinX = getMinX(other);

        boolean maxXEquals = Double.compare(maxX, otherMaxX) == 0;
        boolean minXEquals = Double.compare(minX, otherMinX) == 0;

        return maxXEquals && minXEquals;
    }

    private boolean horizontalCombinableWith(ThinRectangle other) {
        double maxY = getMaxY(this);
        double minY = getMinY(this);
        double otherMaxY = getMaxY(other);
        double otherMinY = getMinY(other);


        boolean maxYEquals = Double.compare(maxY, otherMaxY) == 0;
        boolean minYEquals = Double.compare(minY, otherMinY) == 0;

        return maxYEquals && minYEquals;
    }

    public ThinRectangle combine(ThinRectangle other) {
        double minY = getMinY(this);
        double maxY = getMaxY(this);
        double minX = getMinX(this);
        double maxX = getMaxX(this);

        double minY1 = getMinY(other);
        double maxY1 = getMaxY(other);
        double minX1 = getMinX(other);
        double maxX1 = getMaxX(other);
        
        double resultMaxY = Math.max(maxY, maxY1);
        double resultMinY = Math.min(minY, minY1);
        double resultMaxX = Math.max(maxX, maxX1);
        double resultMinX = Math.min(minX, minX1);

        Point2D newP0 = new Point2D.Double(resultMinX, resultMinY);
        Point2D newP1 = new Point2D.Double(resultMaxX, resultMinY);
        Point2D newP2 = new Point2D.Double(resultMaxX, resultMaxY);
        Point2D newP3 = new Point2D.Double(resultMinX, resultMaxY);

        return new ThinRectangle(newP0, newP1, newP2, newP3);

    }

    public static class ComparatorXTheY implements Comparator<ThinRectangle> {

        @Override
        public int compare(ThinRectangle o1, ThinRectangle o2) {
            double thisMaxX = ThinRectangle.getMaxX(o1);
            double thisMaxY = ThinRectangle.getMaxY(o1);

            double oMaxX = ThinRectangle.getMaxX(o2);
            double oMaxY = ThinRectangle.getMaxY(o2);
            if (o1.equals(o2)) {
                return 0;
            } else {
                return ThinRectangle.compareMaxXThenY(thisMaxY, oMaxY, thisMaxX, oMaxX);
            }
        }
    }

}
