package com.zyneonstudios.application.minecraft.java.integrations.zyndex;

import com.google.gson.JsonArray;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.JavaStorage;
import com.zyneonstudios.nexus.index.ReadableZyndex;
import com.zyneonstudios.nexus.instance.Instance;
import com.zyneonstudios.nexus.instance.ReadableZynstance;
import com.zyneonstudios.nexus.utilities.file.FileActions;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;

import javax.swing.*;
import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

public class LocalInstance implements Instance {

    // OBJECT STUFF
    private final File directory;
    private final JsonStorage settings;
    private final JsonStorage config;

    // INFO STUFF
    private ArrayList<String> info_authors = new ArrayList<>();
    private String info_name = null;
    private String info_summary = null;
    private String info_version = null;

    // META STUFF
    private ArrayList<String> meta_changelogs = new ArrayList<>();
    private String meta_description = null;
    private String meta_download = null;
    private String meta_id = null;
    private String meta_infoCard = null;;
    private String meta_infoText = null;
    private boolean meta_isEditable = false;
    private boolean meta_isHidden = true;
    private boolean meta_forceUpdates = true;
    private String meta_location = null;
    private String meta_origin = null;
    private ArrayList<String> meta_tags = new ArrayList<>();
    private ArrayList<String> meta_versions = new ArrayList<>();

    // RESOURCES STUFF
    private String resources_background = null;
    private String resources_icon = null;
    private String resources_logo = null;
    private String resources_thumbnail = null;

    // VERSIONS STUFF
    private String versions_fabric = null;
    private String versions_forge = null;
    private String versions_minecraft = null;
    private String versions_neoforge = null;
    private String versions_quilt = null;

    // OTHER JSON STUFF
    private String scheme = null;
    private String modloader = null;

    // LISTS
    private final ArrayList<String> modList = new ArrayList<>();

