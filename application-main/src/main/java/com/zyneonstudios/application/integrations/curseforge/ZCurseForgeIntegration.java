package com.zyneonstudios.application.integrations.curseforge;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zyneonstudios.ApplicationMain;
import com.zyneonstudios.application.Application;
import com.zyneonstudios.application.integrations.zyndex.ZyndexIntegration;
import com.zyneonstudios.nexus.utilities.file.FileExtractor;
import com.zyneonstudios.nexus.utilities.file.FileGetter;
import com.zyneonstudios.nexus.utilities.logger.NexusLogger;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;
import com.zyneonstudios.nexus.utilities.strings.StringGenerator;
import fr.flowarg.flowlogger.Logger;
import fr.flowarg.flowupdater.download.json.CurseModPackInfo;
import fr.flowarg.flowupdater.download.json.Mod;
import fr.flowarg.flowupdater.integrations.curseforgeintegration.CurseForgeIntegration;
import fr.flowarg.flowupdater.integrations.curseforgeintegration.CurseModPack;
import fr.flowarg.flowupdater.utils.IOUtils;
import fr.flowarg.openlauncherlib.NoFramework;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ZCurseForgeIntegration extends CurseForgeIntegration {

    private String pathString;
    private int id;
    private int v;
    private Path instancePath;
    private Path cachePath;
    private Path modsPath;
    private NexusLogger logger = ApplicationMain.getLogger();

    public ZCurseForgeIntegration(NexusLogger logger, int id, int v) throws Exception {
        super(new Logger("CURSE",null), Path.of(Application.getInstancePath()+"instances/curseforge-"+id+"-"+v+"/cache/"));
        this.id = id;
        this.v = v;
        pathString = Application.getInstancePath()+"instances/curseforge-"+id+"-"+v+"/";

        instancePath = Path.of(pathString);
        cachePath = Path.of(pathString+"cache/");
        modsPath = Path.of(pathString+"mods/");
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

    public static String getVersionId(int id, String ve) {
        JsonObject root = new Gson().fromJson(ZCurseForgeIntegration.makeRequest("https://api.curseforge.com/v1/mods/"+id+"/files?gameVersion="+ve), JsonObject.class);
        JsonArray array = root.get("data").getAsJsonArray();
        return array.get(0).getAsJsonObject().get("id").getAsString();
    }

    public static String getVersionId(int id, String ve, NoFramework.ModLoader loader) {
        JsonObject root = new Gson().fromJson(ZCurseForgeIntegration.makeRequest("https://api.curseforge.com/v1/mods/"+id+"/files?modLoaderType="+loader.toString()+"&gameVersion="+ve), JsonObject.class);
        JsonArray array = root.get("data").getAsJsonArray();
        return array.get(0).getAsJsonObject().get("id").getAsString();
    }

    public Path getCachePath() {
        return cachePath;
    }

    public Path getInstancePath() {
        return instancePath;
    }

    public Path getModsPath() {
        return modsPath;
    }

    public String getPathString() {
        return pathString;
    }

    public int getID() {
        return id;
    }

    public void install(int version) {
        logger.log("[CURSEFORGE] (INTEGRATION) Starting installation of CurseForge modpack "+id+"...");
        CompletableFuture.runAsync(()->{
            try {
                logger.log("[CURSEFORGE] (INTEGRATION) Getting modpack info for version "+version+"...");
                CurseModPackInfo info = new CurseModPackInfo(id,version,false);
                logger.log("[CURSEFORGE] (INTEGRATION) Resolving "+id+"-"+version+"...");
                CurseModPack pack = getCurseModPack(info);
                String packName = pack.getName();
                String packVersion = pack.getVersion();
                logger.log("[CURSEFORGE] (INTEGRATION) Resolved modpack "+packName+" v"+packVersion+"!");
                List<CurseModPack.CurseModPackMod> packFiles = pack.getMods();
                logger.log("[CURSEFORGE] (INTEGRATION) Starting download of "+packFiles.size()+" files...");
                downloadFiles(packFiles);
                File cache = new File(cachePath.toUri());
                if(cache.exists()) {
                    if(cache.isDirectory()) {
                        File[] elements = cache.listFiles();
                        assert elements != null;
                        for(File element:elements) {
                            if(element.getName().toLowerCase().endsWith(".zip")) {
                                File temp = new File(ApplicationMain.getDirectoryPath()+"temp/");
                                temp.mkdirs();
                                String tempID = StringGenerator.generateAlphanumericString(16);
                                ApplicationMain.getLogger().deb(element.getName()+" to "+ ApplicationMain.getDirectoryPath()+"temp/"+tempID+"/");
                                new File(ApplicationMain.getDirectoryPath()+"temp/"+tempID+"/").mkdirs();
                                if(FileExtractor.unzipFile(element.getAbsolutePath(), ApplicationMain.getDirectoryPath()+"temp/"+tempID+"/")) {
                                    if(new File(ApplicationMain.getDirectoryPath()+"temp/"+tempID+"/overrides/").exists()) {
                                        if(new File(ApplicationMain.getDirectoryPath()+"temp/"+tempID+"/overrides/").isDirectory()) {
                                            File zip = new File(ApplicationMain.getDirectoryPath()+"temp/"+tempID+"/overrides/");
                                            FileUtils.copyDirectory(zip,new File(pathString));
                                        }
                                    }
                                }
                            }
                            System.gc();
                        }
                    }
                }
                logger.deb(" ");
                logger.log("[CURSEFORGE] (INTEGRATION) Building zyneonInstance file from CurseForge data...");
                JsonStorage instance = new JsonStorage(pathString+"zyneonInstance.json");
                instance.set("modpack.id","curseforge-"+id+"-"+v);
                instance.set("modpack.name",packName);
                instance.set("modpack.version",packVersion);
                logger.log("[CURSEFORGE] (INTEGRATION) Gathering modloader and Minecraft infos...");
                JsonStorage curseforge = new JsonStorage(new File(pathString+"manifest.json"));
                String modloader = "Vanilla";
                String mlversion = "No mods";
                String minecraft = curseforge.getString("minecraft.version");
                if(curseforge.get("minecraft.modLoaders")!=null) {
                    JsonArray loaders = new Gson().fromJson(curseforge.get("minecraft.modLoaders").toString(),JsonArray.class);
                    for (int i = 0; i < loaders.size(); i++) {
                        JsonObject loader = loaders.get(i).getAsJsonObject();
                        String id = loader.get("id").getAsString();
                        if(id.startsWith("forge-")) {
                            modloader = "Forge";
                            mlversion = id.replace("forge-","");
                            instance.set("modpack.forge.version",mlversion);
                        } else if(id.startsWith("fabric-")) {
                            modloader = "Fabric";
                            mlversion = id.replace("fabric-","");
                            instance.set("modpack.fabric",mlversion);
                        } else if(id.startsWith("quilt-")) {
                            modloader = "Quilt";
                            mlversion = id.replace("quilt-","");
                            instance.set("modpack.quilt",mlversion);
                        } else if(id.startsWith("neoforge-")) {
                            modloader = "NeoForge";
                            mlversion = id.replace("neoforge-","");
                            instance.set("modpack.neoforge",mlversion);
                        }
                    }
                }
                logger.log("[CURSEFORGE] (INTEGRATION) Found "+modloader+" ("+mlversion+") for Minecraft "+minecraft+"!");
                instance.set("modpack.minecraft",minecraft);
                instance.set("modpack.instance","instances/curseforge-"+id+"-"+v);
                String description = "This is a modpack instance downloaded from CurseForge!";
                if(curseforge.getString("description")!=null) {
                    description = curseforge.getString("description");
                }
                instance.set("modpack.description",description);
                ZyndexIntegration.convert(instance.getJsonFile());
                logger.log("[CURSEFORGE] (INTEGRATION) Successfully built zyneonInstance file!");
                logger.log("[CURSEFORGE] (INTEGRATION) Installed CurseForge modpack "+packName+" v"+packVersion+"!");
                Application.loadInstances();
                Application.getFrame().getBrowser().loadURL(Application.getInstancesURL()+"?tab=curseforge-"+id+"-"+v);
            } catch (Exception e) {
                e.printStackTrace();
                logger.deb(Arrays.stream(e.getStackTrace()).toList().getFirst().toString());
                logger.err("[CURSEFORGE] (INTEGRATION) Couldn't initialise CurseForge modpack: "+e.getMessage());
                Application.getFrame().getBrowser().loadURL(Application.getInstancesURL());
            }
        });
    }

    private void downloadFiles(List<CurseModPack.CurseModPackMod> files) {
        for(Mod file:files) {
            String name = file.getName();
            String url = file.getDownloadURL();
            String sha1 = file.getSha1();
            long size = file.getSize();
            logger.deb(" ");
            logger.deb("[CURSEFORGE] (INTEGRATION) Preparing "+name+" from: "+url+"...");
            logger.deb("[CURSEFORGE] (INTEGRATION) Data: "+sha1+" ("+size+")");
            if(name.contains("/")) {
                logger.deb("[CURSEFORGE] (INTEGRATION) Created destination folder: "+new File(instancePath.toString()+"/"+name).getParentFile().mkdirs());
                logger.deb("[CURSEFORGE] (INTEGRATION) Downloading...");
                try {
                    logger.deb("[CURSEFORGE] (INTEGRATION) Downloaded "+ FileGetter.downloadFile(url, instancePath.toString() +"/"+ name).getAbsolutePath()+"!");
                } catch (Exception e) {
                    logger.err("[CURSEFORGE] (INTEGRATION) Couldn't download "+name+" from "+url+": "+e.getMessage());
                }
            } else {
                logger.deb("[CURSEFORGE] (INTEGRATION) Created destination folder: " + new File(modsPath.toString() +"/"+ name).getParentFile().mkdirs());
                logger.deb("[CURSEFORGE] (INTEGRATION) Downloading...");
                try {
                    logger.deb("[CURSEFORGE] (INTEGRATION) Downloaded "+FileGetter.downloadFile(url, modsPath.toString() +"/"+ name));
                } catch (Exception e) {
                    logger.err("[CURSEFORGE] (INTEGRATION) Couldn't download " + name + " from " + url + ": " + e.getMessage());
                }
            }
        }
    }
}