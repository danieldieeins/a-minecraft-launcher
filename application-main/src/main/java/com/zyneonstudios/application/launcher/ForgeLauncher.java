package com.zyneonstudios.application.launcher;

import com.zyneonstudios.ApplicationMain;
import com.zyneonstudios.application.Application;
import com.zyneonstudios.application.installer.ForgeInstaller;
import com.zyneonstudios.application.installer.java.OperatingSystem;
import com.zyneonstudios.application.integrations.zyndex.ZyndexIntegration;
import com.zyneonstudios.application.integrations.zyndex.instance.WritableInstance;
import com.zyneonstudios.application.utils.backend.MinecraftVersion;
import com.zyneonstudios.application.utils.frame.LogFrame;
import fr.flowarg.flowupdater.versions.ForgeVersionType;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;

import javax.swing.*;
import java.nio.file.Path;

public class ForgeLauncher {

    public void launch(WritableInstance instance) {
        WritableInstance updatedInstance = ZyndexIntegration.update(instance);
        if(updatedInstance!=null) {
            launch(updatedInstance.getMinecraftVersion(), updatedInstance.getForgeVersion(), ForgeVersionType.valueOf(updatedInstance.getForgeType().toUpperCase()), updatedInstance.getSettings().getMemory(), Path.of(updatedInstance.getPath()),updatedInstance.getId());
        } else {
            launch(instance.getMinecraftVersion(), instance.getForgeVersion(), ForgeVersionType.valueOf(instance.getForgeType().toUpperCase()), instance.getSettings().getMemory(), Path.of(instance.getPath()),instance.getId());
        }
        System.gc();
    }

    public void launch(String minecraftVersion, String forgeVersion, ForgeVersionType forgeType, int ram, Path instancePath, String id) {
        if(forgeType.equals(ForgeVersionType.NEO_FORGE)) {
            return;
        }
        MinecraftVersion.Type type = MinecraftVersion.getType(minecraftVersion);
        if(type!=null) {
            Launcher.setJava(type);
        }
        if(ram<512) {
            ram = 512;
        }
        if(forgeType.equals(ForgeVersionType.NEW)) {
            forgeVersion = forgeVersion.replace(minecraftVersion + "-", "");
        } else {
            if(!forgeVersion.startsWith(minecraftVersion)) {
                forgeVersion = minecraftVersion + "-"+forgeVersion;
            }
        }
        if(new ForgeInstaller().download(minecraftVersion,forgeVersion,forgeType,instancePath)) {
            NoFramework.ModLoader forge;
            if(forgeType==ForgeVersionType.OLD) {
                forge = NoFramework.ModLoader.OLD_FORGE;
            } else {
                forge = NoFramework.ModLoader.FORGE;
            }
            NoFramework framework = new NoFramework(
                    instancePath,
                    Application.auth.getAuthInfos(),
                    GameFolder.FLOW_UPDATER
            );
            if(minecraftVersion.equals("1.7.10")) {
                framework.setCustomModLoaderJsonFileName("1.7.10-Forge" + forgeVersion + ".json");
            }
            framework.getAdditionalVmArgs().add("-Xms512M");
            framework.getAdditionalVmArgs().add("-Xmx" + ram + "M");
            if(ApplicationMain.operatingSystem== OperatingSystem.macOS) {
                framework.getAdditionalVmArgs().add("-XstartOnFirstThread");
            }
            try {
                Process game = framework.launch(minecraftVersion, forgeVersion, forge);
                Application.getFrame().executeJavaScript("launchStarted();");
                if(!Application.running.contains(id)) {
                    Application.running.add(id);
                }
                Application.getFrame().setState(JFrame.ICONIFIED);
                LogFrame log;
                if(Application.logOutput) {
                    log = new LogFrame(game.getInputStream(),"Minecraft "+minecraftVersion+" (with "+forgeType.toString().toLowerCase()+"Forge "+forgeVersion+")");
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
                ApplicationMain.getLogger().err("[LAUNCHER] Couldn't start Forge "+forgeVersion+" ("+forgeType+") for Minecraft "+minecraftVersion+" in "+instancePath+" with "+ram+"M RAM");
                throw new RuntimeException(e);
            }
        } else {
            Application.getFrame().executeJavaScript("launchDefault();");
            Application.running.remove(id);
            ApplicationMain.getLogger().err("[LAUNCHER] Couldn't start Forge "+forgeVersion+" ("+forgeType+") for Minecraft "+minecraftVersion+" in "+instancePath+" with "+ram+"M RAM");
        }
    }
}