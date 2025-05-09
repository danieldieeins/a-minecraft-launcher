package com.zyneonstudios.application.integrations.curseforge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.zyneonstudios.ApplicationMain;
import fr.flowarg.openlauncherlib.NoFramework;

public class CurseForgeMods {

    public static JsonObject search(String query, NoFramework.ModLoader modLoader, String version, int offset, int limit) {
        query=query.replace(" ","%20");
        if(version.equalsIgnoreCase("all")) {
            return search(query,modLoader,offset,limit);
        }
        try {
            String search = "https://api.curseforge.com/v1/mods/search?gameId=432&classId=6&modLoaderType="+modLoader+"&gameVersion="+version+"&searchFilter="+query+"&pageSize="+limit+"&index="+offset;
            Gson gson = new Gson();
            return gson.fromJson(ZCurseForgeIntegration.makeRequest(search), JsonObject.class);
        } catch (Exception e) {
            ApplicationMain.getLogger().err("[CURSEFORGE] (MODS) Couldn't complete search: "+e.getMessage());
            return null;
        }
    }

    public static JsonObject search(String query, NoFramework.ModLoader modLoader, int offset, int limit) {
        query=query.replace(" ","%20");
        try {
            String search = "https://api.curseforge.com/v1/mods/search?gameId=432&classId=6&modLoaderType="+modLoader+"&searchFilter="+query+"&pageSize="+limit+"&index="+offset;
            Gson gson = new Gson();
            return gson.fromJson(ZCurseForgeIntegration.makeRequest(search), JsonObject.class);
        } catch (Exception e) {
            ApplicationMain.getLogger().err("[CURSEFORGE] (MODS) Couldn't complete search: "+e.getMessage());
            return null;
        }
    }
}