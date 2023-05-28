package mkl.testarea.pdfbox2.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureNode;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;
import org.apache.pdfbox.text.PDFMarkedContentExtractor;
import org.apache.pdfbox.text.TextPosition;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ExtractMarkedContent {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/54956720/how-to-replace-a-space-with-a-word-while-extract-the-data-from-pdf-using-pdfbox">
     * How to replace a space with a word while extract the data from PDF using PDFBox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=10ZkdPlGWzMJeahwnQPzE6V7s09d1nvwq">
     * test.pdf
     * </a> as "testWPhromma.pdf"
     * <p>
     * This test shows how to, in principle, extract tagged text.
     * </p>
     */
    @Test
    public void testExtractTestWPhromma() throws IOException {
        System.out.printf("\n\n===\n%s\n===\n", "testWPhromma.pdf");
        try (   InputStream resource = getClass().getResourceAsStream("testWPhromma.pdf")) {
            PDDocument document = PDDocument.load(resource);

            Map<PDPage, Map<Integer, PDMarkedContent>> markedContents = new HashMap<>();

            for (PDPage page : document.getPages()) {
                PDFMarkedContentExtractor extractor = new PDFMarkedContentExtractor();
                extractor.processPage(page);

                Map<Integer, PDMarkedContent> theseMarkedContents = new HashMap<>();
                markedContents.put(page, theseMarkedContents);
                for (PDMarkedContent markedContent : extractor.getMarkedContents()) {
                    theseMarkedContents.put(markedContent.getMCID(), markedContent);
                }
            }

            PDStructureNode root = document.getDocumentCatalog().getStructureTreeRoot();
            showStructure(root, markedContents);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/59192443/get-tags-related-bboxs-even-though-there-is-no-attributes-a-in-document-cata">
     * Get tag's related BBox's even though there is no attributes (/A in document catalog structure) related to Layout in PDFBox?
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1_-tuWuReaTvrDsqQwldTnPYrMHSpXIWp/view?usp=sharing">
     * res_multipage.pdf
     * </a>
     * <p>
     * This test shows how to, in principle, extract tagged text from this document.
     * </p>
     */
    @Test
    public void testExtractResMultipage() throws IOException {
        System.out.printf("\n\n===\n%s\n===\n", "res_multipage.pdf");
        try (   InputStream resource = getClass().getResourceAsStream("res_multipage.pdf")) {
            PDDocument document = PDDocument.load(resource);

            Map<PDPage, Map<Integer, PDMarkedContent>> markedContents = new HashMap<>();

            for (PDPage page : document.getPages()) {
                PDFMarkedContentExtractor extractor = new PDFMarkedContentExtractor();
                extractor.processPage(page);

                Map<Integer, PDMarkedContent> theseMarkedContents = new HashMap<>();
                markedContents.put(page, theseMarkedContents);
                for (PDMarkedContent markedContent : extractor.getMarkedContents()) {
                    theseMarkedContents.put(markedContent.getMCID(), markedContent);
                }
            }

            PDStructureNode root = document.getDocumentCatalog().getStructureTreeRoot();
            showStructure(root, markedContents);
        }
    }

    /**
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-5613">
     * PDFBOX-5613 - uncorrent paragraph split
     * </a>
     * <br/>
     * <a href="https://issues.apache.org/jira/secure/attachment/13058595/Daily%20Report.pdf">
     * Daily Report.pdf
     * </a>
     * <p>
     * Making use of the marked content, the contents of the file can better be extracted.
     * </p>
     */
    @Test
    public void testExtractDailyReport() throws IOException {
        System.out.printf("\n\n===\n%s\n===\n", "Daily Report.pdf");
        try (   InputStream resource = getClass().getResourceAsStream("Daily Report.pdf")) {
            PDDocument document = PDDocument.load(resource);

            Map<PDPage, Map<Integer, PDMarkedContent>> markedContents = new HashMap<>();

            for (PDPage page : document.getPages()) {
                PDFMarkedContentExtractor extractor = new PDFMarkedContentExtractor();
                extractor.processPage(page);

                Map<Integer, PDMarkedContent> theseMarkedContents = new HashMap<>();
                markedContents.put(page, theseMarkedContents);
                for (PDMarkedContent markedContent : extractor.getMarkedContents()) {
                    theseMarkedContents.put(markedContent.getMCID(), markedContent);
                }
            }

            PDStructureNode root = document.getDocumentCatalog().getStructureTreeRoot();
            showStructure(root, markedContents);
        }
    }

    /**
     * @see #testExtractTestWPhromma()
     */
    void showStructure(PDStructureNode node, Map<PDPage, Map<Integer, PDMarkedContent>> markedContents) {
        String structType = null;
        PDPage page = null;
        if (node instanceof PDStructureElement) {
            PDStructureElement element = (PDStructureElement) node;
            structType = element.getStructureType();
            page = element.getPage();
        }
        Map<Integer, PDMarkedContent> theseMarkedContents = markedContents.get(page);
        System.out.printf("<%s>\n", structType);
        for (Object object : node.getKids()) {
            if (object instanceof COSArray) {
                for (COSBase base : (COSArray) object) {
                    if (base instanceof COSDictionary) {
                        showStructure(PDStructureNode.create((COSDictionary) base), markedContents);
                    } else if (base instanceof COSNumber) {
                        showContent(((COSNumber)base).intValue(), theseMarkedContents);
                    } else {
                        System.out.printf("?%s\n", base);
                    }
                }
            } else if (object instanceof PDStructureNode) {
                showStructure((PDStructureNode) object, markedContents);
            } else if (object instanceof Integer) {
                showContent((Integer)object, theseMarkedContents);
            } else {
                System.out.printf("?%s\n", object);
            }

        }
        System.out.printf("</%s>\n", structType);
    }

    /**
     * @see #showStructure(PDStructureNode, Map)
     * @see #testExtractTestWPhromma()
     */
    void showContent(int mcid, Map<Integer, PDMarkedContent> theseMarkedContents) {
        PDMarkedContent markedContent = theseMarkedContents != null ? theseMarkedContents.get(mcid) : null;
        List<Object> contents = markedContent != null ? markedContent.getContents() : Collections.emptyList();
        StringBuilder textContent =  new StringBuilder();
        for (Object object : contents) {
            if (object instanceof TextPosition) {
                textContent.append(((TextPosition)object).getUnicode());
            } else {
                textContent.append("?" + object);
            }
        }
        System.out.printf("%s\n", textContent);
    }
}
