package mkl.testarea.pdfbox2.extract;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorN;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorSpace;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.text.TextPosition;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ExtractText
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/37862159/pdf-reading-via-pdfbox-in-java">
     * pdf reading via pdfbox in java 
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B_Ke2amBgdpedUNwVTR3RVlRTFE/view?usp=sharing">
     * PnL_500010_0314.pdf
     * </a>
     * <p>
     * Indeed, the <code>PDFTextStripper</code> is not even informed about those undecipherable
     * text sections. Essentially the underlying method `PDFTextStreamEngine.showGlyph` filters
     * all unmappable glyphs from composite fonts. 
     * </p>
     */
    @Test
    public void testPnL_500010_0314() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("PnL_500010_0314.pdf")    )
        {
            PDDocument document = Loader.loadPDF(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* PnL_500010_0314.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "PnL_500010_0314.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/37862159/pdf-reading-via-pdfbox-in-java">
     * pdf reading via pdfbox in java 
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B_Ke2amBgdpebm96U05FcWFsSXM/view?usp=sharing">
     * Bal_532935_0314.pdf
     * </a>
     * <p>
     * The issue here is caused by PDFBox guessing an encoding. The underlying method
     * `PDFTextStreamEngine.showGlyph` does this for all unmappable glyphs from simple
     * fonts.
     * </p>
     */
    @Test
    public void testBal_532935_0314() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Bal_532935_0314.pdf")    )
        {
            PDDocument document = Loader.loadPDF(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* Bal_532935_0314.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "Bal_532935_0314.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/38057338/pdfbox-symbolic-fonts-must-have-a-built-in-encoding-error-when-using-pdftextst">
     * PDFBox �Symbolic fonts must have a built-in encoding� error when using PDFTextStripper.getText()
     * </a>
     * <br/>
     * <a href="http://www.cv-foundation.org/openaccess/content_cvpr_2016/papers/Park_Efficient_and_Robust_CVPR_2016_paper.pdf">
     * Park_Efficient_and_Robust_CVPR_2016_paper.pdf
     * </a>
     * <br/>
     * Issue <a href="https://issues.apache.org/jira/browse/PDFBOX-3403">PDFBOX-3403</a>
     * <p>
     * The issue here is caused by PDFBox not knowing MacExpertEncoding yet. But even
     * if there was no known base encoding, there is no need for the exception at all
     * as all font glyphs are covered in the Differences array.
     * </p>
     */
    @Test
    public void testPark_Efficient_and_Robust_CVPR_2016_paper() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Park_Efficient_and_Robust_CVPR_2016_paper.pdf")    )
        {
            PDDocument document = Loader.loadPDF(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* Park_Efficient_and_Robust_CVPR_2016_paper.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "Park_Efficient_and_Robust_CVPR_2016_paper.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/38975091/pdfbox-gettext-not-returning-all-of-the-visible-text">
     * PDFBox getText not returning all of the visible text
     * </a>
     * <br>
     * <a href="https://dl.dropboxusercontent.com/u/14898138/03%20WP%20Enterprise%20BlackBerry%20Compete%20Datasheet_041612%20FINAL%20DRAFT.pdf">
     * 03 WP Enterprise BlackBerry Compete Datasheet_041612 FINAL DRAFT.pdf
     * </a>
     * <p>
     * There is some 'writing' actually done using vector graphics, not text,
     * but aside from that all is accounted for.
     * </p>
     */
    @Test
    public void test03WpEnterpriseBlackBerryCompeteDatasheet_041612FinalDraft() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("03 WP Enterprise BlackBerry Compete Datasheet_041612 FINAL DRAFT.pdf")    )
        {
            PDDocument document = Loader.loadPDF(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* 03 WP Enterprise BlackBerry Compete Datasheet_041612 FINAL DRAFT.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "03 WP Enterprise BlackBerry Compete Datasheet_041612 FINAL DRAFT.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/45895768/pdfbox-2-0-7-extracttext-not-working-but-1-8-13-does-and-pdfreader-as-well">
     * PDFBox 2.0.7 ExtractText not working but 1.8.13 does and PDFReader as well
     * </a>
     * <br/>
     * <a href="https://wetransfer.com/downloads/214674449c23713ee481c5a8f529418320170827201941/b2bea6">
     * test-2.pdf
     * </a>
     * <p>
     * Due to the broken <b>ToUnicode</b> maps the output of this test is
     * unsatisfying. It can be improved by removing these <b>ToUnicode</b>
     * maps, cf. {@link #testNoToUnicodeTest2()}.
     * </p>
     */
    @Test
    public void testTest2() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("test-2.pdf")    )
        {
            PDDocument document = Loader.loadPDF(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            System.out.printf("\n*\n* test-2.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "test-2.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/45895768/pdfbox-2-0-7-extracttext-not-working-but-1-8-13-does-and-pdfreader-as-well">
     * PDFBox 2.0.7 ExtractText not working but 1.8.13 does and PDFReader as well
     * </a>
     * <br/>
     * <a href="https://wetransfer.com/downloads/214674449c23713ee481c5a8f529418320170827201941/b2bea6">
     * test-2.pdf
     * </a>
     * <p>
     * Due to the broken <b>ToUnicode</b> maps the output of immediate text
     * extraction from this document is unsatisfying, cf. {@link #testTest2()}.
     * It can be improved by removing these <b>ToUnicode</b> maps as this test
     * shows.
     * </p>
     */
    @Test
    public void testNoToUnicodeTest2() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("test-2.pdf")    )
        {
            PDDocument document = Loader.loadPDF(resource);

            for (int pageNr = 0; pageNr < document.getNumberOfPages(); pageNr++)
            {
                PDPage page = document.getPage(pageNr);
                PDResources resources = page.getResources();
                removeToUnicodeMaps(resources);
            }

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            System.out.printf("\n*\n* test-2.pdf without ToUnicode\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "test-2_NoToUnicode.txt").toPath(), Collections.singleton(text));
        }
    }

    void removeToUnicodeMaps(PDResources pdResources) throws IOException
    {
        COSDictionary resources = pdResources.getCOSObject();

        COSDictionary fonts = asDictionary(resources, COSName.FONT);
        if (fonts != null)
        {
            for (COSBase object : fonts.getValues())
            {
                while (object instanceof COSObject)
                    object = ((COSObject)object).getObject();
                if (object instanceof COSDictionary)
                {
                    COSDictionary font = (COSDictionary)object;
                    font.removeItem(COSName.TO_UNICODE);
                }
            }
        }

        for (COSName name : pdResources.getXObjectNames())
        {
            PDXObject xobject = pdResources.getXObject(name);
            if (xobject instanceof PDFormXObject)
            {
                PDResources xobjectPdResources = ((PDFormXObject)xobject).getResources();
                removeToUnicodeMaps(xobjectPdResources);
            }
        }
    }

    COSDictionary asDictionary(COSDictionary dictionary, COSName name)
    {
        COSBase object = dictionary.getDictionaryObject(name);
        return object instanceof COSDictionary ? (COSDictionary) object : null;
    }

    /**
     * <a href="https://stackoverflow.com/questions/47515609/invalid-block-type-while-using-pdfbox-2-0-8">
     * Invalid block type while using pdfbox 2.0.8
     * </a>
     * <br>
     * <a href="https://www.dropbox.com/s/xjeksj0cay4x3vo/NoTemplateInError.pdf?dl=0">
     * NoTemplateInError.pdf
     * </a>
     * <p>
     * The issue cannot be reproduced.
     * </p>
     */
    @Test
    public void testNoTemplateInError() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("NoTemplateInError.pdf")    )
        {
            PDDocument document = Loader.loadPDF(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* NoTemplateInError.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "NoTemplateInError.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/48828500/again-having-invisible-text-coming-from-pdftextstripper">
     * Again having invisible text coming from PdfTextStripper
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1P1oFu8cpZnzy9LF4wiGWPrk3PfL6dktt">
     * testFailed.pdf
     * </a>
     * <p>
     * The extracted, invisible text is rendered WHITE on WHITE.
     * </p>
     */
    @Test
    public void testTestFailed() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("testFailed.pdf")    )
        {
            PDDocument document = Loader.loadPDF(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* testFailed.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "testFailed.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/49746202/read-pdf-file-using-pdfbox-in-utf-8-in-java-scala">
     * Read pdf file using pdfbox in UTF-8 in java/scala
     * </a>
     * <br/>
     * <a href="https://1drv.ms/b/s!AmHcFaD-gMGyhg6eyqSy2gu9sLWl">
     * test.pdf
     * </a> as testKabirManandhar.pdf
     * <p>
     * The issue can be reproduced. The cause are incomplete ToUnicode
     * maps. There is an option, though: The embedded font programs
     * appear to include more complete mappings, so repairing the
     * ToUnicode table seems feasible.
     * </p>
     */
    @Test
    public void testTestKabirManandhar() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("testKabirManandhar.pdf")    )
        {
            PDDocument document = Loader.loadPDF(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* testKabirManandhar.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "testKabirManandhar.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/50044892/pdfbox-invisible-text-from-pdftextstripper-not-clip-path-or-color-issue">
     * PDFBox: Invisible text from PdfTextStripper (not clip path or color issue)
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1jOMq4sO393JSD60KoMX9WdzMJtY7ppze">
     * test.pdf
     * </a> as testSeparation.pdf
     * <p>
     * To retrieve the separation color values, one must tell the stripper
     * to look for the generic color operators cs and scn.
     * </p>
     */
    @Test
    public void testTestSeparation() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("testSeparation.pdf")    )
        {
            PDDocument document = Loader.loadPDF(resource);
            PDFTextStripper stripper = new PDFTextStripper() {
                @Override
                protected void processTextPosition(TextPosition text) {
                    PDGraphicsState gs = getGraphicsState();
                    PDColor color = gs.getNonStrokingColor();
                    float[] currentComponents = color.getComponents();
                    if (!Arrays.equals(components, currentComponents)) {
                        System.out.print(Arrays.toString(currentComponents));
                        components = currentComponents;
                    }
                    System.out.print(text.getUnicode());
                    super.processTextPosition(text);
                }
                
                float[] components;
            };
            stripper.addOperator(new SetNonStrokingColorSpace());
            stripper.addOperator(new SetNonStrokingColorN());
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* testSeparation.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "testSeparation.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/51672080/pdfdomtree-does-not-detecting-white-spaces-while-converting-a-pdf-file-to-html">
     * PDFDomTree does not detecting white spaces while converting a pdf file to html
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1SZNFCvGVbQzCxJiRr8HlW99ravC_Cm71/view?usp=sharing">
     * demo.pdf
     * </a>
     * <p>
     * PDFBox shows no issue extracting the text from the given file.
     * </p>
     */
    @Test
    public void testDemo() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("demo.pdf")    )
        {
            PDDocument document = Loader.loadPDF(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* demo.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "demo.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/53382793/java-8-pdfbox-cant-gettext-of-pdf-file">
     * Java 8 PDFbox can't getText of pdf file
     * </a>
     * <br/>
     * <a href="http://www.o-cha.net/english/cup/pdf/29.pdf">
     * 29.pdf
     * </a>
     * <p>
     * Cannot reproduce any issue.
     * </p>
     */
    @Test
    public void test29() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("29.pdf")    )
        {
            PDDocument document = Loader.loadPDF(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* 29.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "29.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/53551335/java-does-pdfbox-have-an-option-to-open-file-instead-of-loading-it">
     * Java- Does pdfBox have an option to open file instead of loading it?
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/osyk2ieoq6od2p8/10-million-password-list-top-1000000.pdf?dl=0">
     * 10-million-password-list-top-1000000.pdf
     * </a>
     * <p>
     * In contrast to the OP I did not need to fiddle with the memory
     * settings at all for a plain extraction. Furthermore, I got 999999
     * lines with words and 3 empty lines from the file, not 10000000
     * passwords.
     * </p>
     */
    @Test
    public void test10MillionPasswordListTop1000000() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("10-million-password-list-top-1000000.pdf")    )
        {
            PDDocument document = Loader.loadPDF(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* 10-million-password-list-top-1000000.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "10-million-password-list-top-1000000.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/53837294/performance-for-loading-pdf-using-pdfbox">
     * Performance for loading pdf using PDFBox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/drive/folders/1KW5tVtF1gtcPNv2R7pIdDFMph8FhafT2?usp=sharing">
     * 284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V1.pdf
     * </a>
     * <p>
     * I cannot reproduce the enormous time required for text
     * extraction claimed by the OP.
     * </p>
     * @see #test284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V2()
     */
    @Test
    public void test284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V1() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V1.pdf")    )
        {
            PDDocument document = Loader.loadPDF(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* 284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V1.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V1.txt").toPath(), Collections.singleton(text));
        }

        int runs = 10;
        long start = System.currentTimeMillis();
        for (int i = 0; i < runs; i++) {
            try (   InputStream resource = getClass().getResourceAsStream("284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V1.pdf")    )
            {
                PDDocument document = Loader.loadPDF(resource);
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.getText(document);
            }
        }
        long duration = System.currentTimeMillis() - start;
        System.out.printf("\nExtract %d times from '284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V1.pdf' took %dms.\n", runs, duration);
    }

    /**
     * <a href="https://stackoverflow.com/questions/53837294/performance-for-loading-pdf-using-pdfbox">
     * Performance for loading pdf using PDFBox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/drive/folders/1KW5tVtF1gtcPNv2R7pIdDFMph8FhafT2?usp=sharing">
     * 284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V2.pdf
     * </a>
     * <p>
     * I cannot reproduce the enormous time required for text
     * extraction claimed by the OP.
     * </p>
     * @see #test284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V1()
     */
    @Test
    public void test284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V2() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V2.pdf")    )
        {
            PDDocument document = Loader.loadPDF(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* 284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V2.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V2.txt").toPath(), Collections.singleton(text));
        }

        int runs = 10;
        long start = System.currentTimeMillis();
        for (int i = 0; i < runs; i++) {
            try (   InputStream resource = getClass().getResourceAsStream("284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V2.pdf")    )
            {
                PDDocument document = Loader.loadPDF(resource);
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.getText(document);
            }
        }
        long duration = System.currentTimeMillis() - start;
        System.out.printf("\nExtract %d times from '284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V2.pdf' took %dms.\n", runs, duration);
    }

    /**
     * <a href="https://stackoverflow.com/questions/53773479/java-rotated-file-extraction">
     * java- rotated file extraction?
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/g1pe8zb9m5kajif/lol.pdf?dl=0">
     * lol.pdf
     * </a>
     * <p>
     * Indeed, regular text extraction results on many lines, essentially
     * one for each text chunk. One can improve this in two ways, either
     * one activates sorting or one removes the Rotate entries from the
     * page dictionaries.
     * </p>
     */
    @Test
    public void testLol() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("lol.pdf")    )
        {
            PDDocument document = Loader.loadPDF(resource);
// Option 1: Remove Rotate entries
//            for (PDPage page : document.getPages()) {
//                page.setRotation(0);
//            }

            PDFTextStripper stripper = new PDFTextStripper();
// Option 2: Sort by position
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* lol.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "lol.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/54644435/error-when-extracting-text-from-pdf-using-pdfbox">
     * Error when extracting text from pdf using pdfbox
     * </a>
     * <br/>
     * <a href="http://ishouhuo.cn/cannotExtract.pdf">
     * cannotExtract.pdf
     * </a>
     * <p>
     * Indeed, all required information for text extraction are missing from the font
     * PingFangSC in all its variants. Thus, text extraction results automatically are
     * lacking.
     * </p>
     */
    @Test
    public void testCannotExtract() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("cannotExtract.pdf")    )
        {
            PDDocument document =  Loader.loadPDF(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            System.out.printf("\n*\n* cannotExtract.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "cannotExtract.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/54822124/pdftextstripperbyarea-and-pdftextstripper-parsing-different-text-output-for-tabl">
     * PDFTextStripperByArea and PDFTextStripper parsing different Text Output for Table with Merged Cell or Table cell with multi-line text content
     * </a>
     * <br/>
     * <a href="https://www4.esc13.net/uploads/webccat/docs/PDFTables_12142005.pdf">
     * PDFTables_12142005.pdf
     * </a>
     * <p>
     * Cannot reproduce the problem, and the OP does not react to clarification requests.
     * </p>
     */
    @Test
    public void testPDFTables_12142005() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("PDFTables_12142005.pdf")    )
        {
            PDDocument document =  Loader.loadPDF(resource);

            PDFTextStripper textStripper = new PDFTextStripper();
            textStripper.setSortByPosition(true);
            textStripper.setAddMoreFormatting(false);
            // textStripper.setSpacingTolerance(1.5F);
            //textStripper.setAverageCharTolerance(averageCharToleranceValue);

            textStripper.setStartPage(2);
            textStripper.setEndPage(2);

            textStripper.getCurrentPage();
            String text = textStripper.getText(document).trim();
            System.out.println("PDF text is: " + "\n" + text.trim());

            System.out.println("----------------------------------------------------------------");

            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);
            stripper.setAddMoreFormatting(false);
            // stripper.setSpacingTolerance(1.5F);

            Dimension dimension = new Dimension();
            dimension.setSize(document.getPage(1).getMediaBox().getWidth(),
                    document.getPage(1).getMediaBox().getHeight());
//            Rectangle2D rect = toJavaRect(document.getBleedBox(), dimension);
//            Rectangle2D rect1 = toJavaRect(document.getArtBox(), dimension);
            PDRectangle mediaBox = document.getPage(1).getMediaBox();
            Rectangle2D rect = new Rectangle2D.Float(mediaBox.getLowerLeftX(), mediaBox.getLowerLeftY(), mediaBox.getWidth(), mediaBox.getHeight());
            Rectangle2D rect1 = rect;

            /*
             * Rectangle2D rect = new
             * Rectangle2D.Float(document.getBleedBox().getLowerLeftX(),
             * document.getBleedBox().getLowerLeftY(), document.getBleedBox().getWidth(),
             * document.getBleedBox().getHeight());
             */

            /*
             * Rectangle2D rect1 = new
             * Rectangle2D.Float(document.getArtBox().getLowerLeftX(),
             * document.getArtBox().getLowerLeftY(), document.getArtBox().getWidth(),
             * document.getArtBox().getHeight());
             */

            /*
             * Rectangle2D rect = new
             * Rectangle2D.Float(document.getBleedBox().getLowerLeftX(),
             * document.getBleedBox().getUpperRightY(), document.getBleedBox().getWidth(),
             * document.getBleedBox().getHeight());
             */

            System.out.println("Rectangle bleedBox Content : " + "\n" + rect);
            System.out.println("----------------------------------------------------------------");
            System.out.println("Rectangle artBox Content : " + "\n" + rect1);
            System.out.println("----------------------------------------------------------------");
            stripper.addRegion("Test1", rect);
            stripper.addRegion("Test2", rect1);
            stripper.extractRegions(document.getPage(1));

            System.out.println("Text in the area-BleedBox : " + "\n" + stripper.getTextForRegion("Test1").trim());
            System.out.println("----------------------------------------------------------------");
            System.out.println("Text in the area1-ArtBox : " + "\n" + stripper.getTextForRegion("Test2").trim());
            System.out.println("----------------------------------------------------------------");

            StringBuilder artPlusBleedBox = new StringBuilder();
            artPlusBleedBox.append(stripper.getTextForRegion("Test2").trim());
            artPlusBleedBox.append("\r\n");
            artPlusBleedBox.append(stripper.getTextForRegion("Test1").trim());

            System.out.println("Whole Page Text : " + artPlusBleedBox);
            System.out.println("----------------------------------------------------------------");
            text = new String(text.trim().getBytes(), "UTF-8");
            String text2 = new String(artPlusBleedBox.toString().trim().getBytes(), "UTF-8");
            System.out.println(" Matches equals with Both Content : " + text.equals(artPlusBleedBox.toString()));
            System.out.println(" String Matches equals with Both Content : " + text.equalsIgnoreCase(text2));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/55154930/unable-to-extract-values-from-pdf-for-specific-coordinates-using-java-apache-pdf">
     * Unable to extract values from PDF for specific coordinates using java apache pdfbox
     * </a>
     * <br/>
     * <a href="https://www.tutorialspoint.com/uipath/uipath_tutorial.pdf">
     * uipath_tutorial.pdf
     * </a>
     * <p>
     * Cannot reproduce the issue. I retrieve more than the text "a part of contests"
     * but considering the width and height that is not surprising. 
     * </p>
     */
    @Test
    public void testUiPathTutorial() throws IOException {
        System.out.println("Extracting like Venkatachalam Neelakantan from uipath_tutorial.pdf\n");
        float MM_TO_UNITS = 1/(10*2.54f)*72;
        String text = getTextUsingPositionsUsingPdf("src/test/resources/mkl/testarea/pdfbox2/extract/uipath_tutorial.pdf",
                0, 55.6 * MM_TO_UNITS, 168.8 * MM_TO_UNITS, 210.0 * MM_TO_UNITS, 297.0 * MM_TO_UNITS);
        System.out.printf("\n---\nResult:\n%s\n", text);
    }

    /**
     * @see #testUiPathTutorial()
     * @author Venkatachalam Neelakantan
     */
    public String getTextUsingPositionsUsingPdf(String pdfLocation, int pageNumber, double x, double y, double width,
            double height) throws IOException {
        String extractedText = "";
        // PDDocument Creates an empty PDF document. You need to add at least
        // one page for the document to be valid.
        // Using load method we can load a PDF document
        PDDocument document = null;
        PDPage page = null;
        try {
            if (pdfLocation.endsWith(".pdf")) {
                document = Loader.loadPDF(new File(pdfLocation));
                int getDocumentPageCount = document.getNumberOfPages();
                System.out.println(getDocumentPageCount);

                // Get specific page. THe parameter is pageindex which starts with // 0. If we need to
                // access the first page then // the pageIdex is 0 PDPage
                if (getDocumentPageCount > 0) {
                    page = document.getPage(pageNumber + 1);
                } else if (getDocumentPageCount == 0) {
                    page = document.getPage(0);
                }
                // To create a rectangle by passing the x axis, y axis, width and height 
                Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);
                String regionName = "region1";

                // Strip the text from PDF using PDFTextStripper Area with the
                // help of Rectangle and named need to given for the rectangle
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);
                stripper.addRegion(regionName, rect);
                stripper.extractRegions(page);
                System.out.println("Region is " + stripper.getTextForRegion("region1"));
                extractedText = stripper.getTextForRegion("region1");
            } else {
                System.out.println("No data return");
            }
        } catch (IOException e) {
            System.out.println("The file  not found" + "");
        } finally {
            document.close();
        }
        // Return the extracted text and this can be used for assertion
        return extractedText;
    }

    /**
     * <a href="https://stackoverflow.com/a/56580253/1729265">
     * wen li's answer to "PDFBox extracting paragraphs"
     * </a>
     * <br/>
     * <a href="https://www.adobe.com/content/dam/acom/en/devnet/pdf/pdfs/PDF32000_2008.pdf">
     * PDF32000_2008.pdf
     * </a>
     * <p>
     * Here it looks the other way around compared to what the OP claims:
     * there is a space at the end of all but the last paragraph line.
     * </p>
     */
    @Test
    public void testPDF32000pageii() throws IOException
    {
        try (   InputStream resource = new URL("https://www.adobe.com/content/dam/acom/en/devnet/pdf/pdfs/PDF32000_2008.pdf").openStream()    )
        {
            PDDocument document = Loader.loadPDF(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(2);
            stripper.setEndPage(2);
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* PDF32000_2008.pdf Page ii\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "PDF32000_2008-page-ii.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/a/56580253/1729265">
     * wen li's answer to "PDFBox extracting paragraphs"
     * </a>
     * <br/>
     * <a href="https://www.adobe.com/content/dam/acom/en/devnet/pdf/pdfs/PDF32000_2008.pdf">
     * PDF32000_2008.pdf
     * </a>
     * <p>
     * Here one sees that there is not always a space at the end of all
     * the non-last paragraph lines, "PDF/X" is split as "PDF/" and "X"
     * between lines, and there is no space in-between.
     * </p>
     */
    @Test
    public void testPDF32000pagevii() throws IOException
    {
        try (   InputStream resource = new URL("https://www.adobe.com/content/dam/acom/en/devnet/pdf/pdfs/PDF32000_2008.pdf").openStream()    )
        {
            PDDocument document = Loader.loadPDF(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(7);
            stripper.setEndPage(7);
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* PDF32000_2008.pdf Page ii\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "PDF32000_2008-page-vii.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/63047485/pdfbox-newer-version-extracts-data-in-jumbled-order">
     * PDFBox Newer version extracts data in jumbled order
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1RbTM4yQWbXGOXyNZAD8LvBlNMKfE8eNv/view?usp=sharing">
     * YOYO.pdf
     * </a>
     * <p>
     * Indeed, with <code>SortByPosition</code> one gets a hodgepodge of
     * the lines while without no line break characters are included.
     * </p>
     * <p>
     * This is caused by the font metadata containing data (Ascent, Descent,
     * CapHeight, and FontBBox) giving rise to the assumption that the glyphs
     * are twice as high than they actually are shown. This confuses line
     * recognition, both for sorting and for inserting line breaks.
     * </p>
     */
    @Test
    public void testYOYO() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("YOYO.pdf")) {
            Rectangle region = new Rectangle();
            region.setRect(55, 75.80, 160, 100);
            PDDocument pdfDoc = Loader.loadPDF(resource);
            PDFTextStripperByArea stripperByArea = new PDFTextStripperByArea ();
            stripperByArea.setSortByPosition(true);
            stripperByArea.addRegion("CVAM", region);
            stripperByArea.extractRegions(pdfDoc.getPages().get(0));
            String text = stripperByArea.getTextForRegion("CVAM");

            System.out.printf("\n*\n* YOYO.pdf region\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "YOYO.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/63047485/pdfbox-newer-version-extracts-data-in-jumbled-order">
     * PDFBox Newer version extracts data in jumbled order
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1RbTM4yQWbXGOXyNZAD8LvBlNMKfE8eNv/view?usp=sharing">
     * YOYO.pdf
     * </a>
     * <p>
     * Making use of the new option to add one's own font size calculation code,
     * though, one can improve the result, both for <code>SortByPosition</code>
     * set to <code>true</code> or <code>false</code>.
     * </p>
     * @see #testYOYO()
     */
    @Test
    public void testCustomFontHeightYOYO() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("YOYO.pdf")) {
            Rectangle region = new Rectangle();
            region.setRect(55, 75.80, 160, 100);
            PDDocument pdfDoc = Loader.loadPDF(resource);
            PDFTextStripperByArea stripperByArea = new PDFTextStripperByArea() {
                @Override
                protected float computeFontHeight(PDFont font) throws IOException {
                    return .5f;
                }
            };
            stripperByArea.setSortByPosition(false);
            stripperByArea.addRegion("CVAM", region);
            stripperByArea.extractRegions(pdfDoc.getPages().get(0));
            String text = stripperByArea.getTextForRegion("CVAM");

            System.out.printf("\n*\n* YOYO.pdf region custom height\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "YOYO-customHeight.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/63949522/getting-text-from-pdf-using-apache-pdfbox">
     * Getting text from PDF using Apache PDFBox
     * </a>
     * <br/>
     * <a href="https://dropmefiles.com/qr8VQ">
     * Olivia Troye_ Pence's former lead coronavirus task force aide slams Trump and endorses Biden in new video - CNNPolitics.pdf
     * </a>
     * <p>
     * Extraction results in the proper text to be expected. The issue was that
     * the OP's text viewer displayed the text assuming the wrong encoding.
     * </p>
     */
    @Test
    public void testOliviaTroyePencesFormerLeadCoronavirusTaskForceAideSlamsTrumpAndEndorsesBidenInNewVideoCNNPolitics() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("Olivia Troye_ Pence's former lead coronavirus task force aide slams Trump and endorses Biden in new video - CNNPolitics.pdf");
                FileWriter writer = new FileWriter(new File(RESULT_FOLDER, "Olivia Troye_ Pence's former lead coronavirus task force aide slams Trump and endorses Biden in new video - CNNPolitics.txt"))) {
            PDDocument document = Loader.loadPDF(resource);
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            String text = pdfTextStripper.getText(document);
            writer.write(text);
            document.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/73261590/pdfbox-cant-read-some-pdfs">
     * PDFBox can't read some PDF's
     * </a>
     * <br/>
     * <a href="https://ufile.io/p86kolu3">
     * Orchesterdienstplan I  8.8.-4.12.22.pdf
     * </a>
     * <p>
     * At first glance the text extracts just fine...
     * </p>
     */
    @Test
    public void testOrchesterdienstplanI8_8_4_12_22() throws IOException {
        try (
            InputStream resource = getClass().getResourceAsStream("Orchesterdienstplan I  8.8.-4.12.22.pdf");
            PDDocument pdDocument = Loader.loadPDF(resource)
        ) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setShouldSeparateByBeads(true);
            String text = stripper.getText(pdDocument);

            System.out.printf("\n*\n* Orchesterdienstplan I  8.8.-4.12.22.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "Orchesterdienstplan I  8.8.-4.12.22.txt").toPath(), Collections.singleton(text));
        }
    }
}
