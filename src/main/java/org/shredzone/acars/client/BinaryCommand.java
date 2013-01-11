package org.shredzone.acars.client;

/**
 * A {@link Command} that transports binary data.
 *
 * @author Richard "Shred" Körber
 */
public class BinaryCommand extends Command {

    private final byte[] value;

    public BinaryCommand(Operation op, byte[] value) {
        super(op);
        this.value = value;
    }

    public byte[] getValue() {
        return value;
    }

}
