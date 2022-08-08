package mkl.testarea.pdfbox2.merge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class MergeDocuments
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/42283777/pdfbox-writing-compressed-object-streams">
     * pdfbox writing compressed object streams
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/sh/elbjegfykxux6wf/AAC8SMU6-7_sAPS7yqgZkDn0a?dl=0">
     * "00002 - Distribui��o - dia 10.11.2016.pdf",
     * "00041 - Digita��o de Documentos - dia 10.11.2016.pdf",
     * "00041 - Digita��o de Documentos - dia 10.11.2016_00042 - Cita��o para Processo Eletr�nico - dia 10.11.2016.pdf" 
     * </a>
     * <p>
     * Given the files the OP shared one cannot reproduce the file size increment
     * claimed by the OP to be caused by `PDFMergerUtility`. If using a sufficiently
     * filled list argument to his own method {@link #criaMarcador(PDDocument, int, List)},
     * though, one can create an arbitrary large result file.
     * </p>
     */
    @Test
    public void testMergeLikeArthurMenezes() throws IOException
    {
        try (   InputStream resource1 = getClass().getResourceAsStream("00002 - Distribui��o - dia 10.11.2016.pdf");
                InputStream resource2 = getClass().getResourceAsStream("00041 - Digita��o de Documentos - dia 10.11.2016.pdf");
                FileOutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "MergeArthurMenezes.pdf")) )
        {
            List<String> marcadores = Collections.nCopies(2000, "Test");
//            List<String> marcadores = Collections.emptyList();
            concatena(resource1, resource2, result, marcadores);
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/42283777/pdfbox-writing-compressed-object-streams">
     * pdfbox writing compressed object streams
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/sh/elbjegfykxux6wf/AAC8SMU6-7_sAPS7yqgZkDn0a?dl=0">
     * "00002 - Distribui��o - dia 10.11.2016.pdf",
     * "00041 - Digita��o de Documentos - dia 10.11.2016.pdf",
     * "00041 - Digita��o de Documentos - dia 10.11.2016_00042 - Cita��o para Processo Eletr�nico - dia 10.11.2016.pdf"
     * "00043 - Juntada de AR - dia 10.01.2017.pdf"
     * "00043 - Juntada de AR - dia 10.01.2017_00044 - 37880-20- - dia 10.01.2017.pdf"
     * "00046 - Ato Ordinat�rio Praticado - dia 16.01.2017.pdf"
     * "00047 - Envio de Documento Eletr�nico - dia 16.01.2017.pdf"
     * "00049 - Juntada - dia 23.01.2017.pdf"
     * "00049 - Juntada - dia 23.01.2017_00050 - 201700130770 - Peti��o Eletr�nica - dia 23.01.2017.pdf"
     * "00049 - Juntada - dia 23.01.2017_00050 - 201700130770 - Peti��o Eletr�nica - dia 23.01.2017_00051 - Anexo de Documento - dia 23.01.2017.pdf"
     * "Peti��o Incial - dia 10.11.2016_00003 - Peti��o Inicial - dia 10.11.2016.pdf"
     * "Peti��o Incial - dia 10.11.2016_00003 - Peti��o Inicial - dia 10.11.2016_00011 - Anexo de Documento - dia 10.11.2016.pdf"
     * "Peti��o Incial - dia 10.11.2016_00003 - Peti��o Inicial - dia 10.11.2016_00012 - Anexo de Documento - dia 10.11.2016.pdf"
     * "Peti��o Incial - dia 10.11.2016_00003 - Peti��o Inicial - dia 10.11.2016_00013 - Anexo de Documento - dia 10.11.2016.pdf"
     * "Peti��o Incial - dia 10.11.2016_00003 - Peti��o Inicial - dia 10.11.2016_00036 - Anexo de Documento - dia 10.11.2016.pdf"
     * "Peti��o Incial - dia 10.11.2016_00003 - Peti��o Inicial - dia 10.11.2016_00040 - Anexo de Documento - dia 10.11.2016.pdf"  
     * </a>
     * <p>
     * Using the OP's code the result indeed is exorbitantly large: the individual
     * file sizes add up to about 5 MB but the OP's concatenation is about 25 MB
     * in size. The cause is that the OP did not reset his ByteArrayOutputStream
     * containing the intermediary concatenations, so the result file effectively
     * contains not only the concatenation of the inputs but additionally all
     * intermediary partial concatenations, too. Actually this only works because
     * PDFBox repairs the inputs under the hood. Resetting the ByteArrayOutputStream
     * at the start of the loop results in the clean desired concatenation with a
     * size of about 5 MB.
     * </p>
     */
    @Test
    public void testMergeManyLikeArthurMenezes() throws IOException
    {
        Collection<String> resourceNames = Arrays.asList(
                "00002 - Distribui��o - dia 10.11.2016.pdf",
                "00041 - Digita��o de Documentos - dia 10.11.2016.pdf",
                "00041 - Digita��o de Documentos - dia 10.11.2016_00042 - Cita��o para Processo Eletr�nico - dia 10.11.2016.pdf",
                "00043 - Juntada de AR - dia 10.01.2017.pdf",
                "00043 - Juntada de AR - dia 10.01.2017_00044 - 37880-20- - dia 10.01.2017.pdf",
                "00046 - Ato Ordinat�rio Praticado - dia 16.01.2017.pdf",
                "00047 - Envio de Documento Eletr�nico - dia 16.01.2017.pdf",
                "00049 - Juntada - dia 23.01.2017.pdf",
                "00049 - Juntada - dia 23.01.2017_00050 - 201700130770 - Peti��o Eletr�nica - dia 23.01.2017.pdf",
                "00049 - Juntada - dia 23.01.2017_00050 - 201700130770 - Peti��o Eletr�nica - dia 23.01.2017_00051 - Anexo de Documento - dia 23.01.2017.pdf",
                "Peti��o Incial - dia 10.11.2016_00003 - Peti��o Inicial - dia 10.11.2016.pdf",
                "Peti��o Incial - dia 10.11.2016_00003 - Peti��o Inicial - dia 10.11.2016_00011 - Anexo de Documento - dia 10.11.2016.pdf",
                "Peti��o Incial - dia 10.11.2016_00003 - Peti��o Inicial - dia 10.11.2016_00012 - Anexo de Documento - dia 10.11.2016.pdf",
                "Peti��o Incial - dia 10.11.2016_00003 - Peti��o Inicial - dia 10.11.2016_00013 - Anexo de Documento - dia 10.11.2016.pdf",
                "Peti��o Incial - dia 10.11.2016_00003 - Peti��o Inicial - dia 10.11.2016_00036 - Anexo de Documento - dia 10.11.2016.pdf",
                "Peti��o Incial - dia 10.11.2016_00003 - Peti��o Inicial - dia 10.11.2016_00040 - Anexo de Documento - dia 10.11.2016.pdf");

        InputStream anterior = null;
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        for (String resourceName : resourceNames)
        {
            saida.reset(); // <-- added to fix the OP's code
            List<String> marcadores = marcadores(resourceName);
            try (   InputStream novo = getClass().getResourceAsStream(resourceName) )
            {
                concatena(anterior, novo, saida, marcadores);                     
                anterior = new ByteArrayInputStream(saida.toByteArray());
            }
        }

        try (OutputStream pdf = new FileOutputStream(new File(RESULT_FOLDER, "MergeManyArthurMenezes.pdf"))  )
        {
            saida.writeTo(pdf);
        }
    }

    private List<String> marcadores(String name) {
        String semExtensao = name.substring(0, name.indexOf(".pdf"));
        return Arrays.asList(semExtensao.split("_"));       
    }

    public void concatena(InputStream anterior, InputStream novo, OutputStream saida, List<String> marcadores)
            throws IOException
    {
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        pdfMerger.setDestinationStream(saida);
        PDDocument dest;
        PDDocument src;
        MemoryUsageSetting setupMainMemoryOnly = MemoryUsageSetting.setupMainMemoryOnly();
        if (anterior != null)
        {
            dest = PDDocument.load(anterior, setupMainMemoryOnly);
            src = PDDocument.load(novo, setupMainMemoryOnly);
        }
        else
        {
            dest = PDDocument.load(novo, setupMainMemoryOnly);
            src = new PDDocument();
        }
        int totalPages = dest.getNumberOfPages();
        pdfMerger.appendDocument(dest, src);
        criaMarcador(dest, totalPages, marcadores);
        saida = pdfMerger.getDestinationStream();
        dest.save(saida);
        dest.close();
        src.close();
    }

    private void criaMarcador(PDDocument src, int numPaginas, List<String> marcadores) {
        if (marcadores != null && !marcadores.isEmpty()) {
            PDDocumentOutline documentOutline = src.getDocumentCatalog().getDocumentOutline();          
            if (documentOutline == null) {
                documentOutline = new PDDocumentOutline();
            }
            PDPage page;
            if (src.getNumberOfPages() == numPaginas) {
                page = src.getPage(0);
            } else {
                page = src.getPage(numPaginas);
            }
            PDOutlineItem bookmark = null;
            PDOutlineItem pai = null;
            String etiquetaAnterior = null;
            for (String etiqueta : marcadores) {                
                bookmark = bookmark(pai != null ? pai : documentOutline, etiqueta);
                if (bookmark == null) {
                    if (etiquetaAnterior != null && !etiquetaAnterior.equals(etiqueta) && pai == null) {
                        pai = bookmark(documentOutline, etiquetaAnterior);
                    }
                    bookmark = new PDOutlineItem();
                    bookmark.setTitle(etiqueta);
                    if (marcadores.indexOf(etiqueta) == marcadores.size() - 1) {
                        bookmark.setDestination(page);
                    }
                    if (pai != null) {
                        pai.addLast(bookmark);
                        pai.openNode();
                    } else {
                        documentOutline.addLast(bookmark);
                    }
                } else {
                    pai = bookmark;
                }
                etiquetaAnterior = etiqueta;
            }   
            src.getDocumentCatalog().setDocumentOutline(documentOutline);           
        }       
    }

    private PDOutlineItem bookmark(PDOutlineNode outline, String etiqueta) {             
        PDOutlineItem current = outline.getFirstChild();
        while (current != null) {
            if (current.getTitle().equals(etiqueta)) {
                return current;
            }
            bookmark(current, etiqueta);
            current = current.getNextSibling();
        }
        return current;
    }
}
