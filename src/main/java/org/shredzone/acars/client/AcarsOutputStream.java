package org.shredzone.acars.client;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes ACARSD commands to the output stream.
 * <p>
 * As this is a client, the {@link AcarsOutputStream} only offers methods required for
 * client-server communication. For a Java implementation of an ACARS server, you would
 * need to add methods for XML, CXML and binary handling.
 *
 * @author Richard "Shred" Körber
 * @see <a href="http://www.acarsd.org/wiki/index.php/Acarsd_client">acarsd.org wiki</a>
 */
public class AcarsOutputStream extends FilterOutputStream {

    private static final int MAX_BUFFER_SIZE = 4096;
    private static final String ENCODING = "iso-8859-1";

    public AcarsOutputStream(OutputStream out) {
        super(out);
    }

    private void writeUnsignedInt(long value) throws IOException {
        byte[] data = new byte[4];

        // Convert to little endian
        data[0] = (byte) ( value        & 0xFF);
        data[1] = (byte) ((value >>  8) & 0xFF);
        data[2] = (byte) ((value >> 16) & 0xFF);
        data[3] = (byte) ((value >> 24) & 0xFF);

        write(data);
    }

    public void writeOperation(Operation operation) throws IOException {
        writeValue(operation, 0l);
    }

    public void writeValue(Operation operation, long numvalue) throws IOException {
        writeUnsignedInt(operation.getValue());
        writeUnsignedInt(1l);
        writeUnsignedInt(numvalue);
        write('^');                     // dummy character
        flush();
    }

    public void writeString(Operation operation, String message) throws IOException {
        byte[] data = message.getBytes(ENCODING);
        if (data.length > MAX_BUFFER_SIZE) {
            throw new IllegalArgumentException("buffer limit exceeded");
        }

        writeUnsignedInt(operation.getValue());
        writeUnsignedInt(data.length);
        writeUnsignedInt(0l);
        write(data);
        flush();
    }

}
