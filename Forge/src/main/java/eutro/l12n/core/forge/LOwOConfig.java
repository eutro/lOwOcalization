package eutro.l12n.core.forge;

import com.google.common.collect.ImmutableList;
import eutro.l12n.core.common.LOwODefaultTransformations;
import eutro.l12n.core.common.LOwOcalizer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
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

    public static void onChange(ModConfigEvent evt) {
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
                    "Refer to the wiki (https://github.com/eutro/lOwOcalization/wiki/Transformations) for what the options are")
                    .defineList("TRANSFORMATIONS", LOwODefaultTransformations.DEFAULT_CONFIG, String.class::isInstance);
        }

    }

}
