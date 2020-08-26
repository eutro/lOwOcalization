package eutros.lowocalization.core;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class LOwOMixinConnector implements IMixinConnector {

    @Override
    public void connect() {
        Mixins.addConfiguration("lowocalization.mixins.json");
    }

}
