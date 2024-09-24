package bstmap;

import org.antlr.v4.runtime.misc.NotNull;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private static class Node<K, V> {
        private K key;
        private V value;
        private Node<K, V> left, right, parent;
        public Node(K key, V value, Node<K, V> parent) {
            left = right = null;
            this.parent = parent;
            this.value = value;
            this.key = key;
        }
    }

    private int size;
    private Node<K, V> root;
    private Set<K> keys;

    public BSTMap() {
        clear();
    }

    public void clear(){
        root = null;
        size = 0;
        keys = new HashSet<K>();
    }

    public boolean containsKey(K key){
        Node<K, V> node = getNode(key);
        return node != null;
    }

    public V get(K key){
        Node<K, V> node = getNode(key);
        return node == null ? null : node.value;
    }

    private void updateSet(K key){
        size += 1;
        keys.add(key);
    }

    private Node<K, V> getNode(K key){
        Node<K, V> current = root;
        while (current != null) {
            if (current.key.equals(key)) {
                return current;
            } else if (current.key.compareTo(key) < 0) {
                current = current.right;
            } else {
                current = current.left;
            }
        }
        return null;
    }

    public void put(K key, V value){
        if (root == null) {
            updateSet(key);
            root = new Node<>(key, value, null);
        }

        else {
            Node<K, V> current = root;
            while (current != null) {
                if (current.key.equals(key)) {
                    current.value = value;
                    break;
                } else if (current.key.compareTo(key) < 0) {
                    if (current.right == null) {
                        current.right = new Node<>(key, value, current);
                        updateSet(key);
                        break;
                    } else {
                        current = current.right;
                    }
                } else {
                    if (current.left == null) {
                        current.left = new Node<>(key, value, current);
                        updateSet(key);
                        break;
                    } else {
                        current = current.left;
                    }
                }
            }
        }
    }

    public int size() { return size; }

    private Node<K, V> removeRoot(Node<K, V> node){
        if (node == null) return null;
        if (node.left == null && node.right == null) {
            return null;
        } else if (node.left == null) {
            node.right.parent = node.parent;
            return node.right;
        } else if (node.right == null) {
            node.left.parent = node.parent;
            return node.left;
        } else {
            Node<K, V> newRoot = node.right;
            while (newRoot.left != null) { newRoot = newRoot.left; }
            node.value = newRoot.value;
            node.key = newRoot.key;
            removeNode(newRoot);
            return node;
        }
    }

    private V removeNode(Node<K, V> node){
        Node<K, V> newSubtree = removeRoot(node);
        if (node != root) {
            if (node.parent.left == node) node.parent.left = newSubtree;
            else if (node.parent.right == node) node.parent.right = newSubtree;
            else throw new RuntimeException("error return val");
        } else {
            root = newSubtree;
        }
        return node.value;
    }

    public V remove(K key) {
        Node<K, V> node = getNode(key);
        if (node == null) return null;
        //if (keys.remove(key)) size -= 1;
        keys.remove(key);
        size -= 1;
        return removeNode(node);
    }

    public V remove(K key, V value) {
        Node<K, V> node = getNode(key);
        if (node == null) return null;
        if (node.value.equals(value)) {
            if (keys.remove(key)) size -= 1;
            return removeNode(node);
        }
        return null;
    }

    public Iterator<K> iterator() { return keys.iterator(); }

    public Set<K> keySet() { return keys; }

}
