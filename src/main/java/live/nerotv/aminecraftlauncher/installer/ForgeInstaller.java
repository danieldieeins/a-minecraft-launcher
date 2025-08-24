package live.nerotv.aminecraftlauncher.installer;

import com.zyneonstudios.nexus.utilities.NexusUtilities;
import com.zyneonstudios.verget.forge.ForgeVerget;
import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.utils.UpdaterOptions;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.flowarg.flowupdater.versions.forge.ForgeVersion;

import java.nio.file.Path;
import java.util.UUID;

public class ForgeInstaller implements MinecraftInstaller {

    private final UUID installerUUID = UUID.randomUUID();

    private String minecraftVersion;
    private String forgeVersion;
    private Path installationPath;

    public ForgeInstaller() {
        this.minecraftVersion = ForgeVerget.getSupportedMinecraftVersions().getFirst();
        this.forgeVersion = ForgeVerget.getVersions().getFirst();
        this.installationPath = Path.of(".");
    }

    public ForgeInstaller(String minecraftVersion, String forgeVersion, Path installationPath) {
        this.minecraftVersion = minecraftVersion;
        this.forgeVersion = forgeVersion;
        this.installationPath = installationPath;
    }

    public ForgeInstaller(String minecraftVersion, String forgeVersion, String installationPath) {
        this.minecraftVersion = minecraftVersion;
        this.forgeVersion = forgeVersion;
        this.installationPath = Path.of(installationPath);
    }

    @Override
    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public String getForgeVersion() {
        return forgeVersion;
    }

    @Override
    public Path getInstallationPath() {
        return installationPath;
    }

    @Override
    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public void setForgeVersion(String forgeVersion) {
        this.forgeVersion = forgeVersion;
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

        ForgeVersion forge = new fr.flowarg.flowupdater.versions.forge.ForgeVersionBuilder()
                .withForgeVersion(forgeVersion)
                .build();

        FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder()
                .withVanillaVersion(vanillaVersion)
                .withModLoaderVersion(forge)
                .withUpdaterOptions(options)
                .build();

        try {
            updater.update(installationPath);
            return true;
        } catch (Exception e) {
            NexusUtilities.getLogger().printErr("A MINECRAFT LAUNCHER","INSTALLER","Couldn't install Minecraft "+minecraftVersion+" with Forge "+forgeVersion+"...",e.getMessage(),e.getStackTrace());
            return false;
        }
    }

    @Override
    public UUID getInstallerUUID() {
        return null;
    }
}