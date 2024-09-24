package deque;
import org.junit.Test;
import static org.junit.Assert.*;
import edu.princeton.cs.algs4.StdRandom;

import java.util.Iterator;

public class IteratorTest {
    @Test
    public void iteratorTest(){
        int length = 16;
        int[] array = new int[length];
        for (int i = 0; i < length; i++){
            array[i] = i;
        }
        ArrayDeque<Integer> A = new ArrayDeque<>();
        LinkedListDeque<Integer> L = new LinkedListDeque<>();
        for (int x: array){
            A.addLast(x);
            L.addLast(x);
        }
        assertEquals(A, L);
    }
}
