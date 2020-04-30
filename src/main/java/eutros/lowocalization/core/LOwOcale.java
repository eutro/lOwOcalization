package eutros.lowocalization.core;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.Locale;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class LOwOcale extends Locale {

    private Locale backer;

    public LOwOcale(Locale backer) {
        this.backer = backer;
        this.properties = backer.properties;
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public String formatMessage(String translateKey, Object[] parameters) {
        return LOwOcalizationHooks.onLocalization(backer.formatMessage(translateKey, parameters));
    }

    @Override
    public synchronized void loadLocaleDataFiles(IResourceManager resourceManager, List<String> languageList) {
        backer.loadLocaleDataFiles(resourceManager, languageList);
    }

    @Override
    public boolean hasKey(@Nonnull String key) {
        return backer.hasKey(key);
    }

}
