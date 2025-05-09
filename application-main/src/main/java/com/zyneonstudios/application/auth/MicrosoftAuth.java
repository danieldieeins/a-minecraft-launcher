package com.zyneonstudios.application.auth;

import com.zyneonstudios.ApplicationMain;
import com.zyneonstudios.application.Application;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;
import com.zyneonstudios.nexus.utilities.storage.ReadableJsonStorage;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;

import javax.crypto.KeyGenerator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class MicrosoftAuth {

    public static void syncTeam(String uuid) {
        uuid = uuid.replace("-","");
        try {
            ReadableJsonStorage cfg = new ReadableJsonStorage("https://github.com/danieldieeins/ZyneonApplicationContent/raw/main/i/team.json");
            String[] teamUUIDs = cfg.getString("team").replace("[","").replace("]","").split(", ");
            if(Arrays.stream(teamUUIDs).toList().contains(uuid)) {
                Application.getFrame().executeJavaScript("document.getElementById('drive').style.display = 'inherit'");
            }
        } catch (Exception ignore) {}
    }

    private AsyncAuth authenticator;

    public MicrosoftAuth() {
        authenticator = new AsyncAuth();
        try {
            new File(URLDecoder.decode(ApplicationMain.getDirectoryPath() + "libs/opapi/arun.json", StandardCharsets.UTF_8)).delete();
        } catch (Exception ignore) {}
        authenticator.setSaveFilePath(URLDecoder.decode(ApplicationMain.getDirectoryPath() + "libs/opapi/arnu.json", StandardCharsets.UTF_8));
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyGenerator.init(256);
        byte[] key = keyGenerator.generateKey().getEncoded();
        String key_ = new String(Base64.getEncoder().encode(key));
        JsonStorage saver = new JsonStorage(authenticator.getSaveFile());
        try {
            if (saver.get("op.k") == null) {
                saver.set("op.k", key_);
            } else {
                key_ = (String) saver.get("op.k");
                key = Base64.getDecoder().decode(key_);
            }
        } catch (Exception e) {
            try {
                FileWriter writer = new FileWriter(saver.getJsonFile());
                writer.write("{}");
                writer.close();
            } catch (IOException ex) {
                Application.getFrame().executeJavaScript("unmessage();");
            }
        }
        authenticator.setKey(key);
        authenticator.isLoggedIn();
    }

    public AuthInfos getAuthInfos() {
        return authenticator.getAuthInfos();
    }

    public boolean isLoggedIn() {
        return authenticator.isLoggedIn();
    }

    public void login() {
        authenticator.login();
    }

    public void setSaveFilePath(String path) {
        authenticator.setSaveFilePath(path);
    }

    public File getSaveFile() {
        return authenticator.getSaveFile();
    }

    public void setKey(byte[] key) {
        authenticator.setKey(key);
    }

    public void destroy() {
        authenticator.destroy();
        authenticator = null;
        System.gc();
    }
}