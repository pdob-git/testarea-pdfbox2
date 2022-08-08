package mkl.testarea.pdfbox2.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Collections;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ExtractVisibleText {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/47358127/remove-invisible-text-from-pdf-using-pdfbox">
     * remove invisible text from pdf using pdfbox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1F8vrzcABwxVGdN5W-7etQggY5xKtGplU/view">
     * RevTeaser09072016.pdf
     * </a>
     * <p>
     * This class tests the {@link PDFVisibleTextStripper} to ignore text hidden
     * by clipping or by covering with a filled path in the OP's sample document.
     * </p>
     */
    @Test
    public void testExtractFromRevTeaser09072016() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("RevTeaser09072016.pdf")  ) {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFVisibleTextStripper(true);
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* RevTeaser09072016.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "RevTeaser09072016.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/47908124/pdfbox-removing-invisible-text-by-clip-filling-paths-issue">
     * PDFBox - Removing invisible text (by clip/filling paths issue)
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1xcZOusx3cEdZX4AT8QAVDqZe33YWla0H">
     * test.pdf
     * </a> as testDmitryK.pdf
     * <p>
     * Indeed, using the original {@link PDFVisibleTextStripper} implementation
     * a lot of visible characters where dropped. This was due to the incorrect
     * calculation of the <code>end</code> of the character baseline in the methods
     * {@link PDFVisibleTextStripper#processTextPosition(org.apache.pdfbox.text.TextPosition)}
     * and {@link PDFVisibleTextStripper#deleteCharsInPath()}.
     * </p>
     * <p>
     * After patching those {@link PDFVisibleTextStripper} methods to make use of
     * <code>end</code> only optionally, running the test with that option results
     * in a decent extraction of visible text.
     * </p>
     */
    @Test
    public void testTestDmitryK() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("testDmitryK.pdf")  ) {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFVisibleTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* testDmitryK.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "testDmitryK.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/47908124/pdfbox-removing-invisible-text-by-clip-filling-paths-issue">
     * PDFBox - Removing invisible text (by clip/filling paths issue)
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1l0Yt9BJXs09bXcBD7pDbxFiZQQqnuaan">
     * test2.pdf
     * </a> as test2DmitryK.pdf
     * <p>
     * Indeed, even the {@link PDFVisibleTextStripper} implementation as originally
     * improved for {@link #testTestDmitryK()} failed for this document. The cause
     * is another normalization by PDFBox text stripping moving the origin into the
     * lower left corner of the crop box.
     * </p>
     * <p>
     * Patching the {@link PDFVisibleTextStripper} methods to add the lower left
     * crop box coordinate values again results in a decent extraction of visible
     * text.
     * </p>
     */
    @Test
    public void testTest2DmitryK() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("test2DmitryK.pdf")  ) {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFVisibleTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* test2DmitryK.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "test2DmitryK.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://github.com/mkl-public/testarea-pdfbox2/issues/3">
     * One case fails to remove invisible texts or symbols
     * </a>
     * <br/>
     * <a href="https://github.com/mkl-public/testarea-pdfbox2/files/2481423/00000000000005fw6q.pdf">
     * 00000000000005fw6q.pdf
     * </a>
     * <p>
     * The "hidden text" recognized by Adobe here is only "hidden"
     * because it uses a glyph (page 1, Font F9, code 0000) for which
     * the embedded font draws nothing but which ToUnicode maps to
     * U+DBD0, a High Private Use Surrogate which by itself in general
     * makes no sense.
     * </p>
     */
    @Test
    public void test00000000000005fw6q() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("00000000000005fw6q.pdf")  ) {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFVisibleTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* 00000000000005fw6q.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "00000000000005fw6q.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/59920280/pdfbox-2-0-invisible-lines-on-rotated-page-clip-path-issue">
     * PDFBox 2.0: invisible lines on rotated page - clip path issue
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1Ex03HhDz17xQlsiTIY1cxaT_cb3nyTf3">
     * 1.pdf
     * </a>
     * <p>
     * Indeed, a number of lines get dropped. An analysis turns out that a glyph
     * origin positioned right on the clip path border has chances of being dropped.
     * This is due to different processing of those data with different errors.
     * </p>
     * @see #testFat1()
     */
    @Test
    public void test1() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("1.pdf")  ) {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFVisibleTextStripper(false, new PrintStream(new File(RESULT_FOLDER, "1-drops.txt")));
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* 1.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "1.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/59920280/pdfbox-2-0-invisible-lines-on-rotated-page-clip-path-issue">
     * PDFBox 2.0: invisible lines on rotated page - clip path issue
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1Ex03HhDz17xQlsiTIY1cxaT_cb3nyTf3">
     * 1.pdf
     * </a>
     * <p>
     * To lessen the impact of floating point errors on coordinate and clip path
     * comparison, we here use "fat glyph origin coordinate comparisons", we check
     * whether a small rectangle around the test coordinates intersects the area
     * instead of checking whether the coordinates are contained in the area.
     * This indeed fixes the error results.
     * </p>
     * @see #test1()
     */
    @Test
    public void testFat1() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("1.pdf")  ) {
            PDDocument document = PDDocument.load(resource);
            PDFVisibleTextStripper stripper = new PDFVisibleTextStripper();
            stripper.setUseFatGlyphOrigin(true);
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* 1.pdf, fat coordinates\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "1-fat.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/59920280/pdfbox-2-0-invisible-lines-on-rotated-page-clip-path-issue">
     * PDFBox 2.0: invisible lines on rotated page - clip path issue
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1F8vrzcABwxVGdN5W-7etQggY5xKtGplU/view">
     * RevTeaser09072016.pdf
     * </a>
     * <p>
     * This test checks whether fat coordinate comparisons introduce unwanted
     * errors by applying to the "RevTeaser09072016.pdf" file
     * </p>
     * @see #test1()
     * @see #testExtractFromRevTeaser09072016()
     */
    @Test
    public void testExtractFatFromRevTeaser09072016() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("RevTeaser09072016.pdf")  ) {
            PDDocument document = PDDocument.load(resource);
            PDFVisibleTextStripper stripper = new PDFVisibleTextStripper(true);
            stripper.setUseFatGlyphOrigin(true);
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* RevTeaser09072016.pdf, fat coordinates\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "RevTeaser09072016-fat.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/59920280/pdfbox-2-0-invisible-lines-on-rotated-page-clip-path-issue">
     * PDFBox 2.0: invisible lines on rotated page - clip path issue
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1l0Yt9BJXs09bXcBD7pDbxFiZQQqnuaan">
     * test2.pdf
     * </a> as test2DmitryK.pdf
     * <p>
     * This test checks whether fat coordinate comparisons introduce unwanted
     * errors by applying to the "test2DmitryK.pdf" file
     * </p>
     * @see #test1()
     * @see #testTest2DmitryK()
     */
    @Test
    public void testFatTest2DmitryK() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("test2DmitryK.pdf")  ) {
            PDDocument document = PDDocument.load(resource);
            PDFVisibleTextStripper stripper = new PDFVisibleTextStripper();
            stripper.setUseFatGlyphOrigin(true);
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* test2DmitryK.pdf, fat coordinates\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "test2DmitryK-fat.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/63936154/how-to-identify-and-remove-hidden-text-from-the-pdf-using-pdfbox-java">
     * How to identify and remove hidden text from the PDF using PDFBox java
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1epCmrJ1lsM9o5X_m3xgVVqhmyYY6Lf1O/view?usp=drivesdk">
     * FooterText.pdf
     * </a>
     * <p>
     * The text covered by a large white rectangle ("Green Bonds – Made by KfW │ Allocation Report:
     * Use of proceeds of 2019 Green Bond issuances│ March 2020 8") is correctly dropped by the
     * {@link PDFVisibleTextStripper}.
     * </p>
     */
    @Test
    public void testExtractFromFooterText() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("FooterText.pdf")  ) {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFVisibleTextStripper(true);
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* FooterText.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "FooterText.txt").toPath(), Collections.singleton(text));
        }
    }
}
