package gh2;

import deque.ArrayDeque;
import deque.Deque;
import deque.LinkedListDeque;

public class HarpString implements Instrument{

    private static final int SR = 44100;
    private static final double DECAY = .996;
    private Deque<Double> buffer;

    public HarpString(double frequency){
        int capacity = (int)Math.round(SR / frequency / 2);
        buffer = new LinkedListDeque<>();
        for (int i = 0; i < capacity; i++) buffer.addLast(0.0);
    }

    public void pluck() {
        for (int i = 0; i < buffer.size(); i++) {
            buffer.removeFirst();
            buffer.addLast(Math.random() - 0.5);
        }
    }

    public void tic() {
        double s1 = buffer.removeFirst();
        double s2 = buffer.get(0);
        double s3 = -0.5 * DECAY * (s1 + s2);
        buffer.addLast(s3);
    }

    public double sample() {
        return buffer.get(0);
    }
}
