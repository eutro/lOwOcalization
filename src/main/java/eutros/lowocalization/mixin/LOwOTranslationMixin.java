package eutros.lowocalization.mixin;

import eutros.lowocalization.core.LOwOLanguageMap;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.util.Language;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.Map;

@Mixin(Language.class)
public class LOwOTranslationMixin {

    private static final MappingResolver mr = FabricLoader.getInstance().getMappingResolver();

    @Nullable
    private static final Field FIELD = FieldUtils.getField(TranslationStorage.class,
            mr.mapFieldName("intermediary",
                    "net.minecraft.class_1078",
                    "field_5330",
                    "Ljava/util/Map;"),
            true);

    private static final Logger LOGGER = LogManager.getLogger();

    @Inject(at = @At("HEAD"), method = "setInstance")
    private static void languageSetInstance(Language language, CallbackInfo ci) {
        if(FIELD == null) {
            LOGGER.warn("Could not set language map, field not found.");
            return;
        }
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
