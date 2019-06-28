package various;

import java.util.ArrayList;
import java.util.Arrays;

public class ArrayTransformations {

    public static int[] getColumnsCount(int[][] array) {
        if (array == null || array.length ==0)
            return new int[0];
        int columns = array[0].length;

        int[] final_array = new int[columns];

        for (int c = 0; c < columns; c++) {
            for (int[] anArray : array) {
                final_array[c] += anArray[c];
            }
        }

        /*
        for(int i = 0; i < final_array.length; i++) {
            System.out.print(final_array[i] + " ");
        }
        */
        return final_array;
    }

    public static void printIntArray(int[][] array) {
        if (array.length == 0)
            System.out.println("Empty array!");
        else {
            int columns = array[0].length;
            for (int[] anArray : array) {
                for (int c = 0; c < columns; c++) {
                    System.out.print(anArray[c] + " ");
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    public static void printOneDimensionArray(String name, int[] array) {
        for (int anArray : array) {
            System.out.print(anArray + "  ");
        }
        System.out.println(" : " + name);
    }

    public static int[][] shrinkArray(int[][] array, int rows) {
        /*
        if(rows > array.length)
        {
            System.out.println("New array must have less rows, than the old one!");
            return null;
        }
        */

        int[][] shrinked_map = new int[rows][array[0].length];
        int[] counts = getColumnsCount(array);

        for (int s = 0; s < array[0].length; s++) {

            for (int r = 0; r < counts[s]; r++) {
                shrinked_map[r][s] = 1;
            }
        }


        return shrinked_map;
    }


    /**
     * takes an array based on min and max slot and then exapnd it to full slots array
     *
     * @param array
     * @param min
     * @param max
     * @return
     */
    public static int[][] expandArray(int[][] array, int slots_number, int min, int max) {

        int[][] map = new int[array.length][slots_number];

        for (int e = 0; e < array.length; e++) {
            System.arraycopy(array[e], min, map[e], min, max + 1 - min);
        }

        return map;
    }


    public static void updateArray(int[][] array, int start, int end, int row) {
        System.out.println("Start: " + start + ", End: " + end + ", Row: " + row);
        for (int i = start; i < end + 1; i++) {
            array[row][i] = 1;
        }
    }

    // concatenates old and new map
    public static int[][] concatMaps(int[][] first, int[][] second, int columns) {
        if (first == null)
            return second;
        if (second.length != 0) {
            //this.scheduleMap = initial;
            int map_length = first.length;
            int[][] new_full_schedule_map = new int[map_length + second.length][columns];
            System.arraycopy(first, 0, new_full_schedule_map, 0, first.length);
            System.arraycopy(second, 0, new_full_schedule_map, first.length, second.length);
            first = new_full_schedule_map;
            //System.out.println("Rows: " + scheduleMap.length + ", Columns: " + scheduleMap[0].length);
        }
        return first;
    }

    public static int findMin (int[] array) {
        int min = array[0];
        for (int s = 1; s < array.length; s++) {
            if (array[s] < min)
                min = array[s];
        }
        return min;
    }

    public static int findMax (int[] array) {
        int max = array[0];
        for (int s = 1; s < array.length; s++) {
            if (array[s] > max)
                max = array[s];
        }
        return max;
    }

    public static double[] normalizeArrayValues (int[] array) {
        int min = findMin(array);
        int max = findMax(array);
        double[] normalizedArray = new double[array.length];
        for (int s = 0; s < array.length; s++) {
            normalizedArray[s] = ((double)array[s] - min) / (max - min);

        }
        return normalizedArray;
    }

    public static int[][] removeRowFromArray (int[][] array, int row) {
        ArrayList<int[]> rowsToKeep = new ArrayList<>();
        for (int r = 0; r < array.length; r++) {
            if (!(r == row))
                rowsToKeep.add(array[r]);
        }
        if (rowsToKeep.isEmpty()) {
            return new int[0][0];
        }
        else {
            int[][] updatedArray = new int[array.length - 1][array[0].length];
            for (int r = 0; r < rowsToKeep.size(); r++) {
                updatedArray[r] = rowsToKeep.get(r);
            }
            return updatedArray;
        }
    }

    /**
     * Receive a bitmap as input and remove several cells (make their value 0)
     * based on the given percentage
     * it first removes lines and then the remaining cells
     * @param array the array to be cleared
     * @param percentage how many of its cells should be removed
     * @return
     */
    public static int[][] clearLines (int[][] array, double percentage) {
        int[][] arrayCopy = Arrays.copyOf(array, array.length);
        int rows = array.length, columns = array[0].length;
        int[] rowCount = new int[rows]; // how many 1s there are in a row
        int ones = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                rowCount[i] += array[i][j];
                if (array[i][j] == 1)
                    ones++;
            }
        }
        int remove = (int) (rows*percentage);
        for (int i = 0; i < remove; i++) {
            int minRow = minRow(rowCount);
            arrayCopy[minRow] = new int[columns];
            rowCount[minRow] = 0;
        }

        return arrayCopy;
    }

    private static int minRow (int[] array) {
        int min = Integer.MAX_VALUE;
        int minRow = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > 0 && array[i] < min) {
                min = array[i];
                minRow = i;
            }
        }
        return minRow;
    }

    public static int[] fakeChargers (int[] chargers, int slotsNumber, int currentSlot) {
        int initial = (int)(0.8*slotsNumber);
        System.out.println("Zero after: " + ((initial+currentSlot) + currentSlot) + "(initial: " + initial + ", current: " + currentSlot);
        System.out.println(initial);
        int fakeChargers[] = new int[chargers.length];
        for (int i = 0; i < slotsNumber; i++) {
            if (i >= (initial+currentSlot) + currentSlot)
                fakeChargers[i] = 0;
            else
                fakeChargers[i] = chargers[i];
        }
        return fakeChargers;
    }
}
