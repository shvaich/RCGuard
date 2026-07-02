package me.shvaich.rcguard.events;

import me.shvaich.rcguard.RCGuard;
import me.shvaich.rcguard.config.RCGuardConfig;
import me.shvaich.rcguard.utils.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;

public class KeybindingListener {

    private static final KeyBinding blockGuardKeyBinding = create("Toggle " + RCGuardConfig.BLOCK_GUARD);
    private static final KeyBinding shovelGuardKeyBinding = create("Toggle " + RCGuardConfig.SHOVEL_GUARD);

    public KeybindingListener() {
        Arrays.asList(
                blockGuardKeyBinding,
                shovelGuardKeyBinding
        ).forEach(ClientRegistry::registerKeyBinding);
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent e) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.theWorld == null || mc.thePlayer == null) return;

        final boolean value;
        final String fieldName;
        final String guard;

        if (blockGuardKeyBinding.isPressed()) {
            value = RCGuardConfig.areBlocksGuarded = !RCGuardConfig.areBlocksGuarded;
            fieldName = "areBlocksGuarded";
            guard = RCGuardConfig.BLOCK_GUARD;
        }
        else if (shovelGuardKeyBinding.isPressed()) {
            value = RCGuardConfig.isShovelGuarded = !RCGuardConfig.isShovelGuarded;
            fieldName = "isShovelGuarded";
            guard = RCGuardConfig.SHOVEL_GUARD;
        }
        else return;

        ChatUtil.addChatMessage(ChatUtil.getModTag() + guard + ": " + ChatUtil.getBooleanMsg(value));
        RCGuardConfig.instance().saveOnlyOneProperty(fieldName);
    }


    private static KeyBinding create(String desc, int defaultKeyCode, String category) {
        return new KeyBinding(desc, defaultKeyCode, category);
    }

    private static KeyBinding create(String desc, int defaultKeyCode) {
        return create(desc, defaultKeyCode, RCGuard.NAME);
    }

    private static KeyBinding create(String desc) {
        return create(desc, Keyboard.KEY_NONE, RCGuard.NAME);
    }
}
