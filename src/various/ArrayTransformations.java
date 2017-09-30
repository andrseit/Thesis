package various;

/**
 * Created by Darling on 31/7/2017.
 */
public class ArrayTransformations {

    public int[] getColumnsCount (int[][] array) {
        int rows = array.length;
        int columns = array[0].length;

        int[] final_array = new int[columns];

        for(int c = 0; c < columns; c++) {
            for(int r = 0; r < rows; r++) {
                final_array[c] += array[r][c];
            }
        }

        /*
        for(int i = 0; i < final_array.length; i++) {
            System.out.print(final_array[i] + " ");
        }
        */
        System.out.println();
        return final_array;
    }

    public void printIntArray(int[][] array) {
        int rows = array.length;
        int columns = array[0].length;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                System.out.print(array[r][c] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public void printOneDimensionArray (String name, int[] array) {
        int columns = array.length;
        System.out.println(name);
        for (int c = 0; c < columns; c++) {
            System.out.print(array[c] + " ");
        }
        System.out.println();
    }

    /**
     * Tou dineis ena map evs-slots, kai sto kanei chargers-slots, dld sou anti na sou leei pote
     * ginetai ena event, sou leei posoi poroi xrisimopoiountai
     * mikri xrisimotita afou exeis to column count - pio poly gia optiki xrisi
     * @param array
     * @param rows
     */
    public int[][] shrinkArray(int[][] array, int rows) {
        /*
        if(rows > array.length)
        {
            System.out.println("New array must have less rows, than the old one!");
            return null;
        }
        */

        int[][] shrinked_map = new int[rows][array[0].length];
        int[] counts = this.getColumnsCount(array);

        for (int s = 0; s < array[0].length; s++) {

            for(int r = 0; r < counts[s]; r++) {
                shrinked_map[r][s] = 1;
            }
        }


        return shrinked_map;
    }


    public void updateArray (int[][] array, int start, int end, int row) {
        System.out.println("Start: " + start + ", End: " + end + ", Row: " + row);
        for (int i = start; i < end + 1; i++) {
            array[row][i] = 1;
        }
    }
}
