package pl.pdob.pdftables.solution.boxfinderinternal;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import pl.pdob.pdftables.MainDemo;
import pl.pdob.pdftables.csv.CsvWriter;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Remove duplicates from Rectangles and draws on results page
 */
public class RectanglesProcessor {

    private final Random random;


    public RectanglesProcessor() {
        this.random = new Random();
    }

    public SortedMap<Integer, ThinRectangle> removeDuplicates(List<ThinRectangle> thinRectangles) {
        SortedMap<Integer, ThinRectangle> rectangleMap = new TreeMap<>();
        for (int i = 0; i < thinRectangles.size(); i++) {
            rectangleMap.put(i, thinRectangles.get(i));
        }
        return rectangleMap;
    }

    public void draw(SortedMap<Integer, ThinRectangle> rectangleMap, Color color) throws IOException {

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


        for (Map.Entry<Integer, ThinRectangle> entry : rectangleMap.entrySet()) {

            contentStream.setStrokingColor(color);
            drawRectangle(entry.getValue(), contentStream);

        }

        contentStream.stroke();

        contentStream.close();
        doc.save(file);
        doc.close();

    }

    private PDDocument getPdDocument(File file) throws IOException {
        PDDocument doc;

        if (file.exists()) {
            doc = PDDocument.load(file);
        } else {
            doc = new PDDocument();
        }
        return doc;
    }

    private void drawRectangle(ThinRectangle thinRectangle, PDPageContentStream contentStream) throws IOException {
        Point2D p0 = thinRectangle.getP0();
        Point2D p1 = thinRectangle.getP1();
        Point2D p2 = thinRectangle.getP2();

        float width = (float) p1.getX() - (float) p0.getX();
        float height = (float) p2.getY() - (float) p0.getY();

        contentStream.addRect((float) p0.getX(), (float) p0.getY(), width, height);

    }


    private Color randomizeColor() {
        List<Color> colors = Arrays.asList(
                Color.GRAY,
                Color.DARK_GRAY,
                Color.BLACK,
                Color.RED,
                Color.PINK,
                Color.ORANGE,
                Color.YELLOW,
                Color.GREEN,
                Color.MAGENTA,
                Color.CYAN,
                Color.BLUE);
        return colors.get(getRandomNumber(0, colors.size() - 1));
    }

    private int getRandomNumber(int min, int max) {
        return random.nextInt(max - min) + min;
    }


    public List<String[]> toCsvString(SortedMap<Integer, ThinRectangle> rectangleMap, String keySuffix) {
        List<String[]> csvStrings = new ArrayList<>();

        csvStrings.add(new String[]{"key", "p0.X", "p0.Y", "p1.X", "p1.Y", "p2.X", "p2.Y", "p3.X", "p3.Y"});

        rectangleMap.forEach((key, value) -> csvStrings.add(new String[]{key.toString() + keySuffix,
                String.valueOf(value.getP0().getX()), String.valueOf(value.getP0().getY()),
                String.valueOf(value.getP1().getX()), String.valueOf(value.getP1().getY()),
                String.valueOf(value.getP2().getX()), String.valueOf(value.getP2().getY()),
                String.valueOf(value.getP3().getX()), String.valueOf(value.getP3().getY())}));

        return csvStrings;

    }

    public void saveAsCsv(SortedMap<Integer, ThinRectangle> rectangleMap, String fileName) {
        List<String[]> csvString = toCsvString(rectangleMap, "");
        CsvWriter.csvWriterOneByOne(csvString, fileName);
    }

    public void segregateData(AllRectangles allRectangles) {

        List<ThinRectangle> thinThinRectangles = allRectangles.getThinRectangles();


        List<ThinRectangle> newHoriz = thinThinRectangles.stream()
                .filter(ThinRectangle::isHorizontal)
                .collect(Collectors.toList());


        List<ThinRectangle> newVert = thinThinRectangles.stream()
                .filter(ThinRectangle::isVertical)
                .collect(Collectors.toList());


        allRectangles.getHorizontalRectangles().clear();
        newHoriz.sort(null);
        allRectangles.getHorizontalRectangles().addAll(newHoriz);

        allRectangles.getVerticalRectangles().clear();
        newVert.sort(null);
        allRectangles.getVerticalRectangles().addAll(newVert);
    }

    @SuppressWarnings("ReassignedVariable")
    public List<ThinRectangle> consolidateList(List<ThinRectangle> thinRectangles, String flag) {
        SortedMap<Integer, ThinRectangle> integerRectangleSortedMap = removeDuplicates(thinRectangles);

        List<ThinRectangle> noDuplicatesList = new ArrayList<>(integerRectangleSortedMap.values());


        switch (flag) {
            case "vertical":
                noDuplicatesList.sort(new ThinRectangle.ComparatorXTheY());
                break;
            case "horizontal":
            default:
                noDuplicatesList.sort(null);
        }

        for (int i = 1; i < noDuplicatesList.size(); ) {

            if (noDuplicatesList.get(i - 1).combinableWith(noDuplicatesList.get(i))) {
                ThinRectangle combine = noDuplicatesList.get(i - 1).combine(noDuplicatesList.get(i));
                noDuplicatesList.set(i - 1, combine);
                noDuplicatesList.remove(i);
            } else {
                i++;
            }

        }
        return noDuplicatesList;
    }

    public void consolidateLists(AllRectangles allRectangles) {

        List<ThinRectangle> rectanglesHorizontal = consolidateList(allRectangles.getHorizontalRectangles(), "horizontal");
        allRectangles.getHorizontalRectangles().clear();
        allRectangles.getHorizontalRectangles().addAll(rectanglesHorizontal);

        List<ThinRectangle> rectanglesVertical = consolidateList(allRectangles.getVerticalRectangles(), "vertical");
        allRectangles.getVerticalRectangles().clear();
        allRectangles.getVerticalRectangles().addAll(rectanglesVertical);

    }
}
