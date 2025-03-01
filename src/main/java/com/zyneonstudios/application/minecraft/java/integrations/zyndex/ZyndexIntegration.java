package com.zyneonstudios.application.minecraft.java.integrations.zyndex;

import com.zyneonstudios.application.download.Download;
import com.zyneonstudios.application.frame.web.ApplicationFrame;
import com.zyneonstudios.application.main.ApplicationStorage;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.JavaStorage;
import com.zyneonstudios.nexus.instance.Instance;
import com.zyneonstudios.nexus.instance.ReadableZynstance;
import com.zyneonstudios.nexus.utilities.file.FileExtractor;
import com.zyneonstudios.nexus.utilities.file.FileGetter;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

public class ZyndexIntegration {


    public static final InstanceSearch search = new InstanceSearch("https://raw.githubusercontent.com/zyneonstudios/nexus-nex/main/zyndex/index.json");;

    public static void searchModpacks(String query, int offset, ApplicationFrame frame) {
        if(offset == 0) {
            frame.executeJavaScript(
                    "addFilterGroup('mje-official-categories','Categories');" +
                            "addToggleFilter(\"Optimization\",\"mje-official-categories\",\"java.searchFilter.official.category.optimization\",true,true);" +

                            "addFilterGroup('mje-official-loaders','Loaders');" +
                            "addToggleFilter(\"Forge\",\"mje-official-loaders\",\"java.searchFilter.official.loader.forge\",true,true);" +
                            "addToggleFilter(\"Fabric\",\"mje-official-loaders\",\"java.searchFilter.official.loader.fabric\",true,true);" +

                            "addFilterGroup('mje-official-minecraft','Minecraft versions');" +
                            "addToggleFilter(\"Show all versions\",\"mje-official-minecraft\",\"java.searchFilter.official.minecraft.showAllVersions\",true,true);" +
                            "addSelectFilter('minecraftVersions','mje-official-minecraft','java.searchFilter.official.minecraft.versions',\"<option>All</option>\",true);"
            );
        }
        frame.executeJavaScript("document.getElementById('load-more').style.display = 'none';");

        String searchTerm = "";
        if (search.getCachedSearchTerm() != null) {
            searchTerm = search.getCachedSearchTerm();
            if (!searchTerm.isEmpty() && !searchTerm.isBlank()) {
                frame.executeJavaScript("document.getElementById(\"search-bar\").placeholder = \"" + searchTerm + "\";");
            }
        }

        if (search.getCachedResults() == null || !searchTerm.equals(query)) {
            search.search(query);
        }
        ArrayList<ReadableZynstance> results = search.getCachedResults();
        for (ReadableZynstance instance : results) {
            String tags = "Tags: " + instance.getTagString();
            String meta = instance.getId() + " | v" + instance.getVersion() + " | Hidden: " + instance.isHidden() + "<br>" + tags;
            String actions = "<a onclick=\\\"connector('java.init.details.instance." + URLEncoder.encode(instance.getLocation(), StandardCharsets.UTF_8) + "');\\\"><i class='bx bx-spreadsheet'></i> More</a> <a style=\\\"background: #5632a8; color: white;\\\" onclick=\\\"connector('async.java.install.nexInstance."+instance.getId()+"');\\\"><i class='bx bx-download'></i> Install</a>";
            String command = "addResult(\"" + instance.getId() + "\",\"" + instance.getThumbnailUrl() + "\",\"" + instance.getName() + "\",\"" + instance.getAuthor() + "\",\"" + instance.getSummary() + "\",\"" + meta + "\",\"" + actions + "\",\"" + instance.getLocation() + "\",\"java.init.details.instance." + instance.getLocation() + "\");";
            frame.executeJavaScript(command);
        }
    }

    public static boolean install(Instance instance, Path installDirPath) {
        return install(instance,installDirPath.toString());
    }

    public static boolean install(Instance instance, String installDirPathString) {
        return install(instance,new File(installDirPathString));
    }

    public static boolean install(Instance instance, File installDir) {
        if(installInstance(instance,installDir)) {
            JavaStorage.reloadLocalZyndex();
            System.gc();
            return true;
        }
        System.gc();
        return false;
    }

    @SuppressWarnings("all")
    private static boolean installInstance(Instance instance, File installDir) {
        try {
            if(!installDir.exists()) {
                if(!installDir.mkdirs()) {
                    throw new NullPointerException("Could not find or create instance directory \""+installDir.getAbsolutePath()+"\"");
                }
            }
            Path path = Paths.get(ApplicationStorage.getApplicationPath()+"temp/"+ UUID.randomUUID() +".zip");
            Download download = new Download(instance.getName(), URI.create(instance.getDownloadUrl()).toURL(),path);
            NexusApplication.getDownloadManager().addDownload(download);
            while (!download.isFinished()) {
                Thread.sleep(1000);
            }
            File zip = path.toFile();
            if(FileExtractor.unzipFile(zip.getAbsolutePath(),installDir.getAbsolutePath())) {
                zip.delete();
                return FileGetter.downloadFile(instance.getLocation(),installDir.getAbsolutePath()+"/zyneonInstance.json").exists();
            }
            zip.delete();
        } catch (Exception e) {
            NexusApplication.getLogger().err("[Minecraft] (ZyndexIntegration) Couldn't install instance "+instance.getId()+" v"+instance.getVersion()+": "+e.getMessage());
        }
        return false;
    }
}
