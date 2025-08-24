package live.nerotv.aminecraftlauncher.installer;

import com.zyneonstudios.nexus.utilities.NexusUtilities;
import com.zyneonstudios.verget.minecraft.MinecraftVerget;
import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.versions.VanillaVersion;

import java.nio.file.Path;
import java.util.UUID;

public class VanillaInstaller implements MinecraftInstaller {

    private final UUID installerUUID = UUID.randomUUID();

    private String minecraftVersion;
    private Path installationPath;

    public VanillaInstaller() {
        this.minecraftVersion = MinecraftVerget.getVersions(MinecraftVerget.Filter.RELEASES).getFirst();
        this.installationPath = Path.of(".");
    }

    public VanillaInstaller(String minecraftVersion, Path installationPath) {
        this.minecraftVersion = minecraftVersion;
        this.installationPath = installationPath;
    }

    public VanillaInstaller(String minecraftVersion, String installationPath) {
        this.minecraftVersion = minecraftVersion;
        this.installationPath = Path.of(installationPath);
    }

    @Override
    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    @Override
    public Path getInstallationPath() {
        return installationPath;
    }

    @Override
    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
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
        NexusUtilities.getLogger().deb("[INSTALLER] Starting download of Minecraft "+minecraftVersion);
        VanillaVersion vanilla = new VanillaVersion.VanillaVersionBuilder()
                .withName(minecraftVersion)
                .build();

        FlowUpdater flowUpdater = new FlowUpdater.FlowUpdaterBuilder()
                .withVanillaVersion(vanilla)
                .build();

        try {
            flowUpdater.update(installationPath);
            return true;
        } catch (Exception e) {
            NexusUtilities.getLogger().printErr("A MINECRAFT LAUNCHER","INSTALLER","Couldn't install Minecraft "+minecraftVersion+"...",e.getMessage(),e.getStackTrace());
            return false;
        }
    }

    @Override
    public UUID getInstallerUUID() {
        return installerUUID;
    }
}