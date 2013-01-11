package org.shredzone.acars.client;

/**
 * A {@link Command} that consists of an {@link Operation} and a String value.
 *
 * @author Richard "Shred" Körber
 */
public class StringCommand extends Command {

    private final String value;

    public StringCommand(Operation op, String value) {
        super(op);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
