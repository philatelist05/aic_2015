package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.BufferOverflowException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class NoGapBufferTest {

    private NoGapBuffer<Integer> buffer;
    private int capacity;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        capacity = 10;
        buffer = new NoGapBuffer<>(Integer::compareTo, this::allItemsInRange, capacity);
    }

    private Set<Integer> allItemsInRange(int t1, int t2) {
        Set<Integer> integers = new HashSet<>();
        if (t1 <= t2) {
            for (int i = t1 + 1; i < t2; i++) {
                integers.add(i);
            }
        } else {
            for (int i = t1 - 1; i > t2 ; i--) {
                integers.add(i);
            }
        }
        return integers;
    }

    @Test
    public void testEmptyMissingItems() throws Exception {
        assertEquals(0, buffer.getMissingElements().size());
    }

    @Test
    public void testNoMissingItemsWithOneElement() throws Exception {
        buffer.add(0);
        assertEquals(0, buffer.getMissingElements().size());
    }

    @Test
    public void testNoMissingItemsWithTwoElements() throws Exception {
        buffer.add(0);
        buffer.add(1);
        assertEquals(0, buffer.getMissingElements().size());
    }

    @Test
    public void testNoMissingItemsWithTwoIdenticalElements() throws Exception {
        buffer.add(0);
        buffer.add(0);
        assertEquals(0, buffer.getMissingElements().size());
    }

    @Test
    public void testNoMissingItemsWithThreeElementsAndTwoIdentical() throws Exception {
        buffer.add(0);
        buffer.add(0);
        buffer.add(1);
        assertEquals(0, buffer.getMissingElements().size());
    }

    @Test
    public void testNoMissingItemsWithThreeElements() throws Exception {
        buffer.add(5);
        buffer.add(4);
        buffer.add(6);
        assertEquals(0, buffer.getMissingElements().size());
    }

    @Test
    public void testOneMissingItem() throws Exception {
        buffer.add(4);
        buffer.add(6);
        assertEquals(1, buffer.getMissingElements().size());
    }

    @Test
    public void testTwoMissingItems() throws Exception {
        buffer.add(3);
        buffer.add(6);
        assertEquals(2, buffer.getMissingElements().size());
    }

    @Test
    public void testTwoMissingItemElements() throws Exception {
        buffer.add(3);
        buffer.add(6);
        assertEquals(new HashSet<>(Arrays.asList(4, 5)), buffer.getMissingElements());
    }

    @Test
    public void testTwoGaps() throws Exception {
        buffer.add(3);
        buffer.add(6);
        buffer.add(7);
        buffer.add(9);
        assertEquals(new HashSet<>(Arrays.asList(4, 5, 8)), buffer.getMissingElements());
    }

    @Test
    public void testTwoGapsUnordered() throws Exception {
        buffer.add(3);
        buffer.add(7);
        buffer.add(6);
        buffer.add(9);
        assertEquals(new HashSet<>(Arrays.asList(4, 5, 8)), buffer.getMissingElements());
    }

    @Test
    public void testTwoGapsDuplicates() throws Exception {
        buffer.add(9);
        buffer.add(3);
        buffer.add(7);
        buffer.add(6);
        buffer.add(7);
        buffer.add(3);
        buffer.add(9);
        assertEquals(new HashSet<>(Arrays.asList(4, 5, 8)), buffer.getMissingElements());
    }

    @Test
    public void testThreeGapsDuplicates() throws Exception {
        buffer.add(3);
        buffer.add(7);
        buffer.add(11);
        buffer.add(6);
        buffer.add(7);
        buffer.add(3);
        buffer.add(9);
        assertEquals(new HashSet<>(Arrays.asList(4, 5, 8, 10)), buffer.getMissingElements());
    }

    @Test
    public void testSizeEmptyBuffer() throws Exception {
        assertEquals(0, buffer.size());
    }

    @Test
    public void testSizeFilledBuffer() throws Exception {
        buffer.add(1);
        assertEquals(1, buffer.size());
    }

    @Test
    public void testOverflowException() throws Exception {
        for (int i = 0; i < capacity; i++) {
            buffer.add(i);
        }

        exception.expect(BufferOverflowException.class);
        buffer.add(100);
    }
}