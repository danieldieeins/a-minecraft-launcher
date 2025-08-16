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

public class VanillaLauncher extends MinecraftLauncher {

    private Process gameProcess;
    private NoFramework framework;
    private boolean launched = false;

    private WritableInstance instance = null;

    private AuthInfos authInfos;
    public VanillaLauncher(AuthInfos authInfos) {
        this.authInfos = authInfos;
    }

    public void setAuthInfos(AuthInfos authInfos) {
        this.authInfos = authInfos;
    }

    public void launch(WritableInstance instance) {
        this.instance = instance;
        ZyndexIntegration.update(instance);
        launch(instance.getMinecraftVersion(), instance.getSettings().getMemory(), Path.of(instance.getPath()),instance.getId());
    }

    public void launch(String version, int ram, Path instancePath, String id) {
        if(!launched) {
            launched = true;

            if (getPreLaunchHook() != null) {
                getPreLaunchHook().run();
            }

            if (ram < 512) {
                ram = 512;
            }
            if (new VanillaInstaller().download(version, instancePath)) {
                framework = new NoFramework(
                        instancePath,
                        authInfos,
                        GameFolder.FLOW_UPDATER
                );
                framework.getAdditionalVmArgs().add("-Xms512M");
                framework.getAdditionalVmArgs().add("-Xmx4096M");
                if (OperatingSystem.getType() == OperatingSystem.Type.macOS) {
                    framework.getAdditionalVmArgs().add("-XstartOnFirstThread");
                }
                try {
                    gameProcess = framework.launch(version, "", NoFramework.ModLoader.VANILLA);
                    if (getPostLaunchHook() != null) {
                        getPostLaunchHook().run();
                    }
                    gameProcess.onExit().thenRun(() -> {
                        if (getGameCloseHook() != null) {
                            getGameCloseHook().run();
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