package statistics;

import java.util.Random;

public class SimpleMath {

    public static double getPercentage (int part, int all) {
        double result = ((double) part) / ((double) all);

        return  round(result*100.0, 2);
    }

    public static double round (double number, int precision) {
        double t = Math.pow(10, precision);
        return Math.round(number*t)/t;
    }

    public static int rangeRandom (int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    public static double getDoubleValue(Object original) { return ((Number) original).doubleValue(); }

    public static int getIntValue(Object original) {
        return ((Number) original).intValue();
    }
}
