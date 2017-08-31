package ru.otus.chepiov.l14;


import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.Assert.assertArrayEquals;

public class SortTest {

    private int[] input;

    @Before
    public void before() {
        final int n = 1000;
        input = new int[n];
        final Random random = new Random();
        IntStream.iterate(0, i -> ++i).limit(n).forEach(i -> input[i] = random.nextInt());
    }

    @Test
    public void sort() {
        int[] actual = ParallelOddEven.sort(input);
        Arrays.sort(input);
        assertArrayEquals(input, actual);
    }
}