package live.nerotv.napp.minecraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zyneonstudios.nexus.application.api.DiscoverAPI;
import com.zyneonstudios.nexus.application.api.LibraryAPI;
import com.zyneonstudios.nexus.application.api.SharedAPI;
import com.zyneonstudios.nexus.application.api.discover.search.zyndex.ZyndexSearch;
import com.zyneonstudios.nexus.application.api.library.events.LibraryPreLoadEvent;
import com.zyneonstudios.nexus.application.api.modules.ApplicationModule;
import com.zyneonstudios.nexus.desktop.NexusDesktop;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;
import live.nerotv.napp.minecraft.discover.MinecraftDiscoverPage;
import live.nerotv.napp.minecraft.java.library.JavaLibrary;

import java.io.File;
import java.util.Arrays;

public class MinecraftModule extends ApplicationModule {

    private static JsonStorage config;
    private final JsonStorage zyndex;

    public MinecraftModule() {
        super("a-minecraft-module", "a Minecraft Module", "3.0.0-alpha.21", new String[]{"nerotvlive"}, new JsonObject());
        new File(SharedAPI.getWorkingDirectory()+"/modules/a-minecraft-module/").mkdirs();
        config = new JsonStorage(SharedAPI.getWorkingDirectory()+"/modules/a-minecraft-module/config.json");
        zyndex = new JsonStorage(SharedAPI.getWorkingDirectory() + "/modules/a-minecraft-module/java-instances.json");
    }

    @Override
    public void onLoad() {
        NexusDesktop.getLogger().log("Loading "+getName()+" ("+getId()+") v"+getVersion()+" by "+ Arrays.toString(getAuthors()) +"...");
    }

    @Override
    public void onEnable() {
        NexusDesktop.getLogger().log("Enabling "+getName()+" ("+getId()+") v"+getVersion()+" by "+ Arrays.toString(getAuthors()) +"...");
        initDiscover();
        initLibrary();
    }

    private void initLibrary() {
        zyndex.set("name","official_local");
        zyndex.set("url","file://"+zyndex.getJsonFile().getAbsoluteFile().toString().replace("\\","/"));
        zyndex.set("owner","a-minecraft-module");
        zyndex.set("modules",new JsonArray());
        zyndex.ensure("instances",new JsonArray());
        new JavaLibrary(zyndex);

        LibraryAPI.registerEvent(new LibraryPreLoadEvent(JavaLibrary.getInstance()) {
            @Override
            public boolean beforeLoad() {
                if(getLibrary()!=null) {

                    return true;
                }
                return false;
            }
        });
    }

    private void initDiscover() {
        try {
            DiscoverAPI.getDiscover().addPage(new MinecraftDiscoverPage());
        } catch (Exception ignore) {}
        initSearch();
    }

    private void initSearch() {
        try {
            ZyndexSearch nexSearch = new ZyndexSearch(DiscoverAPI.getNEX());
            nexSearch.setId("a-minecraft-module@official_nex@instances",true);
            nexSearch.setName("Minecraft: Java Edition modpacks",true);
            DiscoverAPI.getDiscover().getSearch().addSearchSource(nexSearch);
        } catch (Exception e) {
            NexusDesktop.getLogger().err("Couldn't load official Zyndex \"NEX\": "+e.getMessage());
            NexusDesktop.getLogger().err("Disabled module search...");
        }
    }

    @Override
    public void onDisable() {
        NexusDesktop.getLogger().log("Disabling "+getName()+" ("+getId()+") v"+getVersion()+" by "+ Arrays.toString(getAuthors()) +"...");
    }

    public static JsonStorage getConfig() {
        return config;
    }

    public static JavaLibrary getLibrary() {
        return JavaLibrary.getInstance();
    }
}