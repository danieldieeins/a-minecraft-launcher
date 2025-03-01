package com.zyneonstudios.application.minecraft.java.utilities;

import com.zyneonstudios.nexus.Main;
import com.zyneonstudios.verget.Verget;
import com.zyneonstudios.verget.minecraft.MinecraftVerget;
import fr.flowarg.openlauncherlib.NoFramework;

import java.util.ArrayList;

public class MinecraftVersion {

    public static ArrayList<String> supportedVersions = new ArrayList<>();

    public static void syncVersions() {
        supportedVersions = Verget.getMinecraftVersions(MinecraftVerget.Filter.BOTH);
    }

    public static Type getType(String version) {
        if(version.contains(".")) {
            try {
                int i = Integer.parseInt(version.split("\\.")[1]);
                if (i < 13) {
                    return Type.LEGACY;
                } else if (i < 18) {
                    return Type.SEMI_NEW;
                } else {
                    return Type.NEW;
                }
            } catch (Exception e) {
                Main.logger.err("[SYSTEM] Couldn't resolve Minecraft version "+version+": "+e.getMessage());
            }
        }
        return null;
    }

    public static boolean isMinecraftVersion(String version) {
        try {
            return getType(version) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static NoFramework.ModLoader getForgeType(String minecraftVersion) {
        int i = Integer.parseInt(minecraftVersion.split("\\.")[1]);
        if(i<12) {
            return NoFramework.ModLoader.OLD_FORGE;
        } else {
            return NoFramework.ModLoader.FORGE;
        }
    }

    public enum Type {
        LEGACY,
        SEMI_NEW,
        NEW
    }
}
