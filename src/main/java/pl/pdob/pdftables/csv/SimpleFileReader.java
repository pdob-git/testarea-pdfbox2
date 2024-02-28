package pl.pdob.pdftables.csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.invoke.MethodHandles;

public class SimpleFileReader {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Simple File Reader
     */

    public static String readFile(String fileName) {
        String response = "";
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String strLine;
            StringBuffer sb = new StringBuffer();
            while ((strLine = br.readLine()) != null) {
                sb.append(strLine);
                sb.append("\n");
            }
            response = sb.toString();
            logger.info("Saved data \n{}",response);
            fr.close();
            br.close();
        } catch (Exception ex) {
            logger.error("Read file error",ex);
        }
        return response;
    }
}
