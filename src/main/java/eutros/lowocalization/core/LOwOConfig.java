package eutros.lowocalization.core;

import net.minecraft.client.MinecraftClient;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LOwOConfig {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final File CONFIG_DIR = new File(MinecraftClient.getInstance().runDirectory, "config");
    private static final File FILE = new File(CONFIG_DIR, "lowocalization.txt");

    public static final List<String> regExes = new ArrayList<>();

    static void init() {
        new Thread(() -> {
            try(WatchService watcher = FileSystems.getDefault().newWatchService()) {
                if(CONFIG_DIR.mkdirs() || FILE.createNewFile()) {
                    FileUtils.writeLines(FILE, Arrays.asList(
                            "# Add custom regular expressions.",
                            "# If this is not empty, override default behaviour and use these instead.",
                            "# Syntax is similar to the sed UNIX utility, but only for replacements.",
                            "# The regex used to match this is as follows:",
                            "# " + LOwOcalizer.REGEX_PATTERN.toString(),
                            "# For example, you may use \"s/Iron/Lead/g\" to replace all occurrences of \"Iron\" with \"Lead\""
                    ));
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
        List<String> lines = FileUtils.readLines(FILE, "UTF8");
        regExes.clear();
        for(String line : lines) {
            if(line.startsWith("#")) continue;
            regExes.add(line.trim());
        }
        LOGGER.info("Loading. Found custom expressions:");
        regExes.forEach(LOGGER::info);
        LOwOcalizer.INSTANCE.configChange();
        LOGGER.info("Finished loading.");
    }

}