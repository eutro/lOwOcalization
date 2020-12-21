package eutros.lowocalization.core;

import com.google.common.collect.ImmutableList;
import eutros.lowocalization.core.common.LOwOcalizer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

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
            LOwOcalizer.INSTANCE.configChange(CLIENT.transformations.get().stream().map(String.class::cast).collect(Collectors.toList()));
        }
    }

    public static class Client {

        public final ForgeConfigSpec.ConfigValue<List<? extends String>> transformations;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.comment(String.format("Client-side configs for %s.", LOwOcalization.NAME));

            transformations = builder.comment(
                    "The list of transformations to apply.",
                    "Refer to the wiki (https://github.com/eutropius225/lOwOcalization/wiki/Transformations) for what the options are")
                    .defineList("TRANSFORMATIONS",
                            ImmutableList.of(
                                    "'l'->'w'",
                                    "'L'->'W'",
                                    "'r'->'w'",
                                    "'R'->'W'",
                                    "s/(\\w|^)s+(\\W|$)/$1th$2/g",
                                    "s/(\\w|^)S+(\\W|$)/$1TH$2/g",
                                    "s/([NM])([AO])/$1Y$2/g",
                                    "s/([nm])([ao])/$1y$2/ig",
                                    "__asm__ /(\\s|^)(\\w)/g " +
                                            "NEW \"java/lang/StringBuilder\" " +
                                            "DUP " +
                                            "ALOAD 1 " +
                                            "INVOKESPECIAL \"java/lang/StringBuilder\" \"<init>\" \"(Ljava/lang/String;)V\" " +

                                            "ALOAD 3 " +
                                            "INVOKEVIRTUAL \"java/util/Random\" \"nextDouble\" \"()D\" " +
                                            "LDC double 0.3 " +
                                            "DCMPL " +
                                            "IFGE POST_STUTTER " +

                                            "ALOAD 2 " +
                                            "INVOKEVIRTUAL \"java/lang/StringBuilder\" \"append\" \"(Ljava/lang/String;)Ljava/lang/StringBuilder;\" " +
                                            "LDC string \"-\" " +
                                            "INVOKEVIRTUAL \"java/lang/StringBuilder\" \"append\" \"(Ljava/lang/String;)Ljava/lang/StringBuilder;\" " +

                                            "LABEL POST_STUTTER " +

                                            "ALOAD 2 " +
                                            "INVOKEVIRTUAL \"java/lang/StringBuilder\" \"append\" \"(Ljava/lang/String;)Ljava/lang/StringBuilder;\" " +
                                            "INVOKEVIRTUAL \"java/lang/Object\" \"toString\" \"()Ljava/lang/String;\" " +
                                            "ARETURN"
                            ),
                            String.class::isInstance);
        }

    }

}
