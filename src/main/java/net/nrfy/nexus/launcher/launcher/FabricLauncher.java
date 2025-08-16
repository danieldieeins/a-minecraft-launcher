package net.nrfy.nexus.launcher.launcher;

import com.zyneonstudios.nexus.utilities.NexusUtilities;
import com.zyneonstudios.nexus.utilities.system.OperatingSystem;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import net.nrfy.nexus.launcher.installer.FabricInstaller;
import net.nrfy.nexus.launcher.integrations.zyndex.ZyndexIntegration;
import net.nrfy.nexus.launcher.integrations.zyndex.instance.WritableInstance;

import java.nio.file.Path;

public class FabricLauncher extends MinecraftLauncher {

    private Process gameProcess;
    private NoFramework framework;
    private boolean launched = false;

    private WritableInstance instance = null;

    public void launch(WritableInstance instance, AuthInfos authInfos) {
        this.instance = instance;
        WritableInstance updatedInstance = ZyndexIntegration.update(instance);
        if(updatedInstance!=null) {
            launch(updatedInstance.getMinecraftVersion(), updatedInstance.getFabricVersion(), updatedInstance.getSettings().getMemory(), Path.of(updatedInstance.getPath()),updatedInstance.getId(), authInfos);
        } else {
            launch(instance.getMinecraftVersion(), instance.getFabricVersion(), instance.getSettings().getMemory(), Path.of(instance.getPath()),instance.getId(), authInfos);
        }
        System.gc();
    }

    public void launch(String minecraftVersion, String fabricVersion, int ram, Path instancePath, String id, AuthInfos authInfos) {
        if(!launched) {
            launched = true;
            if (getPreLaunchHook() != null) {
                getPreLaunchHook().run();
            }

            if (ram < 512) {
                ram = 512;
            }
            if (new FabricInstaller().download(minecraftVersion, fabricVersion, instancePath)) {
                framework = new NoFramework(
                        instancePath,
                        authInfos,
                        GameFolder.FLOW_UPDATER
                );
                framework.getAdditionalVmArgs().add("-Xms512M");
                framework.getAdditionalVmArgs().add("-Xmx" + ram + "M");
                if (OperatingSystem.getType() == OperatingSystem.Type.macOS) {
                    framework.getAdditionalVmArgs().add("-XstartOnFirstThread");
                }
                try {
                    gameProcess = framework.launch(minecraftVersion, fabricVersion, NoFramework.ModLoader.FABRIC);
                    if (getPostLaunchHook() != null) {
                        getPostLaunchHook().run();
                    }
                    gameProcess.onExit().thenRun(() -> {
                        if (getGameCloseHook() != null) {
                            getGameCloseHook().run();
                        }
                    });
                } catch (Exception e) {
                    NexusUtilities.getLogger().err("[LAUNCHER] Couldn't start Fabric " + fabricVersion + " for Minecraft " + minecraftVersion + " in " + instancePath + " with " + ram + "M RAM.");
                    throw new RuntimeException(e);
                }
            } else {
                NexusUtilities.getLogger().err("[LAUNCHER] Couldn't start Fabric " + fabricVersion + " for Minecraft " + minecraftVersion + " in " + instancePath + " with " + ram + "M RAM.");
            }
        }
    }

    @Override
    public Process getGameProcess() {
        return gameProcess;
    }

    @Override
    public NoFramework getFramework() {
        return framework;
    }

    @Override
    public boolean isLaunched() {
        return launched;
    }

    @Override
    public WritableInstance getInstance() {
        return instance;
    }
}