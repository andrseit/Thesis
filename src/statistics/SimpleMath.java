package statistics;

/**
 * Created by Thesis on 1/2/2018.
 */
public class SimpleMath {

    public static double getPercentage (int part, int all) {
        double result = ((double) part) / ((double) all);

        return  round(result*100.0, 2);
    }

    public static double round (double number, int precision) {
        double t = Math.pow(10, precision);
        return Math.round(number*t)/t;
    }

}
