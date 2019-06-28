package io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

// make this classes under an interface or abstract parent
public class StatisticsWriter {

    public static void addCSVLine (String path, String line) {
        try {
            File f = new File(path);
            if(!f.exists())
                Files.write(Paths.get(path), getHeader().getBytes(), StandardOpenOption.CREATE_NEW);
            Files.write(Paths.get(path), line.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getHeader () {
        String comma = ",";
        StringBuilder builder = new StringBuilder();
        builder.append("stationID").append(comma)
                .append("slot").append(comma)
                .append("accepted").append(comma)
                .append("acceptedAlternative").append(comma)
                .append("rejected").append(comma)
                .append("rejectedAlternative").append(comma)
                .append("delays").append(comma)
                .append("acceptedAlternativeDelay").append(comma)
                .append("delayRejected").append(comma)
                .append("cancellations").append(comma)
                .append("charged").append(comma)
                .append("slotsUsedPercentage").append(comma)
                .append("minSlot").append(comma)
                .append("maxSlot").append(comma)
                .append("evsUtility").append(comma)
                .append("conversationRounds").append(comma)
                .append("negotiatorsUtility").append(comma)
                .append("negotiatorsRounds")
                .append("\n");;
        return builder.toString();
    }
}
