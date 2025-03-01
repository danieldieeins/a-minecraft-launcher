package com.zyneonstudios.application.minecraft.java.installers;

import com.zyneonstudios.application.main.NexusApplication;
import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.utils.UpdaterOptions;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.flowarg.flowupdater.versions.fabric.FabricVersion;
import fr.flowarg.flowupdater.versions.fabric.FabricVersionBuilder;
import fr.flowarg.flowupdater.versions.fabric.QuiltVersion;
import fr.flowarg.flowupdater.versions.fabric.QuiltVersionBuilder;
import fr.flowarg.flowupdater.versions.forge.ForgeVersion;
import fr.flowarg.flowupdater.versions.forge.ForgeVersionBuilder;
import fr.flowarg.flowupdater.versions.neoforge.NeoForgeVersion;
import fr.flowarg.flowupdater.versions.neoforge.NeoForgeVersionBuilder;
import fr.flowarg.openlauncherlib.NoFramework;

import java.nio.file.Path;

public class BasicInstaller implements Installer {

    private final String minecraftVersion;
    private final Path instancePath;

    private NoFramework.ModLoader modloader = null;
    private String modloaderVersion = null;

    private FlowUpdater flowUpdater = null;

    public BasicInstaller(String minecraftVersion, Path instancePath) {
        this.minecraftVersion = minecraftVersion;
        this.instancePath = instancePath;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public Path getInstancePath() {
        return instancePath;
    }

    public NoFramework.ModLoader getModloader() {
        return modloader;
    }

    public String getModloaderVersion() {
        return modloaderVersion;
    }

    public void setModloader(NoFramework.ModLoader modloader) {
        this.modloader = modloader;
    }

    public void setModloaderVersion(String modloaderVersion) {
        this.modloaderVersion = modloaderVersion;
    }

    public FlowUpdater getFlowUpdater() {
        return flowUpdater;
    }

    @Override
    public boolean install() {
        try {
            VanillaVersion gameVersion = new VanillaVersion.VanillaVersionBuilder()
                    .withName(minecraftVersion)
                    .build();

            FlowUpdater.FlowUpdaterBuilder builder = new FlowUpdater.FlowUpdaterBuilder();
            builder.withVanillaVersion(gameVersion);

            if(modloader!=null&&modloaderVersion!=null) {
                if (modloader.equals(NoFramework.ModLoader.FORGE) || modloader.equals(NoFramework.ModLoader.OLD_FORGE) || modloader.equals(NoFramework.ModLoader.VERY_OLD_FORGE)) {
                    ForgeVersion forgeVersion = new ForgeVersionBuilder()
                            .withForgeVersion(modloaderVersion)
                            .build();
                    builder.withModLoaderVersion(forgeVersion);

                } else if (modloader.equals(NoFramework.ModLoader.NEO_FORGE)) {
                    NeoForgeVersion neoForgeVersion = new NeoForgeVersionBuilder()
                            .withNeoForgeVersion(modloaderVersion)
                            .build();
                    builder.withModLoaderVersion(neoForgeVersion);

                } else if (modloader.equals(NoFramework.ModLoader.FABRIC)) {
                    FabricVersion fabricVersion = new FabricVersionBuilder()
                            .withFabricVersion(modloaderVersion)
                            .build();
                    builder.withModLoaderVersion(fabricVersion);

                } else if (modloader.equals(NoFramework.ModLoader.QUILT)) {
                    QuiltVersion quiltVersion = new QuiltVersionBuilder()
                            .withQuiltVersion(modloaderVersion)
                            .build();
                    builder.withModLoaderVersion(quiltVersion);

                }
            }

            UpdaterOptions options = new UpdaterOptions.UpdaterOptionsBuilder().build();
            builder.withUpdaterOptions(options);

            flowUpdater = builder.build();
            flowUpdater.update(instancePath);

            return true;
        } catch (Exception e) {
            NexusApplication.getLogger().err("[Minecraft] (BASIC INSTALLER) Couldn't install Minecraft: "+e.getMessage());
        }
        return false;
    }
}