package com.zyneonstudios.application.launcher;

import com.zyneonstudios.ApplicationMain;
import com.zyneonstudios.application.installer.java.Java;
import com.zyneonstudios.application.installer.java.JavaInstaller;
import com.zyneonstudios.application.utils.backend.MinecraftVersion;
import fr.theshark34.openlauncherlib.JavaUtil;

import java.io.File;

public class Launcher {

    public static void setJava(MinecraftVersion.Type type) {
        ApplicationMain.getLogger().log("[LAUNCHER] Detected Minecraft version type "+type+"!");
        if(type.equals(MinecraftVersion.Type.LEGACY)) {
            JavaUtil.setJavaCommand(null);
            String java = ApplicationMain.getDirectoryPath()+"libs/jre-8/";
            if(!new File(java).exists()) {
                ApplicationMain.getLogger().err("[LAUNCHER] Couldn't find compatible Java Runtime Environment!");
                JavaInstaller javaInstaller = new JavaInstaller(Java.Runtime_8, ApplicationMain.operatingSystem, ApplicationMain.architecture);
                javaInstaller.install();
                ApplicationMain.getLogger().dbg("[LAUNCHER] Starting installation of missing java runtime "+javaInstaller.getVersionString()+"...");
            }
            System.setProperty("java.home", java);
        } else if(type.equals(MinecraftVersion.Type.SEMI_NEW)) {
            JavaUtil.setJavaCommand(null);
            String java = ApplicationMain.getDirectoryPath()+"libs/jre-11/";
            if(!new File(java).exists()) {
                ApplicationMain.getLogger().err("[LAUNCHER] Couldn't find compatible Java Runtime Environment!");
                JavaInstaller javaInstaller = new JavaInstaller(Java.Runtime_11, ApplicationMain.operatingSystem, ApplicationMain.architecture);
                javaInstaller.install();
                ApplicationMain.getLogger().deb("[LAUNCHER] Starting installation of missing java runtime "+javaInstaller.getVersionString()+"...");
            }
            System.setProperty("java.home", java);
        } else if(type.equals(MinecraftVersion.Type.NEW)) {
            JavaUtil.setJavaCommand(null);
            String java = ApplicationMain.getDirectoryPath()+"libs/jre/";
            if(!new File(java).exists()) {
                ApplicationMain.getLogger().err("[LAUNCHER] Couldn't find compatible Java Runtime Environment!");
                JavaInstaller javaInstaller = new JavaInstaller(Java.Runtime_21, ApplicationMain.operatingSystem, ApplicationMain.architecture);
                javaInstaller.install();
                ApplicationMain.getLogger().deb("[LAUNCHER] Starting installation of missing java runtime "+javaInstaller.getVersionString()+"...");
            }
            System.setProperty("java.home", java);
        }
    }
}
