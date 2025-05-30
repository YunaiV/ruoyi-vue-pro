//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.somle.lazada.sdk.util.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class JSONReader {
    public static final int FIRST = 0;
    public static final int CURRENT = 1;
    public static final int NEXT = 2;
    private static final Object OBJECT_END = new Object();
    private static final Object ARRAY_END = new Object();
    private static final Object COLON = new Object();
    private static final Object COMMA = new Object();
    private static Map<Character, Character> escapes = new HashMap();

    static {
        escapes.put('"', '"');
        escapes.put('\\', '\\');
        escapes.put('/', '/');
        escapes.put('b', '\b');
        escapes.put('f', '\f');
        escapes.put('n', '\n');
        escapes.put('r', '\r');
        escapes.put('t', '\t');
    }

    private CharacterIterator it;
    private char c;
    private Object token;
    private StringBuffer buf = new StringBuffer();

    private char next() {
        this.c = this.it.next();
        return this.c;
    }

    private void skipWhiteSpace() {
        while (Character.isWhitespace(this.c)) {
            this.next();
        }

    }

    public Object read(CharacterIterator ci, int start) {
        this.it = ci;
        switch (start) {
            case 0:
                this.c = this.it.first();
                break;
            case 1:
                this.c = this.it.current();
                break;
            case 2:
                this.c = this.it.next();
        }

        return this.read();
    }

    public Object read(CharacterIterator it) {
        return this.read(it, 2);
    }

    public Object read(String string) {
        return this.read(new StringCharacterIterator(string), 0);
    }

    private Object read() {
        this.skipWhiteSpace();
        char ch = this.c;
        this.next();
        switch (ch) {
            case '"':
                this.token = this.string();
                break;
            case ',':
                this.token = COMMA;
                break;
            case ':':
                this.token = COLON;
                break;
            case '[':
                this.token = this.array();
                break;
            case ']':
                this.token = ARRAY_END;
                break;
            case 'f':
                this.next();
                this.next();
                this.next();
                this.next();
                this.token = Boolean.FALSE;
                break;
            case 'n':
                this.next();
                this.next();
                this.next();
                this.token = null;
                break;
            case 't':
                this.next();
                this.next();
                this.next();
                this.token = Boolean.TRUE;
                break;
            case '{':
                this.token = this.object();
                break;
            case '}':
                this.token = OBJECT_END;
                break;
            default:
                this.c = this.it.previous();
                if (Character.isDigit(this.c) || this.c == '-') {
                    this.token = this.number();
                }
        }

        return this.token;
    }

    private Object object() {
        Map<Object, Object> ret = new HashMap();
        Object key = this.read();

        while (this.token != OBJECT_END) {
            this.read();
            if (this.token != OBJECT_END) {
                ret.put(key, this.read());
                if (this.read() == COMMA) {
                    key = this.read();
                }
            }
        }

        return ret;
    }

    private Object array() {
        List<Object> ret = new ArrayList();
        Object value = this.read();

        while (this.token != ARRAY_END) {
            ret.add(value);
            if (this.read() == COMMA) {
                value = this.read();
            }
        }

        return ret;
    }

    private Object number() {
        int length = 0;
        boolean isFloatingPoint = false;
        this.buf.setLength(0);
        if (this.c == '-') {
            this.add();
        }

        length += this.addDigits();
        if (this.c == '.') {
            this.add();
            length += this.addDigits();
            isFloatingPoint = true;
        }

        if (this.c == 'e' || this.c == 'E') {
            this.add();
            if (this.c == '+' || this.c == '-') {
                this.add();
            }

            this.addDigits();
            isFloatingPoint = true;
        }

        String s = this.buf.toString();
        return isFloatingPoint ? (length < 17 ? Double.valueOf(s) : new BigDecimal(s)) : (length < 19 ? Long.valueOf(s) : new BigInteger(s));
    }

    private int addDigits() {
        int ret;
        for (ret = 0; Character.isDigit(this.c); ++ret) {
            this.add();
        }

        return ret;
    }

    private Object string() {
        this.buf.setLength(0);

        while (this.c != '"') {
            if (this.c == '\\') {
                this.next();
                if (this.c == 'u') {
                    this.add(this.unicode());
                } else {
                    Object value = escapes.get(this.c);
                    if (value != null) {
                        this.add((Character) value);
                    }
                }
            } else {
                this.add();
            }
        }

        this.next();
        return this.buf.toString();
    }

    private void add(char cc) {
        this.buf.append(cc);
        this.next();
    }

    private void add() {
        this.add(this.c);
    }

    private char unicode() {
        int value = 0;

        for (int i = 0; i < 4; ++i) {
            switch (this.next()) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    value = (value << 4) + this.c - 48;
                case ':':
                case ';':
                case '<':
                case '=':
                case '>':
                case '?':
                case '@':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '[':
                case '\\':
                case ']':
                case '^':
                case '_':
                case '`':
                default:
                    break;
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                    value = (value << 4) + this.c - 75;
                    break;
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                    value = (value << 4) + this.c - 107;
            }
        }

        return (char) value;
    }
}
