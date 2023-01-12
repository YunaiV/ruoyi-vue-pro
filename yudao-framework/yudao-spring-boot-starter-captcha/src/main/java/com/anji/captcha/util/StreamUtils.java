package com.anji.captcha.util;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.io.*;
import java.nio.charset.Charset;

public abstract class StreamUtils {
    public static final int BUFFER_SIZE = 4096;
    private static final byte[] EMPTY_CONTENT = new byte[0];

    public StreamUtils() {
    }

    public static byte[] copyToByteArray(InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
            copy((InputStream)in, out);
            return out.toByteArray();
        }
    }

    public static String copyToString(InputStream in, Charset charset) throws IOException {
        if (in == null) {
            return "";
        } else {
            StringBuilder out = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(in, charset);
            char[] buffer = new char[4096];
            boolean var5 = true;

            int bytesRead;
            while((bytesRead = reader.read(buffer)) != -1) {
                out.append(buffer, 0, bytesRead);
            }

            return out.toString();
        }
    }

    public static void copy(byte[] in, OutputStream out) throws IOException {
        out.write(in);
    }

    public static void copy(String in, Charset charset, OutputStream out) throws IOException {
        Writer writer = new OutputStreamWriter(out, charset);
        writer.write(in);
        writer.flush();
    }

    public static int copy(InputStream in, OutputStream out) throws IOException {
        int byteCount = 0;
        byte[] buffer = new byte[4096];

        int bytesRead;
        for(boolean var4 = true; (bytesRead = in.read(buffer)) != -1; byteCount += bytesRead) {
            out.write(buffer, 0, bytesRead);
        }

        out.flush();
        return byteCount;
    }

    public static long copyRange(InputStream in, OutputStream out, long start, long end) throws IOException {
        long skipped = in.skip(start);
        if (skipped < start) {
            throw new IOException("Skipped only " + skipped + " bytes out of " + start + " required");
        } else {
            long bytesToCopy = end - start + 1L;
            byte[] buffer = new byte[4096];

            while(bytesToCopy > 0L) {
                int bytesRead = in.read(buffer);
                if (bytesRead == -1) {
                    break;
                }

                if ((long)bytesRead <= bytesToCopy) {
                    out.write(buffer, 0, bytesRead);
                    bytesToCopy -= (long)bytesRead;
                } else {
                    out.write(buffer, 0, (int)bytesToCopy);
                    bytesToCopy = 0L;
                }
            }

            return end - start + 1L - bytesToCopy;
        }
    }

    public static int drain(InputStream in) throws IOException {
        byte[] buffer = new byte[4096];
        int byteCount;
        int bytesRead;
        for(byteCount = 0; (bytesRead = in.read(buffer)) != -1; byteCount += bytesRead) {
        }

        return byteCount;
    }

    public static InputStream emptyInput() {
        return new ByteArrayInputStream(EMPTY_CONTENT);
    }

    public static InputStream nonClosing(InputStream in) {
        return new NonClosingInputStream(in);
    }

    public static OutputStream nonClosing(OutputStream out) {
        return new NonClosingOutputStream(out);
    }

    private static class NonClosingOutputStream extends FilterOutputStream {
        public NonClosingOutputStream(OutputStream out) {
            super(out);
        }

        public void write(byte[] b, int off, int let) throws IOException {
            this.out.write(b, off, let);
        }

        public void close() throws IOException {
        }
    }

    private static class NonClosingInputStream extends FilterInputStream {
        public NonClosingInputStream(InputStream in) {
            super(in);
        }

        public void close() throws IOException {
        }
    }
}

