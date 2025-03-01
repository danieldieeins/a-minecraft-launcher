package com.zyneonstudios.application.minecraft.java.launchers;

import com.zyneonstudios.application.frame.web.ApplicationFrame;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.JavaStorage;
import com.zyneonstudios.application.minecraft.java.installers.BasicInstaller;
import com.zyneonstudios.application.minecraft.java.utilities.JavaSettings;
import com.zyneonstudios.application.minecraft.java.utilities.MinecraftVersion;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Objects;

public class BasicLauncher implements Launcher {

    private final String minecraftVersion;
    private final Path instancePath;
    private final AuthInfos authInfos;
    private final ApplicationFrame parentFrame;

    private int memory = JavaStorage.map.getInteger("settings.global.memory");
    private NoFramework.ModLoader modloader = null;
    private String modloaderVersion = null;

    private NoFramework framework = null;
    private Process gameProcess = null;

    public BasicLauncher(String minecraftVersion, Path instancePath, AuthInfos authInfos, ApplicationFrame parent) {
        this.minecraftVersion = minecraftVersion;
        this.instancePath = instancePath;
        this.authInfos = authInfos;
        this.parentFrame = parent;
    }

    public NoFramework.ModLoader getModloader() {
        return modloader;
    }

    public String getModloaderVersion() {
        return modloaderVersion;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public Path getInstancePath() {
        return instancePath;
    }

    public String getPath() {
        return instancePath.toString();
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public void setModloader(NoFramework.ModLoader modLoader) {
        this.modloader = modLoader;
    }

    public void setModloaderVersion(String modloaderVersion) {
        this.modloaderVersion = modloaderVersion;
    }

    public NoFramework getFramework() {
        return framework;
    }

    public Process getGameProcess() {
        return gameProcess;
    }

    @Override
    public boolean launch() {
        BasicInstaller gameInstaller = new BasicInstaller(minecraftVersion,instancePath);
        if(modloader!=null&&modloaderVersion!=null) {
            gameInstaller.setModloader(modloader);
            gameInstaller.setModloaderVersion(modloaderVersion);
            if(modloader.equals(NoFramework.ModLoader.FORGE)) {
                gameInstaller.setModloaderVersion(minecraftVersion+"-"+modloaderVersion);
            }
        }

        if(gameInstaller.install()) {
            framework = new NoFramework(
                    instancePath,
                    authInfos,
                    GameFolder.FLOW_UPDATER
            );

            framework.getAdditionalVmArgs().add("-Xms" + memory + "M");
            framework.getAdditionalVmArgs().add("-Xmx" + memory + "M");

            NoFramework.ModLoader loader = NoFramework.ModLoader.VANILLA;
            String loaderVersion = "";
            if (modloader != null && modloaderVersion != null) {
                loader = modloader;
                loaderVersion = modloaderVersion;
                if(modloader.equals(NoFramework.ModLoader.FORGE)) {
                    loaderVersion = loaderVersion.replace(minecraftVersion + "-", "");
                }
            }

            try {
                JavaSettings.setJava(Objects.requireNonNull(MinecraftVersion.getType(minecraftVersion)));
                showApp(false);
                gameProcess = framework.launch(minecraftVersion, loaderVersion, loader);
                gameProcess.onExit().thenRun(() -> {
                    showApp(true);
                    System.gc();
                });
                return true;
            } catch (Exception e) {
                NexusApplication.getLogger().err("[MINECRAFT] (BASIC LAUNCHER) Couldn't start the game: " + e.getMessage());
            }
        }
        showApp(true);
        return false;
    }

    protected void showApp(boolean show) {
        if(JavaStorage.map.getBoolean("settings.global.minimizeApp")) {
            if (show) {
                parentFrame.setState(JFrame.NORMAL);
            } else {
                parentFrame.setState(JFrame.ICONIFIED);
            }
        }
    }
}