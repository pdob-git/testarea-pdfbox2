package pl.pdob.pdftables.csv;

import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.lang.invoke.MethodHandles;
import java.util.List;

public class CsvWriter {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static String csvWriterOneByOne(List<String[]> stringArray, String fileName) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(fileName));
            for (String[] array : stringArray) {
                writer.writeNext(array);
            }
            writer.close();
        } catch (Exception ex) {
            logger.error("Write of Csv error",ex);
        }
        return SimpleFileReader.readFile(fileName);
    }

    public static String csvWriterAll(List<String[]> stringArray, String fileName) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(fileName));
            writer.writeAll(stringArray);
            writer.close();
        } catch (Exception ex) {
            logger.error("Write of Csv error",ex);
        }
        return SimpleFileReader.readFile(fileName);
    }
}