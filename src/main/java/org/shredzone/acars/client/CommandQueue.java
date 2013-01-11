package org.shredzone.acars.client;

import java.util.LinkedList;
import java.util.List;

/**
 * A queue for received {@link Command}s.
 *
 * @author Richard "Shred" Körber
 */
public class CommandQueue {

    private List<Command> commandQueue = new LinkedList<Command>();

    public boolean isEmpty() {
        synchronized (commandQueue) {
            return commandQueue.isEmpty();
        }
    }

    /**
     * Receives the next {@link Command} from the queue. The method will wait for a new
     * {@link Command} if the queue is empty.
     *
     * @return {@link Command} that was received
     */
    public Command nextCommand() throws InterruptedException {
        synchronized (commandQueue) {
            while (true) {
                if (!commandQueue.isEmpty()) {
                    return commandQueue.remove(0);
                }
                commandQueue.wait();
            }
        }
    }

    public void add(Command command) {
        synchronized (commandQueue) {
            commandQueue.add(command);
            commandQueue.notifyAll();
        }
    }

}
