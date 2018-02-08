package io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Created by Thesis on 4/1/2018.
 */
public class ArrayFileWriter {

    public void writeSchedule(int[][] full_schedule_map, int[] remaining_chargers) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("schedule"))) {
            out.writeObject(full_schedule_map);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("chargers"))) {
            out.writeObject(remaining_chargers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeSuggestions(int[][] suggestions) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("suggestions"))) {
            out.writeObject(suggestions);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
