package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class NoGapBufferTest {

    private NoGapBuffer<Integer> set;

    @Before
    public void setUp() throws Exception {
        set = new NoGapBuffer<>(Integer::compareTo, this::allItemsInRange);
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
        assertEquals(0, set.getMissingElements().size());
    }

    @Test
    public void testNoMissingItemsWithOneElement() throws Exception {
        set.add(0);
        assertEquals(0, set.getMissingElements().size());
    }

    @Test
    public void testNoMissingItemsWithTwoElements() throws Exception {
        set.add(0);
        set.add(1);
        assertEquals(0, set.getMissingElements().size());
    }

    @Test
    public void testNoMissingItemsWithTwoIdenticalElements() throws Exception {
        set.add(0);
        set.add(0);
        assertEquals(0, set.getMissingElements().size());
    }

    @Test
    public void testNoMissingItemsWithThreeElementsAndTwoIdentical() throws Exception {
        set.add(0);
        set.add(0);
        set.add(1);
        assertEquals(0, set.getMissingElements().size());
    }

    @Test
    public void testNoMissingItemsWithThreeElements() throws Exception {
        set.add(5);
        set.add(4);
        set.add(6);
        assertEquals(0, set.getMissingElements().size());
    }

    @Test
    public void testOneMissingItem() throws Exception {
        set.add(4);
        set.add(6);
        assertEquals(1, set.getMissingElements().size());
    }

    @Test
    public void testTwoMissingItems() throws Exception {
        set.add(3);
        set.add(6);
        assertEquals(2, set.getMissingElements().size());
    }

    @Test
    public void testTwoMissingItemElements() throws Exception {
        set.add(3);
        set.add(6);
        assertEquals(new HashSet<>(Arrays.asList(4, 5)), set.getMissingElements());
    }

    @Test
    public void testTwoGaps() throws Exception {
        set.add(3);
        set.add(6);
        set.add(7);
        set.add(9);
        assertEquals(new HashSet<>(Arrays.asList(4, 5, 8)), set.getMissingElements());
    }

    @Test
    public void testTwoGapsUnordered() throws Exception {
        set.add(3);
        set.add(7);
        set.add(6);
        set.add(9);
        assertEquals(new HashSet<>(Arrays.asList(4, 5, 8)), set.getMissingElements());
    }

    @Test
    public void testTwoGapsDuplicates() throws Exception {
        set.add(9);
        set.add(3);
        set.add(7);
        set.add(6);
        set.add(7);
        set.add(3);
        set.add(9);
        assertEquals(new HashSet<>(Arrays.asList(4, 5, 8)), set.getMissingElements());
    }

    @Test
    public void testThreeGapsDuplicates() throws Exception {
        set.add(3);
        set.add(7);
        set.add(11);
        set.add(6);
        set.add(7);
        set.add(3);
        set.add(9);
        assertEquals(new HashSet<>(Arrays.asList(4, 5, 8, 10)), set.getMissingElements());
    }
}