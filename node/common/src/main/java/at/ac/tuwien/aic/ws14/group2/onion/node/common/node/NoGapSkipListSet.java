package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by stefan on 02.12.14.
 */
public class NoGapSkipListSet<T> extends ConcurrentSkipListSet<T> {

    private final ConcurrentSkipListSet<T> missingElements;
    private final GapComparator<T> gapComparator;

    public NoGapSkipListSet(Comparator<T> comparator, GapComparator<T> gapComparator) {
        super(comparator);
        this.gapComparator = gapComparator;
        missingElements = new ConcurrentSkipListSet<>(comparator);
    }

    @Override
    public synchronized boolean add(T t) {
        if(size() > 0) {
            Set<T> elementsInBetween = t.equals(first()) ?
                    gapComparator.getElementsInBetween(first(), t) : gapComparator.getElementsInBetween(last(), t);
            missingElements.addAll(elementsInBetween);
        }
        missingElements.remove(t);
        return super.add(t);
    }

    public Set<T> getMissingElements() {
        return missingElements;
    }
}
