package com.zyneonstudios.application.installer;

import com.zyneonstudios.ApplicationMain;
import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.utils.UpdaterOptions;
import fr.flowarg.flowupdater.versions.QuiltVersion;
import fr.flowarg.flowupdater.versions.VanillaVersion;

import java.nio.file.Path;

public class QuiltInstaller {

    public boolean download(String minecraftVersion, String quiltVersion, Path instancePath) {
        VanillaVersion vanillaVersion = new VanillaVersion.VanillaVersionBuilder()
                .withName(minecraftVersion)
                .build();

        UpdaterOptions options = new UpdaterOptions.UpdaterOptionsBuilder()
                .build();

        QuiltVersion quilt = new QuiltVersion.QuiltVersionBuilder()
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
            ApplicationMain.getLogger().err("[INSTALLER] Couldn't download Minecraft " + minecraftVersion + " with Quilt " + quiltVersion);
            return false;
        }
    }
}