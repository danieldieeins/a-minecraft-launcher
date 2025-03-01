package com.zyneonstudios.application.minecraft.java.integrations.modrinth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.nexus.utilities.json.GsonUtility;

import java.util.ArrayList;
import java.util.List;

public class ModrinthResourceVersion {

    private final String url;
    private final JsonObject json;

    private final ModrinthResource pack;
    private final String versionId;
    private final String[] gameVersions;
    private final String[] loaders;
    private final String projectId;
    private final String authorId;
    private final boolean featured;
    private final String name;
    private final String versionNumber;
    private final String changelog;
    private final String datePublished;
    private final int downloads;
    private final String versionType;
    private final String status;
    private final JsonArray files;
    private final JsonArray dependencies;

    public ModrinthResourceVersion(ModrinthResource pack, String versionId) {
        this.pack = pack;
        boolean versionExists = false;
        for(String version : pack.getVersions()) {
            if(version.equals(versionId)) {
                versionExists = true;
                break;
            }
        }
        if(versionExists) {
            this.url = "https://api.modrinth.com/v2/version/"+versionId;
            this.json = GsonUtility.getObject(this.url);
            this.versionId = json.get("id").getAsString();

            JsonArray gameVersions = json.getAsJsonArray("game_versions");
            if(!gameVersions.isEmpty()) {
                List<String> versions = new ArrayList<>();
                for(JsonElement element : gameVersions) {
                    try {
                        String version = element.getAsString();
                        if (!versions.contains(version)) {
                            versions.add(version);
                        }
                    } catch (Exception e) {
                        NexusApplication.getLogger().err("[Minecraft] Couldn't parse game version: "+e.getMessage());
                    }
                }
                String[] gV;
                try {
                    gV = versions.toArray(new String[0]);
                } catch (Exception e) {
                    gV = null;
                    NexusApplication.getLogger().err("[Minecraft] Couldn't parse game versions: "+e.getMessage());
                }
                this.gameVersions = gV;
            } else {
                this.gameVersions = null;
            }

            JsonArray loaders = json.getAsJsonArray("loaders");
            if(!loaders.isEmpty()) {
                List<String> loaders_ = new ArrayList<>();
                for(JsonElement element : loaders) {
                    try {
                        String loader = element.getAsString();
                        if (!loaders_.contains(loader)) {
                            loaders_.add(loader);
                        }
                    } catch (Exception e) {
                        NexusApplication.getLogger().err("[Minecraft] Couldn't parse loader: "+e.getMessage());
                    }
                }
                String[] l;
                try {
                    l = loaders_.toArray(new String[0]);
                } catch (Exception e) {
                    l = null;
                    NexusApplication.getLogger().err("[Minecraft] Couldn't parse loaders: "+e.getMessage());
                }
                this.loaders = l;
            } else {
                this.loaders = null;
            }

            this.projectId = json.get("project_id").getAsString();
            this.authorId = json.get("author_id").getAsString();
            this.featured = json.get("featured").getAsBoolean();
            this.name = json.get("name").getAsString();
            this.versionNumber = json.get("version_number").getAsString();
            this.changelog = json.get("changelog").getAsString();
            this.datePublished = json.get("date_published").getAsString();
            this.downloads = json.get("downloads").getAsInt();
            this.versionType = json.get("version_type").getAsString();
            this.status = json.get("status").getAsString();
            this.files = json.getAsJsonArray("files");
            this.dependencies = json.getAsJsonArray("dependencies");

        } else {
            throw new RuntimeException("The specified project version ("+versionId+") does not exist!");
        }
    }

    public String getUrl() {
        return url;
    }

    public JsonObject getJsonObject() {
        return json;
    }

    public ModrinthResource getPack() {
        return pack;
    }

    public String getVersionId() {
        return versionId;
    }

    public String[] getGameVersions() {
        return gameVersions;
    }

    public String[] getLoaders() {
        return loaders;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public boolean isFeatured() {
        return featured;
    }

    public String getName() {
        return name;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public String getChangelog() {
        return changelog;
    }

    public String getDatePublished() {
        return datePublished;
    }

    public int getDownloads() {
        return downloads;
    }

    public String getVersionType() {
        return versionType;
    }

    public String getStatus() {
        return status;
    }

    public JsonArray getFiles() {
        return files;
    }

    public JsonArray getDependencies() {
        return dependencies;
    }
}