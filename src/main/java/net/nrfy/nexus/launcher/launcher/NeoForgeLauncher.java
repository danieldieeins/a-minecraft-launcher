package net.nrfy.nexus.launcher.launcher;

import com.zyneonstudios.nexus.utilities.NexusUtilities;
import com.zyneonstudios.nexus.utilities.system.OperatingSystem;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import net.nrfy.nexus.launcher.installer.NeoForgeInstaller;
import net.nrfy.nexus.launcher.integrations.zyndex.ZyndexIntegration;
import net.nrfy.nexus.launcher.integrations.zyndex.instance.WritableInstance;

import java.nio.file.Path;

public class NeoForgeLauncher {

    private LauncherHook preLaunchHook;
    private LauncherHook postLaunchHook;
    private LauncherHook gameCloseHook;

    public void setGameCloseHook(LauncherHook gameCloseHook) {
        this.gameCloseHook = gameCloseHook;
    }

    public void setPostLaunchHook(LauncherHook postLaunchHook) {
        this.postLaunchHook = postLaunchHook;
    }

    public void setPreLaunchHook(LauncherHook preLaunchHook) {
        this.preLaunchHook = preLaunchHook;
    }

    public LauncherHook getGameCloseHook() {
        return gameCloseHook;
    }

    public LauncherHook getPostLaunchHook() {
        return postLaunchHook;
    }

    public LauncherHook getPreLaunchHook() {
        return preLaunchHook;
    }

    public void launch(WritableInstance instance, AuthInfos authInfos) {
        WritableInstance updatedInstance = ZyndexIntegration.update(instance);
        if(updatedInstance!=null) {
            launch(updatedInstance.getMinecraftVersion(), updatedInstance.getNeoForgeVersion(), updatedInstance.getSettings().getMemory(), Path.of(updatedInstance.getPath()),updatedInstance.getId(), authInfos);
        } else {
            launch(instance.getMinecraftVersion(), instance.getNeoForgeVersion(), instance.getSettings().getMemory(), Path.of(instance.getPath()),instance.getId(), authInfos);
        }
        System.gc();
    }

    public void launch(String minecraftVersion, String neoForgeVersion, int ram, Path instancePath, String id, AuthInfos authInfos) {
        if(preLaunchHook != null) {
            preLaunchHook.run();
        }

        if(ram<512) {
            ram = 512;
        }
        if(new NeoForgeInstaller().download(minecraftVersion,neoForgeVersion,instancePath)) {
            NoFramework framework = new NoFramework(
                    instancePath,
                    authInfos,
                    GameFolder.FLOW_UPDATER
            );
            framework.getAdditionalVmArgs().add("-Xms512M");
            framework.getAdditionalVmArgs().add("-Xmx" + ram + "M");
            if(OperatingSystem.getType() == OperatingSystem.Type.macOS) {
                framework.getAdditionalVmArgs().add("-XstartOnFirstThread");
            }
            try {
                Process game = framework.launch(minecraftVersion, neoForgeVersion, NoFramework.ModLoader.NEO_FORGE);
                if(postLaunchHook != null) {
                    postLaunchHook.run();
                }
                game.onExit().thenRun(()->{
                    if(gameCloseHook != null) {
                        gameCloseHook.run();
                    }
                });
            } catch (Exception e) {
                NexusUtilities.getLogger().err("[LAUNCHER] Couldn't start NeoForge "+neoForgeVersion+" for Minecraft "+minecraftVersion+" in "+instancePath+" with "+ram+"M RAM");
                throw new RuntimeException(e);
            }
        } else {
            NexusUtilities.getLogger().err("[LAUNCHER] Couldn't start NeoForge "+neoForgeVersion+" for Minecraft "+minecraftVersion+" in "+instancePath+" with "+ram+"M RAM");
        }
    }
}