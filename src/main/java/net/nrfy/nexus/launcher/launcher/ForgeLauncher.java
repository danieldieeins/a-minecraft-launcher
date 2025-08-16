package net.nrfy.nexus.launcher.launcher;

import com.zyneonstudios.nexus.utilities.NexusUtilities;
import com.zyneonstudios.nexus.utilities.system.OperatingSystem;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import net.nrfy.nexus.launcher.installer.ForgeInstaller;
import net.nrfy.nexus.launcher.integrations.zyndex.ZyndexIntegration;
import net.nrfy.nexus.launcher.integrations.zyndex.instance.WritableInstance;
import net.nrfy.nexus.launcher.utils.MinecraftVersion;

import java.nio.file.Path;

public class ForgeLauncher {

    public void launch(WritableInstance instance, AuthInfos authInfos) {
        WritableInstance updatedInstance = ZyndexIntegration.update(instance);
        if(updatedInstance!=null) {
            launch(updatedInstance.getMinecraftVersion(), updatedInstance.getForgeVersion(), updatedInstance.getSettings().getMemory(), Path.of(updatedInstance.getPath()),updatedInstance.getId(), authInfos);
        } else {
            launch(instance.getMinecraftVersion(), instance.getForgeVersion(), instance.getSettings().getMemory(), Path.of(instance.getPath()),instance.getId(), authInfos);
        }
        System.gc();
    }

    public void launch(String minecraftVersion, String forgeVersion, int ram, Path instancePath, String id, AuthInfos authInfos) {
        if(ram<512) {
            ram = 512;
        }

        if(new ForgeInstaller().download(minecraftVersion,forgeVersion,instancePath)) {

            NoFramework.ModLoader forge;
            forge = NoFramework.ModLoader.FORGE;
            NoFramework framework = new NoFramework(
                    instancePath,
                    authInfos,
                    GameFolder.FLOW_UPDATER
            );
            if(MinecraftVersion.getForgeType(minecraftVersion) == MinecraftVersion.ForgeType.NEW) {
                forgeVersion = forgeVersion.replace(minecraftVersion + "-", "");
            } else {
                framework.setCustomModLoaderJsonFileName(minecraftVersion + "-forge"+forgeVersion+".json");
            }
            if(minecraftVersion.equals("1.7.10")) {
                forgeVersion = forgeVersion.replace(minecraftVersion+"-","");
                framework.setCustomModLoaderJsonFileName("1.7.10-Forge" + forgeVersion + ".json");
            }
            framework.getAdditionalVmArgs().add("-Xms"+ ram +"M");
            framework.getAdditionalVmArgs().add("-Xmx" + ram + "M");
            if(OperatingSystem.getType() == OperatingSystem.Type.macOS) {
                framework.getAdditionalVmArgs().add("-XstartOnFirstThread");
            }
            try {
                Process game = framework.launch(minecraftVersion, forgeVersion, forge);
                game.onExit().thenRun(()->{

                });
            } catch (Exception e) {
                NexusUtilities.getLogger().err("[LAUNCHER] Couldn't start Forge "+forgeVersion+" for Minecraft "+minecraftVersion+" in "+instancePath+" with "+ram+"M RAM");
                throw new RuntimeException(e);
            }
        } else {
            NexusUtilities.getLogger().err("[LAUNCHER] Couldn't start Forge "+forgeVersion+" for Minecraft "+minecraftVersion+" in "+instancePath+" with "+ram+"M RAM");
        }
    }
}