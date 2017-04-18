package ru.otus.chepiov.l3;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Test case for ArrayList.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class CyclicQueueTest {

    @Test
    public void homeWorkTest() {
        Queue<Integer> queue = new CyclicQueue<>();
        Assert.assertTrue(Collections.addAll(queue, 1, 2, 3));
        Assert.assertTrue(queue.size() == 3);
        Assert.assertTrue(Collections.addAll(queue, 1, 3, 4));
        Assert.assertTrue(Collections.frequency(queue, 1) == 2);
        Assert.assertTrue(Collections.frequency(queue, 2) == 1);
        Assert.assertTrue(Collections.frequency(queue, 3) == 2);
        Assert.assertTrue(Collections.frequency(queue, 4) == 1);
    }

    @Test(expected = NoSuchElementException.class)
    public void addRemoveContainsElements() {
        final Queue<Integer> queue = new CyclicQueue<>();
        Assert.assertTrue(queue.size() == 0);
        Assert.assertTrue(queue.add(1));
        Assert.assertTrue(queue.size() == 1);
        Assert.assertTrue(queue.remove() == 1);
        Assert.assertTrue(queue.size() == 0);

        IntStream.iterate(0, i -> i + 1).limit(20).forEach(queue::add);
        Assert.assertTrue(queue.size() == 20);
        Assert.assertTrue(queue.remove(10));
        Assert.assertFalse(queue.remove(100));
        Assert.assertTrue(queue.size() == 19);
        Assert.assertFalse(queue.contains(10));
        Assert.assertTrue(queue.contains(19));
        Assert.assertTrue(queue.containsAll(IntStream.iterate(0, i -> i + 1).limit(9).boxed().collect(toList())));
        Assert.assertTrue(queue.remove() == 0);
        Assert.assertTrue(queue.size() == 18);

        IntStream.iterate(0, i -> i + 1).limit(18).forEach(ignore -> queue.remove());
        Assert.assertTrue(queue.size() == 0);

        Assert.assertTrue(queue.add(0));
        Assert.assertTrue(queue.size() == 1);
        Assert.assertTrue(queue.addAll(IntStream.iterate(0, i -> i + 1).limit(10).boxed().collect(toList())));
        Assert.assertTrue(queue.size() == 11);
        Assert.assertTrue(queue.addAll(IntStream.iterate(0, i -> i + 1).limit(3).boxed().collect(toList())));
        Assert.assertTrue(queue.size() == 14);

        Assert.assertTrue(queue.removeAll(IntStream.iterate(0, i -> i + 1).limit(10).boxed().collect(toList())));
        Assert.assertTrue(queue.size() == 4);
        Assert.assertTrue(queue.removeAll(IntStream.iterate(0, i -> i + 1).limit(3).boxed().collect(toList())));
        Assert.assertTrue(queue.size() == 1);
        Assert.assertTrue(queue.remove(0));
        Assert.assertTrue(queue.size() == 0);
        queue.remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void pollPeekOfferElements() {
        Queue<Integer> queue = new CyclicQueue<>();
        IntStream.iterate(0, i -> i + 1).limit(100).forEach(queue::offer);
        Assert.assertTrue(queue.size() == 100);
        IntStream.iterate(0, i -> i + 1).limit(100).forEach(i -> {
            Assert.assertTrue(queue.peek() == i);
            Assert.assertTrue(queue.size() == 100 - i);
            Assert.assertTrue(queue.poll() == i);
            Assert.assertTrue(queue.size() == 100 - i - 1);
        });
        Assert.assertTrue(queue.size() == 0);
        Assert.assertTrue(queue.peek() == null);
        Assert.assertTrue(queue.poll() == null);
        queue.element();
    }

    @Test
    public void retainAll() {
        Queue<Integer> queue = new CyclicQueue<>();
        Collections.addAll(queue, 1, 2, 3);

        Collection<Integer> collection  = new HashSet<Integer>(){{
            add(1);
            add(2);
            add(4);
        }};

        queue.retainAll(collection);
        Assert.assertTrue(queue.size() == 2);
    }

    @Test
    public void iterator() {
        List<Integer> sample = Arrays.asList(1, 2, 3);
        Queue<Integer> queue = new CyclicQueue<>();
        //noinspection UseBulkOperation
        sample.forEach(queue::add);
        List<Integer> list = new ArrayList<>();
        Iterator<Integer> iter = queue.iterator();
        //noinspection WhileLoopReplaceableByForEach
        while (iter.hasNext()) {
            //noinspection UseBulkOperation
            list.add(iter.next());
        }
        Assert.assertEquals(list, sample);
        Assert.assertEquals(queue.stream().map(Object::toString).collect(toList()), Arrays.asList("1", "2", "3"));
    }

    @Test(expected = NullPointerException.class)
    public void addNull() {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        Queue<Integer> queue = new CyclicQueue<>();
        queue.add(null);
    }

    @Test
    public void toArray() {
        Queue<Integer> queue = new CyclicQueue<>();
        //noinspection UseBulkOperation
        IntStream.iterate(1, i -> i + 1).limit(3).forEach(queue::add);
        Assert.assertArrayEquals(queue.toArray(), new Object[]{1, 2, 3});
        final Integer[] first = new Integer[3];
        Assert.assertArrayEquals(queue.toArray(first), new Integer[]{1, 2, 3});
        Assert.assertTrue(first == queue.toArray(first));
        final Integer[] second = new Integer[4];
        Assert.assertArrayEquals(queue.toArray(second), new Integer[]{1, 2, 3, null});
        Assert.assertTrue(second == queue.toArray(second));
    }
}
