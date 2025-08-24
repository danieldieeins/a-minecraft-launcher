package live.nerotv.aminecraftlauncher.installer;

import com.zyneonstudios.nexus.utilities.NexusUtilities;
import com.zyneonstudios.verget.quilt.QuiltVerget;
import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.utils.UpdaterOptions;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.flowarg.flowupdater.versions.fabric.QuiltVersion;
import fr.flowarg.flowupdater.versions.fabric.QuiltVersionBuilder;

import java.nio.file.Path;
import java.util.UUID;

public class QuiltInstaller implements MinecraftInstaller{

    private final UUID installerUUID = UUID.randomUUID();

    private String minecraftVersion;
    private String quiltVersion;
    private Path installationPath;

    public QuiltInstaller() {
        this.minecraftVersion = QuiltVerget.getSupportedMinecraftVersions(false).getFirst();
        this.quiltVersion = QuiltVerget.getVersions().getFirst();
        this.installationPath = Path.of(".");
    }

    public QuiltInstaller(String minecraftVersion, String quiltVersion, Path installationPath) {
        this.minecraftVersion = minecraftVersion;
        this.quiltVersion = quiltVersion;
        this.installationPath = installationPath;
    }

    public QuiltInstaller(String minecraftVersion, String quiltVersion, String installationPath) {
        this.minecraftVersion = minecraftVersion;
        this.quiltVersion = quiltVersion;
        this.installationPath = Path.of(installationPath);
    }

    @Override
    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public String getQuiltVersion() {
        return quiltVersion;
    }

    @Override
    public Path getInstallationPath() {
        return installationPath;
    }

    @Override
    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public void setQuiltVersion(String quiltVersion) {
        this.quiltVersion = quiltVersion;
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

        QuiltVersion quilt = new QuiltVersionBuilder()
                .withQuiltVersion(quiltVersion)
                .build();

        FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder()
                .withVanillaVersion(vanillaVersion)
                .withModLoaderVersion(quilt)
                .withUpdaterOptions(options)
                .build();

        try {
            updater.update(installationPath);
            return true;
        } catch (Exception e) {
            NexusUtilities.getLogger().printErr("A MINECRAFT LAUNCHER","INSTALLER","Couldn't install Minecraft "+minecraftVersion+" with Quilt "+quiltVersion+"...",e.getMessage(),e.getStackTrace());
            return false;
        }
    }

    @Override
    public UUID getInstallerUUID() {
        return installerUUID;
    }
}