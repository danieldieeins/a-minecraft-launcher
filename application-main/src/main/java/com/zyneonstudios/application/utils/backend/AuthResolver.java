package com.zyneonstudios.application.utils.backend;

import com.zyneonstudios.application.Application;
import com.zyneonstudios.application.auth.MicrosoftAuth;

public class AuthResolver {

    public AuthResolver() {}

    public void preAuth() {}

    public void postAuth(String username, String uuid) {
        Application.getFrame().executeJavaScript("login('"+username+"');");
        Application.getFrame().executeJavaScript("unmessage();");
        Application.getFrame().executeJavaScript("syncProfileSettings();");
        MicrosoftAuth.syncTeam(uuid);
    }
}