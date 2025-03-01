package com.zyneonstudios.application.minecraft.java.integrations.modrinth;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.integrations.modrinth.search.facets.ModrinthEnvironmentType;
import com.zyneonstudios.nexus.utilities.json.GsonUtility;

import java.util.ArrayList;
import java.util.List;

public class ModrinthResource {

    private final String url;
    private final JsonObject json;

    private final ModrinthEnvironmentType clientSide;
    private final ModrinthEnvironmentType serverSide;
    private final String[] gameVersions;
    private final String id;
    private final String slug;
    private final String projectType;
    private final String teamId;
    private final String organizationId;
    private final String title;
    private final String description;
    private final String body;
    private final String published;
    private final String updated;
    private final String approved;
    private final String status;
    private final String licenseId;
    private final String licenseName;
    private final int downloads;
    private final int followers;
    private final String[] categories;
    private final String[] additionalCategories;
    private final String[] loaders;
    private final String[] versions;
    private final String iconUrl;
    private final String issuesUrl;
    private final String sourceUrl;
    private final String wikiUrl;
    private final String discordUrl;
    private final JsonArray donationUrls;
    private final JsonArray gallery;
    private final String color;
    private final String threadId;
    private final String monetizationStatus;

    public ModrinthResource(String id_or_slug) {
        url = "https://api.modrinth.com/v2/project/" + id_or_slug;
        json = new Gson().fromJson(GsonUtility.getFromURL(url), JsonObject.class);
        clientSide = ModrinthEnvironmentType.valueOf(json.get("client_side").getAsString());
        serverSide = ModrinthEnvironmentType.valueOf(json.get("server_side").getAsString());

        JsonArray gameVersions = json.getAsJsonArray("game_versions");
        if (!gameVersions.isEmpty()) {
            List<String> versions = new ArrayList<>();
            for (JsonElement element : gameVersions) {
                try {
                    String version = element.getAsString();
                    if (!versions.contains(version)) {
                        versions.add(version);
                    }
                } catch (Exception e) {
                    NexusApplication.getLogger().err("[Minecraft] Couldn't parse game version: " + e.getMessage());
                }
            }
            String[] gV;
            try {
                gV = versions.toArray(new String[0]);
            } catch (Exception e) {
                gV = null;
                NexusApplication.getLogger().err("[Minecraft] Couldn't parse game versions: " + e.getMessage());
            }
            this.gameVersions = gV;
        } else {
            this.gameVersions = null;
        }

        id = json.get("id").getAsString();
        slug = json.get("slug").getAsString();
        projectType = json.get("project_type").getAsString();

        if (json.has("team")) {
            if (json.get("team").getAsString().isEmpty()) {
                teamId = null;
            } else {
                teamId = json.get("team").getAsString();
            }
        } else {
            teamId = null;
        }

        if (json.has("organization")) {
            if (json.get("organization").getAsString().isEmpty()) {
                organizationId = null;
            } else {
                organizationId = json.get("organization").getAsString();
            }
        } else {
            organizationId = null;
        }

        title = json.get("title").getAsString();

        if (json.has("description")) {
            if (json.get("description").getAsString().isEmpty()) {
                description = null;
            } else {
                description = json.get("description").getAsString();
            }
        } else {
            description = null;
        }

        if (json.has("body")) {
            if (json.get("body").getAsString().isEmpty()) {
                body = null;
            } else {
                body = json.get("body").getAsString();
            }
        } else {
            body = null;
        }

        if (json.has("published")) {
            if (json.get("published").getAsString().isEmpty()) {
                published = null;
            } else {
                published = json.get("published").getAsString();
            }
        } else {
            published = null;
        }

        if (json.has("updated")) {
            if (json.get("updated").getAsString().isEmpty()) {
                updated = null;
            } else {
                updated = json.get("updated").getAsString();
            }
        } else {
            updated = null;
        }

        if (json.has("approved")) {
            if (json.get("approved").getAsString().isEmpty()) {
                approved = null;
            } else {
                approved = json.get("approved").getAsString();
            }
        } else {
            approved = null;
        }

        if (json.has("status")) {
            if (json.get("status").getAsString().isEmpty()) {
                status = null;
            } else {
                status = json.get("status").getAsString();
            }
        } else {
            status = null;
        }

        if (json.has("license")) {
            JsonObject license = json.getAsJsonObject("license");
            if (license.has("id")) {
                if (license.get("id").getAsString().isEmpty()) {
                    licenseId = null;
                } else {
                    licenseId = license.get("id").getAsString();
                }
            } else {
                licenseId = null;
            }

            if (license.has("name")) {
                if (license.get("name").getAsString().isEmpty()) {
                    licenseName = null;
                } else {
                    licenseName = license.get("name").getAsString();
                }
            } else {
                licenseName = null;
            }

        } else {
            licenseId = null;
            licenseName = null;
        }

        if (json.has("downloads")) {
            downloads = json.get("downloads").getAsInt();
        } else {
            downloads = 0;
        }

        if (json.has("followers")) {
            followers = json.get("followers").getAsInt();
        } else {
            followers = 0;
        }

        JsonArray categories = json.getAsJsonArray("categories");
        if (!categories.isEmpty()) {
            List<String> categories_ = new ArrayList<>();
            for (JsonElement element : categories) {
                try {
                    String category = element.getAsString();
                    if (!categories_.contains(category)) {
                        categories_.add(category);
                    }
                } catch (Exception e) {
                    NexusApplication.getLogger().err("[Minecraft] Couldn't parse category: " + e.getMessage());
                }
            }
            String[] c;
            try {
                c = categories_.toArray(new String[0]);
            } catch (Exception e) {
                c = null;
                NexusApplication.getLogger().err("[Minecraft] Couldn't parse categories: " + e.getMessage());
            }
            this.categories = c;
        } else {
            this.categories = null;
        }

        JsonArray additionalCategories = json.getAsJsonArray("additional_categories");
        if (!additionalCategories.isEmpty()) {
            List<String> categories_ = new ArrayList<>();
            for (JsonElement element : additionalCategories) {
                try {
                    String category = element.getAsString();
                    if (!categories_.contains(category)) {
                        categories_.add(category);
                    }
                } catch (Exception e) {
                    NexusApplication.getLogger().err("[Minecraft] Couldn't parse additional category: " + e.getMessage());
                }
            }
            String[] c;
            try {
                c = categories_.toArray(new String[0]);
            } catch (Exception e) {
                c = null;
                NexusApplication.getLogger().err("[Minecraft] Couldn't parse additional categories: " + e.getMessage());
            }
            this.additionalCategories = c;
        } else {
            this.additionalCategories = null;
        }

        JsonArray loaders = json.getAsJsonArray("loaders");
        if (!loaders.isEmpty()) {
            List<String> loaders_ = new ArrayList<>();
            for (JsonElement element : loaders) {
                try {
                    String loader = element.getAsString();
                    if (!loaders_.contains(loader)) {
                        loaders_.add(loader);
                    }
                } catch (Exception e) {
                    NexusApplication.getLogger().err("[Minecraft] Couldn't parse loader: " + e.getMessage());
                }
            }
            String[] l;
            try {
                l = loaders_.toArray(new String[0]);
            } catch (Exception e) {
                l = null;
                NexusApplication.getLogger().err("[Minecraft] Couldn't parse loaders: " + e.getMessage());
            }
            this.loaders = l;
        } else {
            this.loaders = null;
        }

        JsonArray versions = json.getAsJsonArray("versions");
        if (!versions.isEmpty()) {
            List<String> versions_ = new ArrayList<>();
            for (JsonElement element : versions) {
                try {
                    String version = element.getAsString();
                    if (!versions_.contains(version)) {
                        versions_.add(version);
                    }
                } catch (Exception e) {
                    NexusApplication.getLogger().err("[Minecraft] Couldn't parse version: " + e.getMessage());
                }
            }
            String[] v;
            try {
                v = versions_.toArray(new String[0]);
            } catch (Exception e) {
                v = null;
                NexusApplication.getLogger().err("[Minecraft] Couldn't parse versions: " + e.getMessage());
            }
            this.versions = v;
        } else {
            this.versions = null;
        }

        if (json.has("icon_url")) {
            if (json.get("icon_url").getAsString().isEmpty()) {
                iconUrl = null;
            } else {
                iconUrl = json.get("icon_url").getAsString();
            }
        } else {
            iconUrl = null;
        }

        if (json.has("issues_url")) {
            if (json.get("issues_url").getAsString().isEmpty()) {
                issuesUrl = null;
            } else {
                issuesUrl = json.get("issues_url").getAsString();
            }
        } else {
            issuesUrl = null;
        }

        if (json.has("source_url")) {
            if (json.get("source_url").getAsString().isEmpty()) {
                sourceUrl = null;
            } else {
                sourceUrl = json.get("source_url").getAsString();
            }
        } else {
            sourceUrl = null;
        }

        if (json.has("wiki_url")) {
            if (json.get("wiki_url").getAsString().isEmpty()) {
                wikiUrl = null;
            } else {
                wikiUrl = json.get("wiki_url").getAsString();
            }
        } else {
            wikiUrl = null;
        }

        if (json.has("discord_url")) {
            if (json.get("discord_url").getAsString().isEmpty()) {
                discordUrl = null;
            } else {
                discordUrl = json.get("discord_url").getAsString();
            }
        } else {
            discordUrl = null;
        }

        if (json.has("donation_urls")) {
            donationUrls = json.getAsJsonArray("donation_urls");
        } else {
            donationUrls = null;
        }

        if (json.has("gallery")) {
            gallery = json.getAsJsonArray("gallery");
        } else {
            gallery = null;
        }

        if (json.has("color")) {
            if (json.get("color").getAsString().isEmpty()) {
                color = null;
            } else {
                color = json.get("color").getAsString();
            }
        } else {
            color = null;
        }

        if (json.has("thread_id")) {
            if (json.get("thread_id").getAsString().isEmpty()) {
                threadId = null;
            } else {
                threadId = json.get("thread_id").getAsString();
            }
        } else {
            threadId = null;
        }

        if (json.has("monetization_status")) {
            if (json.get("monetization_status").getAsString().isEmpty()) {
                monetizationStatus = null;
            } else {
                monetizationStatus = json.get("monetization_status").getAsString();
            }
        } else {
            monetizationStatus = null;
        }
    }

