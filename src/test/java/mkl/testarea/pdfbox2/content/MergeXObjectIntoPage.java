package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class MergeXObjectIntoPage {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/66274818/add-formxobject-content-from-resources-to-content-stream-using-pdfbox">
     * Add FormXobject content from resources to content stream using PDFBox?
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1lT4pwGAk4I6vaekTHhWnx_hngcKCm0DK/view?usp=sharing">
     * HighPioneerFallNewsletterADApdf_2.pdf
     * </a>
     * <p>
     * This test takes the OP's code and adds a q-Q envelope around the inserted instructions.
     * This doesn't make a difference for the example document because in it the XObject Do
     * instruction always was the last instruction before a Q anyways. The problem with the
     * example document is based in the XObjects being transparency groups and the copy of the
     * instructions does not transport the grouping.
     * </p>
     */
    @Test
    public void testMergeLikeFascinatingCoder() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("HighPioneerFallNewsletterADApdf_2.pdf");
                PDDocument document = PDDocument.load(resource)) {
            parseFormXobject(document);
            document.save(new File(RESULT_FOLDER, "HighPioneerFallNewsletterADApdf_2-MergeLikeFascinatingCoder.pdf"));
        }
    }

    private PDDocument parseFormXobject(PDDocument document) throws IOException {
        PDDocument newdocument = new PDDocument();
        for (int pg_ind = 0; pg_ind < document.getNumberOfPages(); pg_ind++) {
            List<Object> tokens1 = (List<Object>) (getTokens(document, pg_ind)).get(pg_ind);
            PDStream newContents = new PDStream(document);
            OutputStream out = newContents.createOutputStream(COSName.FLATE_DECODE);
            ContentStreamWriter writer = new ContentStreamWriter(out);

            PDPage pageinner = document.getPage(pg_ind);
            PDResources resources = pageinner.getResources();
            PDResources new_resources = new PDResources();
            new_resources = resources;

            COSDictionary fntdict = new COSDictionary();
            COSDictionary imgdict = new COSDictionary();
            COSDictionary extgsdict = new COSDictionary();
            COSDictionary colordict = new COSDictionary();
            int img_count = 0;
            for (COSName xObjectName : resources.getXObjectNames()) {
                PDXObject  xObject = resources.getXObject(xObjectName);
                if (xObject instanceof PDFormXObject) {

                    PDFStreamParser parser = new PDFStreamParser(((PDFormXObject) xObject));
                    parser.parse();
                    List<Object>  tokens3 = parser.getTokens();
                    int ind =0;
                    System.out.println(xObjectName.getName());
                        for (COSName colorname :((PDFormXObject) xObject).getResources().getColorSpaceNames())
                        {
                            COSName new_name = COSName.getPDFName(colorname.getName()+"_Fm"+img_count);
                            PDColorSpace pdcolor = ((PDFormXObject) xObject).getResources().getColorSpace(colorname);
                            colordict.setItem(new_name,pdcolor);
                        }
                        for (COSName fontName :((PDFormXObject) xObject).getResources().getFontNames() )
                        {
                            COSName new_name = COSName.getPDFName(fontName.getName()+"_Fm"+img_count);
                            PDFont font =((PDFormXObject) xObject).getResources().getFont(fontName);
                            font.getCOSObject().setItem(COSName.NAME, new_name);
                            fntdict.setItem(new_name,font);
                        }
                        for (COSName ExtGSName :((PDFormXObject) xObject).getResources().getExtGStateNames() )
                        {
                            COSName new_name = COSName.getPDFName(ExtGSName.getName()+"_Fm"+img_count);
                            PDExtendedGraphicsState ExtGState =((PDFormXObject) xObject).getResources().getExtGState(ExtGSName);
                            ExtGState.getCOSObject().setItem(COSName.NAME, new_name);
                            extgsdict.setItem(new_name,ExtGState);
                        }
                        imgdict.setItem(xObjectName, xObject);
                        for (COSName Imgname :((PDFormXObject) xObject).getResources().getXObjectNames() )
                        {
                            COSName new_name = COSName.getPDFName(Imgname.getName()+"_Fm"+img_count);
                            xObject.getCOSObject().setItem(COSName.NAME, new_name);
                            PDXObject img =((PDFormXObject) xObject).getResources().getXObject(Imgname);
                            imgdict.setItem(new_name, img);
                        }

                            for (int k=0; k< tokens1.size(); k++) {
                                if ( ((tokens1.get(k) instanceof Operator) && ((Operator)tokens1.get(k)).getName().toString().equals("Do"))
                                        && ((COSName)tokens1.get(k-1)).getName().toString().equals(xObjectName.getName().toString()) ) {
                                    System.out.println(tokens1.get(k).toString());
                                    tokens1.remove(k-1);
                                    tokens1.remove(k-1);
                                    ind =k-1;
                                    break;
                                }
                            }

                            // vvv--- Add q-Q envelop
                            tokens1.add(ind++, Operator.getOperator("q"));
                            tokens1.add(ind, Operator.getOperator("Q"));
                            // ^^^--- Add q-Q envelop

                            for (int k=0; k< tokens3.size(); k++) {
                            if ( (tokens3.size() > k+1) && (tokens3.get(k+1) instanceof Operator) && (((Operator)tokens3.get(k+1)).getName().toString().equals("Do")
                                    || ((Operator)tokens3.get(k+1)).getName().toString().equals("gs")
                                    || ((Operator)tokens3.get(k+1)).getName().toString().equals("cs")  ) ) {
                                COSName new_name = COSName.getPDFName( ((COSName) tokens3.get(k)).getName()+"_Fm"+img_count );
                                tokens1.add(ind+k, new_name );
                            }else if ( (tokens3.size() > k+2) && (tokens3.get(k+2) instanceof Operator)
                                    && ((Operator)tokens3.get(k+2)).getName().toString().equals("Tf") ) {
                                COSName new_name = COSName.getPDFName( ((COSName) tokens3.get(k)).getName()+"_Fm"+img_count );
                                tokens1.add(ind+k, new_name );
                            }else
                                tokens1.add(ind+k,tokens3.get(k));
                        }

                        img_count +=1;
                }else
                    imgdict.setItem(xObjectName, xObject);
            }
            for (COSName fontName :new_resources.getFontNames() )
            {
                PDFont font =new_resources.getFont(fontName);
                fntdict.setItem(fontName,font);
            }
            for (COSName ExtGSName :new_resources.getExtGStateNames() )
            {
                PDExtendedGraphicsState extg =new_resources.getExtGState(ExtGSName);
                extgsdict.setItem(ExtGSName,extg);
            }
            for (COSName colorname :new_resources.getColorSpaceNames() )
            {
                PDColorSpace color =new_resources.getColorSpace(colorname);
                colordict.setItem(colorname,color);
            }
            resources.getCOSObject().setItem(COSName.EXT_G_STATE,extgsdict);
            resources.getCOSObject().setItem(COSName.FONT,fntdict);
            resources.getCOSObject().setItem(COSName.XOBJECT,imgdict);
            resources.getCOSObject().setItem(COSName.COLORSPACE, colordict);

            writer.writeTokens(tokens1);
            out.close();
            document.getPage(pg_ind).setContents(newContents);
            //document.getPage(pg_ind).setMediaBox(PDFUtils.Media_box);
            document.getPage(pg_ind).setResources(resources);
            newdocument.addPage(document.getPage(pg_ind));
        }
        return newdocument;
    }

    private static Map getTokens(PDDocument oldDocument, Integer pageIndex) throws IOException {
        // TODO Auto- it will return the tokens of pdf
        Map oldDocumentTokens = new HashMap();
        PDPage pg = oldDocument.getPage(pageIndex);
        PDFStreamParser parser = new PDFStreamParser(pg);
        parser.parse();
        List<Object> tokens = /*PDFUtils.removeTokens*/(parser.getTokens());
        oldDocumentTokens.put(pageIndex, tokens);
        return oldDocumentTokens;
    }
}