    private void init() {
        if(config.get("instance.info.authors")!=null) {
            info_authors = (ArrayList<String>)config.get("instance.info.authors");
        } else if(config.get("instance.info.author")!=null) {
            info_authors = new ArrayList<>();
            info_authors.add(config.getString("instance.info.author"));
        } else {
            info_authors = new ArrayList<>();
            info_authors.add("Anonymous");
        }
        if(config.get("instance.info.name")!=null) {
            info_name = config.getString("instance.info.name");
        } else {
            info_name = "Unknown";
        }
        if(config.get("instance.info.summary")!=null) {
            info_summary = config.getString("instance.info.summary");
        } else if(config.get("instance.info.description")!=null) {
            info_summary = config.getString("instance.info.description");
        }
        if(config.get("instance.info.version")!=null) {
            info_version = config.getString("instance.info.version");
        } else {
            info_version = "-1";
        }

        if(config.get("instance.meta.changelogs")!=null) {
            meta_changelogs = (ArrayList<String>)config.get("instance.meta.changelogs");
        } else {
            meta_changelogs = new ArrayList<>();
        }
        if(config.get("instance.meta.description")!=null) {
            meta_description = config.getString("instance.meta.description");
        } else if(config.get("instance.info.summary")!=null) {
            meta_description = config.getString("instance.info.summary");
        }
        if(meta_description==null) {
            meta_description = "No description...";
        }
        if(info_summary==null) {
            info_summary = "No summary...";
        }
        if(config.get("instance.meta.download")!=null) {
            meta_download = config.getString("instance.meta.download");
        } else {
            meta_download = "No download...";
        }
        if(config.get("instance.meta.id")!=null) {
            meta_id = config.getString("instance.meta.id");
        } else {
            meta_id = "unknown";
        }
        if(config.get("instance.meta.infoCard")!=null) {
            meta_infoCard = config.getString("instance.meta.infoCard");
        } else {
            meta_infoCard = null;
        }
        if(config.get("instance.meta.infoText")!=null) {
            meta_infoText = config.getString("instance.meta.infoText");
        } else {
            meta_infoText = null;
        }
        if(config.get("instance.meta.isEditable")!=null) {
            meta_isEditable = config.getBoolean("instance.meta.isEditable");
        } else {
            meta_isEditable = false;
        }
        if(config.get("instance.meta.isHidden")!=null) {
            meta_isHidden = config.getBoolean("instance.meta.isHidden");
        } else {
            meta_isHidden = true;
        }
        if(config.has("instance.meta.forceUpdates")) {
            meta_forceUpdates = config.getBoolean("instance.meta.forceUpdates");
        } else {
            meta_forceUpdates = false;
        }

        if(config.get("instance.meta.location")!=null) {
            meta_location = config.getString("instance.meta.location");
        } else {
            meta_location = null;
        }
        if(config.get("instance.meta.origin")!=null) {
            meta_origin = config.getString("instance.meta.origin");
        } else {
            meta_origin = null;
        }
        if(config.get("instance.meta.tags")!=null) {
            meta_tags = (ArrayList<String>)config.get("instance.meta.tags");
        } else {
            meta_tags = new ArrayList<>();
        }
        if(config.get("instance.meta.versions")!=null) {
            meta_versions = (ArrayList<String>)config.get("instance.meta.versions");
        } else {
            meta_versions = new ArrayList<>();
        }

        if(config.get("instance.resources.background")!=null) {
            resources_background = config.getString("instance.resources.background");
        } else {
            resources_background = null;
        }
        if(config.get("instance.resources.icon")!=null) {
            resources_icon = config.getString("instance.resources.icon");
        } else {
            resources_icon = null;
        }
        if(config.get("instance.resources.logo")!=null) {
            resources_logo = config.getString("instance.resources.logo");
        } else {
            resources_logo = null;
        }
        if(config.get("instance.resources.thumbnail")!=null) {
            resources_thumbnail = config.getString("instance.resources.thumbnail");
        } else {
            resources_thumbnail = null;
        }

        if(config.get("instance.versions.fabric")!=null) {
            versions_fabric = config.getString("instance.versions.fabric");
        } else {
            versions_fabric = null;
        }
        if(config.get("instance.versions.forge")!=null) {
            versions_forge = config.getString("instance.versions.forge");
        } else {
            versions_forge = null;
        }
        if(config.get("instance.versions.minecraft")!=null) {
            versions_minecraft = config.getString("instance.versions.minecraft");
        } else {
            versions_minecraft = null;
        }
        if(config.get("instance.versions.neoforge")!=null) {
            versions_neoforge = config.getString("instance.versions.neoforge");
        } else {
            versions_neoforge = null;
        }
        if(config.get("instance.versions.quilt")!=null) {
            versions_quilt = config.getString("instance.versions.quilt");
        } else {
            versions_quilt = null;
        }



        if(versions_quilt!=null) {
            modloader = "Quilt";
        } else if(versions_fabric!=null) {
            modloader = "Fabric";
        } else if(versions_forge!=null) {
            modloader = "Forge";
        } else if(versions_neoforge!=null) {
            modloader = "NeoForge";
        } else {
            modloader = "Vanilla";
        }

        config.set("instance.meta.modloader",modloader.toLowerCase());

        if(config.get("scheme")!=null) {
            scheme = config.getString("scheme");
        } else {
            scheme = null;
        }
        settings.ensure("settings.java.jvm-arguments",new JsonArray());
        settings.ensure("settings.instance.updates","always");
    }

    public LocalInstance(File file) {
        config = new JsonStorage(file);
        this.directory = file.getParentFile();
        String path = getPath().toString();
        if(path.endsWith("/")) {
            path = path + "meta/instanceSettings.json";
        } else {
            path = path + "/meta/instanceSettings.json";
        }
        settings = new JsonStorage(path);
        init();
    }

    public File getInstanceFile() {
        return config.getJsonFile();
    }

    public File getDirectory() {
        return directory;
    }

    public Path getPath() {
        return Path.of(URLDecoder.decode(directory.getAbsolutePath().replace("\\\\","\\").replace("\\","/"), StandardCharsets.UTF_8));
    }

    public JsonStorage getSettings() {
        return settings;
    }

