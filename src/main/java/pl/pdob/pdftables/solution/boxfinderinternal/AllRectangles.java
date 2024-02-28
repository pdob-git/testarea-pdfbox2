package pl.pdob.pdftables.solution.boxfinderinternal;

import java.util.ArrayList;
import java.util.List;

public class AllRectangles {

    private final List<ThinRectangle> horizontalThinRectangles = new ArrayList<>();
    private final List<ThinRectangle> verticalThinRectangles = new ArrayList<>();
    private final List<ThinRectangle> thinThinRectangles = new ArrayList<>();


    public List<ThinRectangle> getHorizontalRectangles() {
        return horizontalThinRectangles;
    }



    public void addThinRectangle(ThinRectangle thinRectangle){
        thinThinRectangles.add(thinRectangle);
    }

    public List<ThinRectangle> getVerticalRectangles() {
        return verticalThinRectangles;
    }

    public List<ThinRectangle> getThinRectangles() {
        return thinThinRectangles;
    }

    public void clear(){
        getThinRectangles().clear();
        getHorizontalRectangles().clear();
        getVerticalRectangles().clear();
    }
}
