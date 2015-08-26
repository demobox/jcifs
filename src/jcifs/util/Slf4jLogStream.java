package jcifs.util;

import java.io.OutputStream;
import java.io.IOException;

import org.slf4j.Logger;

public class Slf4jLogStream extends LogStream {

    public Slf4jLogStream( Logger logger ) {
        super( new Slf4jLoggingOutputStream( logger ) );
    }

    @Override
    public void println( String x ) {
        super.println( x );
        flush();
    }

    @Override
    public void println( Object x ) {
        super.println( x );
        flush();
    }

    @Override
    public void println( char[] x ) {
        super.println( x );
        flush();
    }

    private static class Slf4jLoggingOutputStream extends OutputStream {
        private static final String LINE_SEP = System.getProperty("line.separator");
        private static final int LINE_SEP_LENGTH = LINE_SEP.length();
        private static final int DEFAULT_BUFFER_SIZE = 2048;

        private final Logger logger;
        private volatile boolean closed = false;
        private byte[] buf;
        private int count;
        private int bufSize;

        private Slf4jLoggingOutputStream( Logger logger ) {
            this.logger = logger;
            bufSize = DEFAULT_BUFFER_SIZE;
            buf = new byte[bufSize];
            count = 0;
        }

        @Override
        public void close() {
            flush();
            closed = true;
        }

        @Override
        public void write(final int b) throws IOException {
            if (closed) {
                throw new IOException("The stream has been closed.");
            }

            if (count == bufSize) {
                // grow the buffer
                final int newBufSize = bufSize + DEFAULT_BUFFER_SIZE;
                final byte[] newBuf = new byte[newBufSize];

                System.arraycopy(buf, 0, newBuf, 0, bufSize);
                buf = newBuf;

                bufSize = newBufSize;
            }

            buf[count] = (byte) b;
            count++;
        }

        @Override
        public void flush() {
            if (count == 0) {
                return;
            }

            final byte[] msgBytes = new byte[count];
            System.arraycopy(buf, 0, msgBytes, 0, count);
            if (logger.isDebugEnabled()) {
                String msg = new String(msgBytes);
                logger.debug(stripTrailingNewlines(msg));
            }
            reset();
        }

        private static String stripTrailingNewlines(String str) {
            // very inefficient
            String result = str;
            while (result.endsWith(LINE_SEP)) {
                result = result.substring(0, result.length() - LINE_SEP_LENGTH);
            }
            return result;
        }

        private void reset() {
            count = 0;
        }
    }
}