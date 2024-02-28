package pl.pdob.pdftables.solution.boxfinderinternal;

import org.junit.jupiter.api.Test;
import pl.pdob.pdftables.MainDemo;
import pl.pdob.pdftables.solution.boxfinderinternal.BoxProcessor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.*;

class BoxProcessorTest {

    @Test
    void generateBoxesTest() throws IOException {
        MainDemo mainDemo = new MainDemo();
        mainDemo.extractBoxes(new File(MainDemo.INPUT_FOLDER,"Minimal_example.pdf"));
        BoxProcessor boxProcessor = new BoxProcessor();
        SortedMap<String, pl.pdob.pdftables.solution.boxfinderinternal.Box> boxesMap = boxProcessor.generateBoxes(mainDemo.getAllRectangles());
        System.out.println(boxesMap);
        boxProcessor.draw(boxesMap, Color.GREEN);

        assertEquals(3,boxesMap.size());

    }
}