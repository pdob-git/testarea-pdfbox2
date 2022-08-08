package mkl.testarea.pdfbox2.meta;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;

/**
 * @author mkl
 */
public class DetermineMaxObjectId {
    /**
     * <a href="https://stackoverflow.com/questions/64368995/why-does-the-trailer-object-report-a-previous-value-for-the-size-entry">
     * Why does the trailer object report a previous value for the “Size” entry?
     * </a>
     * <br/>
     * <a href="https://www.tecxoft.com/samples/sample01.pdf">
     * sample01.pdf
     * </a>
     * <p>
     * Indeed, due to a relic optimization of old time parsing of linearized
     * PDFs COSDictionary.addAll(COSDictionary) does not replace existing
     * entries for the Size key. The trailer object returned by
     * <code>COSDocument.getTrailer()</code> on the other hand is the union
     * of all trailers from earliest to newest. Thus, all entries in it have
     * the newest (existing) value except the Size entry having the oldest
     * one. 
     * </p>
     */
    @Test
    public void testGetMaxObjIdSample01() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("sample01.pdf");
                PDDocument document = PDDocument.load(resource)  ) {
            long maxObjectId = getMaxObjId(document);
            switch ((int)maxObjectId) {
            case 412:
                System.out.println("412 - last trailer");
                break;
            case 399:
                System.out.println("399 - linearization trailer front");
                break;
            case 218:
                System.out.println("218 - linearization trailer back");
                break;
            default:
                System.out.printf("%d - not from a trailer", maxObjectId);
                break;
            }
        }
    }

    private static long getMaxObjId(PDDocument doc) {
        COSDocument cosdoc = doc.getDocument();
        COSDictionary trailer = cosdoc.getTrailer();
        long maxobj = trailer.getLong(COSName.SIZE);
        return maxobj;
    }
}
