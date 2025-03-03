package live.nerotv.napp.minecraft;

import com.google.gson.JsonObject;
import com.zyneonstudios.nexus.application.api.DiscoverAPI;
import com.zyneonstudios.nexus.application.api.discover.search.zyndex.ZyndexSearch;
import com.zyneonstudios.nexus.application.api.modules.ApplicationModule;
import com.zyneonstudios.nexus.application.api.shared.body.elements.*;
import com.zyneonstudios.nexus.desktop.NexusDesktop;

import java.util.Arrays;

public class MinecraftModule extends ApplicationModule {

    public MinecraftModule() {
        super("a-minecraft-module", "a Minecraft Module", "3.0.0-alpha.5", new String[]{"nerotvlive"}, new JsonObject());
    }

    @Override
    public void onLoad() {
        NexusDesktop.getLogger().log("Loading "+getName()+" ("+getId()+") v"+getVersion()+" by "+ Arrays.toString(getAuthors()) +"...");
    }

    @Override
    public void onEnable() {
        NexusDesktop.getLogger().log("Enabling "+getName()+" ("+getId()+") v"+getVersion()+" by "+ Arrays.toString(getAuthors()) +"...");
        initDiscover();
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
        discoverPage.setTitle("a Minecraft Module");
        discoverPage.addElement(descriptionRow);

        try {
            DiscoverAPI.getDiscover().addPage(discoverPage);
        } catch (Exception ignore) {}
        initSearch();
    }

    private void initSearch() {
        try {
            ZyndexSearch nexSearch = new ZyndexSearch(DiscoverAPI.getNEX());
            nexSearch.setId("a-minecraft-module@official.nex",true);
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
}