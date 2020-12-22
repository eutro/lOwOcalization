package eutros.lowocalization.core;

import eutros.lowocalization.core.common.LOwOcalizer;
import net.minecraft.client.MinecraftClient;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LOwOConfig {

    private static final Logger LOGGER = LogManager.getLogger(LOwOcalization.MOD_NAME + " Config");

    private static final File CONFIG_DIR = new File(MinecraftClient.getInstance().runDirectory, "config");
    private static final File FILE = new File(CONFIG_DIR, "lowocalization.lowo");

    public static final List<String> DEFAULT_LINES = Arrays.asList(
            "// The list of transformations to apply.",
            "// Refer to the wiki (https://github.com/eutropius225/lOwOcalization/wiki/Transformations) for what the options are",
            "// This file format looks pretty decent with Kotlin highlighting, actually!",
            "\"'l'->'w'\"",
            "\"'L'->'W'\"",
            "\"'r'->'w'\"",
            "\"'R'->'W'\"",
            "\"\"\"s/(\\w|^)s+(\\W|$)/$1th$2/g\"\"\"",
            "\"\"\"s/(\\w|^)S+(\\W|$)/$1TH$2/g\"\"\"",
            "\"\"\"s/([NM])([AO])/$1Y$2/g\"\"\"",
            "\"\"\"s/([nm])([ao])/$1y$2/ig\"\"\"",
            "",
            "\"\"\"__asm__ /(\\s|^)(\\w)/g",
            "NEW \"java/lang/StringBuilder\"",
            "DUP",
            "ALOAD 1",
            "INVOKESPECIAL \"java/lang/StringBuilder\" \"<init>\" \"(Ljava/lang/String;)V\"",
            "",
            "ALOAD 3",
            "INVOKEVIRTUAL \"java/util/Random\" \"nextDouble\" \"()D\"",
            "LDC double 0.3",
            "DCMPL",
            "IFGE POST_STUTTER",
            "",
            "ALOAD 2",
            "INVOKEVIRTUAL \"java/lang/StringBuilder\" \"append\" \"(Ljava/lang/String;)Ljava/lang/StringBuilder;\"",
            "LDC string \"-\"",
            "INVOKEVIRTUAL \"java/lang/StringBuilder\" \"append\" \"(Ljava/lang/String;)Ljava/lang/StringBuilder;\"",
            "",
            "LABEL POST_STUTTER",
            "",
            "ALOAD 2",
            "INVOKEVIRTUAL \"java/lang/StringBuilder\" \"append\" \"(Ljava/lang/String;)Ljava/lang/StringBuilder;\"",
            "INVOKEVIRTUAL \"java/lang/Object\" \"toString\" \"()Ljava/lang/String;\"",
            "ARETURN\"\"\""
    );

    static void init() {
        new Thread(() -> {
            try(WatchService watcher = FileSystems.getDefault().newWatchService()) {
                if(CONFIG_DIR.mkdirs() || FILE.createNewFile()) {
                    FileUtils.writeLines(FILE, DEFAULT_LINES);
                }
                onChange();

                Path path = CONFIG_DIR.toPath();
                path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
                while(true) {
                    WatchKey key;
                    try {
                        key = watcher.poll(25, TimeUnit.MILLISECONDS);
                    } catch(InterruptedException e) {
                        return;
                    }
                    if(key == null) {
                        Thread.yield();
                        continue;
                    }

                    for(WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();

                        if(kind == StandardWatchEventKinds.OVERFLOW) {
                            Thread.yield();
                            continue;
                        } else if(kind == StandardWatchEventKinds.ENTRY_MODIFY
                                && filename.toString().equals(FILE.getName())) {
                            onChange();
                        }
                        boolean valid = key.reset();
                        if(!valid) {
                            break;
                        }
                    }
                    Thread.yield();
                }
            } catch(Throwable e) {
                LOGGER.error(e);
            }
        }, String.format("%s Config Watcher", LOwOcalization.MOD_NAME)).start();
    }

    private static void onChange() throws IOException {
        List<String> transformations = new LinkedList<>();
        if(!FILE.exists()) FileUtils.writeLines(FILE, DEFAULT_LINES); // how
        try(PushbackReader rd = new PushbackReader(new BufferedReader(new InputStreamReader(new FileInputStream(FILE))), 3)) {
            int c;
            while((c = rd.read()) != -1) {
                switch(c) {
                    case '\"': // string literals!!
                    case '\'':
                        char delimiter = (char) c;
                        int c1, c2;
                        boolean raw; // Kotlin syntax for raw strings
                        raw = (c1 = rd.read()) == delimiter;
                        raw &= (c2 = rd.read()) == delimiter;
                        if(!raw) {
                            rd.unread(c2);
                            rd.unread(c1);
                        }
                        StringBuilder sb = new StringBuilder();
                        while(true) {
                            while((c = rd.read()) != delimiter) {
                                if(c == '\\' && !raw) {
                                    switch(c = rd.read()) {
                                        case 't':
                                            sb.append('\t');
                                            break;
                                        case 'b':
                                            sb.append('\b');
                                            break;
                                        case 'n':
                                            sb.append('\n');
                                            break;
                                        case 'r':
                                            sb.append('\r');
                                            break;
                                        case 'f':
                                            sb.append('\f');
                                            break;
                                        case 'u':
                                            int hex = Character.digit(c, 16);
                                            for(int i = 0; i < 3; i++) {
                                                c = rd.read();
                                                if(!(('0' <= c && c <= '9') ||
                                                        ('a' <= c && c <= 'f') ||
                                                        ('A' <= c && c <= 'F'))) {
                                                    LOGGER.error("Unrecognised hex digit '{}'", (char) c);
                                                    return;
                                                }
                                                hex *= 16;
                                                hex += Character.digit(c, 16);
                                            }
                                            sb.append((char) hex);
                                            break;
                                        case '\'':
                                        case '\"':
                                        case '\\':
                                            sb.append((char) c);
                                            break;
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
                                            sb.append((char) octal);
                                            break;
                                        default:
                                            LOGGER.error("Unrecognised escape character: '{}'", (char) c);
                                            return;
                                    }
                                } else if(c == -1) {
                                    LOGGER.error("Unexpected EOF while reading string");
                                } else {
                                    sb.append((char) c);
                                }
                            }
                            if(raw) {
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
                        transformations.add(sb.toString());
                        break;
                    case '#': // # line comments
                    case ';': // ; line comments
                        while(c != -1 && c != '\n') c = rd.read();
                        break;
                    case '/':
                        if((c = rd.read()) == '*') { // /* */ block comments
                            do /* do do */ do c = rd.read(); while(c != '*' && c != -1); while(rd.read() != '/');
                            break;
                        } else if (c == '/') { // // line comments
                            while(c != -1 && c != '\n') c = rd.read();
                            break;
                        }

                        c = '/';
                    default:
                        if(Character.isWhitespace(c) || c == ',') break;
                        LOGGER.error("Unexpected character '{}'", (char) c);
                        return;
                }
            }
        }
        LOwOcalizer.INSTANCE.configChange(transformations);
    }

}