    public int getMemory() {
        if(settings.get("settings.java.memory")!=null||settings.get("settings.memory")!=null) {
            try {
                return settings.getInt("settings.java.memory");
            } catch (Exception exception) {
                try {
                    return settings.getInt("settings.memory");
                } catch (Exception e) {
                    NexusApplication.getLogger().err("[Minecraft] Couldn't read memory int ("+settings.getPath()+"): "+e.getMessage());
                }
            }
        }
        return JavaStorage.map.getInteger("settings.global.memory");
    }

    public boolean isExperimental() {
        if(modloader!=null) {
            if(!modloader.equals("experimental")) {
                return false;
            }
        }
        settings.ensure("settings.game.isExperimental",false);
        return settings.getBoolean("settings.game.isExperimental");
    }

    public void setExperimental(boolean experimental) {
        settings.set("settings.game.isExperimental",experimental);
    }


    // INFO GETTER
    @Override
    public String getAuthor() {
        return info_authors.toString().replace("[","").replace("]","");
    }

    @Override
    public ArrayList<String> getAuthors() {
        return info_authors;
    }

    @Override
    public String getName() {
        return info_name;
    }

    @Override
    public String getSummary() {
        return info_summary;
    }

    @Override
    public String getVersion() {
        return info_version;
    }


    // META GETTER
    @Override
    public ArrayList<String> getChangelogs() {
        return meta_changelogs;
    }

    @Override
    public String getDescription() {
        return meta_description;
    }

    @Override
    public String getDownloadUrl() {
        return meta_download;
    }

    @Override
    public String getId() {
        return meta_id;
    }

    @Override
    public String getIndexUrl() {
        return meta_origin;
    }

    @Override
    public String getInfoCard() {
        return meta_infoCard;
    }

    @Override
    public String getInfoText() {
        return meta_infoText;
    }

    @Override
    public Boolean isEditable() {
        return meta_isEditable;
    }

    @Override
    public Boolean isHidden() {
        return meta_isHidden;
    }

    @Override
    public Boolean forceUpdates() {
        return meta_forceUpdates;
    }

    @Override
    public String getLocation() {
        return meta_location;
    }

    @Override
    public String getOrigin() {
        return config.getJsonFile().getAbsolutePath();
    }

    @Override
    public ArrayList<String> getTags() {
        return meta_tags;
    }

    @Override
    public String getTagString() {
        return meta_tags.toString().replace("[","").replace("]","");
    }

    @Override
    public ArrayList<String> getVersions() {
        return meta_versions;
    }


    // RESOURCES GETTER
    @Override
    public String getBackgroundUrl() {
        return resources_background;
    }

    @Override
    public String getIconUrl() {
        return resources_icon;
    }

    @Override
    public String getLogoUrl() {
        return resources_logo;
    }

    @Override
    public String getThumbnailUrl() {
        return resources_thumbnail;
    }


    // VERSIONS GETTER
    @Override
    public String getFabricVersion() {
        return versions_fabric;
    }

    @Override
    public String getForgeType() {
        return "";
    }

    @Override
    public String getForgeVersion() {
        return versions_forge;
    }

    @Override
    public String getMinecraftVersion() {
        return versions_minecraft;
    }

    @Override
    public String getNeoForgeVersion() {
        return versions_neoforge;
    }

    @Override
    public String getQuiltVersion() {
        return versions_quilt;
    }


    // GETTER
    @Override
    public String getModloader() {
        return modloader;
    }

    @Override
    public String getSchemeVersion() {
        return scheme;
    }

    public String[] getModList() {
        return modList.toArray(new String[0]);
    }

    public void scanMods() {
        modList.clear();
        NexusApplication.getLogger().dbg("[Minecraft] (Instance) Starting mod scan for "+meta_id+"...");
        File mods = new File(getPath().toString().replace("\\","/")+"/mods");
        if(mods.exists()) {
            for(File file : Objects.requireNonNull(mods.listFiles())) {
                if(file.getName().toLowerCase().endsWith(".jar")) {
                    modList.add(file.getName());
                }
            }
        }
    }

