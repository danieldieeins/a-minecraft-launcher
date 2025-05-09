package com.zyneonstudios.application.integrations.modrinth;

import com.google.gson.JsonObject;
import com.zyneonstudios.ApplicationMain;
import com.zyneonstudios.nexus.utilities.json.GsonUtility;
import fr.flowarg.openlauncherlib.NoFramework;

public class ModrinthMods {

    public static JsonObject search(String query, NoFramework.ModLoader modLoader, String version, int offset, int limit) {
        if(version.equalsIgnoreCase("all")) {
            return search(query,modLoader,offset,limit);
        }
        try {
            String modloader = modLoader.toString().toLowerCase().replace("_","");
            String search = "https://api.modrinth.com/v2/search?query="+query.toLowerCase()+"&facets=[[%22categories:"+modloader+"%22],[%22versions:"+version+"%22],[%22project_type:mod%22]]&offset="+offset+"&limit="+limit;
            return GsonUtility.getObject(search);
        } catch (Exception e) {
            ApplicationMain.getLogger().err("[MODRINTH] (MODS) Couldn't complete search: "+e.getMessage());
            return null;
        }
    }

    public static JsonObject search(String query, NoFramework.ModLoader modLoader, int offset, int limit) {
        try {
            String modloader = modLoader.toString().toLowerCase().replace("_","");
            String search = "https://api.modrinth.com/v2/search?query="+query.toLowerCase()+"&facets=[[%22categories:"+modloader+"%22],[%22project_type:mod%22]]&offset="+offset+"&limit="+limit;
            return GsonUtility.getObject(search);
        } catch (Exception e) {
            ApplicationMain.getLogger().err("[MODRINTH] (MODS) Couldn't complete search: "+e.getMessage());
            return null;
        }
    }
}