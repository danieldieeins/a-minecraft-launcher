package com.zyneonstudios.application.integrations.curseforge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.zyneonstudios.ApplicationMain;

public class CurseForgeShaders {

    public static JsonObject search(String query, String version, int offset, int limit) {
        query=query.replace(" ","%20");
        if(version.equalsIgnoreCase("all")) {
            return search(query,offset,limit);
        }
        try {
            String search = "https://api.curseforge.com/v1/mods/search?gameId=432&classId=6552&gameVersion="+version+"&searchFilter="+query+"&pageSize="+limit+"&index="+offset;
            Gson gson = new Gson();
            return gson.fromJson(ZCurseForgeIntegration.makeRequest(search), JsonObject.class);
        } catch (Exception e) {
            ApplicationMain.getLogger().err("[CURSEFORGE] (SHADERS) Couldn't complete search: "+e.getMessage());
            return null;
        }
    }

    public static JsonObject search(String query, int offset, int limit) {
        query=query.replace(" ","%20");
        try {
            String search = "https://api.curseforge.com/v1/mods/search?gameId=432&classId=6552&searchFilter="+query+"&pageSize="+limit+"&index="+offset;
            Gson gson = new Gson();
            return gson.fromJson(ZCurseForgeIntegration.makeRequest(search), JsonObject.class);
        } catch (Exception e) {
            ApplicationMain.getLogger().err("[CURSEFORGE] (SHADERS) Couldn't complete search: "+e.getMessage());
            return null;
        }
    }
}