package org.shredzone.acars.client;

/**
 * A {@link Command} that consists of an {@link Operation} and a numerical value.
 *
 * @author Richard "Shred" Körber
 */
public class ValueCommand extends Command {

    private final long value;

    public ValueCommand(Operation op, long value) {
        super(op);
        this.value = value;
    }

    /**
     * Returns the transported value.
     *
     * @return Value. It is unsigned 32bit, wrapped in a (signed) long.
     */
    public long getValue() {
        return value;
    }

}
