package net.nrfy.nexus.launcher.installer;

import com.zyneonstudios.nexus.utilities.NexusUtilities;
import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.utils.UpdaterOptions;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.flowarg.flowupdater.versions.forge.ForgeVersion;

import java.nio.file.Path;

public class ForgeInstaller {

    public boolean download(String minecraftVersion, String forgeVersion, Path instancePath) {
        VanillaVersion vanillaVersion = new VanillaVersion.VanillaVersionBuilder()
                .withName(minecraftVersion)
                .build();

        UpdaterOptions options = new UpdaterOptions.UpdaterOptionsBuilder()
                .build();

        ForgeVersion forge = new fr.flowarg.flowupdater.versions.forge.ForgeVersionBuilder()
                .withForgeVersion(forgeVersion)
                .build();

        FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder()
                .withVanillaVersion(vanillaVersion)
                .withModLoaderVersion(forge)
                .withUpdaterOptions(options)
                .build();

        try {
            updater.update(instancePath);
            return true;
        } catch (Exception e) {
            NexusUtilities.getLogger().err("[INSTALLER] Couldn't download Minecraft "+minecraftVersion+" with Forge "+forgeVersion+": "+e.getMessage());
            return false;
        }
    }
}