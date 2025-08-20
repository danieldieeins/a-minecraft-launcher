package net.nrfy.nexus.launcher.launcher;

import com.zyneonstudios.nexus.Main;
import com.zyneonstudios.nexus.utilities.NexusUtilities;
import com.zyneonstudios.nexus.utilities.system.OperatingSystem;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import net.nrfy.nexus.launcher.installer.ForgeInstaller;
import net.nrfy.nexus.launcher.integrations.zyndex.ZZyndexIntegration;
import net.nrfy.nexus.launcher.integrations.zyndex.instance.WritableZInstance;

import java.nio.file.Path;

public class ForgeLauncher extends MinecraftLauncher {

    private Process gameProcess;
    private NoFramework framework;
    private boolean launched = false;

    private WritableZInstance instance = null;

    private AuthInfos authInfos;
    public ForgeLauncher(AuthInfos authInfos) {
        this.authInfos = authInfos;
    }

    public void setAuthInfos(AuthInfos authInfos) {
        this.authInfos = authInfos;
    }

    public void launch(WritableZInstance instance) {
        this.instance = instance;
        WritableZInstance updatedInstance = ZZyndexIntegration.update(instance);
        if(updatedInstance!=null) {
            launch(updatedInstance.getMinecraftVersion(), updatedInstance.getForgeVersion(), updatedInstance.getSettings().getMemory(), Path.of(updatedInstance.getPath()),updatedInstance.getId());
        } else {
            launch(instance.getMinecraftVersion(), instance.getForgeVersion(), instance.getSettings().getMemory(), Path.of(instance.getPath()),instance.getId());
        }
        System.gc();
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

            if (new ForgeInstaller().download(minecraftVersion, forgeVersion, instancePath)) {
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

    @Override
    public WritableZInstance getInstance() {
        return instance;
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
                Main.logger.err("[SYSTEM] Couldn't resolve Minecraft version "+mcVersion+": "+e.getMessage());
            }
        }
        return null;
    }

    public enum ForgeType {
        OLD,
        NEW
    }
}