package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE

    @Test
    public void buggyAListTest(){

        AListNoResizing<Integer> truth = new AListNoResizing<>();
        BuggyAList<Integer> buggy = new BuggyAList<>();
        int[] items = {4, 5, 6};
        for(int x: items){
            truth.addLast(x);
            buggy.addLast(x);
        }
        for (int i = 0; i < items.length; i++) {
            assertEquals(truth.removeLast(), buggy.removeLast());
        }
    }

    @Test
    public void randomizedText(){
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> buggy = new BuggyAList<>();

        int N = 10000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 2);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                buggy.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            /*
            } else if (operationNumber == 1) {
                // size
                System.out.println("size: " + L.size() + ", " + buggy.size());
                assertEquals(L.size(), buggy.size());

            */
            } else if (operationNumber == 1) {
                if (L.size() > 0){
                    System.out.println("last: " + L.getLast() + ", " + buggy.getLast() + " (" + L.size() + ")");
                    assertEquals(L.removeLast(), buggy.removeLast());
                }
            }
        }
    }

}
