package mkl.testarea.pdfbox2.annotate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mklink
 *
 */
public class RemoveStrikeoutComment {
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/45812696/pdfbox-delete-comment-maintain-strikethrough">
     * PDFBox delete comment maintain strikethrough
     * </a>
     * <br/>
     * <a href="https://expirebox.com/files/3d955e6df4ca5874c38dbf92fc43b5af.pdf">
     * only_fields.pdf
     * </a>
     * <a href="https://file.io/DTvqhC">
     * (alternative download)
     * </a>
     * <p>
     * Due to a bug in the <code>COSArrayList</code> usage for page annotations,
     * the indirect reference to the annotation in question is not removed from
     * the actual page annotations array.
     * </p>
     */
    @Test
    public void testRemoveLikeStephan() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("only_fields.pdf")) {
            PDDocument document = PDDocument.load(resource);
            List<PDAnnotation> annotations = new ArrayList<>();
            PDPageTree allPages = document.getDocumentCatalog().getPages();

            for (int i = 0; i < allPages.getCount(); i++) {
                PDPage page = allPages.get(i);
                annotations = page.getAnnotations();

                List<PDAnnotation> annotationToRemove = new ArrayList<PDAnnotation>();

                if (annotations.size() < 1)
                    continue;
                else {
                    for (PDAnnotation annotation : annotations) {

                        if (annotation.getContents() != null
                                && annotation.getContents().equals("Sample Strikethrough")) {
                            annotationToRemove.add(annotation);
                        }
                    }
                    annotations.removeAll(annotationToRemove);
                }
            }

            document.save(new File(RESULT_FOLDER, "only_fields-removeLikeStephan.pdf"));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/45812696/pdfbox-delete-comment-maintain-strikethrough">
     * PDFBox delete comment maintain strikethrough
     * </a>
     * <br/>
     * <a href="https://expirebox.com/files/3d955e6df4ca5874c38dbf92fc43b5af.pdf">
     * only_fields.pdf
     * </a>
     * <a href="https://file.io/DTvqhC">
     * (alternative download)
     * </a>
     * <p>
     * The OP only wanted the comment removed, not the strike-through. Thus, we must
     * not remove the annotation but merely the comment building attributes.
     * </p>
     */
    @Test
    public void testRemoveLikeStephanImproved() throws IOException {
        final COSName POPUP = COSName.getPDFName("Popup");
        try (InputStream resource = getClass().getResourceAsStream("only_fields.pdf")) {
            PDDocument document = PDDocument.load(resource);
            List<PDAnnotation> annotations = new ArrayList<>();
            PDPageTree allPages = document.getDocumentCatalog().getPages();

            List<COSObjectable> objectsToRemove = new ArrayList<>();

            for (int i = 0; i < allPages.getCount(); i++) {
                PDPage page = allPages.get(i);
                annotations = page.getAnnotations();

                for (PDAnnotation annotation : annotations) {
                    if ("StrikeOut".equals(annotation.getSubtype()))
                    {
                        COSDictionary annotationDict = annotation.getCOSObject();
                        COSBase popup = annotationDict.getItem(POPUP);
                        annotationDict.removeItem(POPUP);
                        annotationDict.removeItem(COSName.CONTENTS); // plain text comment
                        annotationDict.removeItem(COSName.RC);       // rich text comment
                        annotationDict.removeItem(COSName.T);        // author

                        if (popup != null)
                            objectsToRemove.add(popup);
                    }
                }

                annotations.removeAll(objectsToRemove);
            }

            document.save(new File(RESULT_FOLDER, "only_fields-removeImproved.pdf"));
        }
    }
}
