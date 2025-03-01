package com.zyneonstudios.application.minecraft.java.integrations.modrinth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zyneonstudios.application.frame.web.ApplicationFrame;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.integrations.modrinth.search.ModrinthSearch;
import com.zyneonstudios.application.minecraft.java.integrations.modrinth.search.facets.ModrinthFacetsBuilder;
import com.zyneonstudios.application.minecraft.java.integrations.modrinth.search.facets.ModrinthProjectType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ModrinthIntegration {

    public static void searchModpacks(String query, int offset, ApplicationFrame frame) {
        if(offset==0) {
            frame.executeJavaScript(
                    "addFilterGroup('mje-modrinth-categories','Categories');" +
                            "addToggleFilter(\"Adventure\",\"mje-modrinth-categories\",\"java.searchFilter.modrinth.category.adventure\",true,true);" +
                            "addToggleFilter(\"Challenging\",\"mje-modrinth-categories\",\"java.searchFilter.modrinth.category.challenging\",true,true);" +
                            "addToggleFilter(\"Combat\",\"mje-modrinth-categories\",\"java.searchFilter.modrinth.category.combat\",true,true);" +
                            "addToggleFilter(\"Kitchen Sink\",\"mje-modrinth-categories\",\"java.searchFilter.modrinth.category.kitchen_sink\",true,true);" +
                            "addToggleFilter(\"Lightweight\",\"mje-modrinth-categories\",\"java.searchFilter.modrinth.category.lightweight\",true,true);" +
                            "addToggleFilter(\"Magic\",\"mje-modrinth-categories\",\"java.searchFilter.modrinth.category.magic\",true,true);" +
                            "addToggleFilter(\"Multiplayer\",\"mje-modrinth-categories\",\"java.searchFilter.modrinth.category.multiplayer\",true,true);" +
                            "addToggleFilter(\"Optimization\",\"mje-modrinth-categories\",\"java.searchFilter.modrinth.category.optimization\",true,true);" +
                            "addToggleFilter(\"Quests\",\"mje-modrinth-categories\",\"java.searchFilter.modrinth.category.quests\",true,true);" +
                            "addToggleFilter(\"Technology\",\"mje-modrinth-categories\",\"java.searchFilter.modrinth.category.technology\",true,true);" +

                            "addFilterGroup('mje-modrinth-loaders','Loaders');" +
                            "addToggleFilter(\"Fabric\",\"mje-modrinth-loaders\",\"java.searchFilter.modrinth.loader.fabric\",true,true);" +
                            "addToggleFilter(\"Forge\",\"mje-modrinth-loaders\",\"java.searchFilter.modrinth.loader.forge\",true,true);" +
                            "addToggleFilter(\"NeoForge\",\"mje-modrinth-loaders\",\"java.searchFilter.modrinth.loader.neoforge\",true,true);" +
                            "addToggleFilter(\"Quilt\",\"mje-modrinth-loaders\",\"java.searchFilter.modrinth.loader.quilt\",true,true);" +

                            "addFilterGroup('mje-modrinth-environments','Environments');" +
                            "addToggleFilter(\"Client\",\"mje-modrinth-environments\",\"java.searchFilter.modrinth.environment.client\",true,true);" +
                            "addToggleFilter(\"Server\",\"mje-modrinth-environments\",\"java.searchFilter.modrinth.environment.server\",true,true);" +

                            "addFilterGroup('mje-modrinth-minecraft','Minecraft versions');" +
                            "addToggleFilter(\"Show all versions\",\"mje-modrinth-minecraft\",\"java.searchFilter.modrinth.minecraft.showAllVersions\",true,true);" +
                            "addSelectFilter('minecraftVersions','mje-modrinth-minecraft','java.searchFilter.modrinth.minecraft.versions',\"<option>Unfiltered</option>\",true);"
            );
        }
        frame.executeJavaScript("document.getElementById('load-more').style.display = 'unset';");

        ModrinthSearch modrinthSearch = new ModrinthSearch();
        modrinthSearch.setLimit(20);
        modrinthSearch.setOffset(offset);
        if(!query.isEmpty()) {
            modrinthSearch.setQuery(URLEncoder.encode(query, StandardCharsets.UTF_8));
        }
        ModrinthFacetsBuilder facets = new ModrinthFacetsBuilder();
        facets.withProjectType(ModrinthProjectType.modpack);
        modrinthSearch.setFacets(facets.build());

        JsonObject json = modrinthSearch.search();

        if(json.has("hits")) {
            JsonArray results = json.getAsJsonArray("hits");
            if(results.size()<20) {
                frame.executeJavaScript("document.getElementById('load-more').style.display = 'none';");
            }
            for(JsonElement element:results) {
                try {
                    JsonObject result = element.getAsJsonObject();
                    String id = result.get("slug").getAsString();
                    String name = result.get("title").getAsString();
                    StringBuilder tags = new StringBuilder("Tags: ");
                    if (result.has("categories")) {
                        for (JsonElement tagElement : result.getAsJsonArray("categories")) {
                            tags.append(tagElement.getAsString()).append(", ");
                        }
                    }
                    String categories = tags.toString();
                    String author = result.get("author").getAsString();
                    String description = "No summary...";
                    if (result.has("description")) {
                        description = result.get("description").getAsString();
                    }
                    String downloads = result.get("downloads").getAsString();
                    String follows = result.get("follows").getAsString();
                    String iconUrl = result.get("icon_url").getAsString();
                    String dateCreated = result.get("date_created").getAsString();
                    String dateModified = result.get("date_modified").getAsString();
                    String license = result.get("license").getAsString();
                    String clientSide = result.get("client_side").getAsString();
                    String serverSide = result.get("server_side").getAsString();
                    String meta = id + " (" + result.get("project_id").getAsString() + ") | " + downloads + " downloads | " + follows + " follows | created: " + dateCreated + " | modified: " + dateModified + " | " + license + " license | client: " + clientSide + " | server: " + serverSide;
                    String actions = "<a onclick=\\\"connector('java.init.details.modrinthInstance." + id + "');\\\"><i class='bx bx-spreadsheet'></i> More</a> <a style=\\\"background: #5632a8; color: white;\\\" onclick=\\\"connector('async.java.install.modrinthInstance." + id + "');\\\"><i class='bx bx-download'></i> Install</a>";
                    String command = "addResult(\"" + id + "\",\"" + iconUrl + "\",\"" + name + "\",\"" + author + "\",\"" + description + "\",\"" + meta + "\",\"" + actions + "\",\"https://api.modrinth.com/v2/project/" + id + "\",\"java.init.details.modrinthInstance." + id + "\");";
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
}