package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

/**
 * Created by Thomas on 25.01.2015.
 */
public interface ConnectionWorkerObserver {

    /**
     * Called when the connection of the corresponding ConnectionWorker is closed.
     *
     * @param connectionWorker The object observed by this observer.
     */
    void connectionClosed(ConnectionWorker connectionWorker);
}
