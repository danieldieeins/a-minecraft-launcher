package live.nerotv.aminecraftlauncher.launcher;

import com.zyneonstudios.nexus.utilities.NexusUtilities;
import com.zyneonstudios.nexus.utilities.system.OperatingSystem;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import live.nerotv.aminecraftlauncher.installer.FabricInstaller;

import java.nio.file.Path;

public class FabricLauncher extends MinecraftLauncher {

    private Process gameProcess;
    private NoFramework framework;
    private boolean launched = false;

    private AuthInfos authInfos;
    public FabricLauncher(AuthInfos authInfos) {
        this.authInfos = authInfos;
    }

    public void setAuthInfos(AuthInfos authInfos) {
        this.authInfos = authInfos;
    }

    public void launch(String minecraftVersion, String fabricVersion, int ram, Path instancePath, String id) {
        if(!launched) {
            launched = true;
            if (getPreLaunchHook() != null) {
                getPreLaunchHook().run();
            }

            if (ram < 512) {
                ram = 512;
            }
            if (new FabricInstaller(minecraftVersion, fabricVersion, instancePath).install()) {
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
}