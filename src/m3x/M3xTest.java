package m3x;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author toshko.todorov
 * @since 1.0
 */
public class M3xTest {

    /**
     * Initialize a default input file.
     */
    private static String inputFile = "m3x-data.in";

    /**
     * Initialize a default output file.
     */
    private static String outputFile = "m3x-data.out";

    /**
     * Initialize a default width of first matrix.
     */
    private static int m;

    /**
     * Initialize a default height of first matrix, width of second matrix.
     */
    private static int n;

    /**
     * Initialize a default height of second matrix.
     */
    private static int k;

    /**
     * Initialize a default number of threads used.
     */
    private static int numThreads = 1;

    /**
     * Initialize a default boolean for quiet mode.
     */
    private static boolean quiet;

    /**
     * Initialize a new matrix to store results.
     */
    protected static List<Double> result = new ArrayList<>();

    /**
     * Run distributed mx3 calculation
     *
     * @param pathRead  the input file
     * @param pathWrite the output file
     * @since 1.0
     */
    private static void runDistributed(Path pathRead, Path pathWrite) throws IOException, NumberFormatException, IndexOutOfBoundsException {
        // Record the start time for execution.
        final long startTime = System.currentTimeMillis();

        // Initialize thread usage counter.
        int jobNumber = 0;

        List<Double> matrix1 = new ArrayList<>();
        List<Double> matrix2 = new ArrayList<>();

        // If there are non-null sizes given create matrices.
        if (Files.isWritable(pathRead) && getM() > 0 && getN() > 0 && getK() > 0) {
            Files.write(pathRead, Arrays.asList("=== цитат ==="), Charset.forName("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);
            Files.write(pathRead, Arrays.asList(String.format("%d %d %d", getM(), getN(), getK())), Charset.forName("UTF-8"), StandardOpenOption.APPEND);

            for (int m = 0; m < getM(); m++) {
                StringBuilder sb = new StringBuilder();
                for (int n = 0; n < getN(); n++) {
                    Double d = ThreadLocalRandom.current().nextDouble();
                    sb.append(String.format("%.2f ", d));
                    matrix1.add(d);
                }
                Files.write(pathRead, Arrays.asList(sb.toString()), Charset.forName("UTF-8"), StandardOpenOption.APPEND);
            }

            for (int n = 0; n < getN(); n++) {
                StringBuilder sb = new StringBuilder();
                for (int k = 0; k < getK(); k++) {
                    Double d = ThreadLocalRandom.current().nextDouble();
                    sb.append(String.format("%.2f ", d));
                    matrix2.add(d);
                }
                Files.write(pathRead, Arrays.asList(sb.toString()), Charset.forName("UTF-8"), StandardOpenOption.APPEND);
            }

            Files.write(pathRead, Arrays.asList("=== цитат ==="), Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        } else if (Files.isReadable(pathRead)) {
            List<String> pathReadLines = new ArrayList<>();
            Files.lines(pathRead).forEach(s -> pathReadLines.add(s));
            List<String> matrixSizeStr = Arrays.asList(pathReadLines.get(1).split("\\s+"));
            setM(Integer.valueOf(matrixSizeStr.get(0)));
            setN(Integer.valueOf(matrixSizeStr.get(1)));
            setK(Integer.valueOf(matrixSizeStr.get(2)));

            for (int m = 2; m < getM() + 2; m++) {
                List<String> matrixRow = Arrays.asList(pathReadLines.get(m).split("\\s+"));
                for (int n = 0; n < getN(); n++) {
                    matrix1.add(Double.valueOf(matrixRow.get(n)));
                }
            }

            for (int i = 2 + getM(); i < getM() + getN() + 2; i++) {
                List<String> matrixRow = Arrays.asList(pathReadLines.get(i).split("\\s+"));
                for (int k = 0; k < getK(); k++) {
                    matrix2.add(Double.valueOf(matrixRow.get(k)));
                }
            }
        } else {
            // P<rint out the time for execution to console.
            System.err.println("Data corrupted!");
        }

        List<Thread> jobs = new ArrayList<>();

        // Distribute task on multiple threads.contentList
        if (getNumThreads() > getM() * getK()) {
            for (int m = 0; m < getM(); m++) {
                List<Double> matrix2Sub = new ArrayList<>();
                for (int k = 0; k < getK(); k++) {
                    for (int n = 0; n < getN(); n++) {
                        matrix2Sub.add(matrix2.get(k * n));
                    }
                    M3xRunnable r = new M3xRunnable(matrix1.subList(m * getN(), (m + 1) * getN() - 1), matrix2Sub);
                    Thread t = new Thread(r);
                    t.start();
                    jobs.add(t);
                    if (!isQuiet()) {
                        System.out.format("Thread-%d started.\n", ++jobNumber);
                    }
                }
            }
        } else {
            int counter = 0;
            for (int m = 0; m < getM(); m++) {
                List<Double> matrix2Sub = new ArrayList<>();
                for (int k = 0; k < getK(); k++) {
                    for (int n = 0; n < getN(); n++) {
                        matrix2Sub.add(matrix2.get(k * n));
                    }
                    M3xRunnable r = new M3xRunnable(matrix1.subList(m * getN(), (m + 1) * getN() - 1), matrix2Sub);
                    if (counter < getNumThreads()) {
                        Thread t = new Thread(r);
                        t.start();
                        jobs.add(t);
                        if (!isQuiet()) {
                            System.out.format("Thread-%d started.\n", ++jobNumber);
                        }
                    } else {
                        Thread t = new Thread(r);
                        t.start();
                        jobs.set(counter % getNumThreads(), t);
                    }
                    counter++;
                }
            }
        }

        // Join all threads.
        for (int i = 0; i < jobNumber; i++) {
            try {
                if (jobs.get(i) != null) {
                    jobs.get(i).join();
                    if (!isQuiet()) {
                        System.out.format("Thread-%d stopped.\n", i + 1);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!isQuiet()) {
            System.out.format("Threads used in current run: %d\n", jobNumber);
        }

        if (Files.isWritable(pathWrite)) {
            Files.write(pathWrite, Arrays.asList("=== цитат ==="), Charset.forName("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);
            for (int m = 0; m < getM(); m++) {
                StringBuilder matrixProductRow = new StringBuilder();
                for (int k = 0; k < getK(); k++) {
                    matrixProductRow.append(String.format("%.2f ", result.get((m + 1) * k)));
                }
                Files.write(pathWrite, Arrays.asList(matrixProductRow), Charset.forName("UTF-8"), StandardOpenOption.APPEND);
            }
            Files.write(pathWrite, Arrays.asList("=== цитат ==="), Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        } else {
            System.err.println("Data corrupted!");
        }

        // Record the end time for execution.
        final long stopTime = System.currentTimeMillis();

        // Calculate the time for execution.
        final long elapsedTime = stopTime - startTime;

        // Print out the time for execution to console.
        System.out.format("Total execution time for current run (millis): %d\n", elapsedTime);
    }

    /**
     * Execute the program implementing m3x calculation algorithm.
     *
     * @since 1.0.
     */
    public static void main(String[] args) {
        // Print out a leading message to introduce the user to the program.
        System.out.format("This is an example for m3x calculation.\nEnter input file name, output file name, "
                                          + "width and height of the first matrix, width and height of the second, "
                                          + "number od threads and mode in format: -i String -o String -m int -n int -k int -t int -q\n");

        // Set a response in case of bad request.
        final String WRONG_REQUEST = "Wrong request format!";

        // Read the input from the console.
        Scanner scan = new Scanner(System.in);
        List<String> request = Arrays.asList(scan.nextLine().split("\\s+"));

        // Test for bad input.
        if (request.size() != 6 && request.size() != 7 && request.size() != 10 && request.size() != 11 && request.size() != 12 && request.size() != 13) {
            System.err.println(WRONG_REQUEST);
            return;
        }

        // Initialize if there are 6 or 7 parameters.
        if (request.size() == 6 || request.size() == 7) {
            if (!"-i".equals(request.get(0)) || !"-o".equals(request.get(2)) || !"-t".equals(request.get(4))) {
                System.err.println(WRONG_REQUEST);
                return;
            } else {
                setInputFile(request.get(1));
                setOutputFile(request.get(3));
                try {
                    setNumThreads(Integer.valueOf(request.get(3)));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (request.size() == 7 && "-q".equals(request.get(6))) {
                    setQuiet(true);
                }
            }
        }

        // Initialize if there are 10 or 11 parameters.
        if (request.size() == 10 || request.size() == 11) {
            if (!"-o".equals(request.get(0)) || !"-m".equals(request.get(2)) || !"-n".equals(request.get(4)) || !"-k".equals(request.get(6)) || !"-t".equals(
                            request.get(6))) {
                System.err.println(WRONG_REQUEST);
                return;
            } else {
                setOutputFile(request.get(1));
                try {
                    setM(Integer.valueOf(request.get(3)));
                    setN(Integer.valueOf(request.get(5)));
                    setK(Integer.valueOf(request.get(7)));
                    setNumThreads(Integer.valueOf(request.get(9)));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (request.size() == 11 && "-q".equals(request.get(10))) {
                    setQuiet(true);
                }
            }
        }

        // Initialize if there are 12 or 13 parameters.
        if (request.size() == 12 || request.size() == 13) {
            if (!"-i".equals(request.get(0)) || !"-o".equals(request.get(2)) || !"-m".equals(request.get(4)) || !"-n".equals(request.get(6)) || !"-k".equals(
                            request.get(8)) || !"-t".equals(request.get(10))) {
                System.err.println(WRONG_REQUEST);
                return;
            } else {
                setInputFile(request.get(1));
                setOutputFile(request.get(3));
                try {
                    setM(Integer.valueOf(request.get(5)));
                    setN(Integer.valueOf(request.get(7)));
                    setK(Integer.valueOf(request.get(9)));
                    setNumThreads(Integer.valueOf(request.get(11)));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (request.size() == 13 && "-q".equals(request.get(12))) {
                    setQuiet(true);
                }
            }
        }

        Path pathRead = Paths.get(getInputFile());
        Path pathWrite = Paths.get(getOutputFile());

        // Do the magic.
        try {
            runDistributed(pathRead, pathWrite);
        } catch (IOException | NumberFormatException | IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public static String getInputFile() {
        return inputFile;
    }

    public static void setInputFile(String inputFile) {
        M3xTest.inputFile = inputFile;
    }

    public static String getOutputFile() {
        return outputFile;
    }

    public static void setOutputFile(String outputFile) {
        M3xTest.outputFile = outputFile;
    }

    public static int getM() {
        return m;
    }

    public static void setM(int m) {
        M3xTest.m = m;
    }

    public static int getN() {
        return n;
    }

    public static void setN(int n) {
        M3xTest.n = n;
    }

    public static int getK() {
        return k;
    }

    public static void setK(int k) {
        M3xTest.k = k;
    }

    public static int getNumThreads() {
        return numThreads;
    }

    public static void setNumThreads(int numThreads) {
        M3xTest.numThreads = numThreads;
    }

    public static boolean isQuiet() {
        return quiet;
    }

    public static void setQuiet(boolean quiet) {
        M3xTest.quiet = quiet;
    }
}