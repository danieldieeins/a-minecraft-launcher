package com.zyneonstudios.application.utils.backend;

import com.zyneonstudios.nexus.Main;
import com.zyneonstudios.verget.Verget;
import com.zyneonstudios.verget.minecraft.MinecraftVerget;

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
        return Type.NEW;
    }

    public static ForgeType getForgeType(String mcVersion) {
        if(mcVersion.contains(".")) {
            try {
                int i = Integer.parseInt(mcVersion.split("\\.")[1]);
                if (i < 12) {
                    return ForgeType.OLD;
                } else {
                    return ForgeType.NEW;
                }
            } catch (Exception e) {
                Main.logger.err("[SYSTEM] Couldn't resolve Minecraft version "+mcVersion+": "+e.getMessage());
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

    public enum Type {
        LEGACY,
        SEMI_NEW,
        NEW
    }

    public enum ForgeType {
        OLD,
        NEW
    }
}
