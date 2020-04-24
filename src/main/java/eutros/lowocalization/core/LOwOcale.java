package eutros.lowocalization.core;

import net.minecraft.client.resources.Locale;
import net.minecraft.resources.IResourceManager;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class LOwOcale extends Locale {

    private Locale backer;

    public LOwOcale(Locale backer) {
        this.backer = backer;
        properties = backer.properties;
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public String formatMessage(String translateKey, Object[] parameters) {
        return LOwOcalizationHooks.onLocalization(backer.formatMessage(translateKey, parameters));
    }

    @ParametersAreNonnullByDefault
    @Override
    public synchronized void func_195811_a(IResourceManager p_195811_1_, List<String> p_195811_2_) {
        backer.func_195811_a(p_195811_1_, p_195811_2_);
    }

    @Override
    public boolean hasKey(@Nonnull String key) {
        return backer.hasKey(key);
    }

}
