package com.zyneonstudios.application.minecraft.java.integrations.curseforge.search.facets;

import java.util.Arrays;

public class CurseForgeFacetsBuilder {

    private Integer classId = null;
    private Integer categoryId = null;
    private int[] categoryIds = null;
    private String gameVersion = null;
    private String[] gameVersions = null;
    private ModLoaderType modLoaderType = null;
    private ModLoaderType[] modLoaderTypes = null;
    private Integer gameVersionTypeId = null;
    private Integer authorId = null;
    private Integer primaryAuthorId = null;
    private String slug = null;

    public CurseForgeFacetsBuilder withClassId(int classId) {
        this.classId = classId;
        return this;
    }

    public CurseForgeFacetsBuilder withCategoryId(int categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public CurseForgeFacetsBuilder withCategoryIds(int[] categoryIds) {
        this.categoryIds = categoryIds;
        return this;
    }

    public CurseForgeFacetsBuilder withGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
        return this;
    }

    public CurseForgeFacetsBuilder withGameVersions(String[] gameVersions) {
        this.gameVersions = gameVersions;
        return this;
    }

    public CurseForgeFacetsBuilder withModLoaderType(ModLoaderType modLoader) {
        this.modLoaderType = modLoader;
        return this;
    }

    public CurseForgeFacetsBuilder withModLoaderTypes(ModLoaderType[] modLoaders) {
        this.modLoaderTypes = modLoaders;
        return this;
    }

    public CurseForgeFacetsBuilder withGameVersionTypeId(int gameVersionTypeId) {
        this.gameVersionTypeId = gameVersionTypeId;
        return this;
    }

    public CurseForgeFacetsBuilder withAuthorId(int authorId) {
        this.authorId = authorId;
        return this;
    }

    public CurseForgeFacetsBuilder withPrimaryAuthorId(int primaryAuthorId) {
        this.primaryAuthorId = primaryAuthorId;
        return this;
    }

    public CurseForgeFacetsBuilder withSlug(String slug) {
        this.slug = slug;
        return this;
    }

    public String build() {
        String facets = "";
        if(classId != null) {
            facets = facets+"&classId=" + classId;
        }
        if(categoryId != null) {
            facets = facets+"&categoryId=" + categoryId;
        }
        if(categoryIds != null) {
            facets = facets+"&categoryIds=" + Arrays.toString(categoryIds).replace("[","]");
        }
        if(gameVersion != null) {
            facets = facets+"&gameVersion=" + gameVersion;
        }
        if(gameVersions != null) {
            facets = facets+"&gameVersions=" + Arrays.toString(gameVersions).replace("[","]");
        }
        if(modLoaderType != null) {
            facets = facets+"&modLoaderType=" + modLoaderType;
        }
        if(modLoaderTypes != null) {
            facets = facets+"&modLoaderTypes=" + Arrays.toString(modLoaderTypes).replace("[","]");
        }
        if(gameVersionTypeId != null) {
            facets = facets+"&gameVersionTypeId=" + gameVersionTypeId;
        }
        if(authorId != null) {
            facets = facets+"&authorId=" + authorId;
        }
        if(primaryAuthorId != null) {
            facets = facets+"&primaryAuthorId=" + primaryAuthorId;
        }
        if(slug != null) {
            facets = facets+"&slug=" + slug;
        }
        return facets;
    }

    public enum ModLoaderType {
        Any,
        Forge,
        Cauldron,
        LiteLoader,
        Fabric,
        Quilt,
        NeoForge
    }
}