    public String getUrl() {
        return url;
    }

    public JsonObject getResultJson() {
        return json;
    }

    public ModrinthEnvironmentType getClientSideState() {
        return clientSide;
    }

    public ModrinthEnvironmentType getServerSideState() {
        return serverSide;
    }

    public String[] getGameVersions() {
        return gameVersions;
    }

    public String getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getProjectType() {
        return projectType;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getBody() {
        return body;
    }

    public String getDatePublished() {
        return published;
    }

    public String getDateUpdated() {
        return updated;
    }

    public String getDateApproved() {
        return approved;
    }

    public String getStatus() {
        return status;
    }

    public String getLicenseId() {
        return licenseId;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public int getDownloads() {
        return downloads;
    }

    public int getFollowers() {
        return followers;
    }

    public String[] getCategories() {
        return categories;
    }

    public String[] getAdditionalCategories() {
        return additionalCategories;
    }

    public String[] getLoaders() {
        return loaders;
    }

    public String[] getVersions() {
        return versions;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getIssuesUrl() {
        return issuesUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getWikiUrl() {
        return wikiUrl;
    }

    public String getDiscordUrl() {
        return discordUrl;
    }

    public JsonArray getDonationUrls() {
        return donationUrls;
    }

    public JsonArray getGallery() {
        return gallery;
    }

    public String getColor() {
        return color;
    }

    public String getThreadId() {
        return threadId;
    }

    public String getMonetizationStatus() {
        return monetizationStatus;
    }

    public ModrinthResourceVersion getLatestVersion(boolean unstable, boolean unlisted) {
        try {
            if (unstable) {
                if (unlisted) {
                    return new ModrinthResourceVersion(this, versions[versions.length - 1]);
                } else {
                    for (int i = versions.length-1; i >= 0; i--) {
                        String version = versions[i];
                        try {
                            ModrinthResourceVersion v = new ModrinthResourceVersion(this, version);
                            if (v.getStatus().equals("listed")) {
                                return v;
                            }
                        } catch (Exception ignore) {}
                    }
                }
            }
            for (int i = versions.length-1; i >= 0; i--) {
                String version = versions[i];
                try {
                    ModrinthResourceVersion v = new ModrinthResourceVersion(this, version);
                    if (v.getVersionType().equals("release")) {
                        if (unlisted) {
                            return v;
                        } else {
                            if (v.getStatus().equals("listed")) {
                                return v;
                            }
                        }
                    }
                } catch (Exception ignore) {
                }
            }
        } catch (Exception e) {
            NexusApplication.getLogger().err("[Minecraft] Couldn't get latest pack version: " + e.getMessage());
        }
        return null;
    }
}