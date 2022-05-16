package eutro.l12n.api;


public class LOwOcalizationEvent {

    private final String original;
    private String current;
    private boolean cancelled;

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

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