    public void setModloader(String modloader, String version) {
        modloader = modloader.toLowerCase();
        switch (modloader) {
            case "fabric": {
                config.delete("instance.versions.forge");
                config.delete("instance.versions.quilt");
                config.delete("instance.versions.neoforge");
            }
            case "forge": {
                config.delete("instance.versions.fabric");
                config.delete("instance.versions.quilt");
                config.delete("instance.versions.neoforge");
            }
            case "neoforge": {
                config.delete("instance.versions.forge");
                config.delete("instance.versions.quilt");
                config.delete("instance.versions.fabric");
            }
            case "quilt": {
                config.delete("instance.versions.forge");
                config.delete("instance.versions.fabric");
                config.delete("instance.versions.neoforge");
            }
            default: {
                config.delete("instance.versions.fabric");
                config.delete("instance.versions.forge");
                config.delete("instance.versions.quilt");
                config.delete("instance.versions.neoforge");
            }
        }
        config.set("instance.meta.modloader",modloader);
        this.modloader = modloader;
        if(version!=null) {
            if(!version.isEmpty()) {
                config.set("instance.versions." + modloader, version);
            }
        }
    }

    public void setGameVersion(String version) {
        versions_minecraft = version;
        config.set("instance.versions.minecraft",version);
    }

    public void setName(String name) {
        config.set("instance.info.name",name);
        info_name = name;
    }

    public void setVersion(String version) {
        config.set("instance.info.version",version);
        info_version = version;
    }

    public void setSummary(String summary) {
        config.set("instance.info.summary",summary);
        info_summary = summary;
    }

    public void setIconUrl(String url) {
        if(url==null) {
            config.delete("instance.resources.icon");
            resources_icon = null;
            return;
        }
        config.set("instance.resources.icon",url);
        resources_icon = url;
    }

    public void setLogoUrl(String url) {
        if(url==null) {
            config.delete("instance.resources.logo");
            resources_logo = null;
            return;
        }
        config.set("instance.resources.logo",url);
        resources_logo = url;
    }

    public void setBackgroundUrl(String url) {
        if(url==null) {
            config.delete("instance.resources.background");
            resources_background = null;
            return;
        }
        config.set("instance.resources.background",url);
        resources_background = url;
    }

    public void setEditable(boolean editable) {
        config.set("instance.meta.isEditable",editable);
        meta_isEditable = editable;
    }

    public boolean update() {
        if (meta_origin.toLowerCase().startsWith("https://") && meta_location.startsWith("https://")) {
            try {
                ReadableZyndex origin = new ReadableZyndex(meta_origin);
                if (origin.getInstancesById().containsKey(meta_id)) {
                    ReadableZynstance update = origin.getInstancesById().get(meta_id);
                    NexusApplication.getLogger().deb("[Minecraft] (Instance) Found instance \"" + meta_id + "\" on origin server!");
                    if (!update.getVersion().equals(info_version)) {
                        NexusApplication.getLogger().log("[Minecraft] (Instance) Found update for instance \"" + meta_id + "\": " + info_version + " -> " + update.getVersion());
                        if(shouldUpdate()) {
                            String path = this.getPath().toString().replace("\\", "/");
                            if (!path.endsWith("/")) {
                                path = path + "/";
                            }
                            File mods = new File(path + "mods/");
                            if (mods.exists()) {
                                if (mods.isDirectory()) {
                                    FileActions.deleteFolder(mods);
                                }
                                if (mods.exists()) {
                                    throw new RuntimeException("Could not delete old mods folder!");
                                }
                            }
                            return ZyndexIntegration.install(update, path);
                        } else {
                            return false;
                        }
                    }
                    return true;
                }
            } catch (Exception e) {
                NexusApplication.getLogger().err("[Minecraft] (Instance) Couldn't update zyndex instance " + meta_id + ": " + e.getMessage());
            }
        }
        return false;
    }

    private boolean shouldUpdate() {
        if (meta_forceUpdates || settings.getString("settings.instance.updates").equals("always")) {
            return true;
        } else if(settings.getString("settings.instance.updates").equals("never")) {
            return false;
        }
        int result = JOptionPane.showConfirmDialog(
                null,
                "There is an instance update available, would you like to install it?",
                "Update available!",
                JOptionPane.YES_NO_OPTION
        );

        return result == JOptionPane.YES_OPTION;
    }
}