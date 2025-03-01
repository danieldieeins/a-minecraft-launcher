package com.zyneonstudios.application.minecraft.java.integrations.curseforge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zyneonstudios.application.frame.web.ApplicationFrame;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.integrations.curseforge.search.CurseForgeSearch;
import com.zyneonstudios.application.minecraft.java.integrations.curseforge.search.facets.CurseForgeFacetsBuilder;
import fr.flowarg.flowupdater.utils.IOUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CurseForgeIntegration {

    public static void searchModpacks(String query, int offset, ApplicationFrame frame) {
        if(offset==0) {
            frame.executeJavaScript(
                    "addFilterGroup('mje-curseforge-loaders','Mod Loaders');" +
                            "addToggleFilter(\"Forge\",\"mje-curseforge-loaders\",\"java.searchFilter.curseforge.loader.forge\",true,true);" +
                            "addToggleFilter(\"Fabric\",\"mje-curseforge-loaders\",\"java.searchFilter.curseforge.loader.fabric\",true,true);" +
                            "addToggleFilter(\"Quilt\",\"mje-curseforge-loaders\",\"java.searchFilter.curseforge.loader.quilt\",true,true);" +
                            "addToggleFilter(\"NeoForge\",\"mje-curseforge-loaders\",\"java.searchFilter.curseforge.loader.neoforge\",true,true);" +

                            "addFilterGroup('mje-curseforge-minecraft','Game Version');" +
                            "addSelectFilter('minecraftVersions','mje-curseforge-minecraft','java.searchFilter.curseforge.minecraft.versions',\"<option>All</option>\",true);" +

                            "addFilterGroup('mje-curseforge-categories','Categories');" +
                            "addToggleFilter(\"Adventure and RPG\",\"mje-curseforge-categories\",\"java.searchFilter.curseforge.category.\",true,true);" +
                            "addToggleFilter(\"Combat / PvP\",\"mje-curseforge-categories\",\"java.searchFilter.curseforge.category.\",true,true);" +
                            "addToggleFilter(\"Exploration\",\"mje-curseforge-categories\",\"java.searchFilter.curseforge.category.\",true,true);" +
                            "addToggleFilter(\"Extra Large\",\"mje-curseforge-categories\",\"java.searchFilter.curseforge.category.\",true,true);" +
                            "addToggleFilter(\"FTB Official Pack\",\"mje-curseforge-categories\",\"java.searchFilter.curseforge.category.\",true,true);" +
                            "addToggleFilter(\"Hardcore\",\"mje-curseforge-categories\",\"java.searchFilter.curseforge.category.\",true,true);" +
                            "addToggleFilter(\"Magic\",\"mje-curseforge-categories\",\"java.searchFilter.curseforge.category.\",true,true);" +
                            "addToggleFilter(\"Map Based\",\"mje-curseforge-categories\",\"java.searchFilter.curseforge.category.\",true,true);" +
                            "addToggleFilter(\"Mini Game\",\"mje-curseforge-categories\",\"java.searchFilter.curseforge.category.\",true,true);" +
                            "addToggleFilter(\"Multiplayer\",\"mje-curseforge-categories\",\"java.searchFilter.curseforge.category.\",true,true);" +
                            "addToggleFilter(\"Quests\",\"mje-curseforge-categories\",\"java.searchFilter.curseforge.category.\",true,true);" +
                            "addToggleFilter(\"Sci-Fi\",\"mje-curseforge-categories\",\"java.searchFilter.curseforge.category.\",true,true);" +
                            "addToggleFilter(\"Skyblock\",\"mje-curseforge-categories\",\"java.searchFilter.curseforge.category.\",true,true);" +
                            "addToggleFilter(\"Small / Light\",\"mje-curseforge-categories\",\"java.searchFilter.curseforge.category.\",true,true);" +
                            "addToggleFilter(\"Tech\",\"mje-curseforge-categories\",\"java.searchFilter.curseforge.category.\",true,true);" +
                            "addToggleFilter(\"Vanilla+\",\"mje-curseforge-categories\",\"java.searchFilter.curseforge.category.\",true,true);"
            );
        }
        frame.executeJavaScript("document.getElementById('load-more').style.display = 'unset';");

        CurseForgeSearch curseforgeSearch = new CurseForgeSearch();
        curseforgeSearch.setLimit(20);
        curseforgeSearch.setOffset(offset);
        if(!query.isEmpty()) {
            curseforgeSearch.setQuery(URLEncoder.encode(query, StandardCharsets.UTF_8));
        }
        CurseForgeFacetsBuilder facets = new CurseForgeFacetsBuilder();
        facets.withClassId(4471);
        curseforgeSearch.setFacets(facets.build());

        JsonObject json = curseforgeSearch.search();

        if(json.has("data")) {
            JsonArray results = json.getAsJsonArray("data");
            if(results.size() < 20) {
                frame.executeJavaScript("document.getElementById('load-more').style.display = 'none';");
            }
            for(JsonElement element:results) {
                try {
                    JsonObject result = element.getAsJsonObject();
                    String id = result.get("id").getAsString();
                    String name = result.get("name").getAsString();
                    StringBuilder tags = new StringBuilder("Tags: ");
                    if (result.has("categories")) {
                        for (JsonElement tagElement : result.getAsJsonArray("categories")) {
                            tags.append(tagElement.getAsJsonObject().get("slug").getAsString()).append(", ");
                        }
                    }
                    String categories = tags.toString();
                    String author = "Unknown";
                    if (result.has("authors")) {
                        author = result.getAsJsonArray("authors").get(0).getAsJsonObject().get("name").getAsString();
                    }
                    String description = "No summary...";
                    if (result.has("summary")) {
                        description = result.get("summary").getAsString();
                    } else if (result.has("description")) {
                        description = result.get("description").getAsString();
                    }
                    String downloads = result.get("downloadCount").getAsString();
                    String iconUrl = null;
                    if(result.has("logo")) {
                        iconUrl = result.get("logo").getAsJsonObject().get("url").getAsString();
                    }
                    String dateCreated = result.get("dateCreated").getAsString();
                    String dateModified = result.get("dateModified").getAsString();
                    String dateReleased = result.get("dateReleased").getAsString();
                    String meta = id + " (" + result.get("slug").getAsString() + ") | " + downloads + " downloads | created: " + dateCreated + " | modified: " + dateModified + " | released:" + dateReleased;
                    String actions = "<a onclick=\\\"connector('java.init.details.curseForgeInstance." + id + "');\\\"><i class='bx bx-spreadsheet'></i> More</a> <a style=\\\"background: #5632a8; color: white;\\\" onclick=\\\"connector('async.java.install.curseForgeInstance." + id + "');\\\"><i class='bx bx-download'></i> Install</a>";
                    String command = "addResult(\"" + id + "\",\"" + iconUrl + "\",\"" + name + "\",\"" + author + "\",\"" + description + "\",\"" + meta + "\",\"" + actions + "\",\"https://api.curseforge.com/v1/mods/" + id + "\",\"java.init.details.curseForgeInstance." + id + "\");";
                    frame.executeJavaScript(command);
                } catch (Exception e) {
                    NexusApplication.getLogger().err(e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            frame.executeJavaScript("document.getElementById('load-more').style.display = 'none';");
        }
    }

    public static String makeRequest(String url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("x-api-key", "$2a$10$DJiIWDCef9nkUl0fchY9eecGQunflMcS/TxFMn5Ng68cX5KpGOaEC");
            return IOUtils.getContent(connection.getInputStream());
        } catch (Exception e) {
            return null;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }
}