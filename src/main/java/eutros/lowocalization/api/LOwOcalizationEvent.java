package eutros.lowocalization.api;

import net.minecraftforge.fml.common.eventhandler.Event;

public class LOwOcalizationEvent extends Event {

    private final String original;
    private String current;

    public LOwOcalizationEvent(String localized) {
        this.original = localized;
        this.current = localized;
    }

    public String getOriginal() {
        return original;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

}
