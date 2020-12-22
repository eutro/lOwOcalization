package eutros.lowocalization.core;

import eutros.lowocalization.core.common.LOwOcalizer;
import net.minecraft.client.MinecraftClient;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LOwOConfig {

    private static final Logger LOGGER = LogManager.getLogger(LOwOcalization.MOD_NAME + " Config");

    private static final File CONFIG_DIR = new File(MinecraftClient.getInstance().runDirectory, "config");
    private static final File FILE = new File(CONFIG_DIR, "lowocalization.lowo");

    public static final List<String> DEFAULT_LINES = Arrays.asList(
            "# The list of transformations to apply.",
            "# Refer to the wiki (https://github.com/eutropius225/lOwOcalization/wiki/Transformations) for what the options are",
            "# This file format looks pretty decent with Python highlighting, actually!",
            "'\"l\"->\"w\"'",
            "'\"L\"->\"W\"'",
            "'\"r\"->\"w\"'",
            "'\"R\"->\"W\"'",
            "'s/(\\w|^)s+(\\W|$)/$1th$2/g'",
            "'s/(\\w|^)S+(\\W|$)/$1TH$2/g'",
            "'s/([NM])([AO])/$1Y$2/g'",
            "'s/([nm])([ao])/$1y$2/ig'",
            "",
            "'''",
            "__asm__ /(\\s|^)(\\w)/g",
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
            "ARETURN",
            "'''"
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
        if(!FILE.exists()) FileUtils.writeLines(FILE, DEFAULT_LINES); // how
        try(Reader rd = new BufferedReader(new InputStreamReader(new FileInputStream(FILE)))) {
            LOwOcalizer.INSTANCE.configChange(LOwOReader.read(rd));
        } catch(LOwOReader.LOwOSyntaxException e) {
            LOGGER.error("Syntax error parsing config.", e);
        }
    }

}
