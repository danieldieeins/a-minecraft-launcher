package live.nerotv.aminecraftlauncher.installer;

import com.zyneonstudios.nexus.utilities.NexusUtilities;
import com.zyneonstudios.verget.neoforge.NeoForgeVerget;
import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.utils.UpdaterOptions;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.flowarg.flowupdater.versions.neoforge.NeoForgeVersion;
import fr.flowarg.flowupdater.versions.neoforge.NeoForgeVersionBuilder;

import java.nio.file.Path;
import java.util.UUID;

public class NeoForgeInstaller implements MinecraftInstaller {

    private final UUID installerUUID = UUID.randomUUID();

    private String minecraftVersion;
    private String neoForgeVersion;
    private Path installationPath;

    public NeoForgeInstaller() {
        this.minecraftVersion = NeoForgeVerget.getSupportedMinecraftVersions().getFirst();
        this.neoForgeVersion = NeoForgeVerget.getVersions().getFirst();
        this.installationPath = Path.of(".");
    }

    public NeoForgeInstaller(String minecraftVersion, String neoForgeVersion, Path installationPath) {
        this.minecraftVersion = minecraftVersion;
        this.neoForgeVersion = neoForgeVersion;
        this.installationPath = installationPath;
    }

    public NeoForgeInstaller(String minecraftVersion, String neoForgeVersion, String installationPath) {
        this.minecraftVersion = minecraftVersion;
        this.neoForgeVersion = neoForgeVersion;
        this.installationPath = Path.of(installationPath);
    }

    @Override
    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public String getNeoForgeVersion() {
        return neoForgeVersion;
    }

    @Override
    public Path getInstallationPath() {
        return installationPath;
    }

    @Override
    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public void setNeoForgeVersion(String neoForgeVersion) {
        this.neoForgeVersion = neoForgeVersion;
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

        NeoForgeVersion neoForge = new NeoForgeVersionBuilder()
                .withNeoForgeVersion(neoForgeVersion)
                .build();

        FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder()
                .withVanillaVersion(vanillaVersion)
                .withModLoaderVersion(neoForge)
                .withUpdaterOptions(options)
                .build();

        try {
            updater.update(installationPath);
            return true;
        } catch (Exception e) {
            NexusUtilities.getLogger().printErr("A MINECRAFT LAUNCHER","INSTALLER","Couldn't install Minecraft "+minecraftVersion+" with NeoForge "+neoForgeVersion+"...",e.getMessage(),e.getStackTrace());
            return false;
        }
    }

    @Override
    public UUID getInstallerUUID() {
        return installerUUID;
    }
}