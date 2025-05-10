package com.zyneonstudios.application;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.zyneonstudios.ApplicationMain;
import com.zyneonstudios.application.auth.MicrosoftAuth;
import com.zyneonstudios.application.installer.java.OperatingSystem;
import com.zyneonstudios.application.integrations.zyndex.ZyndexIntegration;
import com.zyneonstudios.application.integrations.zyndex.instance.ReadableInstance;
import com.zyneonstudios.application.utils.backend.MinecraftVersion;
import com.zyneonstudios.application.utils.backend.Runner;
import com.zyneonstudios.application.utils.frame.web.CustomWebFrame;
import com.zyneonstudios.application.utils.frame.web.ZyneonWebFrame;
import com.zyneonstudios.nexus.desktop.NexusDesktop;
import com.zyneonstudios.nexus.utilities.json.GsonUtility;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Application {

    public static JsonStorage config;
    private static ZyneonWebFrame frame;
    public static String startTab = "start";
    public static int memory;
    public static String instancePath;
    public static MicrosoftAuth auth;
    public static JsonStorage instances;
    public static String version;
    public static String theme;
    public static boolean logOutput;
    public static boolean thirdPartyWarn;
    public static String lastInstance;
    public static ArrayList<String> args;
    public static String updateChannel = "stable";

    public static final Runner runner = new Runner();
    public static ArrayList<String> running = new ArrayList<>();

    public Application(ArrayList<String> arguments) {
        args = arguments;
        if(!args.isEmpty()) {
            version = arguments.getFirst();
        } else {
            throw new RuntimeException("Missing arguments");
        }
    }

    public Runner getRunner() {
        return runner;
    }

    private void init() {
        initConfig();
        NexusDesktop.init();
    }

    private void initConfig() {
        JsonStorage updaterConfig = new JsonStorage(new File(ApplicationMain.getDirectoryPath().replace("/Application/","/NEXUS App/") + "config/updater.json"));
        if(updaterConfig.getString("updater.versions.app.type")!=null) {
            updateChannel = updaterConfig.getString("updater.versions.app.type");
        }

        config = new JsonStorage(new File(ApplicationMain.getDirectoryPath() + "config.json"));
        config.ensure("settings.starttab","start");
        config.ensure("settings.language","auto");
        config.ensure("settings.logOutput",false);
        config.ensure("settings.memory.default", 1024);
        config.ensure("settings.logger.debug", false);
        config.ensure("settings.appearance.theme","default.dark");
        config.ensure("settings.lastInstance","zyneon::overview");
        config.ensure("settings.warnings.thirdParty",true);

        thirdPartyWarn = config.getBool("settings.warnings.thirdParty");
        logOutput = config.getBool("settings.logOutput");
        theme = config.getString("settings.appearance.theme");
        memory = config.getInteger("settings.memory.default");
        startTab = config.getString("settings.starttab");
        if(config.getString("settings.lastInstance").equalsIgnoreCase("zyneon::overview")) {
            lastInstance = null;
        } else {
            lastInstance = config.getString("settings.lastInstance");
        }
    }

    public void start(boolean online) {
        Application.online = online;
        init();
        try {
            CompletableFuture.runAsync(Application::login);
            CompletableFuture.runAsync(()->{
                ApplicationMain.getLogger().log("[APP] [ASYNC] Syncing available Minecraft versions...");
                MinecraftVersion.syncVersions();
                ApplicationMain.getLogger().log("[APP] [ASYNC] Synced versions!");
            });
            try {
                ApplicationMain.getLogger().log("[APP] Trying to sync installed instances...");
                loadInstances();
            } catch (Exception e) {
                ApplicationMain.getLogger().deb("[APP] Couldn't sync installed instances: "+e.getMessage());
            }
            ApplicationMain.getLogger().log("[APP] Setting up frame and webview...");
            checkURL();
            ApplicationMain.getLogger().log("[APP] Styling webview frame...");
            frame.setTitlebar("NEXUS App", Color.black, Color.white);
            frame.setVisible(true);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    System.exit(0);
                }
            });
            try {
                frame.setIconImage(ImageIO.read(Objects.requireNonNull(getClass().getResource("/logo.png"))).getScaledInstance(32, 32, Image.SCALE_SMOOTH));
            } catch (IOException ignore) {}
            ApplicationMain.getLogger().log("[APP] Showing webview frame and hiding splash icon...");
            ApplicationMain.splash.setVisible(false);
        } catch (UnsupportedPlatformException | CefInitializationException | IOException | InterruptedException e) {
            ApplicationMain.getLogger().err("[APP] FATAL: Couldn't start NEXUS App: "+e.getMessage());
            throw new RuntimeException(e);
        }
        System.gc();
        ApplicationMain.getLogger().log("[APP] NEXUS App successfully started!");
    }

    public static void loadInstances() {
        File file = new File(ApplicationMain.getDirectoryPath() + "libs/zyneon/instances.json");
        ApplicationMain.getLogger().deb("[APP] Created instance json path: " + file.getParentFile().mkdirs());
        if (file.exists()) {
            ApplicationMain.getLogger().deb("[APP] Deleted old instance json: " + file.delete());
        }
        instances = new JsonStorage(file);
        List<Map<String, Object>> instanceList = new ArrayList<>();

        File officialPath = new File(getInstancePath() + "instances/official/");
        ApplicationMain.getLogger().deb("[APP] Created official instance path: " + officialPath.mkdirs());
        File[] officialInstances = officialPath.listFiles();
        if (officialInstances != null) {
            for (File instance : officialInstances) {
                if (instance.isDirectory()) {
                    if (!instance.getName().equals("zyneonplus")) {
                        saveInstance(instanceList, instance);
                    } else {
                        File[] zyneonInstances = instance.listFiles();
                        if (zyneonInstances != null) {
                            for (File zynstance : zyneonInstances) {
                                if (zynstance.isDirectory()) {
                                    saveInstance(instanceList, zynstance);
                                }
                            }
                        }
                    }
                }
            }
        }

        File unofficialPath = new File(getInstancePath() + "instances/");
        ApplicationMain.getLogger().deb("[APP] Created unofficial instance path: " + unofficialPath.mkdirs());
        File[] unofficialInstances = unofficialPath.listFiles();
        if (unofficialInstances != null) {
            for (File instance : unofficialInstances) {
                if (instance.isDirectory()) {
                    if (!instance.getName().equalsIgnoreCase("official")) {
                        saveInstance(instanceList, instance);
                    }
                }
            }
        }

        instances.set("instances", instanceList);
    }

    private static boolean saveInstance(List<Map<String, Object>> instanceList, File local) {
        try {
            File instanceFile = new File(local + "/zyneonInstance.json");
            if (instanceFile.exists()) {
                Map<String, Object> instance_ = new HashMap<>();
                ReadableInstance instance = new ReadableInstance(instanceFile);
                if (instance.getSchemeVersion() == null) {
                    instance = new ReadableInstance(ZyndexIntegration.convert(instanceFile));
                } else if (instance.getSchemeVersion().contains("2024.2")) {
                    instance = new ReadableInstance(ZyndexIntegration.convert(instanceFile));
                }
                if (instance.getIconUrl() != null) {
                    instance_.put("icon", instance.getIconUrl());
                }
                String modloader = instance.getModloader();
                if (modloader.equalsIgnoreCase("forge")) {
                    modloader = "Forge " + instance.getForgeVersion();
                } else if (modloader.equalsIgnoreCase("fabric")) {
                    modloader = "Fabric " + instance.getFabricVersion();
                }
                instance_.put("id", instance.getId());

                boolean isEditable = true;
                try {
                    if(!new Gson().fromJson(GsonUtility.getFromFile(instance.getFile()), JsonObject.class).getAsJsonObject("instance").getAsJsonObject("meta").get("isEditable").getAsBoolean()) {
                        isEditable = false;
                    }
                } catch (Exception ignore) {}

                instance_.put("isEditable", isEditable);

                instance_.put("name", instance.getName());
                instance_.put("version", instance.getVersion());
                instance_.put("minecraft", instance.getMinecraftVersion());
                instance_.put("modloader", modloader);
                instanceList.add(instance_);
            }
            return true;
        } catch (Exception e) {
            ApplicationMain.getLogger().err("[APPLICATION] Couldn't add instance to list: "+e.getMessage());
            return false;
        }
    }

    public static void login() {
        try {
            if (auth != null) {
                auth.destroy();
                auth = null;
                System.gc();
            }
            auth = new MicrosoftAuth();
        } catch (Exception e) {
            ApplicationMain.getLogger().err("[APP] Couldn't login: " + e.getMessage());
        }
    }

    public static String getStartURL() {
        String url = "";
        if (startTab.equalsIgnoreCase("instances")) {
            url = getInstancesURL();
        } else {
            url = getNewsURL();
        }
        if(url.contains("?")) {
            url=url+"&theme="+theme;
        } else {
            url=url+"?theme="+theme;
        }
        return url;
    }

    public static boolean online = false;
    public static String getOnlineStartURL() {
        online = !online;
        return getStartURL();
    }

    public static String getURLBase() {
        return ApplicationMain.getDirectoryPath()+"temp/ui/";
    }

    public static String getNewsURL() {
        if(online) {
            return "https://danieldieeins.github.io/NEXUS-App/content/start.html";
        } else {
            return "file://"+getURLBase()+"start.html";
        }
    }

    public static String getInstancesURL() {
        if(online) {
            return "https://danieldieeins.github.io/NEXUS-App/content/instances.html";
        } else {
            return "file://"+getURLBase()+"instances.html";
        }
    }

    public static String getSettingsURL() {
        if(online) {
            return "https://danieldieeins.github.io/NEXUS-App/content/settings.html";
        } else {
            return "file://"+getURLBase()+"settings.html";
        }
    }

    private void checkURL() throws IOException, UnsupportedPlatformException, CefInitializationException, InterruptedException {
        if(ApplicationMain.operatingSystem==OperatingSystem.Linux) {
            frame = new CustomWebFrame(getStartURL());
        } else {
            frame = new ZyneonWebFrame(getStartURL());
            frame.pack();
        }
        frame.setMinimumSize(new Dimension(1201,501));
        frame.setSize(new Dimension(1201,721));
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
    }

    public static ZyneonWebFrame getFrame() {
        return frame;
    }

    public static String getInstancePath() {
        if(instancePath==null) {
            config.ensure("settings.path.instances","default");
            if(config.getString("settings.path.instances").equals("default")) {
                Application.getFrame().getBrowser().loadURL(Application.getSettingsURL()+"?tab=select");
                throw new RuntimeException("No instance path");
            } else {
                try {
                    String path = config.getString("settings.path.instances");
                    if(!path.toLowerCase().contains("nexus app")) {
                        path = path+"/NEXUS App/";
                    }
                    File instanceFolder = new File(URLDecoder.decode(path, StandardCharsets.UTF_8));
                    ApplicationMain.getLogger().deb("[APP] Instance path created: "+instanceFolder.mkdirs());
                    instancePath = instanceFolder.getAbsolutePath();
                } catch (Exception e) {
                    ApplicationMain.getLogger().err("[APP] Instance path invalid - Please select a new one! Falling back to default path.");
                    throw new RuntimeException("No instance path");
                }
            }
        }
        return instancePath.replace("\\","/")+"/";
    }
}