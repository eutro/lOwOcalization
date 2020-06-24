package eutros.lowocalization.mixin;

import eutros.lowocalization.core.LOwOLanguageMap;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.util.Language;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.Map;

@Mixin(Language.class)
public class LOwOTranslationMixin {

    private static final Field FIELD = FieldUtils.getField(TranslationStorage.class, "translations", true);

    @Inject(at = @At("HEAD"), method = "setInstance")
    private static void languageSetInstance(Language language, CallbackInfo ci) {
        if(language instanceof TranslationStorage) {
            try {
                //noinspection unchecked
                FIELD.set(language, new LOwOLanguageMap((Map<String, String>) FIELD.get(language)));
            } catch(IllegalAccessException | ClassCastException e) {
                e.printStackTrace();
            }
        }
    }
}
