package eutros.lowocalization.core;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = LOwOcalization.MOD_ID)
public class LOwOConfig {

    public static Configuration config;

    public static List<ConfigCategory> categories = new ArrayList<>();

    public static Property stutter;
    public static Property regExes;

    public static void init(File file) {
        config = new Configuration(file);

        refresh();

        Set<String> names = config.getCategoryNames();

        for(String name : names) {
            categories.add(config.getCategory(name));
        }
    }

    public static void refresh() {
        String categoryName;
        String propertyName;

        categoryName = "OwO";
        config.addCustomCategoryComment(categoryName, "Client-side configs for " + LOwOcalization.NAME + ".");

        propertyName = "STUTTER";
        stutter = config.get(categoryName, propertyName, 0.3, "How often should there be stuttering?");

        propertyName = "REGULAR_EXPRESSIONS";
        regExes = config.get(categoryName, propertyName, new String[0],
                String.join("\n",
                        "Add custom regular expressions.",
                        "If this is not empty, override default behaviour and use these instead.",
                        "Syntax is similar to the sed UNIX utility, but only for replacements.",
                        "The regex used to match this is as follows:",
                        LOwOcalizer.REGEX_PATTERN.toString(),
                        "For example, you may use \"s/Iron/Lead/g\" to replace all occurrences of \"Iron\" with \"Lead\""));

        config.save();
        LOwOcalizer.INSTANCE.configChange();
    }

    public static void registerConfig(FMLPreInitializationEvent event) {
        File dir = event.getModConfigurationDirectory();
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        init(new File(dir, LOwOcalization.MOD_ID + ".cfg"));
    }

    @SubscribeEvent
    public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
        if(event.getModID().equals(LOwOcalization.MOD_ID)) {
            refresh();
        }
    }

    public static class Gui extends GuiConfig {

        public Gui(GuiScreen parent) {
            super(parent, getConfigElements(), LOwOcalization.MOD_ID, false, false, LOwOcalization.NAME + " Configuration");
        }

        private static List<IConfigElement> getConfigElements() {
            List<IConfigElement> list = new ArrayList<>();

            for(ConfigCategory category : LOwOConfig.categories) {
                list.add(new ConfigElement(category));
            }

            return list;
        }

    }

}
