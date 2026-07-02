package me.shvaich.rcguard.asm.transformers;

public class InjectionHandler {
    private int count = -1;

    public void setInjectionPoints(int injectionPoints) {
        if (count != -1)
            throw new RuntimeException("Can only be called once!");
        this.count = injectionPoints;
    }

    public void addInjection() {
        this.count--;
    }

    public boolean isSuccessfulInjection() {
        return count == 0;
    }

    public boolean isFatalInjection() {
        return count < 0;
    }

    public int getCount() { return count; }
}