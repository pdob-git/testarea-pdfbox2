package mkl.testarea.pdfbox2.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.BeforeClass;
import org.junit.Test;

import mkl.testarea.pdfbox2.extract.TextSectionDefinition.MultiLine;

/**
 * @author mkl
 */
public class ExtractTextSections
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/41654518/how-to-read-pdf-departmentsheader-abstract-refrences-with-pdfbox">
     * How to read PDF departments(header,abstract,refrences) With PDFBox?
     * </a>
     * <br/>
     * <a href="http://merlot.usc.edu/csac-f06/papers/Wang05a.pdf">
     * Wang05a.pdf
     * </a>
     * <p>
     * This test demonstrates a simple framework for extraction of semantic text sections
     * which are recognizable by their characteristics of each line alone.
     * </p>
     */
    @Test
    public void testWang05a() throws IOException
    {
        List<TextSectionDefinition> sectionDefinitions = Arrays.asList(
                new TextSectionDefinition("Titel", x->x.get(0).get(0).getFont().getName().contains("CMBX12"), MultiLine.singleLine, false),
                new TextSectionDefinition("Authors", x->x.get(0).get(0).getFont().getName().contains("CMR10"), MultiLine.multiLine, false),
                new TextSectionDefinition("Institutions", x->x.get(0).get(0).getFont().getName().contains("CMR9"), MultiLine.multiLine, false),
                new TextSectionDefinition("Addresses", x->x.get(0).get(0).getFont().getName().contains("CMTT9"), MultiLine.multiLine, false),
                new TextSectionDefinition("Abstract", x->x.get(0).get(0).getFont().getName().contains("CMBX9"), MultiLine.multiLineIntro, false),
                new TextSectionDefinition("Section", x->x.get(0).get(0).getFont().getName().contains("CMBX12"), MultiLine.multiLineHeader, true)
                );
        try (   InputStream resource = getClass().getResourceAsStream("Wang05a.pdf")    )
        {
            PDDocument document = PDDocument.load(resource);
            PDFTextSectionStripper stripper = new PDFTextSectionStripper(sectionDefinitions);
            stripper.getText(document);

            System.out.println("Sections:");
            List<String> texts = new ArrayList<>();
            for (TextSection textSection : stripper.getSections())
            {
                String text = textSection.toString();
                System.out.println(text);
                texts.add(text);
            }
            Files.write(new File(RESULT_FOLDER, "Wang05a.txt").toPath(), texts);
        }
    }
}
