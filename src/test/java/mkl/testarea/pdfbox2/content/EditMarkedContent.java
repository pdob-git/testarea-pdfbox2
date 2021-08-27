package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class EditMarkedContent {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/68933448/java-pdfbox-creating-the-artifact-tag-for-lines-and-underlines-in-tagged-pdf">
     * Java-PDFbox: Creating the artifact tag for lines and underlines in tagged PDF
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1Z1R-SIalxPzAHH57_Qs0zGPDV3rjoqtN/view">
     * Trade_Simple1.pdf
     * </a>
     * <p>
     * This test shows how to enclose unmarked path construction and painting instruction
     * sequences in an Artifact marked content sections.
     * </p>
     */
    @Test
    public void testMarkUnmarkedPathsAsArtifactsTradeSimple1() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("Trade_Simple1.pdf");
                PDDocument document = Loader.loadPDF(resource)) {
            for (PDPage page : document.getDocumentCatalog().getPages()) {
                PdfContentStreamEditor markEditor = new PdfContentStreamEditor(document, page) {
                    int markedContentDepth = 0;

                    @Override
                    public void beginMarkedContentSequence(COSName tag, COSDictionary properties) {
                        if (inArtifact) {
                            System.err.println("Structural error in content stream: Path not properly closed by path painting instruction.");
                        }
                        markedContentDepth++;
                        super.beginMarkedContentSequence(tag, properties);
                    }

                    @Override
                    public void endMarkedContentSequence() {
                        markedContentDepth--;
                        super.endMarkedContentSequence();
                    }

                    boolean inArtifact = false;

                    @Override
                    protected void write(ContentStreamWriter contentStreamWriter, Operator operator, List<COSBase> operands) throws IOException {
                        String operatorString = operator.getName();

                        boolean unmarked = markedContentDepth == 0;
                        boolean inArtifactBefore = inArtifact;

                        if (unmarked && (!inArtifactBefore) && PATH_CONSTRUCTION.contains(operatorString)) {
                            super.write(contentStreamWriter, Operator.getOperator("BMC"), Collections.<COSBase>singletonList(COSName.ARTIFACT));
                            inArtifact = true;
                        }

                        super.write(contentStreamWriter, operator, operands);

                        if (unmarked && inArtifactBefore && PATH_PAINTING.contains(operatorString)) {
                            super.write(contentStreamWriter, Operator.getOperator("EMC"), Collections.<COSBase>emptyList());
                            inArtifact = false;
                        }
                    }

                    final List<String> PATH_CONSTRUCTION = Arrays.asList("m", "l", "c", "v", "y", "h", "re");
                    final List<String> PATH_PAINTING = Arrays.asList("s", "S", "f", "F", "f*", "B", "B*", "b", "b*", "n");
                };
                markEditor.processPage(page);
            }
            document.save(new File(RESULT_FOLDER, "Trade_Simple1-marked.pdf"));
        }
    }
}
