package me.shvaich.rcguard.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.*;

public class PlayerUtil {
    public static List<String> getTabPlayerNames(boolean ignoreSelf) {
        final Minecraft mc = Minecraft.getMinecraft();
        final NetHandlerPlayClient netHandler = mc.getNetHandler();
        if (netHandler == null) return Collections.emptyList();
        UUID thePlayerUUID = null;
        if (ignoreSelf && mc.thePlayer != null) {
            thePlayerUUID = mc.thePlayer.getUniqueID();
        }
        final boolean tryIgnoreSelf = thePlayerUUID != null;
        final Collection<NetworkPlayerInfo> playerInfoMap = netHandler.getPlayerInfoMap();
        final List<String> players = new ArrayList<>(playerInfoMap.size());
        for (final NetworkPlayerInfo netInfo : playerInfoMap) {
            final String playerName = netInfo.getGameProfile().getName();
            if (playerName != null && !(tryIgnoreSelf && thePlayerUUID.equals(netInfo.getGameProfile().getId()))) {
                players.add(playerName);
            }
        }
        return players;
    }

    /** Without 'self' in the returned list */
    public static List<String> getTabPlayerNames() { return getTabPlayerNames(true); }

    public static boolean isValidUsername(String name) {
        if (name == null) return false;
        final int len = name.length();
        if (len < 2 || len > 16) return false;
        for (int i = 0; i < len; i++) {
            final char ch = name.charAt(i);
            if (
                !(ch >= 'a' && ch <= 'z')
                && !(ch >= 'A' && ch <= 'Z')
                && !(ch >= '0' && ch <= '9')
                && ch != '_'
            ) return false;
        }
        return true;
    }
}
