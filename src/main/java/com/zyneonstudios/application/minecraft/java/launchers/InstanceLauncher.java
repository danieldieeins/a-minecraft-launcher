package com.zyneonstudios.application.minecraft.java.launchers;

import com.zyneonstudios.application.frame.web.ApplicationFrame;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.JavaStorage;
import com.zyneonstudios.application.minecraft.java.installers.BasicInstaller;
import com.zyneonstudios.application.minecraft.java.integrations.zyndex.LocalInstance;
import com.zyneonstudios.application.minecraft.java.utilities.JavaSettings;
import com.zyneonstudios.application.minecraft.java.utilities.MinecraftVersion;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class InstanceLauncher extends BasicLauncher {

    private NoFramework framework = null;
    private Process gameProcess = null;
    private final UUID uuid = UUID.randomUUID();

    private final AuthInfos authInfos;
    private final LocalInstance instance;
    private final ApplicationFrame parentFrame;

    public InstanceLauncher(LocalInstance instance, AuthInfos authInfos, ApplicationFrame parent) {
        super(instance.getMinecraftVersion(), instance.getPath(), authInfos, parent);
        this.instance = instance; parentFrame = parent;
        setMemory(instance.getMemory()); this.authInfos = authInfos;
        switch (instance.getModloader().toLowerCase()) {
            case "fabric" -> { setModloader(NoFramework.ModLoader.FABRIC); setModloaderVersion(instance.getFabricVersion()); }
            case "forge" -> { setModloader(NoFramework.ModLoader.FORGE); setModloaderVersion(instance.getForgeVersion()); }
            case "neoforge" -> { setModloader(NoFramework.ModLoader.NEO_FORGE); setModloaderVersion(instance.getNeoForgeVersion()); }
            case "quilt" -> { setModloader(NoFramework.ModLoader.QUILT); setModloaderVersion(instance.getQuiltVersion()); }
        }
    }

    @Override
    public NoFramework getFramework() {
        return framework;
    }

    @Override
    public Process getGameProcess() {
        return gameProcess;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean launch() {
        JavaStorage.runningInstances.put(instance.getId(),uuid);
        parentFrame.executeJavaScript("setLaunch(\"UPDATING\",\"bx bx-loader-alt bx-spin\",\"active wiggle\",\"\");");
        NexusApplication.getLogger().deb("[Minecraft] (Instance) Is "+instance.getId()+" up to date: "+instance.update());
        BasicInstaller gameInstaller = new BasicInstaller(getMinecraftVersion(),getInstancePath());
        if(getModloader()!=null&&getModloaderVersion()!=null) {
            gameInstaller.setModloader(getModloader());
            gameInstaller.setModloaderVersion(getModloaderVersion());
            if(getModloader().equals(NoFramework.ModLoader.VERY_OLD_FORGE)||getModloader().equals(NoFramework.ModLoader.OLD_FORGE)||getModloader().equals(NoFramework.ModLoader.FORGE)) {
                if(!getModloaderVersion().startsWith(getMinecraftVersion())) {
                    gameInstaller.setModloaderVersion(getMinecraftVersion() + "-" + getModloaderVersion());
                    setModloaderVersion(gameInstaller.getModloaderVersion());
                }
            }
        }

        if(gameInstaller.install()) {
            parentFrame.executeJavaScript("setLaunch(\"LAUNCHING\",\"bx bx-loader-circle bx-spin\",\"active wiggle\",\"async.java.button.launch."+instance.getId()+"\");");
            framework = new NoFramework(
                    getInstancePath(),
                    authInfos,
                    GameFolder.FLOW_UPDATER
            );

            framework.getAdditionalVmArgs().add("-Xms" + getMemory() + "M");
            framework.getAdditionalVmArgs().add("-Xmx" + getMemory() + "M");

            ArrayList<String> customArgs = (ArrayList<String>)instance.getSettings().get("settings.java.jvm-arguments");
            for(String argument:customArgs) {
                framework.getAdditionalVmArgs().add(argument);
            }

            NoFramework.ModLoader loader = NoFramework.ModLoader.VANILLA;
            String loaderVersion = "";
            if (getModloader() != null && getModloaderVersion() != null) {
                loader = getModloader();
                loaderVersion = getModloaderVersion();
                if(getModloader().equals(NoFramework.ModLoader.FORGE)) {
                    loaderVersion = loaderVersion.replace(getMinecraftVersion() + "-", "");
                }
            }

            try {
                parentFrame.executeJavaScript("setLaunch(\"RUNNING\",\"bx bx-check-circle\",\"active hover-wiggle\",\"async.java.button.launch."+instance.getId()+"\");");
                JavaSettings.setJava(Objects.requireNonNull(MinecraftVersion.getType(getMinecraftVersion())));
                showApp(false);
                gameProcess = framework.launch(getMinecraftVersion(), loaderVersion, loader);
                gameProcess.onExit().thenRun(() -> {
                    parentFrame.executeJavaScript("setLaunch(\"LAUNCH\",\"bx bx-rocket\",\"active hover-wiggle\",\"async.java.button.launch."+instance.getId()+"\");");
                    JavaStorage.runningInstances.remove(instance.getId(),uuid);
                    showApp(true);
                    System.gc();
                });
                return true;
            } catch (Exception e) {
                NexusApplication.getLogger().err("[MINECRAFT] (INSTANCE LAUNCHER) Couldn't start the game: " + e.getMessage());
            }
        }
        parentFrame.executeJavaScript("setLaunch(\"LAUNCH\",\"bx bx-rocket\",\"active hover-wiggle\",\"async.java.button.launch."+instance.getId()+"\");");
        JavaStorage.runningInstances.remove(instance.getId(),uuid);
        showApp(true);
        return false;
    }
}