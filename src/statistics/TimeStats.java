package statistics;

public class TimeStats {

    private long start;
    private long elapsedTime;

    public void startTimer () {
        start = System.nanoTime();
    }

    public void stopTimer () {
        long end = System.nanoTime();
        elapsedTime = end - start;
    }

    public double getMillis () {
        return elapsedTime/Math.pow(10, 6);
    }
}
