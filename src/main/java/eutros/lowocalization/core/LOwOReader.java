package eutros.lowocalization.core;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

// The lowo format used on Fabric
public class LOwOReader {

    public static List<String> read(Reader reader) throws IOException, LOwOSyntaxException {
        List<String> strings = new LinkedList<>();
        PushbackReader rd = new PushbackReader(reader, 2);
        int c;
        while((c = rd.read()) != -1) {
            switch(c) {
                case '\"': // string literals!!
                case '\'':
                    strings.add(readString(rd, (char) c));
                    break;
                case '#': // # line comments
                case ';': // ; line comments
                    lineComment(rd);
                    break;
                case '/':
                    switch(rd.read()) {
                        case '*':
                            blockComment(rd);
                            continue;
                        case '/':
                            lineComment(rd);
                            continue;
                    }
                default:
                    if(Character.isWhitespace(c) || c == ',') break;
                    throw new LOwOSyntaxException("Unexpected character '%c'", (char) c);
            }
        }
        return strings;
    }

    private static void lineComment(PushbackReader rd) throws IOException {
        int c;
        do c = rd.read(); while(c != -1 && c != '\n');
    }

    private static void blockComment(PushbackReader rd) throws IOException {
        int c;
        do /* do do */ do c = rd.read(); while(c != '*' && c != -1); while(rd.read() != '/');
    }

    private static String readString(PushbackReader rd, char delimiter) throws IOException, LOwOSyntaxException {
        int c, c1, c2;
        boolean raw = delimiter == '\'';
        boolean multiline;
        multiline = (c1 = rd.read()) == delimiter;
        multiline &= (c2 = rd.read()) == delimiter;
        if(!multiline) {
            rd.unread(c2);
            rd.unread(c1);
        }
        StringBuilder sb = new StringBuilder();
        while(true) {
            while((c = rd.read()) != delimiter) {
                if(!raw && c == '\\') {
                    c = readEscape(rd);
                    if (c == -1) continue;
                } else if(!multiline && c == '\n') {
                    throw new LOwOSyntaxException("Unexpected end of line while reading single-quoted string");
                } else if(c == -1) {
                    throw new LOwOSyntaxException("Unexpected EOF while reading string");
                }
                sb.append((char) c);
            }
            if(multiline) {
                boolean finished;
                finished = (c1 = rd.read()) == delimiter;
                finished &= (c2 = rd.read()) == delimiter;
                if(finished) break;
                rd.unread(c2);
                rd.unread(c1);
                sb.append((char) c);
                continue;
            }
            break;
        }
        return sb.toString();
    }

    private static int readEscape(PushbackReader rd) throws IOException, LOwOSyntaxException {
        int c;
        switch(c = rd.read()) {
            case 't':
                return '\t';
            case 'b':
                return '\b';
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 'f':
                return '\f';
            case 'u':
                int hex = 0;
                for(int i = 0; i < 4; i++) {
                    c = rd.read();
                    if(!(('0' <= c && c <= '9') || ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F'))) {
                        throw new LOwOSyntaxException("Unrecognised hex digit '%c'", (char) c);
                    }
                    hex *= 16;
                    hex += Character.digit(c, 16);
                }
                return (char) hex;
            case '\'':
            case '\"':
            case '\\':
                return (char) c;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
                int octal = Character.digit(c, 8);
                for(int i = 0; i < 2; i++) {
                    c = rd.read();
                    if(c < '0' || '7' < c) {
                        rd.unread(c);
                        break;
                    }
                    octal *= 8;
                    octal += Character.digit(c, 8);
                }
                return (char) octal;
            case '\r':
                if(rd.read() != '\n') break;
            case '\n':
                do c = rd.read(); while(Character.isWhitespace(c));
                rd.unread(c);
                return -1;
        }
        throw new LOwOSyntaxException("Unrecognised escape sequence \\%c", (char) c);
    }

    public static class LOwOSyntaxException extends Exception {

        public LOwOSyntaxException(String message, Object... format) {
            super(String.format(message, format));
        }

    }

}
