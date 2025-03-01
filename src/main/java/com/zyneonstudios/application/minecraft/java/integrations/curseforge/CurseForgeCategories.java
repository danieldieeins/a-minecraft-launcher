package com.zyneonstudios.application.minecraft.java.integrations.curseforge;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class CurseForgeCategories {

    private static HashMap<Integer, String> categorySlugs = new HashMap<>();
    private static HashMap<String, Integer> categoryIds = new HashMap<>();

    public static void init() {
        JsonArray data = new GsonBuilder().setPrettyPrinting().create().fromJson(CurseForgeIntegration.makeRequest("https://api.curseforge.com/v1/categories?gameId=432"), JsonObject.class).getAsJsonArray("data");
        for (JsonElement jsonElement : data) {
            JsonObject category = jsonElement.getAsJsonObject();
            categorySlugs.put(category.get("id").getAsInt(), category.get("slug").getAsString());
        }
        for (Map.Entry<Integer, String> entry : categorySlugs.entrySet()) {
            int key = entry.getKey();
            String value = entry.getValue();
            categoryIds.put(value, key);
        }
    }

    @Deprecated
    public static int getCategoryIdBySlug(String slug) {
        return categoryIds.get(slug);
    }

    public static String getCategorySlugById(int id) {
        return categorySlugs.get(id);
    }
}