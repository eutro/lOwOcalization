package eutros.lowocalization.core;

import eutros.lowocalization.core.common.LOwOLanguageMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ClientLanguageMap;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.text.LanguageMap;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class LOwOMapInjector implements ISelectiveResourceReloadListener {

    public static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public void onGUI(GuiScreenEvent.InitGuiEvent.Pre evt) {
        wrapMap();
        MinecraftForge.EVENT_BUS.unregister(this);
        ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(this);
    }

    private void wrapMap() {
        if(LanguageMap.getInstance() instanceof ClientLanguageMap) {
            ClientLanguageMap languageMap = (ClientLanguageMap) LanguageMap.getInstance();
            languageMap.field_239495_c_ = new LOwOLanguageMap(languageMap.field_239495_c_);
        } else {
            LOGGER.warn("Failed to set language map.");
        }
    }

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager, @Nonnull Predicate<IResourceType> resourcePredicate) {
        wrapMap();
    }

}
