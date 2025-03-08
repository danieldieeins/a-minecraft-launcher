package live.nerotv.napp.minecraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zyneonstudios.nexus.application.api.DiscoverAPI;
import com.zyneonstudios.nexus.application.api.LibraryAPI;
import com.zyneonstudios.nexus.application.api.SharedAPI;
import com.zyneonstudios.nexus.application.api.discover.search.zyndex.ZyndexSearch;
import com.zyneonstudios.nexus.application.api.library.zyndex.ZyndexLibrary;
import com.zyneonstudios.nexus.application.api.modules.ApplicationModule;
import com.zyneonstudios.nexus.application.api.shared.body.elements.BodyImage;
import com.zyneonstudios.nexus.application.api.shared.body.elements.BodyPage;
import com.zyneonstudios.nexus.application.api.shared.body.elements.BodyRow;
import com.zyneonstudios.nexus.application.api.shared.body.elements.BodyTextCard;
import com.zyneonstudios.nexus.desktop.NexusDesktop;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;

import java.io.File;
import java.util.Arrays;

public class MinecraftModule extends ApplicationModule {

    private ZyndexLibrary library;
    private final JsonStorage config;
    private final JsonStorage zyndex;

    public MinecraftModule() {
        super("a-minecraft-module", "a Minecraft Module", "3.0.0-alpha.13", new String[]{"nerotvlive"}, new JsonObject());
        new File(SharedAPI.getWorkingDirectory()+"/modules/a-minecraft-module/").mkdirs();
        config = new JsonStorage(SharedAPI.getWorkingDirectory()+"/modules/a-minecraft-module/config.json");
        zyndex = new JsonStorage(SharedAPI.getWorkingDirectory()+"/modules/a-minecraft-module/zyndex.json");
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
        zyndex.ensure("instances",new JsonArray());
        zyndex.ensure("modules",new JsonArray());

        library = new ZyndexLibrary(zyndex);
        library.setName("Minecraft: Java Edition");
        LibraryAPI.addLibrary(library);
    }

    private void initDiscover() {
        BodyImage descriptionImage = new BodyImage();
        descriptionImage.setAlt("NEXUS App logo");
        descriptionImage.setSrc("../assets/application/images/logos/app/normal.png");

        BodyTextCard descriptionCard = new BodyTextCard();
        descriptionCard.setTitle("About \""+getName()+"\":");
        descriptionCard.setText(getName()+" ("+getId()+") v"+getVersion()+" by "+ Arrays.toString(getAuthors()) +" is a open source minecraft launching and management module.");

        BodyRow descriptionRow = new BodyRow();
        descriptionRow.addElement(descriptionImage);
        descriptionRow.addElement(descriptionCard);

        BodyPage discoverPage = new BodyPage();
        discoverPage.setActive(true);
        discoverPage.setId("a-minecraft-module");
        discoverPage.setTitle("Minecraft");
        discoverPage.addElement(descriptionRow);

        try {
            DiscoverAPI.getDiscover().addPage(discoverPage);
        } catch (Exception ignore) {}
        initSearch();
    }

    private void initSearch() {
        try {
            ZyndexSearch nexSearch = new ZyndexSearch(DiscoverAPI.getNEX());
            nexSearch.setId("a-minecraft-module@official_nex@instances",true);
            nexSearch.setName("Official Minecraft instances",true);
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

    public JsonStorage getConfig() {
        return config;
    }

    public ZyndexLibrary getLibrary() {
        return library;
    }
}