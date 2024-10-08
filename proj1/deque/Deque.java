package deque;
import java.util.Iterator;

public interface Deque<T> {
    public void addFirst(T item);
    public void addLast(T item);
    public T removeFirst();
    public T removeLast();
    default public boolean isEmpty(){
        return size() == 0;
    }
    public int size();
    public void printDeque();
    public T get(int index);
    public Iterator<T> iterator();
}
