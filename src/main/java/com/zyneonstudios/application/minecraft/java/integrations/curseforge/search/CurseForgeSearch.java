package com.zyneonstudios.application.minecraft.java.integrations.curseforge.search;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.integrations.curseforge.CurseForgeIntegration;

import java.util.UUID;

public class CurseForgeSearch {

    private final UUID searchId = UUID.randomUUID();

    private String searchRequest = null;
    private String query = "";
    private String facets = "";
    private SortOrder sortOrder = SortOrder.Popularity;
    private int offset = 0;
    private int limit = 20;

    public CurseForgeSearch() {}

    public CurseForgeSearch(String requestUrl) {
        searchRequest = requestUrl;
    }

    public CurseForgeSearch(String query, String facets, SortOrder sortOrder, int offset, int limit) {
        this.query = query;
        this.facets = facets;
        this.sortOrder = sortOrder;
        this.offset = offset;
        this.limit = limit;
    }

    public void setSearchRequest(String searchRequest) {
        this.searchRequest = searchRequest;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setFacets(String facets) {
        this.facets = facets;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public UUID getSearchId() {
        return searchId;
    }

    public String getSearchRequest() {
        return searchRequest;
    }

    public String getQuery() {
        return query;
    }

    public String getFacets() {
        return facets;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public JsonObject search() {
        try {
            if (searchRequest == null) {
                String search = "https://api.curseforge.com/v1/mods/search?gameId=432";
                if (query != null) {
                    if (!query.isEmpty() && !query.isBlank()) {
                        String searchFilter = "searchFilter="+query;
                        search = search + "&" + searchFilter;
                    }
                }
                search = search + "&index=" + offset;
                search = search + "&pageSize=" + limit;
                if(facets != null && !facets.isEmpty()) {
                    search = search + facets;
                }
                searchRequest = search;
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.fromJson(CurseForgeIntegration.makeRequest(searchRequest), JsonObject.class);
        } catch (Exception e) {
            NexusApplication.getLogger().err("[Minecraft] Could not search through CurseForge: " + e.getMessage());
            return null;
        }
    }

    public enum SortOrder {
        Featured,
        Popularity,
        LastUpdated,
        Name,
        Author,
        TotalDownloads,
        Category,
        GameVersion,
        EarlyAccess,
        FeaturedReleased,
        ReleasedDate,
        Rating
    }
}