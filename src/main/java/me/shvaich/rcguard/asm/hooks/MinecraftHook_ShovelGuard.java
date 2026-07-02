package me.shvaich.rcguard.asm.hooks;

import me.shvaich.rcguard.config.RCGuardConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;

public class MinecraftHook_ShovelGuard {
    public static boolean shouldCancelRightClick(Minecraft mc) {
        return RCGuardConfig.guardShovelEntityInteraction && doShovelGuard(mc.thePlayer);
    }

    public static boolean doShovelGuard(EntityPlayer thePlayer) {
        if (RCGuardConfig.isShovelGuarded) {
            final ItemStack itemStack = thePlayer.getHeldItem();
            return itemStack != null && itemStack.getItem() instanceof ItemSpade;
        }
        return false;
    }
}
