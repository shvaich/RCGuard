package me.shvaich.rcguard.utils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class DelayedTask {

    private final Runnable fn;
    private int ticks;

    public DelayedTask(Runnable fn, int ticks) {
        if (ticks < 0) throw new IllegalArgumentException("Ticks must be a positive integer or 0");
        this.fn = fn;
        this.ticks = ticks;
        MinecraftForge.EVENT_BUS.register(this);
    }

    public DelayedTask(Runnable fn) {
        this(fn, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START) return;
        if (ticks < 1) {
            MinecraftForge.EVENT_BUS.unregister(this);
            if (ticks == 0) fn.run();
        }
        ticks--;
    }

    public void stop() {
        ticks = -1;
        MinecraftForge.EVENT_BUS.unregister(this);
    }

//    public boolean hasStopped() { return ticks < 0; }
}
