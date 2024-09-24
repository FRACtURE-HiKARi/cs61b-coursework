package deque;

import java.util.Comparator;
import java.util.Iterator;
public class MaxArrayDeque<T> extends ArrayDeque<T>{
    private Comparator<T> comparator;
    public MaxArrayDeque(Comparator<T> c) {
        super();
        comparator = c;
    }

    public T max(){
        return getMax(comparator);
    }

    public T max(Comparator<T> c){
        return getMax(c);
    }

    private T getMax(Comparator<T> c){
        Iterator<T> iter = iterator();
        T max = iter.next();
        while(iter.hasNext()){
            T next = iter.next();
            if (c.compare(max,next) > 0) max = next;
        }
        return max;
    }

}
