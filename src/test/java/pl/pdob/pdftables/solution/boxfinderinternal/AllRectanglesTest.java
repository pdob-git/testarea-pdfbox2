package pl.pdob.pdftables.solution.boxfinderinternal;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.pdob.pdftables.MainDemo;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.*;

class AllRectanglesTest {


    private static pl.pdob.pdftables.solution.boxfinderinternal.AllRectangles allRectangles;
    private static pl.pdob.pdftables.solution.boxfinderinternal.RectanglesProcessor rectanglesProcessor;

    @BeforeAll
    static void beforeAll() throws IOException {
        MainDemo mainDemo = new MainDemo();

        File file = new File(MainDemo.INPUT_FOLDER, "Minimal_example.pdf");

        mainDemo.extractBoxes(file);
        allRectangles = mainDemo.getAllRectangles();

        rectanglesProcessor = new pl.pdob.pdftables.solution.boxfinderinternal.RectanglesProcessor();
    }

    @Test
    @DisplayName("Checks Equals Method for Rectangles")
    void checkRectangleEqualsTest() {
        //given
        List<ThinRectangle> horizontalThinRectangles = allRectangles.getHorizontalRectangles();

        SortedMap<Integer, ThinRectangle> integerRectangleMap = rectanglesProcessor.removeDuplicates(horizontalThinRectangles);

        //when
        ThinRectangle thinRectangle1 = integerRectangleMap.get(0);
        System.out.println("First Rectangle");
        System.out.println(thinRectangle1);
        ThinRectangle thinRectangle2 = integerRectangleMap.get(22);
        System.out.println("Second Rectangle");
        System.out.println(thinRectangle2);

        //then
        assertNotNull(thinRectangle1);
        assertNull(thinRectangle2);

    }

    @Test
    @DisplayName("Test of Combinable method")
    void combinableTest() {
        //given
        List<ThinRectangle> horizontalThinRectangles = allRectangles.getHorizontalRectangles();
        SortedMap<Integer, ThinRectangle> integerRectangleSortedMap = rectanglesProcessor.removeDuplicates(horizontalThinRectangles);

        //when
        ThinRectangle thinRectangle = integerRectangleSortedMap.get(0);
        ThinRectangle thinRectangle1 = integerRectangleSortedMap.get(1);

        //then
        assertTrue(thinRectangle.combinableWith(thinRectangle1));
    }

}