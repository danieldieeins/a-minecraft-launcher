package live.nerotv.aminecraftlauncher.installer;

import com.zyneonstudios.nexus.utilities.NexusUtilities;
import com.zyneonstudios.verget.fabric.FabricVerget;
import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.utils.UpdaterOptions;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.flowarg.flowupdater.versions.fabric.FabricVersion;
import fr.flowarg.flowupdater.versions.fabric.FabricVersionBuilder;

import java.nio.file.Path;
import java.util.UUID;

public class FabricInstaller implements MinecraftInstaller {

    private final UUID installerUUID = UUID.randomUUID();

    private String minecraftVersion;
    private String fabricVersion;
    private Path installationPath;

    public FabricInstaller() {
        this.minecraftVersion = FabricVerget.getSupportedMinecraftVersions(false).getFirst();
        this.fabricVersion = FabricVerget.getVersions(true).getFirst();
        this.installationPath = Path.of(".");
    }

    public FabricInstaller(String minecraftVersion, String fabricVersion, Path installationPath) {
        this.minecraftVersion = minecraftVersion;
        this.fabricVersion = fabricVersion;
        this.installationPath = installationPath;
    }

    public FabricInstaller(String minecraftVersion, String fabricVersion, String installationPath) {
        this.minecraftVersion = minecraftVersion;
        this.fabricVersion = fabricVersion;
        this.installationPath = Path.of(installationPath);
    }

    @Override
    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public String getFabricVersion() {
        return fabricVersion;
    }

    @Override
    public Path getInstallationPath() {
        return installationPath;
    }

    @Override
    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public void setFabricVersion(String fabricVersion) {
        this.fabricVersion = fabricVersion;
    }

    @Override
    public void setInstallationPath(Path installationPath) {
        this.installationPath = installationPath;
    }

    @Override
    public void setInstallationPath(String installationPath) {
        this.installationPath = Path.of(installationPath);
    }

    @Override
    public boolean install() {
        VanillaVersion vanillaVersion = new VanillaVersion.VanillaVersionBuilder()
                .withName(minecraftVersion)
                .build();

        UpdaterOptions options = new UpdaterOptions.UpdaterOptionsBuilder()
                .build();

        FabricVersion fabric = new FabricVersionBuilder()
                .withFabricVersion(fabricVersion)
                .build();

        FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder()
                .withVanillaVersion(vanillaVersion)
                .withModLoaderVersion(fabric)
                .withUpdaterOptions(options)
                .build();

        try {
            updater.update(installationPath);
            return true;
        } catch (Exception e) {
            NexusUtilities.getLogger().printErr("A MINECRAFT LAUNCHER","INSTALLER","Couldn't install Minecraft "+minecraftVersion+" with Fabric "+fabricVersion+"...",e.getMessage(),e.getStackTrace());
            return false;
        }
    }

    @Override
    public UUID getInstallerUUID() {
        return installerUUID;
    }
}