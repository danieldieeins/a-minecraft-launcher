package com.zyneonstudios.application.minecraft.java.installers.java;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.zyneonstudios.application.main.ApplicationStorage;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.installers.Installer;
import com.zyneonstudios.nexus.utilities.file.FileExtractor;
import com.zyneonstudios.nexus.utilities.file.FileGetter;
import com.zyneonstudios.nexus.utilities.json.GsonUtility;

import java.io.File;

public class JavaInstaller implements Installer {

    private Java runtimeVersion;
    private OperatingSystem operatingSystem;
    private Architecture architecture;

    public JavaInstaller() {
        runtimeVersion = null;
        operatingSystem = null;
        architecture = null;
    }

    public JavaInstaller(Java runtimeVersion, OperatingSystem operatingSystem, Architecture architecture) {
        this.runtimeVersion = runtimeVersion;
        this.operatingSystem = operatingSystem;
        this.architecture = architecture;
    }

    public String getVersionString() {
        String os = "null-";
        if(operatingSystem!=null) {
            os = operatingSystem.toString().toLowerCase()+"-";
        }
        String a = "null_";
        if(architecture!=null) {
            a = architecture.toString().toLowerCase()+"_";
        }
        String jre = "jre-null";
        if(runtimeVersion!=null) {
            if(runtimeVersion.equals(Java.Runtime_8)) {
                jre = "jre-8";
            } else if(runtimeVersion.equals(Java.Runtime_11)) {
                jre = "jre-11";
            } else {
                jre = "jre-21";
            }
        }
        return os+a+jre;
    }

    public Architecture getArchitecture() {
        return architecture;
    }

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public Java getRuntimeVersion() {
        return runtimeVersion;
    }

    public void setArchitecture(Architecture architecture) {
        this.architecture = architecture;
    }

    public void setOperatingSystem(OperatingSystem operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public void setRuntimeVersion(Java runtimeVersion) {
        this.runtimeVersion = runtimeVersion;
    }

    @Override
    public boolean install() {
        try {
            String versionString = getVersionString();
            if (versionString.contains("null")) {
                throw new NullPointerException("Couldn't find such a java version");
            } else {
                NexusApplication.getLogger().dbg("[INSTALLER] (JAVA) Gathering java information...");
                JsonObject runtimes = new Gson().fromJson(GsonUtility.getFromURL("https://raw.githubusercontent.com/danieldieeins/ZyneonApplicationContent/main/l/application.json"), JsonObject.class).getAsJsonObject("runtime");
                String download = runtimes.get(versionString).getAsString();
                String zipPath = ApplicationStorage.getApplicationPath() + "libraries/" + runtimeVersion + ".zip";
                NexusApplication.getLogger().dbg("[INSTALLER] (JAVA) Starting download from " + download + " to " + zipPath + "...");
                FileGetter.downloadFile(download, zipPath);
                FileExtractor.unzipFile(zipPath, ApplicationStorage.getApplicationPath() + "libraries/");
                NexusApplication.getLogger().dbg("[INSTALLER] (JAVA) Deleted zip-File: " + new File(zipPath).delete());
                NexusApplication.getLogger().log("[INSTALLER] (JAVA) Installed Java Runtime: " + versionString + "!");
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}