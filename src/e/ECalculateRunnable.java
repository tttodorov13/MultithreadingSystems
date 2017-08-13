package e;

import java.math.BigInteger;
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
            // Calculate the nomerator of the factor.
            BigInteger numerator = new BigInteger("1");
            for (int j = 1; j <= 2 * i + 1; j++) {
                numerator = numerator.multiply(new BigInteger(String.valueOf(j)));
            }
            // Calculate and add the factor.
            ETest.setResult(ETest.getResult() + ((3 - 4 * i * i) / numerator.doubleValue()));
        }
    }
}
