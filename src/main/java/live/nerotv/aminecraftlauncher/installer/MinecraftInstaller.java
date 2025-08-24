package live.nerotv.aminecraftlauncher.installer;

import java.nio.file.Path;
import java.util.UUID;

public interface MinecraftInstaller {

    UUID getInstallerUUID();
    String getMinecraftVersion();
    void setMinecraftVersion(String minecraftVersion);
    Path getInstallationPath();
    void setInstallationPath(Path installationPath);
    void setInstallationPath(String installationPath);
    boolean install();
}