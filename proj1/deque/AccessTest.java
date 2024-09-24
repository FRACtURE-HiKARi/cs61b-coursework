package deque;

import org.junit.Test;
import static org.junit.Assert.*;
import edu.princeton.cs.algs4.StdRandom;

public class AccessTest {
    @Test
    public void randomAccessTest() {
        int N = 10000000;
        ArrayDeque<Integer> A = new ArrayDeque<>();
        LinkedListDeque<Integer> L = new LinkedListDeque<>();
        for (int i = 0; i < N; i++){
            int operationNumber = StdRandom.uniform(0,4);
            int item = StdRandom.uniform(0,100);
            switch (operationNumber){
                case 0:
                    A.addLast(item);
                    L.addLast(item);
                    break;
                case 1:
                    A.addFirst(item);
                    L.addFirst(item);
                    break;
                case 2:
                    assertEquals(A.removeLast(), L.removeLast());
                    break;
                case 3:
                    assertEquals(A.removeFirst(), L.removeFirst());
                    break;
                default:
                    break;
            }
        }
        assertEquals(A, L);
    }

    @Test
    public void circularAccessTest() {
        int N = 10000000;
        int size = 16;
        ArrayDeque<Integer> A = new ArrayDeque<>();
        LinkedListDeque<Integer> L = new LinkedListDeque<>();
        for (int i = 0; i < size; i++){
            A.addLast(i);
            L.addLast(i);
        }
        for (int i = 0; i < N; i++){
            int x = A.removeFirst();
            int y = L.removeFirst();
            assertEquals(x, y);
            A.addLast(y);
            L.addLast(y);
        }

        for (int i = 0; i < N; i++){
            int x = A.removeLast();
            int y = L.removeLast();
            assertEquals(x, y);
            A.addFirst(y);
            L.addFirst(y);
        }
    }
}
