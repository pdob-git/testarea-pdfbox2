package mkl.testarea.pdfbox2.meta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureNode;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class MoveInStructureTree {
    final static File RESULT_FOLDER = new File("target/test-outputs", "meta");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/69040397/java-pdfbox-remove-the-parent-element-in-tagged-pdf">
     * Java PDFBox: Remove the parent element in tagged PDF
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1Z1R-SIalxPzAHH57_Qs0zGPDV3rjoqtN/view">
     * Trade_Simple1.pdf
     * </a>
     * <p>
     * This test shows how to push all Table structure elements one layer up,
     * replacing its respective parent.
     * </p>
     */
    @Test
    public void testMoveTableUpTradeSimple1() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/pdfbox2/content/Trade_Simple1.pdf");
                PDDocument pdDocument = Loader.loadPDF(resource)    ) {
            PDStructureTreeRoot root = pdDocument.getDocumentCatalog().getStructureTreeRoot();
            List<Object> kids = root.getKids();
            for (Object kid: kids) {
                checkAndMoveTableUp(kid);
            }
            pdDocument.save(new File(RESULT_FOLDER, "Trade_Simple1-tableUp.pdf"));
        }
    }

    /** @see #testMoveTableUpTradeSimple1() */
    void checkAndMoveTableUp(Object element) {
        if (element instanceof PDStructureElement) {
            PDStructureElement pdStructureElement = (PDStructureElement) element;
            if ("Table".equals(pdStructureElement.getStructureType())) {
                PDStructureNode parentNode = pdStructureElement.getParent();
                if (parentNode instanceof PDStructureElement) {
                    PDStructureElement parent = (PDStructureElement) parentNode;
                    PDStructureNode newParentNode = parent.getParent();
                    if (newParentNode != null) {
                        newParentNode.insertBefore(pdStructureElement, parent);
                        pdStructureElement.setParent(newParentNode);
                        newParentNode.removeKid(parent);
                    }
                }
            }
        }
        if (element instanceof PDStructureNode) {
            for (Object kid: ((PDStructureNode)element).getKids()) {
                checkAndMoveTableUp(kid);
            }
        }
    }
}
