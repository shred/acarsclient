package org.shredzone.acars.client;

/**
 * A {@link Command} consists of an {@link Operation} and optional parameters. It is
 * either sent to or received from the ACARSD server.
 *
 * @author Richard "Shred" Körber
 */
public class Command {

    private final Operation operation;

    public Command(Operation operation) {
        this.operation = operation;
    }

    public Operation getOperation() {
        return operation;
    }

}
