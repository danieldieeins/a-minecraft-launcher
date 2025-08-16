package net.nrfy.nexus.launcher.launcher;

import com.zyneonstudios.nexus.utilities.NexusUtilities;
import com.zyneonstudios.nexus.utilities.system.OperatingSystem;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import net.nrfy.nexus.launcher.installer.VanillaInstaller;
import net.nrfy.nexus.launcher.integrations.zyndex.ZyndexIntegration;
import net.nrfy.nexus.launcher.integrations.zyndex.instance.WritableInstance;

import java.nio.file.Path;

public class VanillaLauncher {

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
        ZyndexIntegration.update(instance);
        launch(instance.getMinecraftVersion(), instance.getSettings().getMemory(), Path.of(instance.getPath()),instance.getId(),authInfos);
    }

    public void launch(String version, int ram, Path instancePath, String id, AuthInfos authInfos) {
        if(preLaunchHook != null) {
            preLaunchHook.run();
        }

        if(ram<512) {
            ram = 512;
        }
        if(new VanillaInstaller().download(version,instancePath)) {
            NoFramework framework = new NoFramework(
                    instancePath,
                    authInfos,
                    GameFolder.FLOW_UPDATER
            );
            framework.getAdditionalVmArgs().add("-Xms512M");
            framework.getAdditionalVmArgs().add("-Xmx4096M");
            if(OperatingSystem.getType() == OperatingSystem.Type.macOS) {
                framework.getAdditionalVmArgs().add("-XstartOnFirstThread");
            }
            try {
                Process game = framework.launch(version, "", NoFramework.ModLoader.VANILLA);
                if(postLaunchHook != null) {
                    postLaunchHook.run();
                }
                game.onExit().thenRun(()->{
                    if(gameCloseHook != null) {
                        gameCloseHook.run();
                    }
                });
            } catch (Exception e) {
                NexusUtilities.getLogger().err("[LAUNCHER] Couldn't start Minecraft Vanilla " + version + " in " + instancePath + " with " + ram + "M RAM");
                throw new RuntimeException(e);
            }
        } else {
            NexusUtilities.getLogger().err("[LAUNCHER] Couldn't start Minecraft Vanilla " + version + " in " + instancePath + " with " + ram + "M RAM");
        }
    }
}