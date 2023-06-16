package eutro.l12n.core.fabric;

import eutro.l12n.core.common.LOwODefaultTransformations;
import eutro.l12n.core.common.LOwOcalizer;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LOwOConfig {

    private static final Logger LOGGER = LogManager.getLogger(LOwOcalization.MOD_NAME + " Config");

    private static final File CONFIG_DIR = new File(Minecraft.getInstance().gameDirectory, "config");
    private static final File FILE = new File(CONFIG_DIR, "lowocalization.lowo");

    public static final List<String> DEFAULT_LINES = Stream.concat(
            Stream.of(
                    "# The list of transformations to apply.",
                    "# Refer to the wiki (https://github.com/eutro/lOwOcalization/wiki/Transformations) for what the options are",
                    "# This file format looks pretty decent with Python highlighting, actually!"
            ),
            LOwODefaultTransformations.DEFAULT_CONFIG.stream()
                    .map(s -> '\'' + s + '\'')
    ).collect(Collectors.toList());

    static void init() {
        new Thread(() -> {
            try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
                if (CONFIG_DIR.mkdirs() || FILE.createNewFile()) {
                    FileUtils.writeLines(FILE, DEFAULT_LINES);
                }
                onChange();

                Path path = CONFIG_DIR.toPath();
                path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
                while (true) {
                    WatchKey key;
                    try {
                        key = watcher.take();
                    } catch (InterruptedException e) {
                        return;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();

                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY
                                && filename.toString().equals(FILE.getName())) {
                            onChange();
                        }
                        boolean valid = key.reset();
                        if (!valid) {
                            break;
                        }
                    }
                }
            } catch (Throwable e) {
                LOGGER.error(e);
            }
        }, String.format("%s Config Watcher", LOwOcalization.MOD_NAME)).start();
    }

    private static void onChange() throws IOException {
        if (!FILE.exists()) FileUtils.writeLines(FILE, DEFAULT_LINES); // how
        LOGGER.info("Reloading config changes...");
        try (Reader rd = new BufferedReader(new InputStreamReader(new FileInputStream(FILE)))) {
            LOwOcalizer.INSTANCE.configChange(LOwOReader.read(rd));
        } catch (LOwOReader.LOwOSyntaxException e) {
            LOGGER.error("Syntax error parsing config.", e);
        }
        LOGGER.info("Reloaded config changes.");
    }

}
