package e;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Class to test e calculation algorithm.
 *
 * @author Toshko Todorov
 * @since 1.0
 */
public class ETest {
    /**
     * Initialize a default precision used.
     */
    private static int precision = 1;

    /**
     * Initialize a default number of threads used.
     */
    private static int numThreads = 1;

    /**
     * Initialize a default boolean for quiet mode.
     */
    private static boolean quiet;

    /**
     * Initialize a default double for result.
     */
    private static double result;

    /**
     * Run distributed e calculation
     *
     * @param pathWrite the output file
     * @since 1.0
     */
    private static void runDistributed(Path pathWrite) {
        // Record the start time for execution.
        final long startTime = System.currentTimeMillis();

        // Set thread usage counter.
        int jobNumber = 0;

        // Create a content list to be distributed among the threads.
        List<List<Integer>> valuesList = new ArrayList<>();

        /*
         * Split the calculated values into parts.
         */
        if (getPrecision() > getNumThreads()) {
            List<Integer> valuesSubList = new ArrayList<>();
            for (int i = 0; i <= getPrecision(); i++) {
                if (i > 0 && i % (getPrecision() / getNumThreads()) == 0) {
                    valuesList.add(valuesSubList);
                    valuesSubList = new ArrayList<>();
                }
                valuesSubList.add(i);
            }
            for (int i = 0; i < valuesSubList.size(); i++) {
                valuesList.get(i).add(valuesSubList.get(i));
            }
        }

        if (getPrecision() == getNumThreads()) {
            valuesList.add(Arrays.asList(0, 1));
            for (int i = 2; i <= getPrecision(); i++) {
                valuesList.add(Arrays.asList(i));
            }
        }

        if (getPrecision() < getNumThreads()) {
            for (int i = 0; i <= getPrecision(); i++) {
                valuesList.add(Arrays.asList(i));
            }
        }

        // Distribute task on multiple threads.contentList
        Thread jobs[] = new Thread[valuesList.size()];

        for (int i = 0; i < valuesList.size(); i++) {
            ECalculateRunnable r = new ECalculateRunnable(valuesList.get(i));
            Thread t = new Thread(r);
            t.start();
            jobs[i] = t;
            if (!isQuiet()) {
                System.out.format("Thread-%d started.\n", ++jobNumber);
            }
        }

        // Join all threads.
        for (int i = 0; i < valuesList.size(); i++) {
            try {
                jobs[i].join();
                if (!isQuiet()) {
                    System.out.format("Thread-%d stopped.\n", i + 1);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!isQuiet()) {
            System.out.format("Threads used in current run: %d\n", jobNumber);
        }

        // Record the end time for execution.
        final long stopTime = System.currentTimeMillis();

        // Calculate the time for execution.
        final long elapsedTime = stopTime - startTime;

        // Print out the time for execution to console.
        System.out.format("Total execution time for current run (millis): %d\n", elapsedTime);

        // Print out the result of execution to console.
        System.out.format("e: %.8f\n", getResult());

        try {
            // Print out the results.
            List<String> resultWrite = new ArrayList<>();
            resultWrite.add(String.format("%.8f", getResult()));
            Files.write(pathWrite, resultWrite, Charset.forName("UTF-8"));
        } catch (IOException e) {
            // Print a stack trace of input-output exception.
            e.printStackTrace();
        }
    }

    /**
     * Execute the program implementing e calculation algorithm.
     *
     * @since 1.0.
     */
    public static void main(String[] args) {
        // Print out a leading message to introduce the user to the program.
        System.out.format(
                        "This is an example for e calculation.\nEnter precision, threads' number, output file name and mode in format: -p int -t int -o String -q\n");

        // Set a response in case of bad request.
        final String WRONG_REQUEST = "Wrong request format!";

        // Read the input from the console.
        Scanner scan = new Scanner(System.in);
        List<String> request = Arrays.asList(scan.nextLine().split("\\s+"));

        // Test for bad input.
        if ((request.size() != 4 && request.size() != 6 && request.size() != 7) || !"-p".equals(request.get(0)) || !"-t".equals(request.get(2))) {
            System.out.println(WRONG_REQUEST);
            return;
        }

        // Test for bad precision.
        try {
            setPrecision(Integer.valueOf(request.get(1)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        // Test for bad number of threads.
        try {
            setNumThreads(Integer.valueOf(request.get(3)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (getNumThreads() <= 0) {
            System.out.println(WRONG_REQUEST);
            return;
        }

        // Set the output file.
        String outputFile = "result.txt";
        if (request.size() >= 6 && "-o".equals(request.get(4))) {
            outputFile = request.get(5);
        }

        Path pathWrite = Paths.get(outputFile);

        // Set quiet mode
        if (request.size() == 7 && "-q".equals(request.get(6))) {
            setQuiet(true);
        }

        // Do the magic.
        runDistributed(pathWrite);
    }

    public static int getPrecision() {
        return precision;
    }

    public static void setPrecision(int precision) {
        ETest.precision = precision;
    }

    private static int getNumThreads() {
        return numThreads;
    }

    private static void setNumThreads(int numThreads) {
        ETest.numThreads = numThreads;
    }

    private static boolean isQuiet() {
        return quiet;
    }

    private static void setQuiet(boolean quiet) {
        ETest.quiet = quiet;
    }

    public static double getResult() {
        return result;
    }

    public static void setResult(double result) {
        ETest.result = result;
    }
}