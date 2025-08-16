package net.nrfy.nexus.launcher.integrations.modrinth;

import com.google.gson.JsonObject;
import com.zyneonstudios.nexus.utilities.NexusUtilities;
import com.zyneonstudios.nexus.utilities.json.GsonUtility;

public class ModrinthModpacks {

    public static JsonObject search(String query, String version, int offset, int limit) {
        if(version.equalsIgnoreCase("all")) {
            return search(query,offset,limit);
        }
        try {
            String search = "https://api.modrinth.com/v2/search?query="+query.toLowerCase()+"&facets=[[%22versions:"+version+"%22],[%22project_type:modpack%22]]&offset="+offset+"&limit="+limit;
            NexusUtilities.getLogger().deb(search);
            return GsonUtility.getObject(search);
        } catch (Exception e) {
            NexusUtilities.getLogger().err("[MODRINTH] (MODPACKS) Couldn't complete search: "+e.getMessage());
            return null;
        }
    }

    public static JsonObject search(String query, int offset, int limit) {
        try {
            String search = "https://api.modrinth.com/v2/search?query="+query.toLowerCase()+"&facets=[[%22project_type:modpack%22]]&offset="+offset+"&limit="+limit;
            NexusUtilities.getLogger().deb(search);
            return GsonUtility.getObject(search);
        } catch (Exception e) {
            NexusUtilities.getLogger().err("[MODRINTH] (MODPACKS) Couldn't complete search: "+e.getMessage());
            return null;
        }
    }
}