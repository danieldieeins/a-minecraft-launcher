package com.zyneonstudios.application.launcher;

import com.zyneonstudios.ApplicationMain;
import com.zyneonstudios.application.Application;
import com.zyneonstudios.application.installer.NeoForgeInstaller;
import com.zyneonstudios.application.installer.java.OperatingSystem;
import com.zyneonstudios.application.integrations.zyndex.ZyndexIntegration;
import com.zyneonstudios.application.integrations.zyndex.instance.WritableInstance;
import com.zyneonstudios.application.utils.backend.MinecraftVersion;
import com.zyneonstudios.application.utils.frame.LogFrame;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;

import javax.swing.*;
import java.nio.file.Path;

public class NeoForgeLauncher {

    public void launch(WritableInstance instance) {
        WritableInstance updatedInstance = ZyndexIntegration.update(instance);
        if(updatedInstance!=null) {
            launch(updatedInstance.getMinecraftVersion(), updatedInstance.getNeoForgeVersion(), updatedInstance.getSettings().getMemory(), Path.of(updatedInstance.getPath()),updatedInstance.getId());
        } else {
            launch(instance.getMinecraftVersion(), instance.getNeoForgeVersion(), instance.getSettings().getMemory(), Path.of(instance.getPath()),instance.getId());
        }
        System.gc();
    }

    public void launch(String minecraftVersion, String neoForgeVersion, int ram, Path instancePath, String id) {
        MinecraftVersion.Type type = MinecraftVersion.getType(minecraftVersion);
        if(type!=null) {
            Launcher.setJava(type);
        }
        if(ram<512) {
            ram = 512;
        }
        if(new NeoForgeInstaller().download(minecraftVersion,neoForgeVersion,instancePath)) {
            NoFramework framework = new NoFramework(
                    instancePath,
                    Application.auth.getAuthInfos(),
                    GameFolder.FLOW_UPDATER
            );
            framework.getAdditionalVmArgs().add("-Xms512M");
            framework.getAdditionalVmArgs().add("-Xmx" + ram + "M");
            if(ApplicationMain.operatingSystem== OperatingSystem.macOS) {
                framework.getAdditionalVmArgs().add("-XstartOnFirstThread");
            }
            try {
                Process game = framework.launch(minecraftVersion, neoForgeVersion, NoFramework.ModLoader.NEO_FORGE);
                Application.getFrame().executeJavaScript("launchStarted();");
                if(!Application.running.contains(id)) {
                    Application.running.add(id);
                }
                Application.getFrame().setState(JFrame.ICONIFIED);
                LogFrame log;
                if(Application.logOutput) {
                    log = new LogFrame(game.getInputStream(),"Minecraft "+minecraftVersion+" (with NeoForge "+neoForgeVersion+")");
                } else {
                    log = null;
                }
                game.onExit().thenRun(()->{
                    if(log!=null) {
                        log.onStop();
                    }
                    Application.getFrame().setState(JFrame.NORMAL);
                    Application.getFrame().executeJavaScript("launchDefault();");
                    Application.running.remove(id);
                });
            } catch (Exception e) {
                Application.getFrame().executeJavaScript("launchDefault();");
                Application.running.remove(id);
                if(!Application.auth.isLoggedIn()) {
                    Application.getFrame().getBrowser().loadURL(Application.getSettingsURL()+"?tab=profile");
                }
                ApplicationMain.getLogger().err("[LAUNCHER] Couldn't start NeoForge "+neoForgeVersion+" for Minecraft "+minecraftVersion+" in "+instancePath+" with "+ram+"M RAM");
                throw new RuntimeException(e);
            }
        } else {
            Application.getFrame().executeJavaScript("launchDefault();");
            Application.running.remove(id);
            ApplicationMain.getLogger().err("[LAUNCHER] Couldn't start NeoForge "+neoForgeVersion+" for Minecraft "+minecraftVersion+" in "+instancePath+" with "+ram+"M RAM");
        }
    }
}