package com.zyneonstudios.application.minecraft.java.integrations.modrinth.search.facets;

import com.zyneonstudios.application.minecraft.java.integrations.modrinth.search.facets.categories.ModrinthCategory;

import java.util.ArrayList;

public class ModrinthFacetsBuilder {

    private String facets = "[";
    private ArrayList<String> categories = new ArrayList<>();
    private ArrayList<String> gameVersions = new ArrayList<>();

    public ModrinthFacetsBuilder withProjectType(String projectType) {
        facets = facets + "[%22project_type:" + projectType + "%22]";
        return this;
    }

    public ModrinthFacetsBuilder withProjectType(ModrinthProjectType modrinthProjectType) {
        return withProjectType(modrinthProjectType.toString());
    }

    public ModrinthFacetsBuilder withCategory(String category) {
        category = category.replace("feature_", "")
                .replace("loader_", "")
                .replace("performance_", "")
                .replace("proxy_", "")
                .replace("resolution_", "")
                .replace("_and_lower", "-")
                .replace("_and_higher", "+")
                .replace("_", "-");
        if(!categories.contains(category)) {
            categories.add(category);
        }
        return this;
    }

    public ModrinthFacetsBuilder withCategory(ModrinthCategory modrinthCategory) {
        return withCategory(modrinthCategory.toString());
    }

    public ModrinthFacetsBuilder withCategories(String... categories) {
        for (String category : categories) {
            withCategory(category);
        }
        return this;
    }

    public ModrinthFacetsBuilder withCategories(ModrinthCategory... categories) {
        for (ModrinthCategory modrinthCategory : categories) {
            withCategory(modrinthCategory);
        }
        return this;
    }

    public ModrinthFacetsBuilder withGameVersion(String gameVersion) {
        if(!gameVersions.contains(gameVersion)) {
            gameVersions.add(gameVersion);
        }
        return this;
    }

    public ModrinthFacetsBuilder withGameVersions(String... gameVersions) {
        for(String gameVersion : gameVersions) {
            withGameVersion(gameVersion);
        }
        return this;
    }

    public ModrinthFacetsBuilder withClientEnvironment(ModrinthEnvironmentType type) {
        facets = facets + "[%22client_side:" + type + "%22]";
        return this;
    }

    public ModrinthFacetsBuilder withServerEnvironment(ModrinthEnvironmentType type) {
        facets = facets + "[%22server_side:" + type + "%22]";
        return this;
    }

    public ModrinthFacetsBuilder withEnvironments(ModrinthEnvironmentType clientSide, ModrinthEnvironmentType serverSide) {
        facets = facets + "[%22client_side:" + clientSide + "%22][%22server_side:" + serverSide + "%22]";
        return this;
    }

    public ModrinthFacetsBuilder withOpenSourceOnly(boolean openSource) {
        facets = facets + "[%22open_source:" + openSource + "%22]";
        return this;
    }

    public ModrinthFacetsBuilder withTitle(String title) {
        facets = facets + "[%22title:" + title + "%22]";
        return this;
    }

    public ModrinthFacetsBuilder withAuthor(String author) {
        facets = facets + "[%22author:" + author + "%22]";
        return this;
    }

    public ModrinthFacetsBuilder withFollows(int follows) {
        facets = facets + "[%22follows:" + follows + "%22]";
        return this;
    }

    public ModrinthFacetsBuilder withProjectId(String id) {
        facets = facets + "[%22project_id:" + id + "%22]";
        return this;
    }

    public ModrinthFacetsBuilder withLicense(String license) {
        facets = facets + "[%22license:" + license + "%22]";
        return this;
    }

    public ModrinthFacetsBuilder withDownloads(int downloads) {
        facets = facets + "[%22downloads:" + downloads + "%22]";
        return this;
    }

    public ModrinthFacetsBuilder withColor(String color) {
        facets = facets + "[%22color:" + color + "%22]";
        return this;
    }

    public ModrinthFacetsBuilder withCreatedTimestamp(String timestamp) {
        facets = facets + "[%22created_timestamp:" + timestamp + "%22]";
        return this;
    }

    public ModrinthFacetsBuilder withModifiedTimestamp(String timestamp) {
        facets = facets + "[%22modified_timestamp:" + timestamp + "%22]";
        return this;
    }

    public String build() {
        if(!categories.isEmpty()) {
            StringBuilder categories = new StringBuilder();
            for(String category : this.categories) {
                if((categories.isEmpty()) || categories.toString().isBlank()) {
                    categories = new StringBuilder(category);
                } else {
                    categories.append(",").append(category);
                }
            }
            facets = facets + "[%22categories:" + categories + "%22]";
        }

        if(!gameVersions.isEmpty()) {
            StringBuilder gameVersions = new StringBuilder();
            for(String gameVersion : this.gameVersions) {
                if(gameVersions.isEmpty() || gameVersions.toString().isBlank()) {
                    gameVersions = new StringBuilder(gameVersion);
                } else {
                    gameVersions.append(",").append(gameVersion);
                }
            }
            facets = facets + "[%22versions:" + gameVersions + "%22]";
        }

        facets = ((facets + "]").replace("][", "],["));
        return facets;
    }
}