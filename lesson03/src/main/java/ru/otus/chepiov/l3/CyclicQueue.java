package ru.otus.chepiov.l3;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * An unbound queue based on cyclic array.
 * <p>
 * This queue order elements in a FIFO (first-in-first-out) manner and do not allow insertion
 * of {@code null} elements.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public final class CyclicQueue<E> implements Queue<E> {

    private final static int DEFAULT_CAPACITY = 10;

    /**
     * The backed array.
     */
    private Object[] queue;
    /**
     * The head index.
     */
    private int head;
    /**
     * The tail index.
     */
    private int tail;
    /**
     * The mask of indexes of set elements in the backed array.
     */
    private BitSet mask;
    /**
     * Initial capacity of the backed array.
     */
    private final int capacity;
    /**
     * The number of times this list has been <i>structurally modified</i>.
     */
    private transient int modCount = 0;
    /**
     * Size of queue.
     */
    private int size = 0;

    /**
     * Creates new queue with default capacity = 10.
     */
    @SuppressWarnings("WeakerAccess")
    public CyclicQueue() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Creates new queue with {@link #capacity} if capacity > 10 else with capacity = 10.
     *
     * @param capacity initial capacity.
     */
    @SuppressWarnings("WeakerAccess")
    public CyclicQueue(final int capacity) {
        final int cap = capacity > DEFAULT_CAPACITY ? capacity : DEFAULT_CAPACITY;
        this.queue = new Object[cap];
        this.head = 0;
        this.tail = -1;
        this.size = 0;
        this.mask = new BitSet(cap);
        this.capacity = cap;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(final Object o) {
        return !Objects.isNull(o) && findAny(o).isPresent();
    }

    @Override
    public Iterator<E> iterator() {
        return new Iter();
    }

    @Override
    public Object[] toArray() {
        return IntStream.iterate(head, this::nextMasked)
                .limit(size)
                .mapToObj(idx -> queue[idx])
                .toArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(final T[] a) {
        if (a.length < this.size) {
            return IntStream.iterate(this.head, this::nextMasked)
                    .limit(this.size)
                    .mapToObj(idx -> this.queue[idx])
                    .toArray(i -> (T[]) Array.newInstance(a.getClass(), 10));
        } else {
            Stream.iterate(new Pair<>(0, this.head), p -> new Pair<>(p._1 + 1, nextMasked(p._2)))
                    .limit(this.size)
                    .forEach(p -> a[p._1] = (T) this.queue[p._2]);
            IntStream.iterate(size, i -> i + 1)
                    .limit(a.length - this.size)
                    .forEach(i -> a[i] = null);
            return a;
        }
    }

    @Override
    public boolean add(final E e) {
        Objects.requireNonNull(e);
        resizeIfNeeded();
        addToTail(e);
        this.modCount++;
        this.size++;
        return true;
    }

    @Override
    public boolean remove(final Object object) {
        Objects.requireNonNull(object);
        Optional<Integer> found = findAny(object);
        found.ifPresent(idx -> {
            this.mask.clear(idx);
            this.size--;
            if (idx == this.head) {
                headDeleted();
            } else if (idx == this.tail) {
                tailDeleted();
            }
        });
        return found.isPresent();
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        Objects.requireNonNull(c);
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        Objects.requireNonNull(c);
        if (c.isEmpty()) {
            return false;
        }
        // Invariant: between head and tail at least two not masked bit.
        if (c.size() + this.size + 2 > queue.length) {
            Object[] prev = toArray();
            Object[] append = c.toArray();
            final int newSize = c.size() * 2;
            this.queue = new Object[newSize];
            System.arraycopy(prev, 0, this.queue, 0, this.size);
            System.arraycopy(append, 0, this.queue, this.size, c.size());
            this.mask = new BitSet(newSize);
            this.mask.set(0, prev.length);
            this.head = 0;
            this.tail = prev.length + append.length - 1;
            this.modCount++;
            this.size = this.size + c.size();
        } else {
            for (E el : c) {
                add(el);
            }
        }
        return true;
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        for (Object e : c) {
            modified |= remove(e);
        }
        return modified;
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        for (Object e : this) {
            if (!c.contains(e)) {
                modified |= remove(e);
            }
        }
        return modified;
    }

    @Override
    public void clear() {
        this.queue = new Object[capacity];
        this.mask = new BitSet(capacity);
        this.head = 0;
        this.tail = -1;
        this.size = 0;
        this.modCount = 0;
    }

    @Override
    public boolean offer(final E e) {
        return add(e);
    }

    @Override
    public E remove() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return poll();
    }

    @Override
    public E poll() {
        if (isEmpty()) {
            return null;
        }
        @SuppressWarnings("unchecked")
        E el = (E) this.queue[head];
        this.mask.clear(head);
        this.queue[head] = null;
        this.size--;
        headDeleted();
        return el;
    }

    @Override
    public E element() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return peek();
    }

    @Override
    public E peek() {
        if (isEmpty()) {
            return null;
        }
        @SuppressWarnings("unchecked")
        E el = (E) this.queue[head];
        return el;
    }

    /**
     * Invariant: between head and tail at least one not masked bit.
     * <p>
     * After invocation invariant: between head and tail at least two not masked bit.
     */
    private void resizeIfNeeded() {
        if (this.size == this.queue.length - 1) {
            Object[] prev = IntStream.iterate(head, this::nextMasked)
                    .limit(this.size)
                    .mapToObj(idx -> queue[idx])
                    .toArray();
            assert (prev.length == this.size && this.size == this.mask.cardinality());
            final int newSize = prev.length * 2;
            this.queue = new Object[newSize];
            System.arraycopy(prev, 0, this.queue, 0, prev.length);
            this.mask = new BitSet(newSize);
            this.mask.set(0, prev.length);
            this.head = 0;
            this.tail = prev.length - 1;
            this.modCount++;
        }
    }

    /**
     * Finds the index of the next masked element in the backed array.
     *
     * @param fromIndex index from which search will be started, inclusive
     * @return index of the next masked element or -1 if queue is empty
     */
    private int nextMasked(final int fromIndex) {
        if (fromIndex >= this.queue.length || ((this.tail < fromIndex) && fromIndex < this.head)) {
            throw new IllegalArgumentException();
        }
        final int diff = this.tail - this.head;
        int next = CyclicQueue.this.mask.nextSetBit(fromIndex + 1);
        if (diff < 0 && next < 0) {
            return CyclicQueue.this.mask.nextSetBit(0);
        } else {
            return next;
        }
    }

    /**
     * Normalize head if deleted.
     */
    private void headDeleted() {
        if (this.size == 0) {
            this.head = 0;
            this.tail = -1;
        } else {
            this.head = (this.head + 1) % this.queue.length;
        }
    }

    /**
     * Normalize tail if deleted.
     */
    private void tailDeleted() {
        if (this.size == 0) {
            this.head = 0;
            this.tail = -1;
        } else {
            this.tail = this.tail == 0 ? this.queue.length - 1 : this.tail - 1;
        }
    }

    /**
     * Adds element to tail and normalize tail index.
     * <p>
     * Must be called after {@link #resizeIfNeeded()}.
     * <p>
     * Invariant - between head and tail at least two not set bit.
     */
    private void addToTail(final Object e) {
        if (this.tail == this.queue.length - 1) {
            this.tail = this.mask.nextClearBit(0);
        } else {
            this.tail = this.tail + 1;
        }
        this.mask.set(this.tail);
        this.queue[tail] = e;
    }

    /**
     * Try to find any object in this queue equal to defined object.
     *
     * @param object to find
     * @return result
     */
    private Optional<Integer> findAny(final Object object) {
        return mask.stream()
                .mapToObj(i -> new Pair<>(i, this.queue[i]))
                .filter(pair -> Objects.equals(pair._2, object))
                .map(pair -> pair._1)
                .findFirst();
    }

    /**
     * Iterator through backed array with cyclic options.
     * <p>
     * Contains checking for concurrent modifications.
     */
    private final class Iter implements Iterator<E> {
        /**
         * Index of element to be returned by subsequent call to next.
         */
        int cursor = CyclicQueue.this.head;
        /**
         * Index of element returned by most recent call to next or
         * previous.  Reset to -1 if this element is deleted by a call
         * to remove.
         */
        int lastRet = -1;
        /**
         * The modCount value that the iterator believes that the backing
         * List should have.  If this expectation is violated, the iterator
         * has detected concurrent modification.
         */
        int expectedModCount = CyclicQueue.this.modCount;

        @Override
        public boolean hasNext() {
            return this.cursor >= 0;
        }

        @Override
        public E next() {
            checkForComodification();
            try {
                int i = this.cursor;
                @SuppressWarnings("unchecked")
                E next = (E) CyclicQueue.this.queue[i];
                this.lastRet = i;
                this.cursor = CyclicQueue.this.nextMasked(cursor);
                return next;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            if (this.lastRet < 0) {
                throw new IllegalStateException();
            }
            checkForComodification();

            try {
                CyclicQueue.this.queue[lastRet] = null;
                CyclicQueue.this.mask.clear(this.lastRet);
                CyclicQueue.this.size--;
                if (this.lastRet == CyclicQueue.this.head) {
                    CyclicQueue.this.headDeleted();
                }
                if (this.lastRet == CyclicQueue.this.tail) {
                    CyclicQueue.this.tailDeleted();
                }
                this.lastRet = -1;
                CyclicQueue.this.size--;
                this.expectedModCount = CyclicQueue.this.modCount = CyclicQueue.this.modCount + 1;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        void checkForComodification() {
            if (CyclicQueue.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Represents simple pair.
     *
     * @param <A> the type of the first element
     * @param <B> the type of the second element
     */
    private static final class Pair<A, B> {
        final A _1;
        final B _2;

        /**
         * Ctor.
         *
         * @param a first element
         * @param b second element
         */
        Pair(A a, B b) {
            this._1 = a;
            this._2 = b;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CyclicQueue<?> that = (CyclicQueue<?>) o;

        return head == that.head
                && tail == that.tail
                && Arrays.equals(toArray(), that.toArray());
    }

    @Override
    public int hashCode() {
        int result = head;
        result = 31 * result + tail;
        result = 31 * result + size;
        return result;
    }

    @Override
    public String toString() {
        return "CyclicQueue{" +
                (Objects.isNull(queue) ? "" : ", head=" + queue[head]) +
                (Objects.isNull(queue) ? "" : ", tail=" + queue[tail]) +
                ", size=" + size +
                '}';
    }
}
