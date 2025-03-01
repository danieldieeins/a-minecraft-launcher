package com.zyneonstudios.application.minecraft.java.authentication;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.zyneonstudios.application.MinecraftJavaAddon;
import com.zyneonstudios.application.frame.web.ApplicationFrame;
import com.zyneonstudios.application.main.ApplicationStorage;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.JavaStorage;
import com.zyneonstudios.nexus.utilities.json.GsonUtility;

import java.util.UUID;

public class AuthenticationResolver {

    private final MinecraftJavaAddon moduleInfo;

    public AuthenticationResolver(MinecraftJavaAddon moduleInfo) {
        this.moduleInfo = moduleInfo;
    }

    public void preAuth() {
        moduleInfo.setAuthState(MinecraftJavaAddon.AuthState.LOGGING_IN);
        ((ApplicationFrame)moduleInfo.getApplication().getFrame()).openCustomPage("Minecraft: Java Edition Login","mje-authentication", JavaStorage.getUrlBase()+"mje-login.html?id=1&enable=false");
    }

    public void postAuth(String username, String uuid) {
        JavaStorage.map.set("auth.username", username);
        JavaStorage.map.set("auth.uuid", UUID.fromString(uuid));
        moduleInfo.setAuthState(MinecraftJavaAddon.AuthState.LOGGED_IN);
        try {
            JsonArray team = GsonUtility.getObject("https://danieldieeins.github.io/ZyneonApplicationContent/i/team.json").getAsJsonArray("team");
            for (JsonElement element : team) {
                if (element.getAsString().equals(uuid.replace("-", ""))) {
                    NexusApplication.getLogger().dbg("[Minecraft] (AuthResolver) This account does have access to Zyneon Drive.");
                    ApplicationStorage.enableDriveAccess();
                    break;
                } else {
                    NexusApplication.getLogger().dbg("[Minecraft] (AuthResolver) This account doesn't have access to Zyneon Drive.");
                }
            }
        } catch (Exception e) {
            NexusApplication.getLogger().err("[Minecraft] (AuthResolver) Couldn't resolve Zyneon Drive access: " + e.getMessage());
        }

        if (((ApplicationFrame) moduleInfo.getApplication().getFrame()).getBrowser().getURL().contains("mje-login.html")) {
            moduleInfo.getConnector().resolveFrameRequest("java.sync.library");
        }
    }
}
