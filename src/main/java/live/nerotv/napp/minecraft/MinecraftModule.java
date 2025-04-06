package live.nerotv.napp.minecraft;

import com.google.gson.JsonArray;
import com.zyneonstudios.nexus.application.Main;
import com.zyneonstudios.nexus.application.main.NexusApplication;
import com.zyneonstudios.nexus.application.modules.NexusModule;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;

import java.io.File;
import java.util.Arrays;

public class MinecraftModule extends NexusModule {

    private static JsonStorage config;
    private final JsonStorage zyndex;

    public MinecraftModule() {
        new File(NexusApplication.getInstance().getWorkingPath()+"/modules/a-minecraft-module/").mkdirs();
        config = new JsonStorage(NexusApplication.getInstance().getWorkingPath()+"/modules/a-minecraft-module/config.json");
        zyndex = new JsonStorage(NexusApplication.getInstance().getWorkingPath() + "/modules/a-minecraft-module/java-instances.json");
    }

    @Override
    public final String getModuleId() {
        return "a-minecraft-module";
    }

    @Override
    public final String getModuleName() {
        return "a Minecraft module";
    }

    @Override
    public final String getModuleVersion() {
        return "3.0.0-alpha";
    }

    @Override
    public final String getModuleOwner() {
        return "nerotvlive";
    }

    @Override
    public final String[] getModuleContributors() {
        return new String[0];
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        Main.getLogger().log("Enabling "+getModuleName()+" ("+getModuleId()+") v"+getModuleVersion()+" by "+ getModuleOwner() +"...");
        initLibrary();
    }

    private void initLibrary() {
        zyndex.set("name","official_local");
        zyndex.set("url","file://"+zyndex.getJsonFile().getAbsoluteFile().toString().replace("\\","/"));
        zyndex.set("owner","a-minecraft-module");
        zyndex.set("modules",new JsonArray());
        zyndex.ensure("instances",new JsonArray());
    }

    @Override
    public void onDisable() {
        Main.getLogger().log("Disabling "+getModuleName()+" ("+getModuleId()+") v"+getModuleVersion()+" by "+ Arrays.toString(getModuleAuthors()) +"...");
    }

    public static JsonStorage getConfig() {
        return config;
    }
}