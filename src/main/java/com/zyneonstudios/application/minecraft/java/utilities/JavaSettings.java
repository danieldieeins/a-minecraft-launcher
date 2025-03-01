package com.zyneonstudios.application.minecraft.java.utilities;

import com.zyneonstudios.application.main.ApplicationStorage;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.JavaStorage;
import com.zyneonstudios.application.minecraft.java.installers.java.Architecture;
import com.zyneonstudios.application.minecraft.java.installers.java.Java;
import com.zyneonstudios.application.minecraft.java.installers.java.JavaInstaller;
import com.zyneonstudios.application.minecraft.java.installers.java.OperatingSystem;
import fr.theshark34.openlauncherlib.JavaUtil;

import java.io.File;
import java.util.ArrayList;

public class JavaSettings {

    public static void setJava(MinecraftVersion.Type type) {
        NexusApplication.getLogger().log("[LAUNCHER] Detected Minecraft version type "+type+"!");
        if(type.equals(MinecraftVersion.Type.LEGACY)) {
            JavaUtil.setJavaCommand(null);
            String java = ApplicationStorage.getApplicationPath() +"libraries/jre-8/";
            if(!new File(java).exists()) {
                NexusApplication.getLogger().err("[LAUNCHER] Couldn't find compatible Java Runtime Environment!");
                JavaInstaller javaInstaller = new JavaInstaller(Java.Runtime_8, (OperatingSystem)JavaStorage.map.get("system.os"), getArchitecture());
                javaInstaller.install();
                NexusApplication.getLogger().dbg("[LAUNCHER] Starting installation of missing java runtime "+javaInstaller.getVersionString()+"...");
            }
            System.setProperty("java.home", java);
        } else if(type.equals(MinecraftVersion.Type.SEMI_NEW)) {
            JavaUtil.setJavaCommand(null);
            String java = ApplicationStorage.getApplicationPath()+"libraries/jre-11/";
            if(!new File(java).exists()) {
                NexusApplication.getLogger().err("[LAUNCHER] Couldn't find compatible Java Runtime Environment!");
                JavaInstaller javaInstaller = new JavaInstaller(Java.Runtime_11,(OperatingSystem)JavaStorage.map.get("system.os"), getArchitecture());
                javaInstaller.install();
                NexusApplication.getLogger().dbg("[LAUNCHER] Starting installation of missing java runtime "+javaInstaller.getVersionString()+"...");
            }
            System.setProperty("java.home", java);
        } else if(type.equals(MinecraftVersion.Type.NEW)) {
            JavaUtil.setJavaCommand(null);
            String java = ApplicationStorage.getApplicationPath()+"libraries/jre/";
            if(!new File(java).exists()) {
                NexusApplication.getLogger().err("[LAUNCHER] Couldn't find compatible Java Runtime Environment!");
                JavaInstaller javaInstaller = new JavaInstaller(Java.Runtime_21,(OperatingSystem)JavaStorage.map.get("system.os"), getArchitecture());
                javaInstaller.install();
                NexusApplication.getLogger().dbg("[LAUNCHER] Starting installation of missing java runtime "+javaInstaller.getVersionString()+"...");
            }
            System.setProperty("java.home", java);
        }
    }

    private static Architecture getArchitecture() {
        String os = System.getProperty("os.arch");
        ArrayList<String> aarch = new ArrayList<>();
        aarch.add("ARM");
        aarch.add("ARM64");
        aarch.add("aarch64");
        aarch.add("armv6l");
        aarch.add("armv7l");
        for(String arch_os:aarch) {
            if(arch_os.equalsIgnoreCase(os)) {
                return Architecture.aarch64;
            }
        }
        return Architecture.x64;
    }
}
