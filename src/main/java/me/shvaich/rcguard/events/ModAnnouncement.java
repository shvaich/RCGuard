package me.shvaich.rcguard.events;

import me.shvaich.rcguard.utils.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ModAnnouncement {

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld != null && mc.thePlayer != null) {
            MinecraftForge.EVENT_BUS.unregister(this);
            ChatUtil.addChatMessage(ChatUtil.getModTag() + EnumChatFormatting.GRAY + "view /rcguard help");
        }
    }
}
