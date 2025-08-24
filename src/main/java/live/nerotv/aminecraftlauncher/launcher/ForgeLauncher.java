package live.nerotv.aminecraftlauncher.launcher;

import com.zyneonstudios.nexus.utilities.NexusUtilities;
import com.zyneonstudios.nexus.utilities.system.OperatingSystem;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import live.nerotv.aminecraftlauncher.installer.ForgeInstaller;

import java.nio.file.Path;

public class ForgeLauncher extends MinecraftLauncher {

    private Process gameProcess;
    private NoFramework framework;
    private boolean launched = false;

    private AuthInfos authInfos;
    public ForgeLauncher(AuthInfos authInfos) {
        this.authInfos = authInfos;
    }

    public void setAuthInfos(AuthInfos authInfos) {
        this.authInfos = authInfos;
    }

    public void launch(String minecraftVersion, String forgeVersion, int ram, Path instancePath, String id) {
        if(!launched) {
            launched = true;
            if (getPreLaunchHook() != null) {
                getPreLaunchHook().run();
            }

            if (ram < 512) {
                ram = 512;
            }

            if (new ForgeInstaller(minecraftVersion, forgeVersion, instancePath).install()) {
                NoFramework.ModLoader forge;
                forge = NoFramework.ModLoader.FORGE;
                framework = new NoFramework(
                        instancePath,
                        authInfos,
                        GameFolder.FLOW_UPDATER
                );
                if (getForgeType(minecraftVersion) == ForgeType.NEW) {
                    forgeVersion = forgeVersion.replace(minecraftVersion + "-", "");
                } else {
                    framework.setCustomModLoaderJsonFileName(minecraftVersion + "-forge" + forgeVersion + ".json");
                }
                if (minecraftVersion.equals("1.7.10")) {
                    forgeVersion = forgeVersion.replace(minecraftVersion + "-", "");
                    framework.setCustomModLoaderJsonFileName("1.7.10-Forge" + forgeVersion + ".json");
                }
                framework.getAdditionalVmArgs().add("-Xms" + ram + "M");
                framework.getAdditionalVmArgs().add("-Xmx" + ram + "M");
                if (OperatingSystem.getType() == OperatingSystem.Type.macOS) {
                    framework.getAdditionalVmArgs().add("-XstartOnFirstThread");
                }
                try {
                    gameProcess = framework.launch(minecraftVersion, forgeVersion, forge);
                    if (getPostLaunchHook() != null) {
                        getPostLaunchHook().run();
                    }
                    gameProcess.onExit().thenRun(() -> {
                        if (getGameCloseHook() != null) {
                            getGameCloseHook().run();
                        }
                    });
                } catch (Exception e) {
                    NexusUtilities.getLogger().err("[LAUNCHER] Couldn't start Forge " + forgeVersion + " for Minecraft " + minecraftVersion + " in " + instancePath + " with " + ram + "M RAM");
                    throw new RuntimeException(e);
                }
            } else {
                NexusUtilities.getLogger().err("[LAUNCHER] Couldn't start Forge " + forgeVersion + " for Minecraft " + minecraftVersion + " in " + instancePath + " with " + ram + "M RAM");
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

    private ForgeType getForgeType(String mcVersion) {
        if(mcVersion.contains(".")) {
            try {
                int i = Integer.parseInt(mcVersion.split("\\.")[1]);
                if (i < 12) {
                    return ForgeType.OLD;
                } else {
                    return ForgeType.NEW;
                }
            } catch (Exception e) {
                NexusUtilities.getLogger().err("Couldn't determine Forge type for Minecraft version " + mcVersion+": "+ e.getMessage());
            }
        }
        return null;
    }

    public enum ForgeType {
        OLD,
        NEW
    }
}