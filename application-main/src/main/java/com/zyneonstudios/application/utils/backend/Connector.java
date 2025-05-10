package com.zyneonstudios.application.utils.backend;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.zyneonstudios.ApplicationMain;
import com.zyneonstudios.application.Application;
import com.zyneonstudios.application.auth.MicrosoftAuth;
import com.zyneonstudios.application.integrations.Integrator;
import com.zyneonstudios.application.integrations.curseforge.*;
import com.zyneonstudios.application.integrations.modrinth.*;
import com.zyneonstudios.application.integrations.zyndex.ZyndexIntegration;
import com.zyneonstudios.application.integrations.zyndex.instance.ReadableInstance;
import com.zyneonstudios.application.integrations.zyndex.instance.WritableInstance;
import com.zyneonstudios.application.launcher.*;
import com.zyneonstudios.application.utils.frame.MemoryFrame;
import com.zyneonstudios.application.utils.frame.web.ZyneonWebFrame;
import com.zyneonstudios.nexus.index.ReadableZyndex;
import com.zyneonstudios.nexus.instance.ZynstanceBuilder;
import com.zyneonstudios.nexus.utilities.file.FileActions;
import com.zyneonstudios.nexus.utilities.file.FileGetter;
import com.zyneonstudios.nexus.utilities.json.GsonUtility;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;
import com.zyneonstudios.nexus.utilities.storage.ReadableJsonStorage;
import com.zyneonstudios.nexus.utilities.strings.StringConverter;
import com.zyneonstudios.verget.Verget;
import com.zyneonstudios.verget.minecraft.MinecraftVerget;
import fr.flowarg.openlauncherlib.NoFramework;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class Connector {

    private final ZyneonWebFrame frame;

    public Connector(ZyneonWebFrame frame) {
        this.frame = frame;
    }

    private void syncSettings(String type) {
        type = type.toLowerCase();
        switch (type) {
            case "general" -> {
                String tab = "start";
                if (Application.getStartURL().toLowerCase().contains("instances.html")) {
                    tab = "instances";
                }
                frame.executeJavaScript("syncGeneral('" + tab + "','"+Application.updateChannel+"');");
            }
            case "global" ->
                    frame.executeJavaScript("syncGlobal('" + Application.config.getString("settings.memory.default").replace(".0", "") + " MB','" + Application.getInstancePath() + "','"+Application.logOutput+"')");
            case "profile" -> {
                if(Application.auth!=null) {
                    if (Application.auth.isLoggedIn()) {
                        frame.executeJavaScript("syncProfile('" + Application.auth.getAuthInfos().getUsername() + "','" + StringConverter.getUUIDString(Application.auth.getAuthInfos().getUuid()) + "');");
                        return;
                    }
                }
                frame.executeJavaScript("logout();");
            }
            case "version" -> frame.executeJavaScript("syncApp('"+Application.updateChannel+" ▪ "+Application.version+"');");
        }
    }

    public void resolveRequest(String request) {
        //frame.sendNotification("Resolving...","(BackendConnector) resolving "+request+"...","",false);
        frame.executeJavaScript("checkForWeb();");
        if (request.equals("sync.settings.general")) {
            if(!frame.getBrowser().getURL().contains("tab=general")) {
                frame.getBrowser().loadURL(Application.getSettingsURL() + "?tab=general");
            }
        }
        if (request.equals("button.copy.uuid")) {
            StringSelection uuid = new StringSelection(StringConverter.getUUIDString(Application.auth.getAuthInfos().getUuid()));
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(uuid, uuid);
        } else if (request.startsWith("button.notification.remove.")) {
            frame.removeNotification(request.replace("button.notification.remove.",""));
        } else if (request.startsWith("button.updateChannel.")) {
            Application.updateChannel = request.replace("button.updateChannel.","");
            new JsonStorage(ApplicationMain.getDirectoryPath().replace("\\","/").replace("/Application/","/NEXUS App/")+"config/updater.json").set("updater.versions.app.type",Application.updateChannel);
        } else if (request.equals("button.online")) {
            frame.getBrowser().loadURL(Application.getOnlineStartURL());
        } else if(request.equals("sync.notifications")) {
            CompletableFuture.runAsync(()-> {
                frame.syncNotifications();
                Application.runner.run();
            });
        } else if(request.startsWith("sync.creator.")) {
            final String request_ = request.replace("sync.creator.", "");
            SwingUtilities.invokeLater(() -> {
                ArrayList<String> versions;
                switch (request_) {
                    case "snapshots" -> versions = Verget.getMinecraftVersions(MinecraftVerget.Filter.EXPERIMENTAL);
                    case "quilt" -> {
                        versions = Verget.getQuiltGameVersions(true);
                        ArrayList<String> mlversions = Verget.getQuiltVersions(versions.getFirst());
                        for (String version : mlversions) {
                            frame.executeJavaScript("addToSelect('creator-mlversion','" + version.toLowerCase().replace(" (latest)", "") + "','" + version + "')");
                        }
                    }
                    case "fabric" -> {
                        versions = Verget.getFabricGameVersions(true);
                        ArrayList<String> mlversions = Verget.getFabricVersions(true, versions.getFirst());
                        for (String version : mlversions) {
                            frame.executeJavaScript("addToSelect('creator-mlversion','" + version.toLowerCase().replace(" (latest)", "") + "','" + version + "')");
                        }
                    }
                    case "neoforge" -> {
                        versions = Verget.getNeoForgeGameVersions();
                        ArrayList<String> mlversions = Verget.getNeoForgeVersions(versions.getFirst());
                        for (String version : mlversions) {
                            frame.executeJavaScript("addToSelect('creator-mlversion','" + version.toLowerCase().replace(" (latest)", "") + "','" + version + "')");
                        }
                    }
                    case "forge" -> {
                        versions = Verget.getForgeGameVersions();
                        String ver = versions.getFirst();
                        String prefix = "";
                        ArrayList<String> mlversions = Verget.getForgeVersions(ver);
                        for (String version : mlversions) {
                            frame.executeJavaScript("addToSelect('creator-mlversion','" + prefix + version.toLowerCase().replace(" (latest)", "") + "','" + prefix + version + "')");
                        }
                    }
                    default -> versions = Verget.getMinecraftVersions(MinecraftVerget.Filter.RELEASES);
                }
                for (String version : versions) {
                    frame.executeJavaScript("addToSelect('creator-minecraft','" + version.toLowerCase().replace(" (latest)", "") + "','" + version + "')");
                }
            });
        } else if(request.startsWith("sync.creator-version.")) {
            final String requestS = request.replace("sync.creator-version.", "");
            SwingUtilities.invokeLater(() -> {
                String[] request_ = requestS.split("\\.", 2);
                String type = request_[0];
                String version = request_[1];
                if (type.equalsIgnoreCase("forge")) {
                    ArrayList<String> mlversions = Verget.getForgeVersions(version);
                    String prefix = "";
                    for (String v : mlversions) {
                        frame.executeJavaScript("addToSelect('creator-mlversion','" + prefix + v.toLowerCase().replace(" (latest)", "") + "','" + prefix + v + "')");
                    }
                } else if (type.equalsIgnoreCase("fabric")) {
                    ArrayList<String> mlversions = Verget.getFabricVersions(true, version);
                    for (String v : mlversions) {
                        frame.executeJavaScript("addToSelect('creator-mlversion','" + v.toLowerCase().replace(" (latest)", "") + "','" + v + "')");
                    }
                } else if (type.equalsIgnoreCase("neoforge")) {
                    ArrayList<String> mlversions = Verget.getNeoForgeVersions(version);
                    for (String v : mlversions) {
                        frame.executeJavaScript("addToSelect('creator-mlversion','" + v.toLowerCase().replace(" (latest)", "") + "','" + v + "')");
                    }
                } else if (type.equalsIgnoreCase("quilt")) {
                    ArrayList<String> mlversions = Verget.getQuiltVersions(version);
                    for (String v : mlversions) {
                        frame.executeJavaScript("addToSelect('creator-mlversion','" + v.toLowerCase().replace(" (latest)", "") + "','" + v + "')");
                    }
                }
            });
        } else if(request.startsWith("sync.updater.")) {
            final String request_ = request.replace("sync.updater.", "");
            SwingUtilities.invokeLater(() -> {
                ArrayList<String> versions;
                switch (request_) {
                    case "snapshots" -> versions = Verget.getMinecraftVersions(MinecraftVerget.Filter.EXPERIMENTAL);
                    case "quilt" -> {
                        versions = Verget.getQuiltGameVersions(true);
                        ArrayList<String> mlversions = Verget.getQuiltVersions(versions.getFirst());
                        for (String version : mlversions) {
                            frame.executeJavaScript("addToSelect('settings-mlversion','" + version.toLowerCase().replace(" (latest)", "") + "','" + version + "')");
                        }
                    }
                    case "fabric" -> {
                        versions = Verget.getFabricGameVersions(true);
                        ArrayList<String> mlversions = Verget.getFabricVersions(true, versions.getFirst());
                        for (String version : mlversions) {
                            frame.executeJavaScript("addToSelect('settings-mlversion','" + version.toLowerCase().replace(" (latest)", "") + "','" + version + "')");
                        }
                    }
                    case "neoforge" -> {
                        versions = Verget.getNeoForgeGameVersions();
                        ArrayList<String> mlversions = Verget.getNeoForgeVersions(versions.getFirst());
                        for (String version : mlversions) {
                            frame.executeJavaScript("addToSelect('settings-mlversion','" + version.toLowerCase().replace(" (latest)", "") + "','" + version + "')");
                        }
                    }
                    case "forge" -> {
                        versions = Verget.getForgeGameVersions();
                        String ver = versions.getFirst();
                        String prefix = "";
                        ArrayList<String> mlversions = Verget.getForgeVersions(ver);
                        for (String version : mlversions) {
                            frame.executeJavaScript("addToSelect('settings-mlversion','" + prefix + version.toLowerCase().replace(" (latest)", "") + "','" + prefix + version + "')");
                        }
                    }
                    default -> versions = Verget.getMinecraftVersions(MinecraftVerget.Filter.RELEASES);
                }
                for (String version : versions) {
                    frame.executeJavaScript("addToSelect('settings-minecraft','" + version.toLowerCase().replace(" (latest)", "") + "','" + version + "')");
                }
            });
        } else if(request.startsWith("sync.updater-version.")) {
            final String requestS = request.replace("sync.updater-version.","");
            SwingUtilities.invokeLater(()->{
                String[] request_ = requestS.split("\\.", 2);
                String type = request_[0];
                String version = request_[1];
                if(type.equalsIgnoreCase("forge")) {
                    ArrayList<String> mlversions = Verget.getForgeVersions(version);
                    String prefix = "";
                    for (String v : mlversions) {
                        frame.executeJavaScript("addToSelect('settings-mlversion','" + prefix + v.toLowerCase().replace(" (latest)", "") + "','" + prefix + v + "')");
                    }
                } else if(type.equalsIgnoreCase("neoforge")) {
                    ArrayList<String> mlversions = Verget.getNeoForgeVersions(version);
                    for (String v : mlversions) {
                        frame.executeJavaScript("addToSelect('settings-mlversion','" + v.toLowerCase().replace(" (latest)", "") + "','" + v + "')");
                    }
                } else if(type.equalsIgnoreCase("quilt")) {
                    ArrayList<String> mlversions = Verget.getQuiltVersions(version);
                    for (String v : mlversions) {
                        frame.executeJavaScript("addToSelect('settings-mlversion','" + v.toLowerCase().replace(" (latest)", "") + "','" + v + "')");
                    }
                } else if(type.equalsIgnoreCase("fabric")) {
                    ArrayList<String> mlversions = Verget.getFabricVersions(true,version);
                    for (String v : mlversions) {
                        frame.executeJavaScript("addToSelect('settings-mlversion','" + v.toLowerCase().replace(" (latest)", "") + "','" + v + "')");
                    }
                }
            });
        } else if (request.startsWith("button.configure.")) {
            request = request.replace("button.configure.","");
            if(request.startsWith("log.")) {
                request = request.replace("log.","");
                if(request.equals("enable")) {
                    Application.config.set("settings.logOutput",true);
                } else {
                    Application.config.set("settings.logOutput",false);
                }
                Application.logOutput = Application.config.getBool("settings.logOutput");
                syncSettings("global");
            }
        } else if (request.contains("sync.instances.list")) {
                Application.getInstancePath();
                String filePath = ApplicationMain.getDirectoryPath() + "libs/zyneon/instances.json";
                Gson gson = new Gson();
                try (JsonReader reader = new JsonReader(new FileReader(filePath))) {
                    JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
                    JsonArray instances = jsonObject.getAsJsonArray("instances");
                    CompletableFuture.runAsync(() -> {
                        for (JsonElement element : instances) {
                            JsonObject instance = element.getAsJsonObject();
                            String png = "assets/zyneon/images/instances/" + instance.get("id").toString().replace("\"", "") + ".png";
                            if (new File(Application.getURLBase() + png).exists()) {
                                frame.executeJavaScript("addInstanceToList(" + instance.get("id") + "," + instance.get("name") + ",'" + png + "',true);");
                            } else if (instance.get("icon") != null) {
                                png = instance.get("icon").toString().replace("\"", "");
                                frame.executeJavaScript("addInstanceToList(" + instance.get("id") + "," + instance.get("name") + ",'" + png + "',true);");
                            } else {
                                frame.executeJavaScript("addInstanceToList(" + instance.get("id") + "," + instance.get("name") + ",true);");
                            }
                        }
                    });
                } catch (IOException e) {
                    ApplicationMain.getLogger().err(e.getMessage());
                }
                frame.executeJavaScript("loadTab('" + Application.lastInstance + "');");
        } else if (request.contains("sync.web")) {
            frame.getBrowser().loadURL(Application.getOnlineStartURL());
        } else if (request.contains("sync.start")) {
            if(Application.updateChannel.equalsIgnoreCase("experimental")) {
                frame.executeJavaScript("document.getElementById('experimental-notice').style.display = 'block';");
            }
            frame.executeJavaScript("syncStart('app');");
            frame.executeJavaScript("loadNews(true);");
        } else if (request.contains("sync.login")) {
            SwingUtilities.invokeLater(() -> {
                try {
                    if (Application.auth.isLoggedIn()) {
                        frame.executeJavaScript("login('" + Application.auth.getAuthInfos().getUsername() + "');");
                        MicrosoftAuth.syncTeam(Application.auth.getAuthInfos().getUuid());
                    } else {
                        frame.executeJavaScript("logout();");
                    }
                } catch (Exception e) {
                    frame.executeJavaScript("logout();");
                }
            });
        } else if (request.startsWith("sync.theme.")) {
            syncTheme(request.replace("sync.theme.",""));
        } else if (request.contains("sync.settings.")) {
            syncSettings(request.replace("sync.settings.", ""));
        } else if (request.contains("button.theme.default.light")) {
            Application.theme = "default.light";
            Application.config.set("settings.appearance.theme", Application.theme);
            frame.setTitlebar("NEXUS App", Color.white, Color.black);
        } else if (request.contains("button.theme.default.zyneon")) {
            Application.theme = "default.zyneon";
            Application.config.set("settings.appearance.theme", Application.theme);
            frame.setTitlebar("NEXUS App", Color.decode("#050113"), Color.white);
        } else if (request.contains("button.theme.default.dark")) {
            Application.theme = "default.dark";
            Application.config.set("settings.appearance.theme", Application.theme);
            frame.setTitlebar("NEXUS App", Color.black, Color.white);
        } else if (request.startsWith("zyndex.install.modpack.")) {
            request = request.replace("zyndex.install.modpack.","");
            String[] request_ = request.split("\\.", 2);
            String index = request_[0].replace("%DOT%",".");
            String id = request_[1];
            ReadableZyndex zyndex = new ReadableZyndex(index);
            if(zyndex.getInstancesById().containsKey(id)) {
                ZyndexIntegration.install(zyndex.getInstancesById().get(id));
            }
        } else if (request.contains("button.refresh")) {
            if (request.contains(".instances")) {
                Application.loadInstances();
                frame.getBrowser().loadURL(Application.getInstancesURL());
            } else {
                frame.getBrowser().loadURL(Application.getStartURL());
            }
        } else if (request.contains("button.exit")) {
            SwingUtilities.invokeLater(() -> frame.getInstance().dispatchEvent(new WindowEvent(frame.getInstance(), WindowEvent.WINDOW_CLOSING)));
        } else if (request.contains("button.instance.")) {
            String id = request.replace("button.instance.", "").toLowerCase();
            CompletableFuture.runAsync(() -> {
                File file = new File(Application.getInstancePath() + "instances/" + id + "/zyneonInstance.json");
                if (file.exists()) {
                    ReadableInstance instance = new ReadableInstance(file);
                    if (instance.getSchemeVersion() == null) {
                        instance = new ReadableInstance(ZyndexIntegration.convert(file));
                    } else if (instance.getSchemeVersion().contains("2024.2")) {
                        instance = new ReadableInstance(ZyndexIntegration.convert(file));
                    }
                    String name = instance.getName().replace("'", "\\'");
                    String version = instance.getVersion();
                    String description;
                    if (id.contains("official/")) {
                        description = "This instance is outdated. Try to update.";
                    } else {
                        description = "This is an instance created by YOU!";
                    }
                    if (instance.getDescription() != null) {
                        description = instance.getDescription().replace("\"", "''");
                    }
                    String minecraft = instance.getMinecraftVersion();
                    String modloader = instance.getModloader();
                    String mlversion;
                    if (modloader.equalsIgnoreCase("forge")) {
                        mlversion = instance.getForgeVersion();
                    } else if (modloader.equalsIgnoreCase("neoforge")) {
                        mlversion = instance.getNeoForgeVersion();
                    } else if (modloader.equalsIgnoreCase("quilt")) {
                        mlversion = instance.getQuiltVersion();
                    } else if (modloader.equalsIgnoreCase("fabric")) {
                        mlversion = instance.getFabricVersion();
                    } else {
                        mlversion = "No mods";
                    }

                    try {
                        if(new Gson().fromJson(GsonUtility.getFromFile(instance.getFile()),JsonObject.class).getAsJsonObject("instance").getAsJsonObject("meta").get("isEditable").getAsBoolean()) {
                            frame.executeJavaScript("makeEditable(\""+id+"\");");
                        }
                    } catch (Exception ignore) {}

                    File icon = new File(Application.getURLBase() + "assets/zyneon/images/instances/" + id + ".png");
                    File logo = new File(Application.getURLBase() + "assets/zyneon/images/instances/" + id + "-logo.png");
                    File background = new File(Application.getURLBase() + "assets/zyneon/images/instances/" + id + ".webp");
                    String icon_ = "";
                    String logo_ = "";
                    String background_ = "";
                    ApplicationMain.getLogger().deb(" ");
                    ApplicationMain.getLogger().deb("[CONNECTOR] Searching for icon of: " + id + "...");
                    if (icon.exists()) {
                        ApplicationMain.getLogger().deb("[CONNECTOR] Found asset icon for " + id + "!");
                        icon_ = "assets/zyneon/images/instances/" + id + ".png";
                        ApplicationMain.getLogger().deb("[CONNECTOR] Applied asset icon \"" + "assets/zyneon/images/instances/" + id + ".png" + "\" to " + id);
                    } else if (instance.getIconUrl() != null) {
                        ApplicationMain.getLogger().deb("[CONNECTOR] Found custom icon for " + id + "!");
                        icon_ = instance.getIconUrl();
                        ApplicationMain.getLogger().deb("[CONNECTOR] Applied custom icon \"" + instance.getIconUrl() + "\" to " + id);
                    } else {
                        ApplicationMain.getLogger().deb("[CONNECTOR] Couldn't find icon file for " + id);
                    }
                    ApplicationMain.getLogger().deb(" ");
                    ApplicationMain.getLogger().deb("[CONNECTOR] Searching for logo of: " + id + "...");
                    if (logo.exists()) {
                        ApplicationMain.getLogger().deb("[CONNECTOR] Found asset logo for " + id + "!");
                        logo_ = "assets/zyneon/images/instances/" + id + "-logo.png";
                        ApplicationMain.getLogger().deb("[CONNECTOR] Applied asset logo \"" + "assets/zyneon/images/instances/" + id + "-logo.png" + "\" to " + id);
                    } else if (instance.getLogoUrl() != null) {
                        ApplicationMain.getLogger().deb("[CONNECTOR] Found custom logo for " + id + "!");
                        logo_ = instance.getLogoUrl();
                        ApplicationMain.getLogger().deb("[CONNECTOR] Applied custom logo \"" + instance.getLogoUrl() + "\" to " + id);
                    } else {
                        ApplicationMain.getLogger().deb("[CONNECTOR] Couldn't find logo file for " + id);
                    }
                    ApplicationMain.getLogger().deb(" ");
                    ApplicationMain.getLogger().deb("[CONNECTOR] Searching for background of: " + id + "...");
                    if (background.exists()) {
                        ApplicationMain.getLogger().deb("[CONNECTOR] Found asset background for " + id + "!");
                        background_ = "assets/zyneon/images/instances/" + id + ".webp";
                        ApplicationMain.getLogger().deb("[CONNECTOR] Applied asset background \"" + "assets/zyneon/images/instances/" + id + ".webp" + "\" to " + id);
                    } else if (instance.getBackgroundUrl() != null) {
                        ApplicationMain.getLogger().deb("[CONNECTOR] Found custom background for " + id + "!");
                        background_ = instance.getBackgroundUrl();
                        ApplicationMain.getLogger().deb("[CONNECTOR] Applied custom background \"" + instance.getBackgroundUrl() + "\" to " + id);
                    } else {
                        ApplicationMain.getLogger().deb("[CONNECTOR] Couldn't find background file for " + id);
                    }
                    ApplicationMain.getLogger().deb(" ");
                    frame.executeJavaScript("syncDescription(\"" + description + "\");");
                    frame.executeJavaScript("syncTitle('" + name + "','" + icon_ + "');");
                    frame.executeJavaScript("syncLogo('" + logo_ + "');");
                    frame.executeJavaScript("syncBackground('" + background_ + "');");
                    frame.executeJavaScript("syncDock('" + id + "','" + version + "','" + minecraft + "','" + modloader + "','" + mlversion + "');");

                    int ram = instance.getSettings().getMemory();

                    String command = "syncSettings(\"" + id + "\",\"" + ram + " MB\",\"" + name + "\",\"" + version + "\",\"" + description + "\",\"" + minecraft + "\",\"" + modloader + "\",\"" + mlversion + "\",\"" + icon_ + "\",\"" + logo_ + "\",\"" + background_ + "\");";
                    ApplicationMain.getLogger().deb("[CONNECTOR] Sending command: " + command);
                    frame.executeJavaScript(command);

                    Application.lastInstance = id;
                    Application.config.set("settings.lastInstance", Application.lastInstance);
                    if(Application.running.contains(id)) {
                        frame.executeJavaScript("launchStarted();");
                    } else {
                        frame.executeJavaScript("launchDefault();");
                    }
                }
            });
        } else if (request.contains("button.delete.")) {
            request = request.replace("button.delete.", "");
            File instance = new File(Application.getInstancePath() + "instances/" + request + "/");
            if (instance.exists()) {
                FileActions.deleteFolder(instance);
                resolveRequest("button.refresh.instances");
            }
        } else if (request.contains("sync.search.")) {
            request = request.replace("sync.search.","").replace("%DOT�",".de");
            String[] request_ = request.split("\\.", 7);
            String source = request_[0];
            String type = request_[1];
            String version = request_[2].replace("%",".");
            String query = request_[3];
            int b = 0;
            int i = Integer.parseInt(request_[4]);
            i=i*20;
            b=b+i;
            String instanceID = request_[5];
            String zyndexUrl = request_[6];
            if(source.equalsIgnoreCase("modrinth")) {
                if (type.equalsIgnoreCase("forge") || type.equalsIgnoreCase("neoforge") || type.equalsIgnoreCase("quilt") || type.equalsIgnoreCase("fabric")) {
                    if(type.equalsIgnoreCase("neoforge")) {
                        Integrator.modrinthToConnector(ModrinthMods.search(query, NoFramework.ModLoader.NEO_FORGE, version, b, 20),instanceID);
                    } else {
                        Integrator.modrinthToConnector(ModrinthMods.search(query, NoFramework.ModLoader.valueOf(type.toUpperCase()), version, b, 20),instanceID);
                    }
                } else if (type.equalsIgnoreCase("shaders")) {
                    Integrator.modrinthToConnector(ModrinthShaders.search(query, version, b, 20),instanceID);
                } else if (type.equalsIgnoreCase("resourcepacks")) {
                    Integrator.modrinthToConnector(ModrinthResourcepacks.search(query, version, b, 20),instanceID);
                } else if (type.equalsIgnoreCase("modpacks")) {
                    Integrator.modrinthToConnector(ModrinthModpacks.search(query, version, b, 20),instanceID);
                }

            } else if(source.equalsIgnoreCase("curseforge")) {
                if (type.equalsIgnoreCase("neoforge") || type.equalsIgnoreCase("forge") || type.equalsIgnoreCase("quilt") || type.equalsIgnoreCase("fabric")) {
                    if(type.equalsIgnoreCase("neoforge")) {
                        Integrator.curseForgeToConnector(CurseForgeMods.search(query, NoFramework.ModLoader.NEO_FORGE, version, b, 20),instanceID);
                    } else {
                        Integrator.curseForgeToConnector(CurseForgeMods.search(query, NoFramework.ModLoader.valueOf(type.toUpperCase()), version, b, 20),instanceID);
                    }
                } else if (type.equalsIgnoreCase("shaders")) {
                    Integrator.curseForgeToConnector(CurseForgeShaders.search(query, version, b, 20),instanceID);
                } else if (type.equalsIgnoreCase("resourcepacks")) {
                    Integrator.curseForgeToConnector(CurseForgeResourcepacks.search(query, version, b, 20),instanceID);
                } else if (type.equalsIgnoreCase("modpacks")) {
                    Integrator.curseForgeToConnector(CurseForgeModpacks.search(query, version, b, 20),instanceID);
                }

            } else if(source.equalsIgnoreCase("zyneon")) {
                CompletableFuture.runAsync(() -> {
                    if (type.equalsIgnoreCase("modpacks")) {
                        Integrator.nexToConnector(ZyndexIntegration.search(new ReadableZyndex("https://zyneonstudios.github.io/nexus-nex/zyndex/index.json"), query, version), instanceID);
                    }
                });
            } else if(source.equalsIgnoreCase("zyndex")) {
                CompletableFuture.runAsync(() -> {
                    if (type.equalsIgnoreCase("modpacks")) {
                        String index = zyndexUrl.replace("%DOT%",".");
                        Integrator.zyndexToConnector(ZyndexIntegration.search(new ReadableZyndex(index), query, version),instanceID);
                    }
                });
            }
        } else if (request.contains("sync.select.minecraft.")) {
            String id = request.replace("sync.select.minecraft.", "");
            for (String version : MinecraftVersion.supportedVersions) {
                frame.executeJavaScript("addToSelect('" + id + "','" + version.toLowerCase().replace(" (latest)", "") + "','" + version + "')");
            }
            if(request.contains("search-version")) {
                frame.executeJavaScript("syncSearch();");
            }
        } else if (request.contains("button.creator.update.")) {
            String[] creator = request.replace("button.creator.update.", "").split("\\.", 7);
            String id = creator[0];
            String name = creator[1];
            name = name.replace("%DOT%", ".");
            String version = creator[2];
            version = version.replace("%DOT%", ".");
            String minecraft = creator[3];
            minecraft = minecraft.replace("%DOT%", ".");
            String modloader = creator[4];
            String mlversion = creator[5];
            mlversion = mlversion.replace("%DOT%", ".");
            String description = creator[6];
            description = description.replace("%DOT%", ".");
            File instancePath = new File(Application.getInstancePath() + "instances/" + id + "/");
            if (instancePath.exists()) {
                ApplicationMain.getLogger().deb("[CONNECTOR] Created instance path: " + instancePath.mkdirs());
                JsonStorage instanceConfig = new JsonStorage(instancePath.getAbsolutePath() + "/zyneonInstance.json");
                WritableInstance instance = new WritableInstance(instanceConfig.getJsonFile());
                instance.setName(name);
                instance.setVersion(version);
                instance.setDescription(description);
                instance.setMinecraftVersion(minecraft);
                if (modloader.equalsIgnoreCase("forge")) {
                    instanceConfig.delete("instance.versions.fabric");
                    instanceConfig.delete("instance.versions.quilt");
                    instanceConfig.delete("instance.versions.neoforge");
                    if (mlversion.toLowerCase().startsWith("old")) {
                        instance.setForgeType("OLD");
                        instance.setForgeVersion(mlversion.replace("old", ""));
                    } else if (mlversion.toLowerCase().startsWith("neo")) {
                        instance.setForgeType("NEO_FORGE");
                        instance.setForgeVersion(mlversion.replace("neo", ""));
                    } else {
                        instance.setForgeType("NEW");
                        instance.setForgeVersion(mlversion.replace("new", ""));
                    }
                } else if (modloader.equalsIgnoreCase("neoforge")) {
                    instanceConfig.delete("instance.versions.quilt");
                    instanceConfig.delete("instance.versions.fabric");
                    instanceConfig.delete("instance.versions.forge");
                    instanceConfig.delete("instance.meta.forgeType");
                    instance.setNeoForgeVersion(mlversion);
                } else if (modloader.equalsIgnoreCase("quilt")) {
                    instanceConfig.delete("instance.versions.neoforge");
                    instanceConfig.delete("instance.versions.fabric");
                    instanceConfig.delete("instance.versions.forge");
                    instanceConfig.delete("instance.meta.forgeType");
                    instance.setQuiltVersion(mlversion);
                } else if (modloader.equalsIgnoreCase("fabric")) {
                    instanceConfig.delete("instance.versions.neoforge");
                    instanceConfig.delete("instance.versions.quilt");
                    instanceConfig.delete("instance.versions.forge");
                    instanceConfig.delete("instance.meta.forgeType");
                    instance.setFabricVersion(mlversion);
                } else {
                    instanceConfig.delete("instance.versions.neoforge");
                    instanceConfig.delete("instance.versions.quilt");
                    instanceConfig.delete("instance.versions.fabric");
                    instanceConfig.delete("instance.versions.forge");
                    instanceConfig.delete("instance.meta.forgeType");
                }
            }
            Application.loadInstances();
            frame.getBrowser().loadURL(Application.getInstancesURL()+"?tab="+id);
        } else if (request.contains("button.creator.create.")) {
            String[] creator = request.replace("button.creator.create.", "").split("\\.", 5);
            String name = creator[0];
            name = name.replace("%DOT%", ".");
            String version = creator[1];
            version = version.replace("%DOT%", ".");
            String minecraft = creator[2];
            minecraft = minecraft.replace("%DOT%", ".");
            String modloader = creator[3];
            String mlversion = creator[4];
            mlversion = mlversion.replace("%DOT%", ".");
            String id = name.toLowerCase().replaceAll("[^a-z0-9]", "");
            File instancePath = new File(Application.getInstancePath() + "instances/" + id + "/");
            if (!instancePath.exists()) {
                ApplicationMain.getLogger().deb("[CONNECTOR] Created instance path: " + instancePath.mkdirs());
                JsonStorage instanceConfig = new JsonStorage(instancePath.getAbsolutePath() + "/zyneonInstance.json");
                ZynstanceBuilder instance = new ZynstanceBuilder(instanceConfig);
                instance.setId(id);
                instance.setName(name);
                if(Application.auth!=null) {
                    if (Application.auth.isLoggedIn()) {
                        ArrayList<String> a = new ArrayList<>();
                        a.add(Application.auth.getAuthInfos().getUsername());
                        instance.setAuthors(a);
                    }
                }
                instance.setVersion(version);
                instance.setMinecraftVersion(minecraft);
                if (modloader.equalsIgnoreCase("forge")) {
                    if (mlversion.toLowerCase().startsWith("old")) {
                        instance.setForgeType("OLD");
                        instance.setForgeVersion(mlversion.replace("old", ""));
                    } else {
                        instance.setForgeType("NEW");
                        instance.setForgeVersion(mlversion.replace("new", ""));
                    }
                } else if (modloader.equalsIgnoreCase("neoforge")) {
                    instance.setNeoForgeVersion(mlversion);
                } else if (modloader.equalsIgnoreCase("quilt")) {
                    instance.setQuiltVersion(mlversion);
                } else if (modloader.equalsIgnoreCase("fabric")) {
                    instance.setFabricVersion(mlversion);
                }
                instance.createFile();
            }
            Application.loadInstances();
            frame.getBrowser().loadURL(Application.getInstancesURL()+"?tab="+id);
        } else if (request.contains("button.start.")) {
            String finalRequest = request;
            CompletableFuture.runAsync(()->{
                frame.executeJavaScript("launchUpdate();");
                ApplicationMain.getLogger().deb("[CONNECTOR] Trying to start instance " + finalRequest.replace("button.start.", ""));
                resolveInstanceRequest(InstanceAction.RUN, finalRequest.replace("button.start.", ""));
            });
        } else if (request.contains("button.starttab.")) {
            String tab = request.replace("button.starttab.", "");
            if (tab.equalsIgnoreCase("instances")) {
                Application.config.set("settings.starttab", "instances");
                Application.startTab = "instances";
            } else {
                Application.config.set("settings.starttab", "start");
                Application.startTab = "start";
            }
            syncSettings("general");
        } else if (request.equalsIgnoreCase("button.username")) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(URI.create("https://www.minecraft.net/de-de/msaprofile/mygames/editprofile"));
                } catch (IOException ignore) {
                }
            }
        } else if (request.equalsIgnoreCase("button.skin")) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(URI.create("https://www.minecraft.net/de-de/msaprofile/mygames/editskin"));
                } catch (IOException ignore) {
                }
            }
        } else if (request.equalsIgnoreCase("button.website")) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(URI.create("https://www.zyneonstudios.com/home"));
                } catch (IOException ignore) {
                }
            }
        } else if (request.equalsIgnoreCase("button.discord")) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(URI.create("https://discord.gg/99YZNfGRSU"));
                } catch (IOException ignore) {
                }
            }
        } else if (request.equalsIgnoreCase("button.laby")) {
            if (Application.auth.isLoggedIn()) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(URI.create("https://laby.net/@" + Application.auth.getAuthInfos().getUsername()));
                    } catch (IOException ignore) {
                    }
                }
            }
        } else if(request.startsWith("button.confirm.")) {
                request = request.replace("button.confirm.","");
                String[] request_ = request.split("\\.", 3);
                String text = request_[0];
                String button = request_[1];
                String continueRequest = request_[2];
                if(text.isEmpty()) {
                    text = null;
                }
                if(button.isEmpty()) {
                    button = null;
                }
                if(continueRequest.isEmpty()) {
                    continueRequest = null;
                }
                thridPartyConfirm(text,button,continueRequest);
        } else if (request.contains("button.icon.")) {
            resolveInstanceRequest(InstanceAction.SHOW_ICON, request.replace("button.icon.", ""));
        } else if (request.contains("button.logo.")) {
            resolveInstanceRequest(InstanceAction.SHOW_LOGO, request.replace("button.logo.", ""));
        } else if (request.contains("button.background.")) {
            resolveInstanceRequest(InstanceAction.SHOW_BACKGROUND, request.replace("button.background.", ""));
        } else if (request.contains("button.mods.")) {
            resolveInstanceRequest(InstanceAction.SHOW_MODS, request.replace("button.mods.", ""));
        } else if (request.contains("button.folder.")) {
            if (request.equals("button.folder.instances")) {
                resolveInstanceRequest(InstanceAction.OPEN_FOLDER, "");
            } else {
                resolveInstanceRequest(InstanceAction.OPEN_FOLDER, request.replace("button.folder.", ""));
            }
        } else if (request.startsWith("button.disable.warn.")) {
            request = request.replace("button.disable.warn.", "");
            if(request.equalsIgnoreCase("thirdparty")) {
                Application.config.set("settings.warnings.thirdParty",false);
                Application.thirdPartyWarn = false;
                frame.executeJavaScript("unmessage();");
            }
        } else if (request.contains("button.screenshots.")) {
            resolveInstanceRequest(InstanceAction.SHOW_SCREENSHOTS, request.replace("button.screenshots.", ""));
        } else if (request.contains("button.change.icon.")) {
            String id = request.replace("button.change.icon.", "");
            SwingUtilities.invokeLater(() -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select an image file");
                chooser.setMultiSelectionEnabled(false);
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "png", "jpeg", "jpg", "webp");
                chooser.addChoosableFileFilter(filter);
                int answer = chooser.showOpenDialog(null);
                if (answer == JFileChooser.APPROVE_OPTION) {
                    String path = URLDecoder.decode(chooser.getSelectedFile().getAbsolutePath().replace("\\", "/"), StandardCharsets.UTF_8);
                    try {
                        String extension;
                        if (path.toLowerCase().endsWith(".jpeg")) {
                            extension = ".jpeg";
                        } else if (path.toLowerCase().endsWith(".jpg")) {
                            extension = ".jpg";
                        } else if (path.toLowerCase().endsWith(".webp")) {
                            extension = ".webp";
                        } else {
                            extension = ".png";
                        }
                        File file = new File(URLDecoder.decode(Application.getInstancePath() + "instances/" + id + "/zyneonIcon" + extension, StandardCharsets.UTF_8));
                        if (file.exists()) {
                            ApplicationMain.getLogger().deb("[CONNECTOR] Deleted old icon: " + file.delete());
                        }
                        Files.copy(Paths.get(path), Paths.get(URLDecoder.decode(Application.getInstancePath() + "instances/" + id + "/zyneonIcon" + extension, StandardCharsets.UTF_8)));
                        JsonStorage instance = new JsonStorage(Application.getInstancePath() + "instances/" + id + "/zyneonInstance.json");
                        instance.set("instance.resources.icon", file.getAbsolutePath().replace("\\", "/"));
                        Application.loadInstances();
                        frame.getBrowser().loadURL(Application.getInstancesURL() + "?tab=" + id);
                    } catch (Exception e) {
                        ApplicationMain.getLogger().err("[CONNECTOR] An error occurred (Icon-chooser): " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
            });
        } else if (request.contains("button.change.logo.")) {
            String id = request.replace("button.change.logo.", "");
            SwingUtilities.invokeLater(() -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select an image file");
                chooser.setMultiSelectionEnabled(false);
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "png", "jpeg", "jpg", "webp");
                chooser.addChoosableFileFilter(filter);
                int answer = chooser.showOpenDialog(null);
                if (answer == JFileChooser.APPROVE_OPTION) {
                    String path = URLDecoder.decode(chooser.getSelectedFile().getAbsolutePath().replace("\\", "/"), StandardCharsets.UTF_8);
                    try {
                        String extension;
                        if (path.toLowerCase().endsWith(".jpeg")) {
                            extension = ".jpeg";
                        } else if (path.toLowerCase().endsWith(".jpg")) {
                            extension = ".jpg";
                        } else if (path.toLowerCase().endsWith(".webp")) {
                            extension = ".webp";
                        } else {
                            extension = ".png";
                        }
                        File file = new File(URLDecoder.decode(Application.getInstancePath() + "instances/" + id + "/zyneonLogo" + extension, StandardCharsets.UTF_8));
                        if (file.exists()) {
                            ApplicationMain.getLogger().deb("[CONNECTOR] Deleted old logo: " + file.delete());
                        }
                        Files.copy(Paths.get(path), Paths.get(URLDecoder.decode(Application.getInstancePath() + "instances/" + id + "/zyneonLogo" + extension, StandardCharsets.UTF_8)));
                        JsonStorage instance = new JsonStorage(Application.getInstancePath() + "instances/" + id + "/zyneonInstance.json");
                        instance.set("instance.resources.logo", file.getAbsolutePath().replace("\\", "/"));
                        Application.loadInstances();
                        frame.getBrowser().loadURL(Application.getInstancesURL() + "?tab=" + id);
                    } catch (Exception e) {
                        ApplicationMain.getLogger().err("[CONNECTOR] An error occurred (Logo-chooser): " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
            });
        } else if (request.contains("button.change.background.")) {
            String id = request.replace("button.change.background.", "");
            SwingUtilities.invokeLater(() -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select an image file");
                chooser.setMultiSelectionEnabled(false);
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "png", "jpeg", "jpg", "webp");
                chooser.addChoosableFileFilter(filter);
                int answer = chooser.showOpenDialog(null);
                if (answer == JFileChooser.APPROVE_OPTION) {
                    String path = URLDecoder.decode(chooser.getSelectedFile().getAbsolutePath().replace("\\", "/"), StandardCharsets.UTF_8);
                    try {
                        String extension;
                        if (path.toLowerCase().endsWith(".jpeg")) {
                            extension = ".jpeg";
                        } else if (path.toLowerCase().endsWith(".jpg")) {
                            extension = ".jpg";
                        } else if (path.toLowerCase().endsWith(".webp")) {
                            extension = ".webp";
                        } else {
                            extension = ".png";
                        }
                        File file = new File(URLDecoder.decode(Application.getInstancePath() + "instances/" + id + "/zyneonBackground" + extension, StandardCharsets.UTF_8));
                        if (file.exists()) {
                            ApplicationMain.getLogger().deb("[CONNECTOR] Deleted old background: " + file.delete());
                        }
                        Files.copy(Paths.get(path), Paths.get(URLDecoder.decode(Application.getInstancePath() + "instances/" + id + "/zyneonBackground" + extension, StandardCharsets.UTF_8)));
                        JsonStorage instance = new JsonStorage(Application.getInstancePath() + "instances/" + id + "/zyneonInstance.json");
                        instance.set("instance.resources.background", file.getAbsolutePath().replace("\\", "/"));
                        Application.loadInstances();
                        frame.getBrowser().loadURL(Application.getInstancesURL() + "?tab=" + id);
                    } catch (Exception e) {
                        ApplicationMain.getLogger().err("[CONNECTOR] An error occurred (Background-chooser): " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
            });
        } else if (request.contains("browser.")) {
            String url = request.replace("browser.","");
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(URI.create(url));
                } catch (IOException ignore) {
                }
            }
        } else if (request.contains("button.install.")) {
            String id = request.replace("button.install.", "");
            ReadableZyndex nex = new ReadableZyndex("https://zyneonstudios.github.io/nexus-nex/zyndex/index.json");
            if(nex.getInstancesById().containsKey(id)) {
                ApplicationMain.getLogger().deb("[CONNECTOR] Installed NEX instance "+id+": "+ZyndexIntegration.install(nex.getInstancesById().get(id)));
            }
        } else if (request.contains("button.resourcepacks.")) {
            resolveInstanceRequest(InstanceAction.SHOW_RESOURCEPACKS, request.replace("button.resourcepacks.", ""));
        } else if (request.contains("button.shaders.")) {
            resolveInstanceRequest(InstanceAction.SHOW_SHADERS, request.replace("button.shaders.", ""));
        } else if (request.contains("button.worlds.")) {
            resolveInstanceRequest(InstanceAction.SHOW_WORLDS, request.replace("button.worlds.", ""));
        } else if (request.contains("button.settings.")) {
            resolveInstanceRequest(InstanceAction.SETTINGS_MEMORY, request.replace("button.settings.", "").replace("memory", "default"));
        } else if (request.contains("button.path.")) {
            request = request.replace("button.path.", "").toLowerCase();
            if (request.equals("instances")) {
                SwingUtilities.invokeLater(() -> {
                    JFileChooser chooser = getJDirectoryChooser();
                    int answer = chooser.showOpenDialog(null);
                    if (answer == JFileChooser.APPROVE_OPTION) {
                        String instancesPath = URLDecoder.decode(chooser.getSelectedFile().getAbsolutePath().replace("\\", "/"), StandardCharsets.UTF_8);
                        Application.config.set("settings.path.instances", instancesPath);
                        if (!instancesPath.toLowerCase().contains("nexus app")) {
                            instancesPath = instancesPath + "/NEXUS App";
                        }
                        Application.instancePath = instancesPath;
                        Application.loadInstances();
                        frame.getBrowser().loadURL(Application.getSettingsURL() + "?tab=global");
                    }
                });
            }
        } else if (request.startsWith("curseforge.")) {
            request = request.replace("curseforge.","");
            resolveCurseForgeRequest(request);
        } else if (request.startsWith("modrinth.")) {
            request = request.replace("modrinth.","");
            resolveModrinthRequest(request);
        } else if (request.contains("button.account")) {
            if(Application.auth!=null) {
                if (Application.auth.isLoggedIn()) {
                    resolveRequest("button.logout");
                    return;
                }
            }
            frame.executeJavaScript("message(\"<i class='bx bx-loader-alt bx-spin bx-rotate-90'></i> Logging in...\");");

                Application.auth = new MicrosoftAuth();
                Application.auth.login();

        } else if (request.contains("button.logout")) {
            if(Application.auth!=null) {
                if (Application.auth.isLoggedIn()) {
                    JsonStorage saver = new JsonStorage(Application.auth.getSaveFile());
                    saver.delete("opapi.ms");
                    ApplicationMain.getLogger().deb("[CONNECTOR] Deleted login: " + Application.auth.getSaveFile().delete());
                    Application.auth = null;
                }
            }
            frame.executeJavaScript("logout();");
            frame.executeJavaScript("syncProfileSettings();");
        } else {
            ApplicationMain.getLogger().err("[CONNECTOR] REQUEST NOT RESOLVED: " + request);
        }
    }

    private void resolveCurseForgeRequest(String request) {
        if(request.startsWith("install.modpack.")) {
            Application.getFrame().getBrowser().loadURL(StringConverter.getURLString(Application.getURLBase()+"sub/installing.html"));
            String[] modpack = request.replace("install.modpack.", "").split("\\.", 2);
            int mID = Integer.parseInt(modpack[0]);
            String vID = modpack[1];
            try {
                int ver;
                if(vID.equalsIgnoreCase("all")) {
                    JsonArray array = new Gson().fromJson(ZCurseForgeIntegration.makeRequest("https://api.curseforge.com/v1/mods/" + mID + "/files"), JsonObject.class).get("data").getAsJsonArray();
                    ver = Integer.parseInt(array.get(0).getAsJsonObject().get("id").getAsString());
                } else {
                    ver = Integer.parseInt(ZCurseForgeIntegration.getVersionId(mID,vID));
                }
                ZCurseForgeIntegration integration = new ZCurseForgeIntegration(ApplicationMain.getLogger(), mID, ver);
                integration.install(ver);
            } catch (Exception e) {
                ApplicationMain.getLogger().err("[CONNECTOR] Couldn't install CurseForge modpack "+mID+" v"+vID+": "+e.getMessage());
            }
        } else if(request.startsWith("install.fabric.")||request.startsWith("install.neoforge.")||request.startsWith("install.quilt.")||request.startsWith("install.forge.")) {
            String modloader = "";
            if(request.startsWith("install.forge")) {
                modloader = "forge";
            } else if(request.startsWith("install.fabric")) {
                modloader = "fabric";
            } else if(request.startsWith("install.neoforge")) {
                modloader = "neoforge";
            } else if(request.startsWith("install.quilt")) {
                modloader = "quilt";
            }
            request = request.replace("install."+modloader+".","");
            String[] request_ = request.split("\\.", 3);
            String slug = request_[0];
            String id = request_[1];
            String version = request_[2];
            ApplicationMain.getLogger().deb("[CONNECTOR] Installing CurseForge mod "+slug+"...");
            try {
                JsonObject root;
                if(version.equalsIgnoreCase("all")) {
                    Gson gson = new Gson();
                    JsonArray array = gson.fromJson(ZCurseForgeIntegration.makeRequest("https://api.curseforge.com/v1/mods/"+slug+"/files?modLoaderType="+modloader),JsonObject.class).get("data").getAsJsonArray();
                    String vID = array.get(0).getAsJsonObject().get("id").getAsString();
                    root = new Gson().fromJson(ZCurseForgeIntegration.makeRequest("https://api.curseforge.com/v1/mods/"+slug+"/files/"+vID),JsonObject.class);
                } else {
                    root = new Gson().fromJson(ZCurseForgeIntegration.makeRequest("https://api.curseforge.com/v1/mods/"+slug+"/files/"+ZCurseForgeIntegration.getVersionId(Integer.parseInt(slug),version,NoFramework.ModLoader.valueOf(modloader.toUpperCase()))),JsonObject.class);
                }
                root = root.get("data").getAsJsonObject();
                String download = root.get("downloadUrl").getAsString();
                String fileName = "mods/"+root.get("fileName").getAsString();
                ApplicationMain.getLogger().deb("Created mods folder: "+new File(Application.getInstancePath() + "instances/" + id + "/" + fileName).getParentFile().mkdirs());
                FileGetter.downloadFile(download,Application.getInstancePath()+"instances/"+id+"/"+fileName);
                ApplicationMain.getLogger().deb("[CONNECTOR] Successfully installed CurseForge mod "+slug+"!");
                frame.executeJavaScript("setButton('"+slug+"','INSTALLED');");
            } catch (Exception e) {
                ApplicationMain.getLogger().err("[CONNECTOR] Failed to install CurseForge mod "+slug+": "+e.getMessage());
                frame.executeJavaScript("setButton('"+slug+"','FAILED');");
            }
        } else if(request.startsWith("install.shaders.")) {
            request = request.replace("install.shaders.","");
            String[] request_ = request.split("\\.", 3);
            String slug = request_[0];
            String id = request_[1];
            String version = request_[2];
            ApplicationMain.getLogger().deb("[CONNECTOR] Installing CurseForge shader pack "+slug+"...");
            try {
                JsonObject root;
                if(version.equalsIgnoreCase("all")) {
                    Gson gson = new Gson();
                    JsonArray array = gson.fromJson(ZCurseForgeIntegration.makeRequest("https://api.curseforge.com/v1/mods/"+slug+"/files"),JsonObject.class).get("data").getAsJsonArray();
                    String vID = array.get(0).getAsJsonObject().get("id").getAsString();
                    root = new Gson().fromJson(ZCurseForgeIntegration.makeRequest("https://api.curseforge.com/v1/mods/"+slug+"/files/"+vID),JsonObject.class);
                } else {
                    root = new Gson().fromJson(ZCurseForgeIntegration.makeRequest("https://api.curseforge.com/v1/mods/"+slug+"/files/"+ZCurseForgeIntegration.getVersionId(Integer.parseInt(slug),version)),JsonObject.class);
                }
                root = root.get("data").getAsJsonObject();
                String download = root.get("downloadUrl").getAsString();
                String fileName = "shaderpacks/"+root.get("fileName").getAsString();
                ApplicationMain.getLogger().deb("[CONNECTOR] Created shaderpacks folder: "+new File(Application.getInstancePath() + "instances/" + id + "/" + fileName).getParentFile().mkdirs());
                FileGetter.downloadFile(download,Application.getInstancePath()+"instances/"+id+"/"+fileName);
                ApplicationMain.getLogger().deb("[CONNECTOR] Successfully installed CurseForge shader pack "+slug+"!");
                frame.executeJavaScript("setButton('"+slug+"','INSTALLED');");
            } catch (Exception e) {
                ApplicationMain.getLogger().err("[CONNECTOR] Failed to install CurseForge shader pack "+slug+": "+e.getMessage());
                frame.executeJavaScript("setButton('"+slug+"','FAILED');");
            }
        } else if(request.startsWith("install.resourcepacks.")) {
            request = request.replace("install.resourcepacks.","");
            String[] request_ = request.split("\\.", 3);
            String slug = request_[0];
            String id = request_[1];
            String version = request_[2];
            ApplicationMain.getLogger().deb("[CONNECTOR] Installing CurseForge resource pack "+slug+"...");
            try {
                JsonObject root;
                if(version.equalsIgnoreCase("all")) {
                    Gson gson = new Gson();
                    JsonArray array = gson.fromJson(ZCurseForgeIntegration.makeRequest("https://api.curseforge.com/v1/mods/"+slug+"/files"),JsonObject.class).get("data").getAsJsonArray();
                    String vID = array.get(0).getAsJsonObject().get("id").getAsString();
                    root = new Gson().fromJson(ZCurseForgeIntegration.makeRequest("https://api.curseforge.com/v1/mods/"+slug+"/files/"+vID),JsonObject.class);
                } else {
                    root = new Gson().fromJson(ZCurseForgeIntegration.makeRequest("https://api.curseforge.com/v1/mods/"+slug+"/files/"+ZCurseForgeIntegration.getVersionId(Integer.parseInt(slug),version)),JsonObject.class);
                }
                root = root.get("data").getAsJsonObject();
                String download = root.get("downloadUrl").getAsString();
                String fileName = "resourcepacks/"+root.get("fileName").getAsString();
                ApplicationMain.getLogger().deb("[CONNECTOR] Created resourcepacks folder: "+new File(Application.getInstancePath() + "instances/" + id + "/" + fileName).getParentFile().mkdirs());
                FileGetter.downloadFile(download,Application.getInstancePath()+"instances/"+id+"/"+fileName);
                ApplicationMain.getLogger().deb("[CONNECTOR] Successfully installed CurseForge resource pack "+slug+"!");
                frame.executeJavaScript("setButton('"+slug+"','INSTALLED');");
            } catch (Exception e) {
                ApplicationMain.getLogger().err("[CONNECTOR] Failed to install CurseForge resource pack "+slug+": "+e.getMessage());
                frame.executeJavaScript("setButton('"+slug+"','FAILED');");
            }
        } else {
            resolveRequest("not-resolved");
        }
    }

    private void thridPartyConfirm(String text, String button, String continueRequest) {
        if(Application.thirdPartyWarn) {
            if (text == null) {
                text = "<h3>This is a third party resource!</h3><p>Nerofy assumes no liability for any problems or damage caused by third-party resources. We also do not offer help for third-party resources.</p>";
            }
            if (continueRequest == null) {
                continueRequest = "unmessage();";
            }
            if (button == null) {
                button = "<h1><a onclick=\\\"" + continueRequest + "; unmessage();\\\" class='button'>Continue</a> <a onclick=\\\"link('instances.html');\\\" class='button'>Return</a></h1><a onclick=\\\"callJavaMethod('button.disable.warn.thirdparty'); "+continueRequest+";\\\" class='button'>I know the risk and want to continue. Do not show this message again.</a>";
            }
            String command = "message(\"<h1>Warning:</h1><br>" + text + "<br>" + button + "\");";
            frame.executeJavaScript(command);
        } else {
            frame.executeJavaScript(continueRequest);
        }
    }

    private void resolveModrinthRequest(String request) {
        if(request.startsWith("install.modpack.")) {
            Application.getFrame().getBrowser().loadURL(StringConverter.getURLString(Application.getURLBase()+"sub/installing.html"));
            String[] modpack = request.replace("install.modpack.", "").split("\\.", 2);
            String mID = modpack[0];
            String vID = modpack[1];
            try {
                JsonElement e;
                if(vID.equalsIgnoreCase("all")) {
                    e = new ReadableJsonStorage("https://api.modrinth.com/v2/project/" + mID + "/version").getJson().getAsJsonArray().get(0);
                } else {
                    e = new ReadableJsonStorage("https://api.modrinth.com/v2/project/" + mID + "/version?game_versions=[%22" + vID + "%22]").getJson().getAsJsonArray().get(0);
                }
                String v = e.getAsJsonObject().get("version_number").getAsString();
                String v_ = e.getAsJsonObject().get("id").getAsString();
                ZModrinthIntegration integration = new ZModrinthIntegration(ApplicationMain.getLogger(), mID, v_);
                integration.install(v);
            } catch (Exception e) {
                ApplicationMain.getLogger().err("[CONNECTOR] Couldn't install modrinth modpack "+mID+" v"+vID+": "+e.getMessage());
            }
        } else if(request.startsWith("install.fabric.")||request.startsWith("install.forge.")||request.startsWith("install.neoforge.")||request.startsWith("install.quilt.")) {
            String modloader = "";
            if(request.startsWith("install.forge")) {
                modloader = "forge";
            } else if(request.startsWith("install.fabric")) {
                modloader = "fabric";
            } else if(request.startsWith("install.neoforge")) {
                modloader = "neoforge";
            } else if(request.startsWith("install.quilt")) {
                modloader = "quilt";
            }
            request = request.replace("install."+modloader+".","");
            String[] request_ = request.split("\\.", 3);
            String slug = request_[0];
            String id = request_[1];
            String version = request_[2];
            ApplicationMain.getLogger().deb("[CONNECTOR] Installing modrinth mod "+slug+"...");
            try {
                String url;
                if(version.equalsIgnoreCase("all")) {
                    url = "https://api.modrinth.com/v2/project/"+slug+"/version?loaders=[%22"+modloader+"%22]";
                } else {
                    url = "https://api.modrinth.com/v2/project/"+slug+"/version?game_versions=[%22"+version+"%22]&loaders=[%22"+modloader+"%22]";
                }
                JsonObject json = new ReadableJsonStorage(url).getJson().getAsJsonArray().get(0).getAsJsonObject();
                version = json.get("version_number").getAsString();
                String download = json.get("files").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();
                String fileName = "mods/"+slug+"-"+version+".jar";
                ApplicationMain.getLogger().deb("[CONNECTOR] Created mods folder: "+new File(Application.getInstancePath() + "instances/" + id + "/" + fileName).getParentFile().mkdirs());
                FileGetter.downloadFile(download,Application.getInstancePath()+"instances/"+id+"/"+fileName);
                ApplicationMain.getLogger().deb("[CONNECTOR] Successfully installed modrinth mod "+slug+"!");
                frame.executeJavaScript("setButton('"+slug+"','INSTALLED');");
            } catch (Exception e) {
                ApplicationMain.getLogger().err("[CONNECTOR] Failed to install modrinth mod "+slug+": "+e.getMessage());
                frame.executeJavaScript("setButton('"+slug+"','FAILED');");
            }
        } else if(request.startsWith("install.shaders.")) {
            request = request.replace("install.shaders.","");
            String[] request_ = request.split("\\.", 3);
            String slug = request_[0];
            String id = request_[1];
            String version = request_[2].replace("all","");
            ApplicationMain.getLogger().deb("[CONNECTOR] Installing modrinth shader pack "+slug+"...");
            try {
                String url;
                if(version.equalsIgnoreCase("all")) {
                    url = "https://api.modrinth.com/v2/project/"+slug+"/version";
                } else {
                    url = "https://api.modrinth.com/v2/project/"+slug+"/version?game_versions=[%22"+version+"%22]";
                }
                JsonObject json = new ReadableJsonStorage(url).getJson().getAsJsonArray().get(0).getAsJsonObject();
                version = json.get("version_number").getAsString();
                String download = json.get("files").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();
                String fileName = "shaderpacks/"+slug+"-"+version+".zip";
                ApplicationMain.getLogger().deb("[CONNECTOR] Created shaderpacks folder: "+new File(Application.getInstancePath() + "instances/" + id + "/" + fileName).getParentFile().mkdirs());
                FileGetter.downloadFile(download,Application.getInstancePath()+"instances/"+id+"/"+fileName);
                ApplicationMain.getLogger().deb("[CONNECTOR] Successfully installed modrinth shader pack "+slug+"!");
                frame.executeJavaScript("setButton('"+slug+"','INSTALLED');");
            } catch (Exception e) {
                ApplicationMain.getLogger().err("[CONNECTOR] Failed to install modrinth shader pack "+slug+": "+e.getMessage());
                frame.executeJavaScript("setButton('"+slug+"','FAILED');");
            }
        } else if(request.startsWith("install.resourcepacks.")) {
            request = request.replace("install.resourcepacks.","");
            String[] request_ = request.split("\\.", 3);
            String slug = request_[0];
            String id = request_[1];
            String version = request_[2].replace("all","");
            ApplicationMain.getLogger().deb("[CONNECTOR] Installing modrinth resource pack "+slug+"...");
            try {
                String url;
                if(version.equalsIgnoreCase("all")) {
                    url = "https://api.modrinth.com/v2/project/"+slug+"/version";
                } else {
                    url = "https://api.modrinth.com/v2/project/"+slug+"/version?game_versions=[%22"+version+"%22]";
                }
                JsonObject json = new ReadableJsonStorage(url).getJson().getAsJsonArray().get(0).getAsJsonObject();
                version = json.get("version_number").getAsString();
                String download = json.get("files").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();
                String fileName = "resourcepacks/"+slug+"-"+version+".zip";
                ApplicationMain.getLogger().deb("[CONNECTOR] Created resourcepacks folder: "+new File(Application.getInstancePath() + "instances/" + id + "/" + fileName).getParentFile().mkdirs());
                FileGetter.downloadFile(download,Application.getInstancePath()+"instances/"+id+"/"+fileName);
                ApplicationMain.getLogger().deb("[CONNECTOR] Successfully installed modrinth resource pack "+slug+"!");
                frame.executeJavaScript("setButton('"+slug+"','INSTALLED');");
            } catch (Exception e) {
                ApplicationMain.getLogger().err("[CONNECTOR] Failed to install modrinth resource pack "+slug+": "+e.getMessage());
                frame.executeJavaScript("setButton('"+slug+"','FAILED');");
            }
        } else {
            resolveRequest("not-resolved");
        }
    }

    private static JFileChooser getJDirectoryChooser() {
        JFileChooser chooser;
        try {
            chooser = new JFileChooser(Application.getInstancePath());
        } catch (Exception ignore) {
            chooser = new JFileChooser(ApplicationMain.getDirectoryPath());
        }
        chooser.setDialogTitle("Select instances installation path");
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter standardFilter = new FileNameExtensionFilter("Folders only", "*.*");
        chooser.addChoosableFileFilter(standardFilter);
        return chooser;
    }

    public void syncTheme(String theme) {
        if(theme.equalsIgnoreCase("dark")||theme.equalsIgnoreCase("light")||theme.equalsIgnoreCase("zyneon")||theme.equalsIgnoreCase("default")) {
            frame.executeJavaScript("setTheme('default."+theme.toLowerCase().replace("default","dark")+"');");
        } else {
            File themes = new File(ApplicationMain.getDirectoryPath()+"themes/");
            ApplicationMain.getLogger().deb("[CONNECTOR] Created themes folder: "+themes.mkdirs());
        }
    }

    public void resolveInstanceRequest(InstanceAction action, String instance) {
        switch (action) {
            case RUN -> runInstance(instance);
            case OPEN_FOLDER -> openInstanceFolder(instance);
            case SHOW_SCREENSHOTS -> openScreenshotsFolder(instance);
            case SHOW_MODS -> openModsFolder(instance);
            case SHOW_RESOURCEPACKS -> openResourcePacksFolder(instance);
            case SHOW_WORLDS -> openWorldsFolder(instance);
            case SHOW_SHADERS -> openShadersFolder(instance);
            case SETTINGS_MEMORY -> openMemorySettings(instance);
            case SHOW_ICON -> openIcon(instance);
            case SHOW_LOGO -> openLogo(instance);
            case SHOW_BACKGROUND -> openBackground(instance);
        }
    }

    public void runInstance(String instanceString) {
        if(Application.auth!=null) {
            if (!Application.auth.isLoggedIn()) {
                frame.getBrowser().loadURL(Application.getSettingsURL()+"?tab=profile");
                return;
            }
        } else {
            frame.getBrowser().loadURL(Application.getSettingsURL()+"?tab=profile");
            return;
        }
        if (instanceString.startsWith("official/")) {
            File instanceJson;
            if (new File(Application.getInstancePath() + "instances/" + instanceString + "/zyneonInstance.json").exists()) {
                instanceJson = new File(Application.getInstancePath() + "instances/" + instanceString + "/zyneonInstance.json");
            } else {
                ApplicationMain.getLogger().deb("[CONNECTOR] Created instance path: " + new File(Application.getInstancePath() + "instances/" + instanceString + "/").mkdirs());
                String s = "https://raw.githubusercontent.com/danieldieeins/ZyneonApplicationContent/main/m/" + instanceString + ".json";
                instanceJson = FileGetter.downloadFile(s, Application.getInstancePath() + "instances/" + instanceString + "/zyneonInstance.json");
            }
            launch(new WritableInstance(instanceJson));
        } else {
            File file = new File(Application.getInstancePath() + "instances/" + instanceString + "/zyneonInstance.json");
            if (file.exists()) {
                launch(new WritableInstance(file));
            }
        }
        System.gc();
    }

    private void launch(WritableInstance instance) {
        if(instance.getModloader().equalsIgnoreCase("fabric")) {
            new FabricLauncher().launch(instance);
        } else if(instance.getModloader().equalsIgnoreCase("quilt")) {
            new QuiltLauncher().launch(instance);
        } else if(instance.getModloader().equalsIgnoreCase("forge")) {
            new ForgeLauncher().launch(instance);
        } else if(instance.getModloader().equalsIgnoreCase("neoforge")) {
            new NeoForgeLauncher().launch(instance);
        } else {
            new VanillaLauncher().launch(instance);
        }
    }

    public void openInstanceFolder(String instance) {
        if (instance == null) {
            instance = "";
        }
        File folder;
        if (instance.isEmpty()) {
            folder = new File(Application.getInstancePath() + "instances/");
        } else {
            folder = new File(Application.getInstancePath() + "instances/" + instance + "/");
        }
        createIfNotExist(folder);
    }

    private void openModsFolder(String instance) {
        File folder = new File(Application.getInstancePath() + "instances/" + instance + "/mods/");
        createIfNotExist(folder);
    }

    private void openIcon(String id) {
        ReadableInstance instance = new ReadableInstance(Application.getInstancePath() + "instances/" + id + "/zyneonInstance.json");
        if (instance.getIconUrl() != null) {
            File png = new File(URLDecoder.decode(instance.getIconUrl(), StandardCharsets.UTF_8));
            if (png.exists()) {
                createIfNotExist(png);
            }
        }
    }

    private void openLogo(String id) {
        ReadableInstance instance = new ReadableInstance(Application.getInstancePath() + "instances/" + id + "/zyneonInstance.json");
        if (instance.getLogoUrl() != null) {
            File png = new File(URLDecoder.decode(instance.getLogoUrl(), StandardCharsets.UTF_8));
            if (png.exists()) {
                createIfNotExist(png);
            }
        }
    }

    private void openBackground(String id) {
        ReadableInstance instance = new ReadableInstance(Application.getInstancePath() + "instances/" + id + "/zyneonInstance.json");
        if (instance.getBackgroundUrl() != null) {
            File png = new File(URLDecoder.decode(instance.getBackgroundUrl(), StandardCharsets.UTF_8));
            if (png.exists()) {
                createIfNotExist(png);
            }
        }
    }

    private void openScreenshotsFolder(String instance) {
        File folder = new File(Application.getInstancePath() + "instances/" + instance + "/screenshots/");
        createIfNotExist(folder);
    }

    private void openResourcePacksFolder(String instance) {
        File folder = new File(Application.getInstancePath() + "instances/" + instance + "/resourcepacks/");
        createIfNotExist(folder);
    }

    private void openShadersFolder(String instance) {
        File folder = new File(Application.getInstancePath() + "instances/" + instance + "/shaderpacks/");
        createIfNotExist(folder);
    }

    private void openWorldsFolder(String instance) {
        File folder = new File(Application.getInstancePath() + "instances/" + instance + "/saves/");
        createIfNotExist(folder);
    }

    private void createIfNotExist(File folder) {
        ApplicationMain.getLogger().deb("[CONNECTOR] Created instance path: " + folder.mkdirs());
        if (folder.exists()) {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    try {
                        desktop.open(folder);
                    } catch (Exception ignore) {
                    }
                }
            }
        }
    }

    private void openMemorySettings(String instance) {
        String title = "Configure memory (" + instance + ")";
        if(instance.equalsIgnoreCase("default")) {
            new MemoryFrame(Application.config, title, "default");
        } else {
            new MemoryFrame(new ReadableInstance(Application.getInstancePath() + "instances/" + instance + "/zyneonInstance.json").getSettings(), title, instance);
        }
    }

    public enum InstanceAction {
        RUN,
        OPEN_FOLDER,
        SHOW_SCREENSHOTS,
        SHOW_MODS,
        SHOW_RESOURCEPACKS,
        SHOW_SHADERS,
        SHOW_WORLDS,
        SETTINGS_MEMORY,
        SHOW_ICON,
        SHOW_LOGO,
        SHOW_BACKGROUND
    }
}