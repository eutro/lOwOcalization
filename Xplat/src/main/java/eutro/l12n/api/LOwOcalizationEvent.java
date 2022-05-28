package eutro.l12n.api;


import org.jetbrains.annotations.NotNull;

public class LOwOcalizationEvent {
    @NotNull
    private final String original;
    @NotNull
    private String current;
    private boolean cancelled;

    public LOwOcalizationEvent(@NotNull String localized) {
        this.original = localized;
        this.current = localized;
    }

    @NotNull
    public String getOriginal() {
        return original;
    }

    @NotNull
    public String getCurrent() {
        return current;
    }

    public void setCurrent(@NotNull String current) {
        this.current = current;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
