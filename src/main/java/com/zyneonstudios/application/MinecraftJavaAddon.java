package com.zyneonstudios.application;

import com.zyneonstudios.Main;
import com.zyneonstudios.application.frame.ZyneonSplash;
import com.zyneonstudios.application.frame.web.ApplicationFrame;
import com.zyneonstudios.application.main.ApplicationStorage;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.JavaConnector;
import com.zyneonstudios.application.minecraft.java.JavaStorage;
import com.zyneonstudios.application.minecraft.java.authentication.MicrosoftAuthenticator;
import com.zyneonstudios.application.minecraft.java.integrations.curseforge.CurseForgeCategories;
import com.zyneonstudios.application.minecraft.java.integrations.zyndex.ZyndexIntegration;
import com.zyneonstudios.application.modules.ApplicationModule;
import com.zyneonstudios.nexus.desktop.NexusDesktop;
import com.zyneonstudios.nexus.utilities.NexusUtilities;
import com.zyneonstudios.nexus.utilities.file.FileExtractor;
import com.zyneonstudios.nexus.utilities.logger.NexusLogger;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;
import com.zyneonstudios.nexus.utilities.strings.StringGenerator;

import javax.crypto.KeyGenerator;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;

public class MinecraftJavaAddon extends ApplicationModule {

    private MicrosoftAuthenticator authenticator = null;
    private AuthState authState = AuthState.LOGGED_OUT;
    private final byte[] key;

    public MinecraftJavaAddon(NexusApplication application, String id, String name, String version, String authors) {
        super(application, id, name, version, authors);
        try {
            KeyGenerator keyGenerator;
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            key = keyGenerator.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            onDisable();
            throw new RuntimeException(e);
        }
    }

    public MicrosoftAuthenticator getAuthenticator(byte[] key) {
        if (key == this.key) {
            if (authenticator == null) {
                authenticator = createNewAuthenticator();
            }
            return authenticator;
        } else {
            return null;
        }
    }

    public AuthState getAuthState() {
        return authState;
    }

    public void setAuthState(AuthState authState) {
        this.authState = authState;
    }

    public MicrosoftAuthenticator createNewAuthenticator() {
        if (authenticator != null) {
            File old = authenticator.getSaveFile();
            authenticator.destroy();
            authenticator = null;
            System.gc();
            if (!old.delete()) {
                NexusApplication.getLogger().err("[Minecraft] Couldn't delete old auth file...");
                JsonStorage oldAuth = new JsonStorage(old);
                try {
                    oldAuth.delete("op");
                    oldAuth.delete("opapi");
                } catch (Exception e) {
                    NexusApplication.getLogger().err("[Minecraft] Couldn't reset auth credentials...");
                    return null;
                }
            }
        }
        authenticator = new MicrosoftAuthenticator(this);
        return authenticator;
    }


    private void load() {
        NexusApplication.getLogger().log(" ");
        NexusApplication.getLogger().log("==(GAME MODULE)===================================================================");
        NexusApplication.getLogger().log("Module: " + getName() + " (id: " + getId() + ")");
        NexusApplication.getLogger().log("Authors: " + getAuthors());
        NexusApplication.getLogger().log("Version: " + getVersion());
        NexusApplication.getLogger().log("--------------------------------------------------------------------(NEXUS Team)--");
        String prefix = "[" + getName() + "] ";
        NexusApplication.getLogger().log(prefix + "Loading...");

        NexusApplication.getLogger().log(prefix + "Building module storage...");
        JavaStorage.init(getId());

        NexusApplication.getLogger().log(prefix + "Building UI...");
        update();

        NexusApplication.getLogger().log(prefix + "Setting module connector to new JavaConnector...");
        setConnector(new JavaConnector(this, key));

        CompletableFuture.runAsync(()-> {
            NexusApplication.getLogger().log(prefix + "Loading Microsoft authentication...");
            createNewAuthenticator();
            if (authenticator.isLoggedIn()) {
                NexusApplication.getLogger().log(prefix + "Logged in as " + authenticator.getAuthInfos().getUsername() + ": " + authenticator.getAuthInfos().getUuid());
                authState = AuthState.LOGGED_IN;
            } else {
                NexusApplication.getLogger().log(prefix + "Not logged in...");
                authState = AuthState.LOGGED_OUT;
            }
        });

        CompletableFuture.runAsync(()->{
            NexusApplication.getLogger().log(prefix + "Initializing CurseForge categories...");
            CurseForgeCategories.init();
        });

        NexusApplication.getLogger().log(prefix + "Caching NEX...");
        try {
            ZyndexIntegration.searchModpacks("", 0, (ApplicationFrame) getApplication().getFrame());
        } catch (Exception e) {
            NexusApplication.getLogger().err("[Minecraft] Couldn't cache NEX: " + e.getMessage());
        }

        NexusApplication.getLogger().log(prefix + "Loaded!");
        NexusApplication.getLogger().log(" ");
    }

