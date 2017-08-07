package e;

import java.util.List;

/**
 * Implement e calculation algorithm.
 *
 * @author Toshko Todorov
 * @since 1.0.
 */
class ECalculateRunnable implements Runnable {

    /**
     * Declare an int for calculated values.
     */
    private List<Integer> values;

    ECalculateRunnable(List<Integer> values) {
        this.values = values;
    }

    @Override
    public void run() {
        // Calculated e for each value and add it to the common result.
        for (Integer i : values) {
            int numerator = 1;
            for (int j = 0; j <= i; j++) {
                numerator += 2 * j + 1;
            }
            ETest.setResult((3 - 4 * i * i) / (double) numerator);
        }
    }
}
