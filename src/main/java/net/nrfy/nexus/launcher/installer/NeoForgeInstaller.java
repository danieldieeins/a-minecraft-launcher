package net.nrfy.nexus.launcher.installer;

import com.zyneonstudios.nexus.utilities.NexusUtilities;
import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.utils.UpdaterOptions;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.flowarg.flowupdater.versions.neoforge.NeoForgeVersion;
import fr.flowarg.flowupdater.versions.neoforge.NeoForgeVersionBuilder;

import java.nio.file.Path;

public class NeoForgeInstaller {

    public boolean download(String minecraftVersion, String neoForgeVersion, Path instancePath) {
        VanillaVersion vanillaVersion = new VanillaVersion.VanillaVersionBuilder()
                .withName(minecraftVersion)
                .build();

        UpdaterOptions options = new UpdaterOptions.UpdaterOptionsBuilder()
                .build();

        NeoForgeVersion neoForge = new NeoForgeVersionBuilder()
                .withNeoForgeVersion(neoForgeVersion)
                .build();

        FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder()
                .withVanillaVersion(vanillaVersion)
                .withModLoaderVersion(neoForge)
                .withUpdaterOptions(options)
                .build();

        try {
            updater.update(instancePath);
            return true;
        } catch (Exception e) {
            NexusUtilities.getLogger().err("[INSTALLER] Couldn't download Minecraft "+minecraftVersion+" with NeoForge "+neoForgeVersion+": "+e.getMessage());
            return false;
        }
    }
}