package com.zyneonstudios.application.installer;

import com.zyneonstudios.ApplicationMain;
import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.utils.UpdaterOptions;
import fr.flowarg.flowupdater.versions.FabricVersion;
import fr.flowarg.flowupdater.versions.VanillaVersion;

import java.nio.file.Path;

public class FabricInstaller {

    public boolean download(String minecraftVersion, String fabricVersion, Path instancePath) {
        VanillaVersion vanillaVersion = new VanillaVersion.VanillaVersionBuilder()
                .withName(minecraftVersion)
                .build();

        UpdaterOptions options = new UpdaterOptions.UpdaterOptionsBuilder()
                .build();

        FabricVersion fabric = new FabricVersion.FabricVersionBuilder()
                .withFabricVersion(fabricVersion)
                .build();

        FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder()
                .withVanillaVersion(vanillaVersion)
                .withModLoaderVersion(fabric)
                .withUpdaterOptions(options)
                .build();

        try {
            updater.update(instancePath);
            return true;
        } catch (Exception e) {
            ApplicationMain.getLogger().err("[INSTALLER] Couldn't download Minecraft " + minecraftVersion + " with Fabric " + fabricVersion);
            return false;
        }
    }
}