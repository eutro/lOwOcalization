package eutros.lowocalization.mixin;

import eutros.lowocalization.core.common.LOwOLanguageMap;
import net.minecraft.client.resource.language.TranslationStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

@Mixin(TranslationStorage.class)
public class LOwOTranslationMixin {

    @ModifyVariable(method = "<init>",
                    at = @At("HEAD"))
    private static Map<String, String> translationStorageInit(Map<String, String> translations) {
        return new LOwOLanguageMap(translations);
    }

}
