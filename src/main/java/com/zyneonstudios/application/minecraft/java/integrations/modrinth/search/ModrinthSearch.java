package com.zyneonstudios.application.minecraft.java.integrations.modrinth.search;

import com.google.gson.JsonObject;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.nexus.utilities.json.GsonUtility;

import java.util.UUID;

public class ModrinthSearch {

    private final UUID searchId = UUID.randomUUID();

    private String searchRequest = null;
    private String query = "";
    private String facets = "";
    private SortOrder sortOrder = SortOrder.relevance;
    private int offset = 0;
    private int limit = 20;

    public ModrinthSearch() {}

    public ModrinthSearch(String requestUrl) {
        searchRequest = requestUrl;
    }

    public ModrinthSearch(String query, String facets, SortOrder sortOrder, int offset, int limit) {
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

                String search = "https://api.modrinth.com/v2/search";

                if (query != null) {
                    if (!query.isEmpty() && !query.isBlank()) {
                        search = search + "?query=" + query;
                    }
                }

                if (facets != null) {
                    if (!facets.isEmpty() && !facets.isBlank()) {
                        if (search.contains("?")) {
                            search = search + "&facets=" + facets;
                        } else {
                            search = search + "?facets=" + facets;
                        }
                    }
                }

                if (sortOrder != null) {
                    if (search.contains("?")) {
                        search = search + "&index=" + sortOrder;
                    } else {
                        search = search + "?index=" + sortOrder;
                    }
                }

                if (offset > 0) {
                    if (search.contains("?")) {
                        search = search + "&offset=" + offset;
                    } else {
                        search = search + "?offset=" + offset;
                    }
                }

                if (limit > 0) {
                    if (search.contains("?")) {
                        search = search + "&limit=" + limit;
                    } else {
                        search = search + "?limit=" + limit;
                    }
                }

                searchRequest = search;
            }
            return GsonUtility.getObject(searchRequest);
        } catch (Exception e) {
            NexusApplication.getLogger().err("[Minecraft] Could not search through Modrinth: " + e.getMessage());
            return null;
        }
    }

    public enum SortOrder {

        relevance,
        downloads,
        follows,
        newest,
        updated

    }
}