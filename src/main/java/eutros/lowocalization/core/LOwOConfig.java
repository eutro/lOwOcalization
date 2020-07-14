package eutros.lowocalization.core;

import eutros.lowocalization.core.common.LOwOcalizer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LOwOConfig {

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        final Pair<Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = clientPair.getRight();
        CLIENT = clientPair.getLeft();
    }

    public static void onChange(ModConfig.ModConfigEvent evt) {
        if(evt.getConfig().getModId().equals(LOwOcalization.MOD_ID)) {
            LOwOcalizer.INSTANCE.configChange(CLIENT.regExes.get().stream().map(String.class::cast).collect(Collectors.toList()));
        }
    }

    public static class Client {

        public final ForgeConfigSpec.ConfigValue<Number> stutter;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> regExes;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.comment(String.format("Client-side configs for %s.", LOwOcalization.NAME));

            stutter = builder.comment("",
                    "How often should there be stuttering?")
                    .define("STUTTER", 0.3, Number.class::isInstance);

            regExes = builder.comment("",
                    "Add custom regular expressions.",
                    "If this is not empty, override default behaviour and use these instead.",
                    "Syntax is similar to the sed UNIX utility, but only for replacements.",
                    "The regex used to match this is as follows:",
                    LOwOcalizer.REGEX_PATTERN.toString(),
                    "For example, you may use \"s/Iron/Lead/g\" to replace all occurrences of \"Iron\" with \"Lead\"")
                    .defineList("REGULAR_EXPRESSIONS", Collections.emptyList(), String.class::isInstance);
        }

    }

}
