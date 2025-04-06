package live.nerotv.napp.minecraft;

import com.google.gson.JsonArray;
import com.zyneonstudios.nexus.application.Main;
import com.zyneonstudios.nexus.application.modules.NexusModule;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;

import java.io.File;
import java.util.Arrays;

public class MinecraftModule extends NexusModule {

    private static JsonStorage config;
    private final JsonStorage zyndex;

    private final String id = "a-minecraft-module";
    private final String name = "a Minecraft module";
    private final String version = "3.0.0-alpha";
    private final String owner = "nerotvlive";
    private final String[] contributors = new String[0];

    public MinecraftModule() {
        new File(Main.getApplication().getWorkingPath()+"/modules/a-minecraft-module/").mkdirs();
        config = new JsonStorage(Main.getApplication().getWorkingPath()+"/modules/a-minecraft-module/config.json");
        zyndex = new JsonStorage(Main.getApplication().getWorkingPath() + "/modules/a-minecraft-module/java-instances.json");
    }

    @Override
    public final String getModuleId() {
        return id;
    }

    @Override
    public final String getModuleName() {
        return name;
    }

    @Override
    public final String getModuleVersion() {
        return version;
    }

    @Override
    public final String getModuleOwner() {
        return owner;
    }

    @Override
    public final String[] getModuleContributors() {
        return contributors;
    }

    @Override
    public void onLoad() {
        Main.getLogger().log("Loading "+getModuleName()+" ("+getModuleId()+") v"+getModuleVersion()+" by "+ Arrays.toString(getModuleAuthors()) +"...");
    }

    @Override
    public void onEnable() {
        Main.getLogger().log("Enabling "+getModuleName()+" ("+getModuleId()+") v"+getModuleVersion()+" by "+ Arrays.toString(getModuleAuthors()) +"...");
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