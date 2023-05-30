package mkl.testarea.pdfbox2.content;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class IsolateFigures {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/76333586/images-inverted-and-split-when-extracting-images-from-pdf-document-by-using-pdfb">
     * images inverted and split when extracting images from pdf document by using PDFBox or Poppler
     * </a>
     * <br/>
     * <a href="https://banyafx.oss-cn-hangzhou.aliyuncs.com/assets/pdf/my.pdf">
     * my.pdf
     * </a>
     * <p>
     * Based on the initial observation that the figures in the documents are drawn without
     * text object insets, this code isolates the figures by writing each block between text
     * objects to a separate page.
     * </p>
     * <p>
     * Obviously, there are some such blocks that only consist of e.g. an underline. The code
     * attempts to drop them, see the conditional addition of the new page to the document in
     * <code>endFigurePage</code>.
     * </p>
     * <p>
     * As it turns out, there unfortunately are some figures which appear to have been patched
     * afterwards; those patches then are displayed on separate pages...
     * </p>
     */
    @Test
    public void testIsolateInMy() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("my.pdf");
                PDDocument document = PDDocument.load(resource)) {
            for (PDPage page : document.getDocumentCatalog().getPages()) {
                PdfContentStreamEditor editor = new PdfContentStreamEditor(document, page) {
                    ByteArrayOutputStream commonRaw = null;
                    ContentStreamWriter commonWriter = null;
                    int depth = 0;

                    @Override
                    public void processPage(PDPage page) throws IOException {
                        commonRaw = new ByteArrayOutputStream();
                        try {
                            commonWriter = new ContentStreamWriter(commonRaw);
                            startFigurePage(page);
                            super.processPage(page);
                        } finally {
                            endFigurePage();
                            commonRaw.close();
                        }
                    }

                    @Override
                    protected void write(ContentStreamWriter contentStreamWriter, Operator operator,
                            List<COSBase> operands) throws IOException {
                        String operatorString = operator.getName();
                        if (operatorString.equals("BT")) {
                            endFigurePage();
                        }
                        if (operatorString.equals("q")) {
                            depth++;
                        }
                        writeFigure(operator, operands);
                        if (operatorString.equals("Q")) {
                            depth--;
                        }
                        if (operatorString.equals("ET")) {
                            startFigurePage(getCurrentPage());
                        }

                        super.write(contentStreamWriter, operator, operands);
                    }

                    OutputStream figureRaw = null;
                    ContentStreamWriter figureWriter = null;
                    PDPage figurePage = null;
                    int xobjectsDrawn = 0;
                    int pathsPainted = 0;

                    void startFigurePage(PDPage currentPage) throws IOException {
                        figurePage = new PDPage(currentPage.getMediaBox());
                        figurePage.setResources(currentPage.getResources());
                        PDStream stream = new PDStream(document);
                        figurePage.setContents(stream);
                        figureWriter = new ContentStreamWriter(figureRaw = stream.createOutputStream(COSName.FLATE_DECODE));
                        figureRaw.write(commonRaw.toByteArray());
                        xobjectsDrawn = 0;
                        pathsPainted = 0;
                    }

                    void endFigurePage() throws IOException {
                        if (figureWriter != null) {
                            figureWriter = null;
                            figureRaw.close();
                            figureRaw = null;
                            if (xobjectsDrawn > 0 || pathsPainted > 3)
                                document.addPage(figurePage);
                            figurePage = null;
                        }
                    }

                    final List<String> PATH_PAINTING_OPERATORS = Arrays.asList("S", "s", "F", "f", "f*",
                            "B", "B*", "b", "b*");

                    void writeFigure(Operator operator, List<COSBase> operands) throws IOException {
                        if (figureWriter != null) {
                            String operatorString = operator.getName();
                            boolean isXObjectDo = operatorString.equals("Do");
                            boolean isPathPainting = PATH_PAINTING_OPERATORS.contains(operatorString);
                            if (isXObjectDo)
                                xobjectsDrawn++;
                            if (isPathPainting)
                                pathsPainted++;
                            figureWriter.writeTokens(operands);
                            figureWriter.writeToken(operator);
                            if (depth == 0) {
                                if (!isXObjectDo) {
                                    if (isPathPainting)
                                        operator = Operator.getOperator("n");
                                    commonWriter.writeTokens(operands);
                                    commonWriter.writeToken(operator);
                                }
                            }
                        }
                    }
                };
                editor.processPage(page);
            }
            document.save(new File(RESULT_FOLDER, "my-isolatedFigures.pdf"));
        }
    }

}
