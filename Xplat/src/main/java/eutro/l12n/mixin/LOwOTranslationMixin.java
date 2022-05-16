package eutro.l12n.mixin;

import eutro.l12n.core.common.LOwOLanguageMap;
import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

@Mixin(ClientLanguage.class)
public class LOwOTranslationMixin {

    @ModifyVariable(
            method = "<init>",
            at = @At("HEAD"),
            argsOnly = true
    )
    private static Map<String, String> languageMapInit(Map<String, String> translations) {
        return new LOwOLanguageMap(translations);
    }

}
