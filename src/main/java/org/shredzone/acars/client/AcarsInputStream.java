package org.shredzone.acars.client;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads ACARSD data from an {@link InputStream}.
 *
 * @author Richard "Shred" Körber
 */
public class AcarsInputStream extends FilterInputStream {

    private static final String ENCODING = "iso-8859-1";

    public AcarsInputStream(InputStream in) {
        super(in);
    }

    public void readFully(byte[] buffer) throws IOException {
        int pos = 0;
        while (pos < buffer.length) {
            int got = read(buffer, pos, buffer.length - pos);
            if (got < 0) {
                throw new IOException("eof");
            }
            pos += got;
        }
    }

    public void readFully(byte[] buffer, int start, int length) throws IOException {
        int pos = 0;
        while (pos < length) {
            int got = read(buffer, start + pos, length - pos);
            if (got < 0) {
                throw new IOException("eof");
            }
            pos += got;
        }
    }

    public long readUnsignedInt() throws IOException {
        byte[] data = new byte[4];
        readFully(data);

        // Little endian encoded
        return   ((data[0] & 0xFF)      )
               | ((data[1] & 0xFF) <<  8)
               | ((data[2] & 0xFF) << 16)
               | ((data[3] & 0xFF) << 24);
    }

    public Operation readOperation() throws IOException {
        int op = (int) readUnsignedInt();
        Operation result = Operation.findOperation(op);
        if (result == null) {
            throw new IOException("Unknown operation code " + op);
        }
        return result;
    }

    public String readString(int length) throws IOException {
        byte[] data = new byte[length];
        readFully(data);
        return new String(data, ENCODING);
    }

    public byte[] readBinary(int length) throws IOException {
        byte[] data = new byte[length];
        readFully(data);
        return data;
    }

    public void skipFully(int length) throws IOException {
        long skipped = 0;
        while (skipped < length) {
            long real = skip(length);
            if (real >= 0) {
                skipped += real;
            }
        }
    }

}
