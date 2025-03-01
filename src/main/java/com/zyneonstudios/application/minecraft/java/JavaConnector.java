package com.zyneonstudios.application.minecraft.java;

import com.sun.management.OperatingSystemMXBean;
import com.zyneonstudios.application.MinecraftJavaAddon;
import com.zyneonstudios.application.frame.FrameConnector;
import com.zyneonstudios.application.frame.web.ApplicationFrame;
import com.zyneonstudios.application.main.ApplicationStorage;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.integrations.curseforge.CurseForgeIntegration;
import com.zyneonstudios.application.minecraft.java.integrations.modrinth.ModrinthIntegration;
import com.zyneonstudios.application.minecraft.java.integrations.zyndex.LocalInstance;
import com.zyneonstudios.application.minecraft.java.integrations.zyndex.ZyndexIntegration;
import com.zyneonstudios.application.minecraft.java.launchers.InstanceLauncher;
import com.zyneonstudios.application.modules.ModuleConnector;
import com.zyneonstudios.nexus.desktop.NexusDesktop;
import com.zyneonstudios.nexus.index.ReadableZyndex;
import com.zyneonstudios.nexus.instance.Instance;
import com.zyneonstudios.nexus.instance.ReadableZynstance;
import com.zyneonstudios.nexus.instance.ZynstanceBuilder;
import com.zyneonstudios.nexus.utilities.NexusUtilities;
import com.zyneonstudios.nexus.utilities.file.FileActions;
import com.zyneonstudios.nexus.utilities.strings.StringConverter;
import com.zyneonstudios.nexus.utilities.strings.StringGenerator;
import com.zyneonstudios.verget.Verget;
import com.zyneonstudios.verget.minecraft.MinecraftVerget;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class JavaConnector extends ModuleConnector {

    private final MinecraftJavaAddon module;
    private final ApplicationFrame frame;
    private final byte[] authKey;

    public JavaConnector(MinecraftJavaAddon module, byte[] authKey) {
        super(module);
        this.authKey = authKey;
        this.module = module;
        this.frame = (ApplicationFrame) this.module.getApplication().getFrame();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void resolveRequest(String request) {
        if (request.startsWith("sync.discover.search.nexus-minecraft-module_java.")) {
            resolveSearchRequest(request.replaceFirst("sync.discover.search.nexus-minecraft-module_java.", ""));
        } else if (request.startsWith("java.auth.")) {
            resolveAuthRequest(request.replaceFirst("java.auth.", ""));
        } else if (request.equals("sync.library.module.nexus-minecraft-module_java") || request.equals("sync.library.module.minecraft-java-edition")) {
            resolveInitRequest("library");
        } else if (request.startsWith("java.settings.")) {
            resolveSettingsRequest(request.replaceFirst("java.settings.", ""));
        } else if (request.startsWith("java.overlay")) {
            frame.executeJavaScript("enableOverlay('https://www.zyneonstudios.com');");
        } else if (request.startsWith("java.init.")) {
            resolveInitRequest(request.replaceFirst("java.init.", ""));
        } else if (request.startsWith("java.button.")) {
            resolveButtonRequest(request.replaceFirst("java.button.", ""));
        } else if (request.startsWith("java.sync.")) {
            resolveSyncRequest(request.replaceFirst("java.sync.", ""));
        } else if (request.startsWith("java.open.")) {
            resolveOpenRequest(request.replaceFirst("java.open.", ""));
        } else if (request.startsWith("java.searchFilter.")) {
            resolveSearchFilter(request.replaceFirst("java.searchFilter.", ""));
        } else if (request.startsWith("java.run.")) {
            resolveRunRequest(request.replaceFirst("java.run.", ""));
        } else if (request.equals("init.settings.modules")) {
            resolveInitRequest("settings.modules");
        } else if (request.startsWith("sync.library.module.nexus-minecraft-module_java.")) {
            resolveSyncRequest(request.replaceFirst("sync.library.module.nexus-minecraft-module_java.", ""));
        } else if (request.startsWith("sync.button.library.menu.group.mje-instances.")) {
            request = request.replaceFirst("sync.button.library.menu.group.mje-instances.", "");
            resolveButtonRequest("view." + request);
        } else if (request.equals("refresh.library")) {
            JavaStorage.reloadLocalZyndex();
            frame.getBrowser().reload();
        } else if (request.startsWith("java.install.")) {
            resolveInstallRequest(request.replaceFirst("java.install.",""));
        } else if (request.equals("init.library")) {
            frame.executeJavaScript("addModuleToList('Minecraft: Java Edition','" + module.getId() + "_java" + "');");
        } else if (request.equals("init.discover")) {
            frame.executeJavaScript("addModuleToList('Minecraft: Java Edition','" + module.getId() + "_java" + "');");
        } else if (request.startsWith("sync.language.")) {
            ApplicationStorage.language = request.replaceFirst("sync.language.", "");
            JavaStorage.init(module.getId());
        } else if (request.startsWith("sync.settings.")) {
            syncSettings(request.replaceFirst("sync.settings.", ""));
        }
    }

    private void resolveInstallRequest(String request) {
        String[] info = request.split("\\.",2);
        String type = info[0];
        String slugId = info[1];
        if (type.equals("nexInstance")) {
            ZyndexIntegration.install(new ReadableZyndex("https://raw.githubusercontent.com/zyneonstudios/nexus-nex/main/zyndex/index.json").getInstancesById().get(slugId), JavaStorage.getFallbackInstancePath() + slugId.replace("\\", "/").replace("/", "-") + "/");
        }
    }

    private void syncSettings(String request) {
        if (request.equals("init")) {
            String group = "addSettingsGroup(\"Minecraft: Java Edition\",\"global\",\"global-mje\");";
            String ram = "addValueToGroup('Memory (RAM)','global-mje','mje-global-memory','java.settings.global.memory','" + JavaStorage.map.getInteger("settings.global.memory") + "MB');";
            String installPath = "addValueToGroup(\"Instance installation path\",\"global-mje\",\"mje-global-path\",\"java.settings.global.path\",\""+JavaStorage.getFallbackInstancePath()+"\");";
            String minimize = "addToggleToGroup('Minimize app on game start','global-mje','mje-global-minimize','java.settings.global.minimize'," + JavaStorage.map.getBoolean("settings.global.minimizeApp") + ");";
            frame.executeJavaScript(group + ram + installPath + minimize);
        }
    }

    public void resolveButtonRequest(String request) {
        if (request.startsWith("launch.")) {
            request = request.replaceFirst("launch.", "");
            LocalInstance instance = JavaStorage.getLocalZyndex().getLocalInstancesById().get(request);
            new InstanceLauncher(instance, module.getAuthenticator(authKey).getAuthInfos(), frame).launch();
        } else if (request.startsWith("view.")) {
            request = request.replaceFirst("view.", "");
            frame.executeJavaScript("showView(\"" + request + "\");");
        }
    }

    public void resolveInitRequest(String request) {
        if (request.equals("library")) {
            if (module.getAuthState() != MinecraftJavaAddon.AuthState.LOGGED_IN) {
                frame.openCustomPage("Minecraft: Java Edition Authentication...", "mje-authentication", JavaStorage.getUrlBase() + "mje-login.html");
                return;
            }
            String name = module.getAuthenticator(authKey).getAuthInfos().getUsername();
            frame.executeJavaScript("setMenuPanel(\"https://cravatar.eu/helmhead/" + name + "/64.png\",\"" + name + "\",\"<a onclick='connector(`java.auth.logout`);'>Logout</a>\",true);");

            frame.executeJavaScript("addAction('" + JavaStorage.Strings.addInstance + "','bx bx-plus','connector(\\'java.init.instances.creator\\');','mje-add-instance'); addAction('" + JavaStorage.Strings.refreshInstances + "','bx bx-refresh',\"connector('refresh.library');\",'mje-refresh-instances'); addGroup('" + JavaStorage.Strings.instances + "','mje-instances');");
            frame.executeJavaScript("document.getElementById(\"select-game-module\").value = 'nexus-minecraft-module_java';");

            List<LocalInstance> instances = JavaStorage.getLocalZyndex().getLocalInstances();
            instances.sort(Comparator.comparing(Instance::getName));

            for (LocalInstance instance : instances) {
                try {
                    String title = instance.getName().replace("\"", "''");
                    String id = instance.getId().replace("\"", "");
                    String image = "";
                    if (instance.getIconUrl() != null) {
                        image = instance.getIconUrl().replace("\"", "'");
                    }
                    frame.executeJavaScript("addGroupEntry(\"mje-instances\",\"" + title + "\",\"" + id + "\",\"" + image + "\");");
                } catch (Exception e) {
                    NexusApplication.getLogger().err("[Minecraft] Couldn't index instance: " + e.getMessage());
                }
            }

            if (JavaStorage.getLastInstance() != null) {
                if(JavaStorage.getLocalZyndex().getLocalInstancesById().containsKey(JavaStorage.getLastInstance())) {
                    frame.executeJavaScript("showView(\"" + JavaStorage.getLastInstance() + "\");");
                }
            }
        } else if (request.startsWith("creator.")) {
            request = request.replaceFirst("creator.", "");

            if(request.startsWith("create.")) {
                String[] creator = request.replaceFirst("create.","").split("\\.",6);
                createInstance(creator[0].replace("%dot%","."),creator[1].replace("%dot%","."),creator[2].replace("%dot%","."),creator[3].replace("%dot%","."),creator[4].replace("%dot%","."),creator[5].replace("%dot%","."));
            } else if(request.startsWith("gameVersion.")) {
                request = request.replaceFirst("gameVersion.","");
                ArrayList<String> versions = switch (request) {
                    case "fabric" -> Verget.getFabricGameVersions(true);
                    case "forge" -> Verget.getForgeGameVersions();
                    case "neoforge" -> Verget.getNeoForgeGameVersions();
                    case "quilt" -> Verget.getQuiltGameVersions(true);
                    case "experimental" -> Verget.getMinecraftVersions(MinecraftVerget.Filter.EXPERIMENTAL);
                    case "vanilla" -> Verget.getMinecraftVersions(MinecraftVerget.Filter.RELEASES);
                    default -> Verget.getMinecraftVersions(MinecraftVerget.Filter.BOTH);
                };
                StringBuilder list = new StringBuilder();
                for(String version : versions) {
                    String option = "<option value='"+version+"'>"+version+"</option>";
                    list.append(option);
                }
                String gameVersions = "document.getElementById(\"mje-settings-game-version\").innerHTML = \""+list+"\";";

                ArrayList<String> modloader = switch (request) {
                    case "fabric" -> Verget.getFabricVersions(true,versions.getFirst());
                    case "forge" -> Verget.getForgeVersions(versions.getFirst());
                    case "neoforge" -> Verget.getNeoForgeVersions(versions.getFirst());
                    case "quilt" -> Verget.getQuiltVersions(versions.getFirst());
                    default -> new ArrayList<>();
                };
                String modloaderVersions = "";
                if(!modloader.isEmpty()) {
                    StringBuilder modloaderList = new StringBuilder();
                    for(String version : modloader) {
                        String option = "<option value='"+version+"'>"+version+"</option>";
                        modloaderList.append(option);
                    }
                    modloaderVersions = "document.getElementById(\"mje-settings-modloader-version\").innerHTML = \""+modloaderList+"\";";
                }

                frame.executeJavaScript(gameVersions,modloaderVersions);
            } else if(request.startsWith("modloaderVersion.")) {
                request = request.replaceFirst("modloaderVersion.","");
                String version = request.split("\\.",2)[1];
                request = request.split("\\.",2)[0];

                ArrayList<String> modloader = switch (request) {
                    case "fabric" -> Verget.getFabricVersions(true,version);
                    case "forge" -> Verget.getForgeVersions(version);
                    case "neoforge" -> Verget.getNeoForgeVersions(version);
                    case "quilt" -> Verget.getQuiltVersions(version);
                    default -> new ArrayList<>();
                };
                String modloaderVersions = "";
                if(!modloader.isEmpty()) {
                    StringBuilder modloaderList = new StringBuilder();
                    for(String v : modloader) {
                        String option = "<option value='"+v+"'>"+v+"</option>";
                        modloaderList.append(option);
                    }
                    modloaderVersions = "document.getElementById(\"mje-settings-modloader-version\").innerHTML = \""+modloaderList+"\";";
                }

                frame.executeJavaScript(modloaderVersions);
            }
        } else if (request.startsWith("details.")) {
            request = request.replaceFirst("details.", "");
            if (request.startsWith("instance.")) {
                request = URLDecoder.decode(request.replaceFirst("instance.", ""), StandardCharsets.UTF_8);
                ReadableZynstance instance = new ReadableZynstance(request);
                frame.executeJavaScript("enableOverlay(\"" + FrameConnector.initDetails(instance.getName(), instance.getId(), "Minecraft: Java Edition instance", instance.getVersion(), instance.getDescription(), instance.getAuthor(), instance.isHidden(), "No tags", instance.getDescription(), "No changelogs", "No version history", "", "", instance.getBackgroundUrl(), instance.getIconUrl(), instance.getLogoUrl(), instance.getThumbnailUrl()).replace("%plus%", "+") + "\");");
            }
        } else if (request.startsWith("auth")) {
            frame.executeJavaScript("document.getElementById('library-button').classList.add('highlighted');");
            if (module.getAuthState() != MinecraftJavaAddon.AuthState.LOGGED_IN && module.getAuthState() != MinecraftJavaAddon.AuthState.LOGGING_IN) {
                frame.openCustomPage("Minecraft: Java Edition Login", "mje-authentication", JavaStorage.getUrlBase() + "mje-login.html?enable=true");
            } else if (module.getAuthState().equals(MinecraftJavaAddon.AuthState.LOGGED_IN)) {
                resolveSyncRequest("library");
            }
        } else if (request.equals("zyndex")) {
            JavaStorage.reloadLocalZyndex();
        } else if (request.equals("mje-settings")) {
            String settings = "file://" + JavaStorage.getUrlBase().replace("\\", "/") + "mje-settings.html?n=" + module.getName() + "&v=" + module.getVersion() + "&a=" + module.getAuthors();
            frame.executeJavaScript("setContent('settings-custom','minecraft.java-edition','" + settings + "');");
        } else if (request.equals("settings.modules")) {
            frame.executeJavaScript("addGroup(\"Minecraft\",\"mc-general\"); addModuleSetting('bx bx-cube',\"" + JavaStorage.Strings.aboutMinecraftModule + "\",'java.init.mje-settings','minecraft-about',false,'mc-general');");
        } else if (request.startsWith("library.")) {
            request = request.replaceFirst("library.", "");
            if (request.equals("select")) {
                frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/library.html?moduleId=-1");
            }
        } else if (request.startsWith("instances.")) {
            request = request.replaceFirst("instances.", "");
            if (request.equals("creator")) {
                frame.getBrowser().loadURL("file://" + JavaStorage.getUrlBase() + "mje-creator.html");
            }
        }
    }

    private void createInstance(String name, String version, String description, String type, String gameVersion, String modloaderVersion) {
        String id = name.toLowerCase().replace(" ","_").replaceAll("[^a-z0-9_-]", "");
        if(new File(JavaStorage.getFallbackInstancePath()+id+"/").exists()) {
            id = id+"_"+StringGenerator.generateAlphanumericString(8);
        }
        ZynstanceBuilder builder = new ZynstanceBuilder(JavaStorage.getFallbackInstancePath()+id+"/zyneonInstance.json");
        builder.setId(id);
        builder.setName(name);
        builder.setVersion(version);
        builder.setSummary(description);
        switch (type) {
            case "fabric" -> builder.setFabricVersion(modloaderVersion);
            case "forge" -> builder.setForgeVersion(modloaderVersion);
            case "neoforge" -> builder.setNeoForgeVersion(modloaderVersion);
            case "quilt" -> builder.setQuiltVersion(modloaderVersion);
        }
        builder.setMinecraftVersion(gameVersion);
        String author = "Unknown";
        if(module.getAuthState().equals(MinecraftJavaAddon.AuthState.LOGGED_IN)) {
            try {
                if (module.getAuthenticator(authKey).getAuthInfos().getUsername() != null) {
                    author = module.getAuthenticator(authKey).getAuthInfos().getUsername();
                }
            } catch (Exception ignore) {}
        }
        ArrayList<String> authors = new ArrayList<>();
        authors.add(author);
        builder.setAuthors(authors);
        builder.create();
        JavaStorage.reloadLocalZyndex();
        frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/library.html?moduleId="+module.getId()+"_java&viewId="+id);
    }

    public void resolveSyncRequest(String request) {
        if (request.startsWith("library.")) {
            request = request.replaceFirst("library.", "");
            if (request.equals("add")) {
                frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/library.html?moduleId=-1");
            } else {
                frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/library.html?moduleId=" + request);
            }
        } else if (request.startsWith("view-settings.")) {
            frame.executeJavaScript("document.getElementById('mje-cog').onclick = function() { location.reload(); };");
            showInstanceSettings(JavaStorage.getLocalZyndex().getLocalInstancesById().get(request.replaceFirst("view-settings.", "")));
        } else if (request.startsWith("view.")) {
            showInstance(JavaStorage.getLocalZyndex().getLocalInstancesById().get(request.replaceFirst("view.", "")));
        } else if (request.equals("zyndex")) {
            JavaStorage.reloadLocalZyndex();
        } else if (request.equals("search")) {
            frame.executeJavaScript("location.href='discover.html?l=search';");
        } else if (request.equals("mje-settings")) {
            frame.executeJavaScript("setTitle('Minecraft: Java Edition'); highlight(document.getElementById('minecraft-about'));");
        } else if (request.equals("library")) {
            frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/library.html");
        } else if (request.startsWith("discoverHover.")) {
            request = request.replaceFirst("discoverHover.", "");
            if (request.equals("on")) {
                frame.executeJavaScript("if(!document.getElementById('discover-button').classList.contains('teased')) { document.getElementById('discover-button').classList.add('teased'); }");
            } else {
                frame.executeJavaScript("if(document.getElementById('discover-button').classList.contains('teased')) { document.getElementById('discover-button').classList.remove('teased'); }");
            }
        } else if (request.equals("creator")) {
            frame.executeJavaScript("document.getElementById('library-button').classList.add('highlighted'); document.getElementById('library-button').onclick = '';");
        } else if (request.equals("home")) {
            frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/start.html");
        } else if (request.equals("discover")) {
            frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/discover.html");
        } else if (request.equals("settings")) {
            frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/settings.html");
        } else if (request.startsWith("instances.")) {
            resolveInstanceSync(request.replaceFirst("instances.", ""));
        } else if (request.equals("instances")) {

        }
    }

    private void showInstance(LocalInstance instance) {
        String icon = "";
        String logo = "";
        String background = "";
        if(instance.getIconUrl()!=null) {
            icon = instance.getIconUrl();
        }
        if(instance.getLogoUrl()!=null) {
            logo = instance.getLogoUrl();
        }
        if(instance.getBackgroundUrl()!=null) {
            background = instance.getBackgroundUrl();
        }
        String command = "document.querySelector('.cnt').style.backgroundImage = \"url('"+background+"')\"; setOverlayContent(\"<div id='library-overlay-loader'><h3>Loading... <i class='bx bx-loader-alt bx-spin' ></i></h3></div>\"); setViewImage('"+logo+"'); setTitle(\""+icon+"\",\""+instance.getName().replace("\"","''")+"\",\"<div class='flex'><h3 id='mje-folder' style='margin-right: 0.25rem !important;'><i class='bx bxs-folder-open'></i></h3><h3 id='mje-cog'><i class='bx bxs-cog'></i></h3></div>\"); setLaunch('LAUNCH','bx bx-rocket','active','async.java.button.launch."+instance.getId()+"'); enableLaunch(); setViewDescription(\""+instance.getSummary().replace("\"","''").replace("<","‹").replace(">","›").replace("\n","<br>")+"\"); document.getElementById('mje-cog').onclick = function() { toggleOverlay('mje-cog'); connector('java.sync.view-settings."+instance.getId()+"'); }; document.getElementById('mje-folder').onclick = function() { connector('java.settings."+instance.getId()+".folder'); };";

        frame.executeJavaScript(command);
        JavaStorage.getConfig().set("settings.values.last.instance",instance.getId());

        if(JavaStorage.runningInstances.containsKey(instance.getId())) {
            frame.executeJavaScript("setLaunch(\"RUNNING\",\"bx bx-check-circle\",\"active hover-wiggle\",\"async.java.button.launch."+instance.getId()+"\");");
        }
    }

    private void showInstanceSettings(LocalInstance instance) {
        String[] modList = instance.getModList();
        String modLoader = instance.getModloader().toLowerCase();
        String minecraftVersion = instance.getMinecraftVersion();
        String uuid = UUID.randomUUID().toString();
        String id = instance.getId().replace(".","%dot%");
        String settingsBase = "<div class='option-group'><h4 class='option'>Game type and version</h4>%</div>";
        String settingsGameType = "<h3 class='option'>Game type (modloader) <label><select id='"+uuid+"-type' onchange='connector(`java.settings."+id+".type."+uuid+".`+this.value);'><option value='vanilla'>Vanilla</option><option value='experimental'>Experimental</option><option value='fabric'>Fabric</option><option value='forge'>Forge</option><option value='neoforge'>NeoForge</option><option value='quilt'>Quilt</option></select></label></h3>";
        ArrayList<String> gameVersions = new ArrayList<>();
        try {
            gameVersions = switch (modLoader) {
                case "fabric" -> Verget.getFabricGameVersions(true);
                case "forge" -> Verget.getForgeGameVersions();
                case "neoforge" -> Verget.getNeoForgeGameVersions();
                case "quilt" -> Verget.getQuiltGameVersions(true);
                case "experimental" -> Verget.getMinecraftVersions(MinecraftVerget.Filter.EXPERIMENTAL);
                case "vanilla" -> Verget.getMinecraftVersions(MinecraftVerget.Filter.RELEASES);
                default -> Verget.getMinecraftVersions(MinecraftVerget.Filter.BOTH);
            };
        } catch (Exception e) {
            NexusApplication.getLogger().err("[Minecraft] (Connector) Couldn't fetch available versions for minecraft: "+e.getMessage());
        }
        StringBuilder settingsGameVersions = new StringBuilder();
        for(String version:gameVersions) {
            settingsGameVersions.append("<option value='").append(version).append("'>").append(version).append("</option>");
        }
        String settingsGameVersion = "<h3 class='option'>Game version <label><select id='"+uuid+"-game-version' onchange='connector(`java.settings."+id+".game-version."+uuid+".`+this.value);'>"+settingsGameVersions+"</select></label></h3>";

        ArrayList<String> loaderVersions = new ArrayList<>();
        try {
            loaderVersions = switch (modLoader) {
                case "fabric" -> Verget.getFabricVersions(true, minecraftVersion);
                case "forge" -> Verget.getForgeVersions(minecraftVersion);
                case "neoforge" -> Verget.getNeoForgeVersions(minecraftVersion);
                case "quilt" -> Verget.getQuiltVersions(minecraftVersion);
                default -> new ArrayList<>();
            };
        } catch (Exception e) {
            NexusApplication.getLogger().err("[Minecraft] (Connector) Couldn't fetch available versions for "+modLoader+": "+e.getMessage());
        }
        StringBuilder settingsLoaderVersions = new StringBuilder();
        for(String version : loaderVersions) {
            settingsLoaderVersions.append("<option value='").append(version).append("'>").append(version).append("</option>");
        }

        String settingsGameLoaderVersion = "<h3 class='option'>Modloader version <label><select id='"+uuid+"-loader-version' onchange='connector(`java.settings."+id+".loader-version."+uuid+".`+this.value);'>"+settingsLoaderVersions+"</select></label></h3>";
        String settingsGame = settingsGameType+settingsGameVersion+settingsGameLoaderVersion;

        String settingsJava = "<div class='option-group'><h4 class='option'>Java settings</h4>%</div>";
        String settingsJVMMemory = "<h3 class='option'>Memory (RAM) <span class='value-option'><span id='"+id+"-memory'><i class='bx bx-loader-alt bx-spin' ></i> Loading...</span> <i class='bx bxs-pencil' onclick='connector(`java.settings."+id+".memory`);'></i></span></h3>";
        String settingsJVMArgs = "<h3 class='option input-list' id='java.settings."+id+".jvm-arguments'>JVM Arguments <span class='input-list-field'><span class='list-input-content'></span><label><input placeholder='Type here...' class='list' type='text'></label></span></h3>";
        settingsJava = settingsJava.replace("%",settingsJVMMemory+settingsJVMArgs);

        boolean isEditable = instance.isEditable();

        String instanceSettings = "";
        if(!instance.forceUpdates()&&instance.getIndexUrl().startsWith("http")&&instance.getLocation().startsWith("http")) {
            instanceSettings = "<div class='option-group'><h4 class='option'>Instance settings</h4>%</div>";
            String updates = "<h3 class='option'>Update instance <label><select onchange='connector(`java.settings."+id+".updates.`+this.value);' id='"+uuid+"-updates'><option value='always'>Always</option><option value='ask'>Ask</option><option value='never'>Never</option></select></label></h3>";
            instanceSettings = instanceSettings.replace("%",updates);
        }

        if(!isEditable) {
            settingsBase = "";
        }
        String settings = settingsBase.replace("%",settingsGame)+settingsJava+instanceSettings;

        String managementBase = "<div class='option-group'><h4 class='option'>Instance information</h4>%</div>";
        String managementName = "<h3 class='option'>Name <label><input class='text' id='"+uuid+"-name' type='text' value=\\\""+instance.getName().replace("\"","''")+"\\\"></label></h3>";
        String managementVersion = "<h3 class='option'>Version <label><input class='text' id='"+uuid+"-version' type='text' value=\\\""+instance.getVersion().replace("\"","''")+"\\\"></label></h3>";
        String managementDescription = "<h3 class='option'>Summary<br><label><textarea id='"+uuid+"-summary' onchange=\\\"log('Test');\\\">"+instance.getSummary().replace("\"","''")+"</textarea></label></h3>";

        String managementAppearance = "<div class='option-group'><h4 class='option'>Instance appearance</h4>%</div>";

        String icon = instance.getIconUrl();
        String appearanceIcon;
        if(icon==null) {
            appearanceIcon = "<h3 class='option' style='padding: 0.5rem;'>Icon image <a class='button' onclick=\\\"connector('async.java.settings."+id+".icon."+uuid+"');\\\">Select</a></h3>";
        } else {
            appearanceIcon = "<h3 class='option' style='height: 9rem;'>Icon image <img id='"+uuid+"-icon' onclick=\\\"this.classList.toggle('active');\\\" src='" + icon + "'> <a class='button danger' onclick=\\\"connector('java.settings."+id+".removeIcon."+uuid+"');\\\">Remove</a><a class='button' onclick=\\\"connector('async.java.settings."+id+".icon."+uuid+"');\\\">Change</a></h3>";
        }

        String logo = instance.getLogoUrl();
        String appearanceLogo;
        if(logo==null) {
            appearanceLogo = "<h3 class='option' style='padding: 0.5rem;'>Logo image <a onclick=\\\"connector('async.java.settings."+id+".logo."+uuid+"');\\\" class='button'>Select</a></h3>";
        } else {
            appearanceLogo = "<h3 class='option' style='height: 9rem;'>Logo image <img id='"+uuid+"-logo' onclick=\\\"this.classList.toggle('active');\\\" src='" + logo + "'> <a class='button danger' onclick=\\\"connector('java.settings."+id+".removeLogo."+uuid+"');\\\">Remove</a><a class='button' onclick=\\\"connector('async.java.settings."+id+".logo."+uuid+"');\\\">Change</a></h3>";
        }

        String background = instance.getBackgroundUrl();
        String appearanceBackground;
        if(background==null) {
            appearanceBackground = "<h3 class='option' style='padding: 0.5rem;'>Background image <a class='button' onclick=\\\"connector('async.java.settings."+id+".background."+uuid+"');\\\">Select</a></h3>";
        } else {
            appearanceBackground = "<h3 class='option' style='height: 9rem;'>Background image <img id='"+uuid+"-background' onclick=\\\"this.classList.toggle('active');\\\" src='" + background + "'> <a onclick=\\\"connector('java.settings."+id+".removeBackground."+uuid+"');\\\" class='button danger'>Remove</a><a class='button' onclick=\\\"connector('async.java.settings."+id+".background."+uuid+"');\\\">Change</a></h3>";
        }

        String deleteInstance = "<h3 class='option' style='padding: 0.5rem;'>Delete instance <a class='button delete' onclick=\\\"connector('java.settings."+id+".delete');\\\">Delete</a></h3>";
        String instanceActions = "<div class='option-group'><h4 class='option'>Instance actions (danger zone)</h4>%</div>";

        String management = managementBase.replace("%",managementName+managementVersion+managementDescription)+managementAppearance.replace("%",appearanceIcon+appearanceLogo+appearanceBackground)+instanceActions.replace("%","<h3 class='option' style='padding: 0.5rem;'>Disable instance management <a onclick=\\\"connector('java.settings."+id+".disableEdit');\\\" class='button disable'>Disable</a></h3>"+deleteInstance);
        if(!isEditable) {
            management = instanceActions.replace("%","<h3 class='option' style='padding: 0.5rem;'>Enable instance management <a onclick=\\\"connector('java.settings."+id+".enableEdit');\\\" class='button'>Enable</a></h3>"+deleteInstance);
        }
        /*StringBuilder contents = new StringBuilder("<div class='option-group'><h4 class='option'>Mods</h4>");
        for(String mod : modList) {
            contents.append("<h3 class='option'>").append(mod).append("</h3>");
        }
        contents.append("</div>");*/

        String content = "<div class='overlay-group' id='"+uuid+"-settings-content'>"+settings+"</div> <div class='overlay-group' id='"+uuid+"-management-content'>"+management+"</div> <div class='overlay-group' id='"+uuid+"-content-content'>"+/*contents+*/"</div>";
        String command = "setOverlayContent(\"<div class='tabs' id='"+uuid+"-tabs'></div> "+content+"\"); addTab('"+uuid+"-tabs','"+uuid+"-settings','Settings',''); addTab('"+uuid+"-tabs','"+uuid+"-management','Management',''); "/*addTab('"+uuid+"-tabs','"+uuid+"-content','Content','');*/ +"switchTab('"+uuid+"-tabs','"+uuid+"-settings');";
        frame.executeJavaScript(command); System.gc();

        String loaderVersion = switch(modLoader) {
            case "fabric" -> instance.getFabricVersion();
            case "forge" -> instance.getForgeVersion();
            case "neoforge" -> instance.getNeoForgeVersion();
            case "quilt" -> instance.getQuiltVersion();
            default -> "";
        };

        String syncSettingsType = "document.getElementById('"+uuid+"-type').value = '"+modLoader.toLowerCase()+"'; document.getElementById('"+uuid+"-game-version').value = '"+minecraftVersion+"'; document.getElementById('"+uuid+"-loader-version').value = '"+loaderVersion+"';";
        String syncSettingsJava = "document.getElementById('"+id+"-memory').innerText = '"+instance.getMemory()+"MB'; initializeListInput('java.settings."+id+".jvm-arguments'); document.getElementById('"+uuid+"-updates').value = '"+instance.getSettings().getString("settings.instance.updates")+"';";
        try {
            StringBuilder args = new StringBuilder();
            for (String arg : (ArrayList<String>) instance.getSettings().get("settings.java.jvm-arguments")) {
                args.append("listInputs.get('java.settings.").append(id).append(".jvm-arguments').push('").append(arg).append("'); ");
            }
            args.append("syncListInput('java.settings.").append(id).append(".jvm-arguments'); ");
            syncSettingsJava += args.toString();
        } catch (Exception ex) {
            NexusApplication.getLogger().err("[Minecraft] (CONNECTOR) Couldn't resolve jvm arguments for "+id+": "+ex.getMessage());
        }
        String listeners = "document.getElementById('"+uuid+"-name').addEventListener('input', function() { clearTimeout(this.timeout); this.timeout = setTimeout(() => { connector('java.settings."+id+".name.'+this.value); }, 250); }); document.getElementById('"+uuid+"-version').addEventListener('input', function() { clearTimeout(this.timeout); this.timeout = setTimeout(() => { connector('java.settings."+id+".version.'+this.value); }, 250); }); document.getElementById('"+uuid+"-summary').addEventListener('input', function() { clearTimeout(this.timeout); this.timeout = setTimeout(() => { connector('java.settings."+id+".summary.'+this.value); }, 250); });";
        frame.executeJavaScript(syncSettingsType+listeners);
        frame.executeJavaScript(syncSettingsJava);
        System.gc();
    }

    private void resolveInstanceSync(String request) {
        if (request.equals("overview")) {
            if (ApplicationStorage.language.equals("de")) {
                frame.executeJavaScript("document.getElementById('mje-add-instance-button').innerText = 'Instanz hinzufügen';");
                frame.executeJavaScript("document.getElementById('mje-menu-title').innerText = 'Instanzen';");
                frame.executeJavaScript("setTitle('Übersicht');");
            }
        }
    }

    public void resolveOpenRequest(String request) {

    }

    public void resolveRunRequest(String request) {

    }

    public void resolveAuthRequest(String request) {
        if (request.equals("login")) {
            if (module.getAuthState() != MinecraftJavaAddon.AuthState.LOGGED_IN) {
                module.getAuthenticator(authKey).login();
            }
        } else if (request.equals("logout")) {
            ApplicationStorage.disableDriveAccess();
            if (module.getAuthState() == MinecraftJavaAddon.AuthState.LOGGED_IN) {
                JavaStorage.map.delete("auth.username");
                JavaStorage.map.delete("auth.uuid");
                module.setAuthState(MinecraftJavaAddon.AuthState.LOGGED_OUT);
                module.createNewAuthenticator();
                frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/start.html");
            }
        }
    }

    private void resolveSearchFilter(String filterRequest) {
        String request = filterRequest.replaceFirst("java.searchFilter.", "");
        if (request.startsWith("source.")) {
            JavaStorage.setSearchSource(request.replaceFirst("source.", ""));
            frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/discover.html?l=search&moduleId=" + module.getId() + "_java");
        }
    }

    private void resolveSearchRequest(String request) {
        int offset;
        if(request.contains(".")) {
            String[] req = request.split("\\.",2);
            offset = Integer.parseInt(req[0]);
            request = "."+req[1];
        } else {
            offset = Integer.parseInt(request);
            request = "";
        }

        if(offset==0) {
            frame.executeJavaScript("addFilterGroup('mje-source','Source'); addSelectFilter('source','mje-source','java.searchFilter.source',\"<option value='official'>Zyneon NEX</option><option value='modrinth'>Modrinth</option><option value='curseforge'>CurseForge</option>\",false); document.getElementById('mje-source-source-select').value = \"" + JavaStorage.getSearchSource() + "\"; deactivateMenu('menu',true);");
        }

        String query = "";
        if (request.startsWith(".")) {
            query = request.replaceFirst(".", "");
        }

        switch (JavaStorage.getSearchSource()) {
            case "official" -> ZyndexIntegration.searchModpacks(query, offset, frame);
            case "curseforge" -> CurseForgeIntegration.searchModpacks(query, offset, frame);
            case "modrinth" -> ModrinthIntegration.searchModpacks(query, offset, frame);
        }
    }

    private void resolveSettingsRequest(String request) {
        if (request.startsWith("global.")) {
            request = request.replaceFirst("global.", "");
            if (request.equals("memory")) {
                OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                long c = 1024L * 1024L;
                long max = os.getTotalMemorySize() / c;
                String url = ApplicationStorage.urlBase + ApplicationStorage.language + "/mje-memory.html?min=0&max=" + max + "&id=global&value=" + JavaStorage.map.getInteger("settings.global.memory");
                url = url.replace("\\", "/");
                frame.executeJavaScript("enableOverlay('" + url + "');");
            } else if (request.startsWith("memory.")) {
                try {
                    int i = Integer.parseInt(request.replace("memory.", ""));
                    JavaStorage.getConfig().set("settings.global.memory", i);
                    JavaStorage.map.setInteger("settings.global.memory", i);
                } catch (Exception ignore) {}
                frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/settings.html?t=global");
            } else if (request.startsWith("minimize.")) {
                boolean minimize = Boolean.parseBoolean(request.replace("minimize.", ""));
                JavaStorage.getConfig().set("settings.global.minimizeApp", minimize);
                JavaStorage.map.setBoolean("settings.global.minimizeApp", minimize);
            } else if (request.equals("path")) {
                File path = openDirectoryChooser();
                if (path != null) {
                    JavaStorage.setFallbackInstancePath(path.toString());
                    frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/settings.html?t=global");
                }
            }
        } else {
            String[] r = request.split("\\.",2);
            String id = r[0].replace("%dot%","."); request = r[1];
            if(JavaStorage.getLocalZyndex().getLocalInstancesById().containsKey(id)) {
                LocalInstance instance = JavaStorage.getLocalZyndex().getLocalInstancesById().get(id);
                if(request.startsWith("type.")) {
                    r = request.split("\\.",3);
                    String type = r[2];
                    String uuid = r[1];

                    ArrayList<String> gameVersions;
                    String gameVersion;
                    try {
                        gameVersions = switch (type) {
                            case "fabric" -> Verget.getFabricGameVersions(true);
                            case "forge" -> Verget.getForgeGameVersions();
                            case "neoforge" -> Verget.getNeoForgeGameVersions();
                            case "quilt" -> Verget.getQuiltGameVersions(true);
                            case "vanilla" -> Verget.getMinecraftVersions(MinecraftVerget.Filter.RELEASES);
                            case "experimental" -> Verget.getMinecraftVersions(MinecraftVerget.Filter.EXPERIMENTAL);
                            default -> Verget.getMinecraftVersions(MinecraftVerget.Filter.BOTH);
                        };
                        gameVersion = gameVersions.getFirst();
                    } catch (Exception e) {
                        NexusApplication.getLogger().err("[Minecraft] (Connector) Couldn't fetch available game versions for "+type+": "+e.getMessage());
                        gameVersions = new ArrayList<>();
                        gameVersion = null;
                    }
                    StringBuilder settingsGameVersions = new StringBuilder();
                    for(String version:gameVersions) {
                        settingsGameVersions.append("<option value='").append(version).append("'>").append(version).append("</option>");
                    }

                    ArrayList<String> loaderVersions;
                    String loaderVersion;
                    try {
                        loaderVersions = switch (type) {
                            case "fabric" -> Verget.getFabricVersions(true,gameVersion);
                            case "forge" -> Verget.getForgeVersions(gameVersion);
                            case "neoforge" -> Verget.getNeoForgeVersions(gameVersion);
                            case "quilt" -> Verget.getQuiltVersions(gameVersion);
                            default -> new ArrayList<>();
                        };
                        if(loaderVersions.isEmpty()) {
                            loaderVersion = null;
                        } else {
                            loaderVersion = loaderVersions.getFirst();
                        }
                    } catch (Exception e) {
                        NexusApplication.getLogger().err("[Minecraft] (Connector) Couldn't fetch available game versions for "+type+": "+e.getMessage());
                        loaderVersions = new ArrayList<>();
                        loaderVersion = null;
                    }
                    StringBuilder settingsLoaderVersions = new StringBuilder();
                    for(String version:loaderVersions) {
                        settingsLoaderVersions.append("<option value='").append(version).append("'>").append(version).append("</option>");
                    }

                    String command = "document.getElementById('"+uuid+"-game-version').innerHTML = \""+settingsGameVersions+"\"; document.getElementById('"+uuid+"-loader-version').innerHTML = \""+settingsLoaderVersions+"\";";
                    frame.executeJavaScript(command);

                    if(gameVersion!=null) {
                        instance.setModloader(type.toLowerCase(), loaderVersion);
                        instance.setGameVersion(gameVersion);
                    }

                } else if(request.startsWith("updates.")) {
                    instance.getSettings().set("settings.instance.updates",request.replaceFirst("updates.",""));

                } else if(request.startsWith("game-version.")) {
                    r = request.split("\\.",3);
                    String gameVersion = r[2];
                    String uuid = r[1];
                    String type = instance.getModloader().toLowerCase();

                    ArrayList<String> loaderVersions;
                    String loaderVersion;
                    try {
                        loaderVersions = switch (type) {
                            case "fabric" -> Verget.getFabricVersions(true,gameVersion);
                            case "forge" -> Verget.getForgeVersions(gameVersion);
                            case "neoforge" -> Verget.getNeoForgeVersions(gameVersion);
                            case "quilt" -> Verget.getQuiltVersions(gameVersion);
                            default -> new ArrayList<>();
                        };
                        if(loaderVersions.isEmpty()) {
                            loaderVersion = null;
                        } else {
                            loaderVersion = loaderVersions.getFirst();
                        }
                    } catch (Exception e) {
                        NexusApplication.getLogger().err("[Minecraft] (Connector) Couldn't fetch available game versions for "+type+": "+e.getMessage());
                        loaderVersions = new ArrayList<>();
                        loaderVersion = null;
                    }
                    StringBuilder settingsLoaderVersions = new StringBuilder();
                    for(String version:loaderVersions) {
                        settingsLoaderVersions.append("<option value='").append(version).append("'>").append(version).append("</option>");
                    }

                    String command = "document.getElementById('"+uuid+"-loader-version').innerHTML = \""+settingsLoaderVersions+"\";";

                    frame.executeJavaScript(command);

                    instance.setModloader(type.toLowerCase(), loaderVersion);
                    instance.setGameVersion(gameVersion);
                } else if(request.startsWith("loader-version.")) {
                    String version = request.split("\\.",3)[2];
                    instance.setModloader(instance.getModloader(),version);
                    String command = "";
                    frame.executeJavaScript(command);
                } else if(request.startsWith("jvm-arguments.add.")) {
                    ArrayList<String> args = (ArrayList<String>)instance.getSettings().get("settings.java.jvm-arguments");
                    String arg = request.replaceFirst("jvm-arguments.add.","");
                    if(!args.contains(arg)) {
                        args.add(arg);
                    }
                    instance.getSettings().set("settings.java.jvm-arguments",args);
                } else if(request.startsWith("jvm-arguments.remove.")) {
                    ArrayList<String> args = (ArrayList<String>)instance.getSettings().get("settings.java.jvm-arguments");
                    args.remove(request.replaceFirst("jvm-arguments.remove.",""));
                    instance.getSettings().set("settings.java.jvm-arguments",args);
                } else if(request.equals("memory")) {
                    id = instance.getId();
                    OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                    long c = 1024L * 1024L;
                    long max = os.getTotalMemorySize() / c;
                    String url = ApplicationStorage.urlBase + ApplicationStorage.language + "/mje-memory.html?min=0&max=" + max + "&id="+id+"&value=" + instance.getMemory();
                    url = url.replace("\\", "/");
                    frame.executeJavaScript("enableOverlay('" + url + "');");
                } else if(request.startsWith("memory.")) {
                    instance.getSettings().set("settings.java.memory",Integer.parseInt(request.replaceFirst("memory.","")));
                    frame.executeJavaScript("disableOverlay(); document.getElementById('"+id+"-memory').innerText = '"+instance.getMemory()+"MB';");
                } else if(request.startsWith("name.")) {
                    instance.setName(request.replaceFirst("name.",""));
                } else if(request.startsWith("version.")) {
                    instance.setVersion(request.replaceFirst("version.",""));
                } else if(request.startsWith("summary.")) {
                    instance.setSummary(request.replaceFirst("summary.",""));
                } else if(request.startsWith("icon.")) {
                    File icon = openImageChooser();
                    if(icon!=null) {
                        try {
                            String path = instance.getDirectory().toString()+"/meta/instanceIcon.png";
                            Files.copy(icon.toPath(), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
                            File iconFile = new File(path);
                            if(iconFile.exists()) {
                                icon = iconFile;
                            }
                        } catch (Exception e) {
                            NexusUtilities.getLogger().err("[Minecraft] (CONNECTOR) Couldn't save icon: "+e.getMessage());
                        }
                        instance.setIconUrl(StringConverter.getURLString(icon.getAbsolutePath()));
                        showInstanceSettings(instance);
                    }
                } else if(request.startsWith("removeIcon.")) {
                    instance.setIconUrl(null);
                    showInstanceSettings(instance);
                } else if(request.startsWith("logo.")) {
                    File logo = openImageChooser();
                    if(logo!=null) {
                        try {
                            String path = instance.getDirectory().toString()+"/meta/instanceLogo.png";
                            Files.copy(logo.toPath(), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
                            File logoFile = new File(path);
                            if(logoFile.exists()) {
                                logo = logoFile;
                            }
                        } catch (Exception e) {
                            NexusUtilities.getLogger().err("[Minecraft] (CONNECTOR) Couldn't save logo: "+e.getMessage());
                        }
                        instance.setLogoUrl(StringConverter.getURLString(logo.getAbsolutePath()));
                        showInstanceSettings(instance);
                    }
                } else if(request.startsWith("removeLogo.")) {
                    instance.setLogoUrl(null);
                    showInstanceSettings(instance);
                } else if(request.startsWith("background.")) {
                    File background = openImageChooser();
                    if(background!=null) {
                        try {
                            String path = instance.getDirectory().toString()+"/meta/instanceBackground.png";
                            Files.copy(background.toPath(), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
                            File backgroundFile = new File(path);
                            if(backgroundFile.exists()) {
                                background = backgroundFile;
                            }
                        } catch (Exception e) {
                            NexusUtilities.getLogger().err("[Minecraft] (CONNECTOR) Couldn't save background: "+e.getMessage());
                        }
                        instance.setBackgroundUrl(StringConverter.getURLString(background.getAbsolutePath()));
                        showInstanceSettings(instance);
                    }
                } else if(request.startsWith("removeBackground.")) {
                    instance.setBackgroundUrl(null);
                    showInstanceSettings(instance);
                } else if(request.equals("enableEdit")) {
                    instance.setEditable(true);
                    JavaStorage.reloadLocalZyndex();
                    showInstanceSettings(instance);
                } else if(request.equals("disableEdit")) {
                    instance.setEditable(false);
                    JavaStorage.reloadLocalZyndex();
                    showInstanceSettings(instance);
                } else if(request.equals("folder")) {
                    try {
                        if(Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(instance.getDirectory().toURI());
                        }
                    } catch (Exception e) {
                        NexusApplication.getLogger().err("[Minecraft] (Connector) Couldn't open folder: "+e.getMessage());
                    }
                } else if(request.equals("delete")) {
                    String url = ApplicationStorage.urlBase + ApplicationStorage.language + "/mje-delete.html?id="+id;
                    url = url.replace("\\", "/");
                    frame.executeJavaScript("enableOverlay('" + url + "');");
                } else if(request.equals("delete.confirmed")) {
                    File directory = instance.getDirectory();
                    instance = null;
                    FileActions.deleteFolder(directory);
                    System.gc();
                    JavaStorage.reloadLocalZyndex();
                    frame.getBrowser().reload();
                }
            }
        }
    }

    private File openImageChooser() {
        NexusDesktop.init();
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select an image file");
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "png", "gif", "jpeg", "jpg", "webp");
        chooser.addChoosableFileFilter(filter);
        int answer = chooser.showOpenDialog(null);
        if (answer == JFileChooser.APPROVE_OPTION) {
            try {
                String path = chooser.getSelectedFile().getAbsolutePath().replace("\\", "/");
                return new File(path);
            } catch (Exception e) {
                NexusApplication.getLogger().err("[Minecraft] (Connector) Couldn't get image file: "+e.getMessage());
            }
        }
        return null;
    }

    private File openDirectoryChooser() {
        JFileChooser chooser;
        try {
            chooser = new JFileChooser(JavaStorage.getFallbackInstancePath());
        } catch (Exception ignore) {
            chooser = new JFileChooser(ApplicationStorage.getApplicationPath());
        }
        chooser.setDialogTitle("Select instances fallback path");
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter standardFilter = new FileNameExtensionFilter("Folders only", "*.*");
        chooser.addChoosableFileFilter(standardFilter);
        int answer = chooser.showOpenDialog(null);
        if (answer == JFileChooser.APPROVE_OPTION) {
            try {
                String path = chooser.getSelectedFile().getAbsolutePath().replace("\\", "/");
                return new File(path);
            } catch (Exception e) {
                NexusApplication.getLogger().err("[Minecraft] (Connector) Couldn't get directory: "+e.getMessage());
            }
        }
        return null;
    }
}