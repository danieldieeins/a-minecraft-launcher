package com.zyneonstudios.application.installer.java;

import com.zyneonstudios.ApplicationMain;
import com.zyneonstudios.nexus.utilities.file.FileExtractor;
import com.zyneonstudios.nexus.utilities.file.FileGetter;
import com.zyneonstudios.nexus.utilities.storage.ReadableJsonStorage;

import java.io.File;

public class JavaInstaller {

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

    public void install() {
        String versionString = getVersionString();
        if(versionString.contains("null")) {
            throw new NullPointerException("Couldn't find such a java version");
        } else {
            ApplicationMain.getLogger().deb("[INSTALLER] (JAVA) Gathering java information...");
            ReadableJsonStorage index = new ReadableJsonStorage("https://raw.githubusercontent.com/danieldieeins/ZyneonApplicationContent/main/l/application.json");
            String download = index.getString("runtime."+versionString);
            String zipPath = ApplicationMain.getDirectoryPath()+"libs/"+runtimeVersion+".zip";
            ApplicationMain.getLogger().deb("[INSTALLER] (JAVA) Starting download from "+download+" to "+zipPath+"...");
            FileGetter.downloadFile(download, zipPath);
            FileExtractor.unzipFile(zipPath, ApplicationMain.getDirectoryPath()+"libs/");
            ApplicationMain.getLogger().deb("[INSTALLER] (JAVA) Deleted zip-File: "+new File(zipPath).delete());
            ApplicationMain.getLogger().log("[INSTALLER] (JAVA) Installed Java Runtime: "+versionString+"!");
        }
    }
}