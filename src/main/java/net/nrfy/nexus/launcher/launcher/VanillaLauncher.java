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

    public void launch(WritableInstance instance, AuthInfos authInfos) {
        ZyndexIntegration.update(instance);
        launch(instance.getMinecraftVersion(), instance.getSettings().getMemory(), Path.of(instance.getPath()),instance.getId(),authInfos);
    }

    public void launch(String version, int ram, Path instancePath, String id, AuthInfos authInfos) {
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
                game.onExit().thenRun(()->{

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