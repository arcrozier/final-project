package photoBracket;

import java.util.*;

public class LinkedListHashSet<E> implements Set<E>, Deque<E> {

    private final Deque<E> order;
    private final Set<E> set;

    public LinkedListHashSet(Collection<? extends E> data) {
        order = new LinkedList<>();
        set = new HashSet<>();
        if (data != null) {
            addAll(data);
        }
    }

    public LinkedListHashSet() {
        this(null);
    }

    @Override
    public void addFirst(E e) {
        if (set.add(e)) order.addFirst(e);
    }

    @Override
    public void addLast(E e) {
        if (set.add(e)) order.addLast(e);
    }

    @Override
    public boolean offerFirst(E e) {
        boolean add;
        if (add = set.add(e)) add = order.offerFirst(e);
        return add;
    }

    @Override
    public boolean offerLast(E e) {
        boolean add;
        if (add = set.add(e)) add = order.offerLast(e);
        return add;
    }

    @Override
    public E removeFirst() {
        E requested = order.removeFirst();
        set.remove(requested);
        return requested;
    }

    @Override
    public E removeLast() {
        E requested = order.removeFirst();
        set.remove(requested);
        return requested;
    }

    @Override
    public E pollFirst() {
        E requested = order.pollFirst();
        set.remove(requested);
        return requested;
    }

    @Override
    public E pollLast() {
        E requested = order.pollLast();
        set.remove(requested);
        return requested;
    }

    @Override
    public E getFirst() {
        return order.getFirst();
    }

    @Override
    public E getLast() {
        return order.getLast();
    }

    @Override
    public E peekFirst() {
        return order.peekFirst();
    }

    @Override
    public E peekLast() {
        return order.peekLast();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return set.remove(o) && order.removeFirstOccurrence(o);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        return set.remove(o) && order.removeLastOccurrence(o);
    }

    @Override
    public boolean offer(E e) {
        boolean accepted;
        if (accepted = (set.add(e) && !order.offer(e))) set.remove(e);
        return accepted;
    }

    @Override
    public E remove() {
        E requested = order.remove();
        set.remove(requested);
        return requested;
    }

    @Override
    public E poll() {
        return pollFirst();
    }

    @Override
    public E element() {
        return getFirst();
    }

    @Override
    public E peek() {
        return peekFirst();
    }

    @Override
    public void push(E e) {
        addFirst(e);
    }

    @Override
    public E pop() {
        return removeFirst();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return order.descendingIterator();
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return order.iterator();
    }

    @Override
    public Object[] toArray() {
        return order.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return order.toArray(a);
    }

    @Override
    public boolean add(E e) {
        if (set.add(e)) {
            order.add(e);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean modified = false;
        for (E datum : c) {
            if (set.add(datum)) {
                order.add(datum);
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = set.retainAll(c);
        order.retainAll(c);
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = set.removeAll(c);
        order.removeAll(c);
        return modified;
    }

    @Override
    public void clear() {
        set.clear();
        order.clear();
    }
}
