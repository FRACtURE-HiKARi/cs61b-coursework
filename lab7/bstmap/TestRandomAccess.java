package bstmap;

import org.junit.Test;
import edu.princeton.cs.algs4.StdRandom;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TestRandomAccess {
    @Test
    public void testRandomAccess() {
        int size = 1000000;
        int numOps = 1000000;
        BSTMap<Integer, Integer> map = new BSTMap<>();
        boolean[] status = new boolean[size];
        Arrays.fill(status, false);
        for (int i = 0; i < numOps; i++) {
            int index = StdRandom.uniform(size);
            int operation = StdRandom.uniform(3);
            if (operation == 0) {
                map.put(index, index);
                status[index] = true;
            } else if (operation == 1){
                map.remove(index);
                status[index] = false;
            } else {
                assertEquals(status[index], map.containsKey(index));
            }
            if (i % (numOps / 100) == 0) {
                int count = 0;
                for (boolean b: status) if (b) count++;
                assertEquals(count, map.size());
            }
        }
    }
}
