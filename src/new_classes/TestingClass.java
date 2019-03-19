package new_classes;

import evs.Preferences;

import java.util.Random;

/**
 * Created by Thesis on 18/3/2019.
 * Used to test various functions before integrating them into the system
 */
public class TestingClass {

    public static boolean checkDelay(int currentSlot, int slotsNumber) {
        Random random = new Random();
        if (random.nextInt(100) < 90) {
            System.out.println("I will delay or cancel!");
            if (random.nextInt() < 50) {
                // cancel
                System.out.println("I shall CANCEL my reservation!");
            } else {
                System.out.println("I shall DELAY my reservation!");
                // delay
                if (!(slotsNumber - currentSlot <= 0)) {
                    return true;
                }
            }
        } else {
            System.out.println("I'll check in normally!");
        }
        return false;
    }

}