    @Override
    public void onEnable() {
        CompletableFuture.runAsync(()->{
            load();
            enable();
        });
    }

    private void enable() {
        NexusApplication.getLogger().log(" ");
        NexusApplication.getLogger().log("==(GAME MODULE)===================================================================");
        NexusApplication.getLogger().log("Module: " + getName() + " (id: " + getId() + ")");
        NexusApplication.getLogger().log("Authors: " + getAuthors());
        NexusApplication.getLogger().log("Version: " + getVersion());
        NexusApplication.getLogger().log("--------------------------------------------------------------------(NEXUS Team)--");
        String prefix = "[" + getName() + "] ";
        NexusApplication.getLogger().log(prefix + "Enabling...");
        NexusApplication.getLogger().log(prefix + "Enabled!");
        NexusApplication.getLogger().log(" ");
    }

    @Override
    public void onDisable() {
        NexusApplication.getLogger().log(" ");
        NexusApplication.getLogger().log("==(GAME MODULE)===================================================================");
        NexusApplication.getLogger().log("Module: " + getName() + " (id: " + getId() + ")");
        NexusApplication.getLogger().log("Authors: " + getAuthors());
        NexusApplication.getLogger().log("Version: " + getVersion());
        NexusApplication.getLogger().log("--------------------------------------------------------------------(NEXUS Team)--");
        String prefix = "[" + getName() + "] ";
        NexusApplication.getLogger().log(prefix + "Disabling...");
        NexusApplication.getLogger().log(prefix + "Disabled!");
        NexusApplication.getLogger().log(" ");
    }

    public enum AuthState {
        LOGGING_IN,
        LOGGED_IN,
        LOGGED_OUT
    }

    private void update() {
        NexusLogger logger = NexusApplication.getLogger();
        try {
            if (new File(ApplicationStorage.getApplicationPath() + "temp/ui/").exists()) {
                logger.dbg("[Minecraft] Deleted old ui files: " + new File(ApplicationStorage.getApplicationPath() + "temp/ui/").delete());
            }
            logger.dbg("[Minecraft] Created new ui path: " + new File(ApplicationStorage.getApplicationPath() + "temp/ui/").mkdirs());
            FileExtractor.extractResourceFile("html.zip", ApplicationStorage.getApplicationPath() + "temp/mje.zip", MinecraftJavaAddon.class);
            FileExtractor.unzipFile(ApplicationStorage.getApplicationPath() + "temp/mje.zip", ApplicationStorage.getApplicationPath() + "temp/ui");
            logger.dbg("[Minecraft] Deleted ui archive: " + new File(ApplicationStorage.getApplicationPath() + "temp/mje.zip").delete());
        } catch (Exception e) {
            logger.err("[Minecraft] Couldn't update application user interface: " + e.getMessage());
        }
        logger.dbg("[Minecraft] Deleted old updatar json: " + new File(ApplicationStorage.getApplicationPath() + "updater.json").delete());
        logger.dbg("[Minecraft] Deleted older updater json: " + new File(ApplicationStorage.getApplicationPath() + "version.json").delete());
    }

    public static void main(String[] args) {
        Main.splash = new ZyneonSplash();
        Main.splash.setVisible(true);
        NexusUtilities.getLogger().setName("TEST-APP",true);
        NexusUtilities.getLogger().enableDebug();
        NexusDesktop.init();
        ArrayList<String> arguments = new ArrayList<>(Arrays.stream(args).toList());
        arguments.add("--test");
        arguments.add("--path:D:/Workspaces/IntelliJ/nexus-app-minecraft-module/target/run/");
        args = arguments.toArray(new String[0]);
        NexusApplication application = new NexusApplication(args);
        try {
            String v = new SimpleDateFormat("yyyy.M.d/HH-mm-ss").format(Calendar.getInstance().getTime());
            NexusApplication.getModuleLoader().loadModule(new MinecraftJavaAddon(application, "nexus-minecraft-module", "Minecraft (Test)", v + "_" + StringGenerator.generateAlphanumericString(4), "Zyneon Studios, Zyneon Nexus"));
        } catch (Exception ignore) {}
        application.launch();
        System.out.println(ApplicationStorage.urlBase+ApplicationStorage.language+"/start.html");
    }
}