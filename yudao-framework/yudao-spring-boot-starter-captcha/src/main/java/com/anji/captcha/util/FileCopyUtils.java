package com.anji.captcha.util;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.io.*;
import java.nio.file.Files;

public abstract class FileCopyUtils {
    public static final int BUFFER_SIZE = 4096;

    public FileCopyUtils() {
    }

    public static int copy(File in, File out) throws IOException {
        return copy(Files.newInputStream(in.toPath()), Files.newOutputStream(out.toPath()));
    }

    public static void copy(byte[] in, File out) throws IOException {
        copy((InputStream) (new ByteArrayInputStream(in)), (OutputStream) Files.newOutputStream(out.toPath()));
    }

    public static byte[] copyToByteArray(File in) throws IOException {
        return copyToByteArray(Files.newInputStream(in.toPath()));
    }

    public static int copy(InputStream in, OutputStream out) throws IOException {
        int var2;
        try {
            var2 = StreamUtils.copy(in, out);
        } finally {
            try {
                in.close();
            } catch (IOException var12) {
            }

            try {
                out.close();
            } catch (IOException var11) {
            }

        }

        return var2;
    }

    public static void copy(byte[] in, OutputStream out) throws IOException {
        try {
            out.write(in);
        } finally {
            try {
                out.close();
            } catch (IOException var8) {
            }

        }

    }

    public static byte[] copyToByteArray(InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
            copy((InputStream) in, (OutputStream) out);
            return out.toByteArray();
        }
    }

    public static int copy(Reader in, Writer out) throws IOException {
        try {
            int byteCount = 0;
            char[] buffer = new char[4096];

            int bytesRead;
            for (boolean var4 = true; (bytesRead = in.read(buffer)) != -1; byteCount += bytesRead) {
                out.write(buffer, 0, bytesRead);
            }

            out.flush();
            return byteCount;
        } finally {
            try {
                in.close();
            } catch (IOException var15) {
            }

            try {
                out.close();
            } catch (IOException var14) {
            }

        }
    }

    public static void copy(String in, Writer out) throws IOException {
        try {
            out.write(in);
        } finally {
            try {
                out.close();
            } catch (IOException var8) {
            }

        }

    }

    public static String copyToString(Reader in) throws IOException {
        if (in == null) {
            return "";
        } else {
            StringWriter out = new StringWriter();
            copy((Reader) in, (Writer) out);
            return out.toString();
        }
    }
}

