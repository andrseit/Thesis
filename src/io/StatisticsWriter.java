package io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

// make this classes under an interface or abstract parent
public class StatisticsWriter {

    private FileWriter writer;

    public static void addCSVLine (String line) {
        try {
            File f = new File("files/statistics.csv");
            if(!f.exists()) {
                Files.write(Paths.get("files/statistics.csv"), line.getBytes(), StandardOpenOption.CREATE_NEW);
            } else
            Files.write(Paths.get("files/statistics.csv"), line.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
