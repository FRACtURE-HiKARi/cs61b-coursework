package deque;

import java.util.Arrays;
import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T> {
    private T[] array;
    private int size;
    private int head;

    public ArrayDeque() {
        array = (T[]) new Object[8];
        head = 0;
        size = 0;
    }

    private void resize(int newSize) {
        head = positiveModule(head, array.length);
        T[] newArray = (T[]) new Object[newSize];
        if (head + size > array.length){
            int tail = array.length - head;
            System.arraycopy(array, head, newArray, 0, tail);
            System.arraycopy(array, 0, newArray, array.length - head, size - tail);
        } else {
            System.arraycopy(array, head, newArray, 0, size);
        }
        head = 0;
        array = newArray;
    }

    private int positiveModule(int a, int b) {
        return (a % b) >= 0 ? (a % b) : (a % b + b);
    }

    private void scaleUp(){
        resize(array.length * 2);
    }

    private void scaleDown(){
        resize((int)(array.length / 2));
    }

    public void addFirst(T item) {
        if (size >= array.length) scaleUp();
        head = positiveModule(head - 1, array.length);
        array[head] = item;
        size += 1;
    }

    public void addLast(T item) {
        if (size >= array.length) scaleUp();
        array[positiveModule(size + head, array.length)] = item;
        size += 1;
    }

    public T removeFirst() {
        if (size == 0) return null;
        head = positiveModule(head, array.length);
        T item = array[head];
        head += 1;
        size -= 1;
        if (size * 4 < array.length && size > 8) scaleDown();
        return item;
    }

    public T removeLast() {
        if (size == 0) return null;
        T item = array[positiveModule(size + head - 1, array.length)];
        if (size * 4 < array.length && size > 8) scaleDown();
        size -= 1;
        return item;
    }

    public T get(int index) {
        if (index < 0 || index >= size) return null;
        return array[index];
    }

    public int size() {
        return size;
    }

    public void printDeque(){
        for(int i = 0; i < size; i++){
            System.out.print(array[positiveModule(i + head, array.length)] + " ");
        }
        System.out.println();
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int index = head;
            public boolean hasNext() {
                return index - head < size;
            }
            public T next() {
                if (!hasNext()) return null;
                return array[positiveModule(index++, array.length)];
            }
            public void remove() {
                //System.arraycopy(array, index + 1, array, index, size - index - 1);
                throw new UnsupportedOperationException();
            }
        };
    }

    // TODO: public boolean equals(Object o)
    public boolean equals(Object o){
        if (o instanceof Iterable){
            Iterator<?> other = ((Deque<?>) o).iterator();
            Iterator<T> self = this.iterator();
            while (other.hasNext() && self.hasNext()){
                if (!other.next().equals(self.next())) return false;
            }
        }
        return true;
    }
}
