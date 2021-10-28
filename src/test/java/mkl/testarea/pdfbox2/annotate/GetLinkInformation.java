package mkl.testarea.pdfbox2.annotate;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.junit.Test;

/**
 * @author mkl
 */
public class GetLinkInformation {
    /**
     * <a href="https://stackoverflow.com/questions/69743792/how-to-find-all-internal-links-in-a-pdf-using-java-apache-pdfbox">
     * How to find all internal links in a PDF, using Java Apache PDFBox
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/rgamdyfny3qcx2j/NBSampleForStackOverflow.pdf?dl=0">
     * NBSampleForStackOverflow.pdf
     * </a>
     * <p>
     * PDPageXYZDestination.getPageNumber only returns remote link page numbers, see
     * the JavaDocs. Use PDPageXYZDestination.getPage instead and get the index of
     * that page in the document pages collection.
     * </p>
     */
    @Test
    public void testGetLinkPageNumber() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("NBSampleForStackOverflow.pdf");
                PDDocument doc = Loader.loadPDF(resource)   ) {
            int pageNo = 0;
            for (PDPage page : doc.getPages()) {
                pageNo++;
                for (PDAnnotation annotation : page.getAnnotations()) {
                    String subtype = annotation.getSubtype();
                    System.out.printf("Found Annotation (%s) on page %d\n", subtype, pageNo);
                    if (annotation instanceof PDAnnotationLink) {
                        String aname = annotation.getAnnotationName();
                        System.out.printf("\t\tfound Link named %s on page %d\n", aname, pageNo);
                        PDAnnotationLink link = (PDAnnotationLink) annotation;
                        System.out.println("\t\tas string: " + link.toString());
                        System.out.println("\t\tdestination: " + link.getDestination());
                        PDDestination dest = link.getDestination();
                        Class<? extends PDDestination> destClass = dest.getClass();
                        System.out.printf("\t\tdest class is %s\n", destClass);
                        if(dest instanceof PDPageXYZDestination) {
                            int pageNumber = ((PDPageXYZDestination) dest).getPageNumber();
                            System.out.printf("\t\tdest page number is %d\n", pageNumber);

                            PDPage targetPage = ((PDPageXYZDestination) dest).getPage();
                            System.out.printf("\t\tdest points to page with number %d\n", 1 + doc.getPages().indexOf(targetPage));
                        }

                        PDAction action = link.getAction();

                        if (action == null) {
                            System.out.println("\t\tbut action is null");
                            continue;
                        }
                        if (action instanceof PDActionURI)
                            System.out.printf("\t\tURI action is %s\n", ((PDActionURI) action).getURI());
                        else
                            System.out.printf("\t\tother action is %s\n", action.getClass());
                    }
                    else {
                        System.out.println("\tNOT a link");
                    }
                }
            }
        }
    }

}
