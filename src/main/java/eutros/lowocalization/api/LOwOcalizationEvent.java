package eutros.lowocalization.api;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
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
