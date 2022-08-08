package mkl.testarea.pdfbox2.meta;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;

/**
 * @author mkl
 */
public class OpenFile
{
    /**
     * <a href="http://stackoverflow.com/questions/39341760/code-stuck-while-loading-password-protected-pdf-using-pdfbox">
     * Code stuck while loading password protected pdf using pdfbox
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/d5ngzsw4a3chlbm/test.pdf?dl=0">
     * test.pdf
     * </a>
     * <p>
     * The issue cannot be reproduced, the provided PDF can be read without any code getting stuck.
     * </p>
     */
    @Test
    public void testOpenTestUser6722137() throws IOException
    {
        try ( InputStream resource = getClass().getResourceAsStream("test.pdf") )
        {
            String password = "test";
            PDDocument pdfdocument = PDDocument.load(resource, password);
            System.out.printf("Producer of User6722137's test.pdf: %s\n", pdfdocument.getDocumentInformation().getProducer());
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/59604736/taking-screenshots-from-pdf-file-with-apache-pdfbox">
     * Taking screenshots from PDF file with Apache PDFBox
     * </a>
     * <br/>
     * <a href="http://aplaidshirt.epizy.com/samplePDF.pdf">
     * samplePDF.pdf
     * </a>
     * <p>
     * According to the stack trace the error occurred during
     * PDDocument.load. Thus, this test tries to load the example
     * file. Unfortunately, no error happens. Thus, this might be
     * an issue in the OP's version of PDFBox which meanwhile is
     * fixed. 
     * </p>
     */
    @Test
    public void testOpenSamplePDFPlaidshirt() throws IOException
    {
        try ( InputStream resource = getClass().getResourceAsStream("samplePDF.pdf") )
        {
            PDDocument pdfdocument = PDDocument.load(resource);
            System.out.printf("Producer of plaidshirt's samplePDF.pdf: %s\n", pdfdocument.getDocumentInformation().getProducer());
        }
    }

    /**
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-5006">
     * java.io.IOException: Error: End-of-File, expected line during PDDocument.load
     * </a>
     * <br/>
     * <a href="http://www.geislerfarms.com/documents/filelibrary/Geisler_COVID_statement_0A7A094E1EFB7.pdf">
     * Geisler_COVID_statement_0A7A094E1EFB7.pdf
     * </a>
     * <p>
     * Cannot reproduce a document loading error.
     * </p>
     */
    @Test
    public void testOpenGeislerCovidStatement_0A7A094E1EFB7() throws IOException
    {
        try ( InputStream resource = getClass().getResourceAsStream("Geisler_COVID_statement_0A7A094E1EFB7.pdf") )
        {
            PDDocument pdfdocument = PDDocument.load(resource);
            System.out.printf("Producer of Geisler_COVID_statement_0A7A094E1EFB7.pdf: %s\n", pdfdocument.getDocumentInformation().getProducer());
        }
    }

    /**
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-5006">
     * java.io.IOException: Error: End-of-File, expected line during PDDocument.load
     * </a>
     * <br/>
     * <a href="https://www.buerger.uni-frankfurt.de/80977779/Rehbein_Schule_Hanau_9_2018.pdf">
     * Rehbein_Schule_Hanau_9_2018.pdf
     * </a>
     * <p>
     * Cannot reproduce a document loading error.
     * </p>
     */
    @Test
    public void testOpenRehbeinSchuleHanau_9_2018() throws IOException
    {
        try ( InputStream resource = getClass().getResourceAsStream("Rehbein-Schule_Hanau_9_2018.pdf") )
        {
            PDDocument pdfdocument = PDDocument.load(resource);
            System.out.printf("Producer of Rehbein-Schule_Hanau_9_2018.pdf: %s\n", pdfdocument.getDocumentInformation().getProducer());
        }
    }

    /**
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-5006">
     * java.io.IOException: Error: End-of-File, expected line during PDDocument.load
     * </a>
     * <br/>
     * <a href="http://www.sahealth.sa.gov.au/wps/wcm/connect/c736e1d5-932e-4f8a-8e56-52ab10a214fd/SALHN+Governing+Board+Minutes+-+5+March+2020.pdf?MOD=AJPERES&CACHEID=ROOTWORKSPACE-c736e1d5-932e-4f8a-8e56-52ab10a214fd-niR9I3J">
     * SALHN+Governing+Board+Minutes+-+5+March+2020.pdf
     * </a>
     * <p>
     * Cannot reproduce a document loading error.
     * </p>
     */
    @Test
    public void testOpenSalhnGoverningBoardMinutes5March2020() throws IOException
    {
        try ( InputStream resource = getClass().getResourceAsStream("SALHN+Governing+Board+Minutes+-+5+March+2020.pdf") )
        {
            PDDocument pdfdocument = PDDocument.load(resource);
            System.out.printf("Producer of SALHN+Governing+Board+Minutes+-+5+March+2020.pdf: %s\n", pdfdocument.getDocumentInformation().getProducer());
        }
    }
}
