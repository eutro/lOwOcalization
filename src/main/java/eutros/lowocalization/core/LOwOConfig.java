package eutros.lowocalization.core;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class LOwOConfig {

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;


    static {
        final Pair<Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = clientPair.getRight();
        CLIENT = clientPair.getLeft();
    }

    public static class Client {

        public final ForgeConfigSpec.ConfigValue<Number> stutter;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.comment(LOwOcalizationHooks.onLocalization("Client-side configs for ") + LOwOcalization.NAME + ".");
            builder.comment();
            builder.comment(LOwOcalizationHooks.onLocalization("How often should there be stuttering?"));
            stutter = builder.define("STUTTER", 0.3F);
        }

    }

}
