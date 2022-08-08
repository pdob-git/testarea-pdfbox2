package mkl.testarea.pdfbox2.meta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDNumberTreeNode;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDParentTreeValue;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureNode;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class RebuildParentTreeFromStructure {
    final static File RESULT_FOLDER = new File("target/test-outputs", "meta");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/59377820/how-to-heal-inconsistent-parent-tree-mappings-in-a-pdf-created-by-pdfbox">
     * How to heal inconsistent parent tree mappings in a PDF created by pdfBox
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/fq6m3o4rx9swq76/Testdatei.pdf?dl=0">
     * Testdatei.pdf
     * </a>
     * <p>
     * This test applies {@link #rebuildParentTree(PDDocument)} to the OP's
     * example file.
     * </p>
     */
    @Test
    public void testTestdatei() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("Testdatei.pdf");
                PDDocument document = PDDocument.load(resource)) {
            rebuildParentTree(document);
            document.save(new File(RESULT_FOLDER, "Testdatei-rebuiltParents.pdf"));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/59377820/how-to-heal-inconsistent-parent-tree-mappings-in-a-pdf-created-by-pdfbox">
     * How to heal inconsistent parent tree mappings in a PDF created by pdfBox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1aD1HGQsEXOovpfWdf7JRNwJhP7tX6pmy/view?usp=sharing">
     * mathpdf.pdf
     * </a>
     * <br/>
     * <a href="https://stackoverflow.com/questions/57591441/find-tag-from-selection-is-not-working-in-tagged-pdf">
     * “Find Tag from Selection” is not working in tagged pdf?
     * </a>
     * <p>
     * This test applies {@link #rebuildParentTree(PDDocument)} to an
     * example file from another question.
     * </p>
     */
    @Test
    public void testMathpdf() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("mathpdf.pdf");
                PDDocument document = PDDocument.load(resource)) {
            rebuildParentTree(document);
            document.save(new File(RESULT_FOLDER, "mathpdf-rebuiltParents.pdf"));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/59377820/how-to-heal-inconsistent-parent-tree-mappings-in-a-pdf-created-by-pdfbox">
     * How to heal inconsistent parent tree mappings in a PDF created by pdfBox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1KTwtyd4J1_hTx4o3xGoA-MR9GpqL8JqS/view?usp=sharing">
     * res29_08_19.pdf
     * </a>
     * <br/>
     * <a href="https://stackoverflow.com/questions/57591441/find-tag-from-selection-is-not-working-in-tagged-pdf">
     * “Find Tag from Selection” is not working in tagged pdf?
     * </a>
     * <p>
     * This test applies {@link #rebuildParentTree(PDDocument)} to an
     * example file from another question.
     * </p>
     */
    @Test
    public void testRes29_08_19() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("res29_08_19.pdf");
                PDDocument document = PDDocument.load(resource)) {
            rebuildParentTree(document);
            document.save(new File(RESULT_FOLDER, "res29_08_19-rebuiltParents.pdf"));
        }
    }

    /**
     * <p>
     * This method tries to rebuild the structure parent tree
     * from information from the structure elements.
     * </p>
     * <p>
     * Beware, this method has been built with only page content
     * streams with marked content in mind, it likely will not yet
     * work properly for content in XObjects etc.
     * </p>
     */
    void rebuildParentTree(PDDocument document) {
        PDStructureTreeRoot root = document.getDocumentCatalog().getStructureTreeRoot();
        Map<PDPage, Map<Integer, PDStructureNode>> parentsByPage = new HashMap<>();
        collect(null, root, parentsByPage);
        rebuildParentTreeFromData(root, parentsByPage);
    }

    /**
     * This method recursively collects marked content parents by page
     * in the map parameter. It inspects the given structure node and adds
     * its kids to the map. The given page is the page referenced in 
     * containing structure nodes and, therefore, can be <code>null</code>.
     * 
     * @see RebuildParentTreeFromStructure#rebuildParentTree(PDDocument)
     */
    void collect(PDPage page, PDStructureNode node, Map<PDPage, Map<Integer, PDStructureNode>> parentsByPage) {
        COSDictionary pageDictionary = node.getCOSObject().getCOSDictionary(COSName.PG);
        if (pageDictionary != null) {
            page = new PDPage(pageDictionary);
        }

        for (Object object : node.getKids()) {
            if (object instanceof COSArray) {
                for (COSBase base : (COSArray) object) {
                    if (base instanceof COSDictionary) {
                        collect(page, PDStructureNode.create((COSDictionary) base), parentsByPage);
                    } else if (base instanceof COSNumber) {
                        setParent(page, node, ((COSNumber)base).intValue(), parentsByPage);
                    } else {
                        System.out.printf("?%s\n", base);
                    }
                }
            } else if (object instanceof PDStructureNode) {
                collect(page, (PDStructureNode) object, parentsByPage);
            } else if (object instanceof Integer) {
                setParent(page, node, (Integer)object, parentsByPage);
            } else {
                System.out.printf("?%s\n", object);
            }
        }
    }

    /**
     * Helper method adding an entry mapping the given page and mcid to
     * the given structure node, creating inner maps if need be.
     * 
     * @see #collect(PDPage, PDStructureNode, Map)
     */
    void setParent(PDPage page, PDStructureNode node, int mcid, Map<PDPage, Map<Integer, PDStructureNode>> parentsByPage) {
        if (node == null) {
            System.err.printf("Cannot set null as parent of MCID %s.\n", mcid);
        } else if (page == null) {
            System.err.printf("Cannot set parent of MCID %s for null page.\n", mcid);
        } else {
            Map<Integer, PDStructureNode> parents = parentsByPage.get(page);
            if (parents == null) {
                parents = new HashMap<>();
                parentsByPage.put(page, parents);
            }
            if (parents.containsKey(mcid)) {
                System.err.printf("MCID %s already has a parent. New parent rejected.\n", mcid);
            } else {
                parents.put(mcid, node);
            }
        }
    }

    /**
     * This method creates a new parent tree in the given structure
     * tree root based on the contents of the mapping of page and
     * MCID to structure node.
     * 
     * @see #rebuildParentTree(PDDocument)
     */
    void rebuildParentTreeFromData(PDStructureTreeRoot root, Map<PDPage, Map<Integer, PDStructureNode>> parentsByPage) {
        int parentTreeMaxkey = -1;
        Map<Integer, COSArray> numbers = new HashMap<>();

        for (Map.Entry<PDPage, Map<Integer, PDStructureNode>> entry : parentsByPage.entrySet()) {
            int parentsId = entry.getKey().getCOSObject().getInt(COSName.STRUCT_PARENTS);
            if (parentsId < 0) {
                System.err.printf("Page without StructsParents. Ignoring %s MCIDs.\n", entry.getValue().size());
            } else {
                if (parentTreeMaxkey < parentsId)
                    parentTreeMaxkey = parentsId;
                COSArray array = new COSArray();
                for (Map.Entry<Integer, PDStructureNode> subEntry : entry.getValue().entrySet()) {
                    array.growToSize(subEntry.getKey() + 1);
                    array.set(subEntry.getKey(), subEntry.getValue());
                }
                numbers.put(parentsId, array);
            }
        }

        PDNumberTreeNode numberTreeNode = new PDNumberTreeNode(PDParentTreeValue.class);
        numberTreeNode.setNumbers(numbers);
        root.setParentTree(numberTreeNode);
        root.setParentTreeNextKey(parentTreeMaxkey + 1);
    }
}
