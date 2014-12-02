package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import java.util.Set;

/**
 * Created by stefan on 02.12.14.
 */
public interface GapComparator<T> {
    Set<T> getElementsInBetween(T t1, T t2);
}
