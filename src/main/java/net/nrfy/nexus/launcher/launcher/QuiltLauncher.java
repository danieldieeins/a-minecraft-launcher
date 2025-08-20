package net.nrfy.nexus.launcher.launcher;

import com.zyneonstudios.nexus.utilities.NexusUtilities;
import com.zyneonstudios.nexus.utilities.system.OperatingSystem;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import net.nrfy.nexus.launcher.installer.QuiltInstaller;
import net.nrfy.nexus.launcher.integrations.zyndex.ZZyndexIntegration;
import net.nrfy.nexus.launcher.integrations.zyndex.instance.WritableZInstance;

import java.nio.file.Path;

public class QuiltLauncher extends MinecraftLauncher {

    private Process gameProcess;
    private NoFramework framework;
    private boolean launched = false;

    private WritableZInstance instance = null;

    private AuthInfos authInfos;
    public QuiltLauncher(AuthInfos authInfos) {
        this.authInfos = authInfos;
    }

    public void setAuthInfos(AuthInfos authInfos) {
        this.authInfos = authInfos;
    }

    public void launch(WritableZInstance instance) {
        this.instance = instance;
        WritableZInstance updatedInstance = ZZyndexIntegration.update(instance);
        if(updatedInstance!=null) {
            launch(updatedInstance.getMinecraftVersion(), updatedInstance.getQuiltVersion(), updatedInstance.getSettings().getMemory(), Path.of(updatedInstance.getPath()),updatedInstance.getId());
        } else {
            launch(instance.getMinecraftVersion(), instance.getQuiltVersion(), instance.getSettings().getMemory(), Path.of(instance.getPath()),instance.getId());
        }
        System.gc();
    }

    public void launch(String minecraftVersion, String quiltVersion, int ram, Path instancePath, String id) {
        if(!launched) {
            launched = true;

            if (getPreLaunchHook() != null) {
                getPreLaunchHook().run();
            }

            if (ram < 512) {
                ram = 512;
            }
            if (new QuiltInstaller().download(minecraftVersion, quiltVersion, instancePath)) {
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
                    gameProcess = framework.launch(minecraftVersion, quiltVersion, NoFramework.ModLoader.QUILT);
                    if (getPostLaunchHook() != null) {
                        getPostLaunchHook().run();
                    }
                    gameProcess.onExit().thenRun(() -> {
                        if (getGameCloseHook() != null) {
                            getGameCloseHook().run();
                        }
                    });
                } catch (Exception e) {
                    NexusUtilities.getLogger().err("[LAUNCHER] Couldn't start Quilt " + quiltVersion + " for Minecraft " + minecraftVersion + " in " + instancePath + " with " + ram + "M RAM.");
                    throw new RuntimeException(e);
                }
            } else {
                NexusUtilities.getLogger().err("[LAUNCHER] Couldn't start Quilt " + quiltVersion + " for Minecraft " + minecraftVersion + " in " + instancePath + " with " + ram + "M RAM.");
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
    public WritableZInstance getInstance() {
        return instance;
    }
}
