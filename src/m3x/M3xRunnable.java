package m3x;

import java.util.List;

/**
 * @author toshko.todorov
 * @since 1.0
 */
public class M3xRunnable implements Runnable {

    /**
     * Declare a Double list for matrix1 row.
     */
    private List<Double> m1Row;

    /**
     * Declare a Double list for matrix2 column.
     */
    private List<Double> m2Col;

    /**
     * Constructor to initialize lists for computable values.
     *
     * @param m1Row matrix1 row
     * @param m2Col matrix2 column
     * @since 1.0
     */
    M3xRunnable(List<Double> m1Row, List<Double> m2Col) {
        this.m1Row = m1Row;
        this.m2Col = m2Col;
    }

    /**
     * Override the run method to calculate and add the new matrix's member.
     *
     * @since 1.0
     */
    @Override
    public void run() {
        Double d = 0.0;
        for (int i = 0; i < m1Row.size(); i++) {
            d += m1Row.get(i) * m2Col.get(i);
        }
        M3xTest.result.add(d);
    }
}