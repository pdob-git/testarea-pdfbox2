package pl.pdob.pdftables.solution.boxfinderinternal;

import org.junit.jupiter.api.Test;
import pl.pdob.pdftables.MainDemo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ThinRectangleTest {

    @Test
    void combineTest() throws IOException {

        //given
        MainDemo mainDemo = new MainDemo();
        mainDemo.extractBoxes(new File(MainDemo.INPUT_FOLDER,"Minimal_example.pdf"));

        AllRectangles allRectangles = mainDemo.getAllRectangles();
        List<ThinRectangle> verticalThinRectangles = allRectangles.getVerticalRectangles();

        System.out.println(verticalThinRectangles);


        //when
        System.out.println("---------------------");
        System.out.println("Rectangle 0");
        ThinRectangle thinRectangle0 = verticalThinRectangles.get(0);
        System.out.println(thinRectangle0);
        System.out.println("---------------------");
        System.out.println("Rectangle 3");
        ThinRectangle thinRectangle3 = verticalThinRectangles.get(3);
        System.out.println(thinRectangle3);

        //then
        ThinRectangle combined = thinRectangle0.combine(thinRectangle3);
        double minX0 = ThinRectangle.getMinX(thinRectangle0);
        double minX3 = ThinRectangle.getMinX(thinRectangle3);
        double minXCombined = Math.min(minX0, minX3);

        double maxY0 = ThinRectangle.getMaxY(thinRectangle0);
        double maxY3 = ThinRectangle.getMaxY(thinRectangle3);
        double maxYCombined = Math.max(maxY0, maxY3);


        assertNotNull(combined);
        assertEquals(combined.getP2().getY(),maxYCombined);
        assertEquals(combined.getP0().getX(),minXCombined);

        System.out.println("---------------------");
        System.out.println("Combined rectangle");
        System.out.println(combined);






    }
}