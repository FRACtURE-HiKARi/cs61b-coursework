package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeAList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeAListConstruction();
    }

    public static void timeAListConstruction() {
        int opCount = 1000;
        int iterations = 10;
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();

        while (iterations > 0) {
            AList<Integer> alist = new AList<>();
            Stopwatch timer = new Stopwatch();
            for (int i = 0; i < opCount; i++) {
                alist.addLast(0);
            }
            Ns.addLast(opCount);
            times.addLast(timer.elapsedTime());

            opCount *= 2;
            iterations--;
        }

        printTimingTable(Ns, times, Ns);
    }
}
