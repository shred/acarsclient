package org.shredzone.acars.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * A thread that reads from an {@link AcarsInputStream}, builds {@link Command} objects
 * and pushes them to a {@link CommandQueue}. {@link Command} objects are generated
 * depending on the {@link Operation} that was received. Also handles compressed data
 * and chunked data.
 *
 * @author Richard "Shred" Körber
 */
public class ReceiverDaemon implements Runnable {

    private static final String ENCODING = "ISO-8859-1";

    private final AcarsInputStream in;
    private final CommandQueue queue = new CommandQueue();

    private Operation currentOp = null;
    private byte[] currentBuffer;
    private int currentPos;
    private int currentFullLength;
    private String currentSearch;

    public ReceiverDaemon(InputStream input) throws IOException {
        in = new AcarsInputStream(input);

        Thread th = new Thread(this);
        th.setName("acars server receiver");
        th.setDaemon(true);
        th.start();
    }

    public CommandQueue getCommandQueue() {
        return queue;
    }

    public void run() {
        while(true) {
            try {
                handleOperation();
            } catch (Exception ex) {
                ex.printStackTrace(); // TODO: log
            }
        }
    }

    private void handleOperation() throws IOException {
        Operation op = in.readOperation();

        Command command = null;

        switch (op) {
            case AS_CBYE:
            case AS_REPDIS:
            case AS_CRCON:
            case AS_CRCOFF:
            case AS_ADDED:
            case AS_ACCMAIL:
            case AS_STOPMAIL:
            case AS_MAILOK:
            case AS_MAILREJ:
            case AS_SREJ:
            case AS_NOSUBSC:
            case AS_NOTT:
            case AS_AREPST:
            case AS_AREPBUSY:
            case AS_AREPFAIL:
            case AS_NOPAGER:
            case AS_PAGEBLOCK:
            case AS_PAGEROK:
            case AS_PAGERERR:
            case AS_NEEDPWD:
            case AS_ENOK:
            case AS_ENERR:
            case AS_IDENTIFY:
            case AS_IDENTUK:
            case AS_PAGEREMP:
            case AS_NOUPD:
            case AS_LISTSND:
            case AS_NODAILY:
            case AS_HEARTOK:
            case AS_PROXYWT:
                command = readPlain(op);
                break;

            case AS_LASTCON:
            case AS_DAILY:
                command = readValue(op);
                break;

            case AS_CXML:
                command = readCompressedString(op);
                break;

            case AS_RECHCNTC:
            case AS_AREPCNTC:
                startCompressedResult(op);
                break;

            case AS_COMPR:
                command = readCompressedResult(op);
                break;

            default:
                command = readString(op);
        }

        if (command != null) {
            queue.add(command);
        }
    }

    private Command readPlain(Operation op) throws IOException {
        long length = in.readUnsignedInt();
        int lengthInt = (int) (length & 0xFFFF);
        in.readUnsignedInt();                       // ignore value
        in.skipFully(lengthInt);
        return new Command(op);
    }

    private Command readString(Operation op) throws IOException {
        long length = in.readUnsignedInt();
        int lengthInt = (int) (length & 0xFFFF);
        in.readUnsignedInt();                       // ignore value
        String message = in.readString(lengthInt);
        return new StringCommand(op, message);
    }

    private Command readValue(Operation op) throws IOException {
        long length = in.readUnsignedInt();
        int lengthInt = (int) (length & 0xFFFF);
        long value = in.readUnsignedInt();
        in.skipFully(lengthInt);
        return new ValueCommand(op, value);
    }

    private Command readCompressedString(Operation op) throws IOException {
        try {
            long length = in.readUnsignedInt();
            int compressedLengthInt = (int) (length & 0xFFFF);
            int fullLengthInt = (int) ((length >> 16) & 0xFFFF);
            in.readUnsignedInt();                       // ignore value

            byte[] data = new byte[compressedLengthInt];
            byte[] uncompressed = new byte[fullLengthInt];

            in.readFully(data);

            Inflater inflater = new Inflater();
            inflater.setInput(data);
            int resultLength = inflater.inflate(uncompressed);
            inflater.end();

            if (resultLength != fullLengthInt) {
                throw new IOException("bad uncompressed length (needed: " + fullLengthInt
                                + ", actual:" + resultLength + ")");
            }

            return new StringCommand(op, new String(uncompressed, ENCODING));
        } catch (DataFormatException ex) {
            throw new IOException("bad compressed data", ex);
        }
    }

    private void startCompressedResult(Operation op) throws IOException {
        if (currentOp != null) {
            throw new IOException("duplicate start of compressed binary");
        }

        currentOp = op;

        long length = in.readUnsignedInt();
        int lengthInt = (int) (length & 0xFFFF);
        in.readUnsignedInt();                       // ignore value
        String[] info = in.readString(lengthInt).split("\\t");
        if (info.length != 3) {
            throw new IOException("bad compression header");
        }

        try {
            int currentCompressedLength = Integer.parseInt(info[0]);
            currentFullLength = Integer.parseInt(info[1]);
            currentSearch = info[2];

            currentBuffer = new byte[currentCompressedLength];
            currentPos = 0;
        } catch (NumberFormatException ex) {
            throw new IOException("bad number in compression header", ex);
        }
    }

    private Command readCompressedResult(Operation op) throws IOException {
        if (currentOp == null) {
            throw new IOException("compressed binary without header");
        }

        long length = in.readUnsignedInt();
        int lengthInt = (int) (length & 0xFFFF);
        in.readUnsignedInt();                       // ignore value

        in.readFully(currentBuffer, currentPos, lengthInt);
        currentPos += lengthInt;

        if (currentPos < currentBuffer.length) {
            return null;
        }

        byte[] uncompressed = new byte[currentFullLength];

        try {
            Inflater inflater = new Inflater();
            inflater.setInput(currentBuffer);
            int resultLength = inflater.inflate(uncompressed);
            inflater.end();

            if (resultLength != currentFullLength) {
                throw new IOException("bad uncompressed length (needed: " + currentFullLength
                                + ", actual:" + resultLength + ")");
            }


            Command result = new SearchStringCommand(currentOp,
                            new String(uncompressed, ENCODING),
                            currentSearch);

            currentBuffer = null;
            currentOp = null;

            return result;
        } catch (DataFormatException ex) {
            throw new IOException("bad compressed data", ex);
        }
    }

}
