
# **a Minecraft Launcher** `2025.8.2`

A third party Minecraft launching library with an EULA compliant name. Based on [OpenLauncherLib](https://github.com/FlowArg/OpenLauncherLib) and [FlowUpdater](https://github.com/FlowArg/FlowUpdater) by [FlowArg](https://github.com/FlowArg).

Used by the **[NEXUS App](https://github.com/nerofynetwork/NEXUS-App)**, a **free** and **open source** Desktop application to install, manage and play Minecraft: Java Edition which supports downloading **modpacks, mods, shaders, worlds, resource packs** and **more** from **Modrinth**, **CurseForge** and **Zyndex instances**.
## Table of contents

- [How to this to your project](#how-to-add-this-library-to-your-project)
- [How to install a Minecraft instance](#how-to-install-a-minecraft-instance)
- [How to launch a Minecraft instance](#how-to-launch-a-minecraft-instance)

## How to add this library to your project



**via Maven:**

Add the official Nerofy Network Maven repository to your pom.xml.
```
<repositories>
    <!--Other repositories...-->

    <repository>
        <id>nerofy-releases</id>
        <name>Nerofy Network Maven repository</name>
        <url>https://maven.nrfy.net/releases</url>
    </repository>
</repositories>
```

After adding the repository, add the "a Minecraft Launcher" dependency.
```
<dependencies>
    <!--Other dependencies...-->

    <dependency>
        <groupId>live.nerotv</groupId>
        <artifactId>a-minecraft-launcher</artifactId>
        <version>2025.8.2</version>
    </dependency>
</dependencies>
```


**via Gradle (Kotlin)**

Add the official Nerofy Network Maven repository to your project.
```
maven {
    name = "nerofyReleases"
    url = uri("https://maven.nrfy.net/releases")
}
```

After adding the repository, add the "a Minecraft Launcher" dependency.
```
implementation("live.nerotv:a-minecraft-launcher:2025.8.2")
```


**via Gradle (Groovy)**

Add the official Nerofy Network Maven repository to your project.
```
maven {
    name "nerofyReleases"
    url "https://maven.nrfy.net/releases"
}
```

After adding the repository, add the "a Minecraft Launcher" dependency.
```
implementation "live.nerotv:a-minecraft-launcher:2025.8.2"
```
## How to install a Minecraft instance



Vanilla:
```java
//A Minecraft Vanilla Installer that installs the latest game version in the current folder.
new VanillaInstaller().install();

//A Minecraft vanilla installer that installs a specific version in a specific folder. Info: If the game got successfully installed, the method will return true, if not, the method will return false, so you can handle the outcome of the installation try.
if(new VanillaInstaller("1.16.5",".minecraft").install()) {
    //TODO do something after a successful game install
} else {
    //TODO do something after a failed game installation try
}

//or
VanillaInstaller installer = new VanillaInstaller();
installer.setMinecraftVersion("1.21.8");
installer.setInstallationPath(".minecraft"); //or use a path installer.setInstallationPath(Path.of(".minecraft"));
installer.install();
```

If you want to install a modloader, you need to initialize the correct installer object. Supported modloaders: Fabric, Forge, NeoForge and Quilt. Here is an example with the Fabric modloader:
```java
//A Minecraft Fabric installer that installs the latest game and modloader version in the current folder.
new FabricInstaller().install();

//A Minecraft Fabric installer that installs a specific Minecraft and Fabric version in a specific folder. Info: If the game got successfully installed, the method will return true, if not, the method will return false, so you can handle the outcome of the installation try.
if(new FabricInstaller("1.16.5","0.16.3",".minecraft-fabric").install()) {
    //TODO do something after a successful game install
} else {
    //TODO do something after a failed game installation try
}

//or
FabricInstaller installer = new FabricInstaller();
installer.setMinecraftVersion("1.21.8");
installer.setFabricVersion("0.17.2");
installer.setInstallationPath(".minecraft"); //or use a path installer.setInstallationPath(Path.of(".minecraft"));
installer.install();
```

If you want to use other modloaders than Fabric, just try to replace "Fabric" with your mod loaders name.


## How to launch a Minecraft instance

**Important:** The launcher will automatically install the specified Minecraft version and the selected modloader with its specific version if they are not present in the game path.

**Important¹:** You'll need a valid AuthInfos object to initialize the game launchers. For more information visit [OpenAuth](https://github.com/Litarvan/OpenAuth) by [Litarvan](https://github.com/Litarvan).

```
//Vanilla
VanillaLauncher launcher = new VanillaLauncher(authInfos);
//Minecraft 1.21.8 with 4GB (4096MB) of memory in the .minecraft folder with the ID "a-minecraft-installation"
launcher.launch("1.21.8",4096,Path.of(".minecraft","a-minecraft-installation"));

//Fabric
FabricLauncher fabricLauncher = new FabricLauncher(authInfos);
//Minecraft 1.21.8 with Fabric 0.17.2 with 4GB (4096MB) of memory in the .minecraft-fabric folder with the ID "a-minecraft-fabric-installation"
fabricLauncher.launch("1.21.8","0.17.2","4096",".minecraft-fabric","a-minecraft-fabric-installation");
```