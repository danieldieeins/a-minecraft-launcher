package net.nrfy.nexus.launcher.installer;

import com.zyneonstudios.nexus.utilities.NexusUtilities;
import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.utils.UpdaterOptions;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.flowarg.flowupdater.versions.fabric.QuiltVersion;
import fr.flowarg.flowupdater.versions.fabric.QuiltVersionBuilder;

import java.nio.file.Path;

public class QuiltInstaller {

    public boolean download(String minecraftVersion, String quiltVersion, Path instancePath) {
        VanillaVersion vanillaVersion = new VanillaVersion.VanillaVersionBuilder()
                .withName(minecraftVersion)
                .build();

        UpdaterOptions options = new UpdaterOptions.UpdaterOptionsBuilder()
                .build();

        QuiltVersion quilt = new QuiltVersionBuilder()
                .withQuiltVersion(quiltVersion)
                .build();

        FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder()
                .withVanillaVersion(vanillaVersion)
                .withModLoaderVersion(quilt)
                .withUpdaterOptions(options)
                .build();

        try {
            updater.update(instancePath);
            return true;
        } catch (Exception e) {
            NexusUtilities.getLogger().err("[INSTALLER] Couldn't download Minecraft " + minecraftVersion + " with Quilt " + quiltVersion);
            return false;
        }
    }
}