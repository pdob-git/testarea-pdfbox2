package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * <a href="https://stackoverflow.com/questions/68402058/tika-isnt-reading-pdf-properly">
 * Tika isnt reading pdf properly
 * </a>
 * <p>
 * To analyze cases like the document this question refers to, this class
 * allows to enlarge the visible canvas of document pages and remove clip
 * paths.
 * </p>
 * 
 * @author mkl
 */
public class ShowVicinity {
    public static void main(String[] args) throws IOException {
        ShowVicinity showVicinity = new ShowVicinity(2, 0, true);
        for (String arg: args) {
            System.out.printf("***\n*** %s\n***\n\n", arg);
            final File file = new File(arg);
            if (file.exists()) {
                File target = new File(file.getParent(), file.getName() + "-withVicinity.pdf");
                showVicinity.addVicinity(file, target);
                System.out.println("   enlarged to show vicinity also.\n");
            } else
                System.err.println("!!! File does not exist: " + file);
        }
    }

    public ShowVicinity(float horizontalFactor, float verticalFactor, boolean unclip) {
        this.horizontalFactor = horizontalFactor;
        this.verticalFactor = verticalFactor;
        this.unclip = unclip;
    }

    public void addVicinity(File source, File target) throws IOException {
        try (   PDDocument document = PDDocument.load(source)    ) {
            for (PDPage page : document.getPages()) {
                page.setArtBox(null);
                page.setTrimBox(null);
                page.setCropBox(null);
                PDRectangle original = page.getMediaBox();
                PDRectangle enlarged = new PDRectangle(
                        original.getLowerLeftX() - horizontalFactor * original.getWidth(),
                        original.getLowerLeftY() - verticalFactor * original.getHeight(),
                        (1 + 2*horizontalFactor) * original.getWidth(),
                        (1 + 2*verticalFactor) * original.getHeight());
                page.setMediaBox(enlarged);

                if (unclip) {
                    PdfContentStreamEditor editor = new PdfContentStreamEditor(document, page) {
                        @Override
                        protected void write(ContentStreamWriter contentStreamWriter, Operator operator, List<COSBase> operands) throws IOException {
                            String operatorString = operator.getName();

                            if (!CLIPPING_OPERATORS.contains(operatorString))
                            {
                                super.write(contentStreamWriter, operator, operands);
                            }
                        }

                        final List<String> CLIPPING_OPERATORS = Arrays.asList("W", "W*");
                    };
                    editor.processPage(page);
                }
            }
            document.setAllSecurityToBeRemoved(true);
            document.save(target);
        }
    }

    final float horizontalFactor;
    final float verticalFactor;
    final boolean unclip;
}
