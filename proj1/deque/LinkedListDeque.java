package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T> {
    private class Node<T> {
        private T data;
        public Node<T> next;
        public Node<T> prev;
        public Node(T data, Node<T> prev, Node<T> next) {
            this.data = data;
            this.next = next;
            this.prev = prev;
        }
    }

    final private Node<T> sentinal;
    private int size;

    public LinkedListDeque() {
        sentinal = new Node<>(null, null, null);
        sentinal.next = sentinal.prev = sentinal;
        size = 0;
    }

    public void addFirst(T data) {
        Node<T> newNode = new Node<>(data, sentinal, sentinal.next);
        sentinal.next.prev = newNode;
        sentinal.next = newNode;
        size += 1;
    }

    public void addLast(T data) {
        Node<T> newNode = new Node<>(data, sentinal.prev, sentinal);
        sentinal.prev.next = newNode;
        sentinal.prev = newNode;
        size += 1;
    }

    public int size(){
        return size;
    }

    public void printDeque(){
        Node<T> node = sentinal.next;
        while (node != sentinal) {
            System.out.print(node.data + " ");
            node = node.next;
        }
    }

    public T removeFirst(){
        if (size > 0) {
            Node<T> first = sentinal.next;
            sentinal.next = first.next;
            first.next.prev = sentinal;
            size -= 1;
            return first.data;
        }
        else return null;
    }

    public T removeLast(){
        if (size > 0) {
            Node<T> last = sentinal.prev;
            sentinal.prev = last.prev;
            last.prev.next = sentinal;
            size -= 1;
            return last.data;
        }
        else return null;
    }

    public T get(int index){
        if (index >= 0 && index < size) {
            Node<T> node = sentinal.next;
            for (int i = 0; i < index; i++) {node = node.next;}
            return node.data;
        } else {
            return null;
        }
    }

    private T traverseRecursive(Node<T> node, int depth){
        if (depth == 0){
            return node.data;
        }
        else return traverseRecursive(node.next, depth-1);
    }

    public T getRecursive(int index){
        if (index >= 0 && index < size) {
            return traverseRecursive(sentinal.next, index);
        }
        else return null;
    }

    public Iterator<T> iterator(){
        return new Iterator<T>(){
            private Node<T> node = sentinal.next;
            public boolean hasNext(){
                return node != sentinal;
            }
            public T next(){
                T data = node.data;
                node = node.next;
                return data;
            }
            public void remove(){
                node.prev.next = node.next;
                node.next.prev = node.prev;
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
