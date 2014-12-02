package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by stefan on 02.12.14.
 */
public class NoGapBuffer<T> {

    private final ConcurrentSkipListSet<T> missingElements;
    private final ConcurrentSkipListSet<T> elements;
    private final RangeOperation<T> rangeOperation;

    public NoGapBuffer(Comparator<T> comparator, RangeOperation<T> rangeOperation) {
        this.rangeOperation = rangeOperation;
        missingElements = new ConcurrentSkipListSet<>(comparator);
        elements = new ConcurrentSkipListSet<>(comparator);
    }

    public synchronized void add(T t) {
        if(elements.size() > 0) {
            Set<T> elementsInBetween = t.equals(elements.first()) ?
                    rangeOperation.getElementsInBetween(elements.first(), t) : rangeOperation.getElementsInBetween(elements.last(), t);
            missingElements.addAll(elementsInBetween);
        }
        missingElements.remove(t);
        elements.add(t);
    }

    public Set<T> getMissingElements() {
        return new HashSet<>(missingElements);
    }
}
