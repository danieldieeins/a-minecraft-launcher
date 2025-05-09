package com.zyneonstudios.application.utils.backend;

import com.zyneonstudios.nexus.Main;
import com.zyneonstudios.verget.Verget;
import com.zyneonstudios.verget.minecraft.MinecraftVerget;
import fr.flowarg.flowupdater.versions.ForgeVersionType;

import java.util.ArrayList;

public class MinecraftVersion {

    public static ArrayList<String> supportedVersions = new ArrayList<>();

    public static void syncVersions() {
        supportedVersions = Verget.getMinecraftVersions(MinecraftVerget.Filter.BOTH);
        /*supportedVersions = new ArrayList<>();
        supportedVersions.add("1.20.4 (Latest)");
        supportedVersions.add("1.20.3");
        supportedVersions.add("1.20.2");
        supportedVersions.add("1.20.1");
        supportedVersions.add("1.20");

        supportedVersions.add("1.19.4");
        supportedVersions.add("1.19.3");
        supportedVersions.add("1.19.2");
        supportedVersions.add("1.19.1");
        supportedVersions.add("1.19");

        supportedVersions.add("1.18.2");
        supportedVersions.add("1.18.1");
        supportedVersions.add("1.18");

        supportedVersions.add("1.17.1");
        supportedVersions.add("1.17");

        supportedVersions.add("1.16.5");
        supportedVersions.add("1.16.4");
        supportedVersions.add("1.16.3");
        supportedVersions.add("1.16.2");
        supportedVersions.add("1.16.1");
        supportedVersions.add("1.16");

        supportedVersions.add("1.15.2");
        supportedVersions.add("1.15.1");
        supportedVersions.add("1.15");

        supportedVersions.add("1.14.4");
        supportedVersions.add("1.14.3");
        supportedVersions.add("1.14.2");
        supportedVersions.add("1.14.1");
        supportedVersions.add("1.14");

        supportedVersions.add("1.13.2");
        supportedVersions.add("1.13.1");
        supportedVersions.add("1.13");

        supportedVersions.add("1.12.2");
        supportedVersions.add("1.12.1");
        supportedVersions.add("1.12");

        supportedVersions.add("1.11.2");
        supportedVersions.add("1.11.1");
        supportedVersions.add("1.11");

        supportedVersions.add("1.10.2");
        supportedVersions.add("1.10.1");
        supportedVersions.add("1.10");

        supportedVersions.add("1.9.4");
        supportedVersions.add("1.9.3");
        supportedVersions.add("1.9.2");
        supportedVersions.add("1.9.1");
        supportedVersions.add("1.9");

        supportedVersions.add("1.8.9");
        supportedVersions.add("1.8.8");
        supportedVersions.add("1.8.7");
        supportedVersions.add("1.8.6");
        supportedVersions.add("1.8.5");
        supportedVersions.add("1.8.4");
        supportedVersions.add("1.8.3");
        supportedVersions.add("1.8.2");
        supportedVersions.add("1.8.1");
        supportedVersions.add("1.8");

        supportedVersions.add("1.7.10");
        supportedVersions.add("1.7.9");
        supportedVersions.add("1.7.8");
        supportedVersions.add("1.7.7");
        supportedVersions.add("1.7.6");
        supportedVersions.add("1.7.5");
        supportedVersions.add("1.7.4");
        supportedVersions.add("1.7.3");

        //UNSUPPORTED VERSIONS 1.6 - 1.7.2

        supportedVersions.add("1.5.2");
        supportedVersions.add("1.5.1");
        supportedVersions.add("1.5");

        supportedVersions.add("1.4.7");
        supportedVersions.add("1.4.6");
        supportedVersions.add("1.4.5");
        supportedVersions.add("1.4.4");
        supportedVersions.add("1.4.1");
        supportedVersions.add("1.4");

        supportedVersions.add("1.3.2");
        supportedVersions.add("1.3.1");

        supportedVersions.add("1.2.5");
        supportedVersions.add("1.2.4");
        supportedVersions.add("1.2.3");
        supportedVersions.add("1.2.1");*/
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

    public static ForgeVersionType getForgeType(String minecraftVersion) {
        int i = Integer.parseInt(minecraftVersion.split("\\.")[1]);
        if(i<12) {
            return ForgeVersionType.OLD;
        } else {
            return ForgeVersionType.NEW;
        }
    }

    public enum Type {
        LEGACY,
        SEMI_NEW,
        NEW
    }
}
