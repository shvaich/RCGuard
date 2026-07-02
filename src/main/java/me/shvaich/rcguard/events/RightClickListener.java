package me.shvaich.rcguard.events;

import me.shvaich.rcguard.asm.hooks.MinecraftHook_ShovelGuard;
import me.shvaich.rcguard.config.RCGuardConfig;
import me.shvaich.rcguard.features.GuardableBlock;
import net.minecraft.block.Block;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RightClickListener {
    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent e) {
        // shit code, deal with it
        if (e.action == Action.RIGHT_CLICK_BLOCK) {
            boolean hasSearchedClickedBlock = false;
            GuardableBlock guardableBlock = null; // in the original ideal way guardedBlock was: Set<GuardableBlock>

            if (RCGuardConfig.areBlocksGuarded && !RCGuardConfig.guardedBlocks.isEmpty()) {
                final ItemStack heldItemStack = e.entityPlayer.getHeldItem();
                if (RCGuardConfig.canPickaxeOverrideBlockGuard && heldItemStack != null && heldItemStack.getItem() instanceof ItemPickaxe)
                    return;

                if (!e.entityPlayer.isSneaking() || (RCGuardConfig.guardSneakAndEmptyHandClicks && heldItemStack == null)) {
                    final Block clickedBlock = e.world.getBlockState(e.pos).getBlock();
                    guardableBlock = GuardableBlock.fromBlock(clickedBlock);
                    if (guardableBlock != null && RCGuardConfig.guardedBlocks.contains(guardableBlock.key)) {
                        e.setCanceled(true);
                        return;
                    }
                    hasSearchedClickedBlock = true;
                }
            }

            if (MinecraftHook_ShovelGuard.doShovelGuard(e.entityPlayer)) {
                if (RCGuardConfig.canOverrideShovelGuard || e.entityPlayer.isSneaking()) {
                    if (!hasSearchedClickedBlock) {
                        final Block clickedBlock = e.world.getBlockState(e.pos).getBlock();
                        guardableBlock = GuardableBlock.fromBlock(clickedBlock);
                    }
                    if (guardableBlock != null) return;
                }
                e.setCanceled(true);
            }
        }
        else if (e.action == Action.RIGHT_CLICK_AIR && MinecraftHook_ShovelGuard.doShovelGuard(e.entityPlayer)) {
            e.setCanceled(true);
        }
    }
}