package com.zyneonstudios.application.auth;

import com.zyneonstudios.application.utils.backend.AuthResolver;
import com.zyneonstudios.nexus.utilities.aes.AESUtility;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import live.nerotv.zyneon.auth.ZyneonAuth;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class AsyncAuth {

    private AuthInfos authInfos;
    private File saveFile;
    private byte[] key;
    private AuthResolver resolver;
    private Boolean isLoggedIn;
    private JsonStorage saver;

    public AsyncAuth() {
        saveFile = null;
        key = null;
        resolver = new AuthResolver();
        isLoggedIn = false;
    }

    public AuthInfos getAuthInfos() {
        return authInfos;
    }

    public File getSaveFile() {
        return saveFile;
    }

    public void setKey(byte[] newKey) {
        key = newKey;
    }

    public void setResolver(AuthResolver newResolver) {
        resolver = newResolver;
    }

    public void setSaveFilePath(String newPath) {
        saveFile = new File(newPath);
        CompletableFuture.runAsync(()->{
            try {
                saver = new JsonStorage(saveFile);
                new File(saveFile.getParent()).mkdirs();
            } catch (Exception ignore) {}
        });
    }

    public boolean isLoggedIn() {
        if (isLoggedIn) {
            return true;
        } else {
            authInfos = null;
            if (saveFile != null) {
                if (key != null) {
                    if (saver.get("opapi.ms.a") != null || saver.get("opapi.ms.r") != null || saver.get("opapi.ms.n") != null || saver.get("opapi.ms.u") != null) {
                        String r = (String) saver.get("opapi.ms.r");
                        try {
                            byte[] b = r.getBytes();
                            b = AESUtility.decrypt(key, b);
                            if(refresh_(new String(b))) {
                                return true;
                            } else {
                                saver.delete("opapi.ms");
                                return false;
                            }
                        } catch (Exception ignore) {
                        }
                    }
                }
            }
        }
        return false;
    }

    public void login() {
        CompletableFuture.runAsync(()->{
            resolver.preAuth();
            try {
                HashMap<ZyneonAuth.AuthInfo, String> authData = ZyneonAuth.getAuthInfos();
                authInfos = new AuthInfos(authData.get(ZyneonAuth.AuthInfo.USERNAME), authData.get(ZyneonAuth.AuthInfo.ACCESS_TOKEN), authData.get(ZyneonAuth.AuthInfo.UUID).replace("-", ""));
                save(authData);
            } catch (Exception ignore) {}
            resolver.postAuth(authInfos.getUsername(),authInfos.getUuid());
        });
    }

    public void refresh(String token) {
        CompletableFuture.runAsync(()->{
            resolver.preAuth();
            try {
                HashMap<ZyneonAuth.AuthInfo, String> authData = ZyneonAuth.getAuthInfos(token);
                authInfos = new AuthInfos(authData.get(ZyneonAuth.AuthInfo.USERNAME), authData.get(ZyneonAuth.AuthInfo.ACCESS_TOKEN), authData.get(ZyneonAuth.AuthInfo.UUID).replace("-", ""));
                save(authData);
            } catch (Exception ignore) {}
            resolver.postAuth(authInfos.getUsername(), authInfos.getUuid());
        });
    }

    @Deprecated
    public boolean refresh_(String token) {
        resolver.preAuth();
        try {
            HashMap<ZyneonAuth.AuthInfo, String> authData = ZyneonAuth.getAuthInfos(token);
            authInfos = new AuthInfos(authData.get(ZyneonAuth.AuthInfo.USERNAME), authData.get(ZyneonAuth.AuthInfo.ACCESS_TOKEN), authData.get(ZyneonAuth.AuthInfo.UUID).replace("-", ""));
            save(authData);
            isLoggedIn = true;
        } catch (Exception e) {
            isLoggedIn = false;
        }
        CompletableFuture.runAsync(() -> resolver.postAuth(authInfos.getUsername(),authInfos.getUuid()));
        return isLoggedIn;
    }

    private void save(HashMap<ZyneonAuth.AuthInfo, String> authData) {
        if (saveFile != null) {
            if (key != null) {
                try {
                    byte[] a = AESUtility.encrypt(key, authData.get(ZyneonAuth.AuthInfo.ACCESS_TOKEN).getBytes());
                    byte[] r = AESUtility.encrypt(key, authData.get(ZyneonAuth.AuthInfo.REFRESH_TOKEN).getBytes());
                    byte[] n = AESUtility.encrypt(key, authData.get(ZyneonAuth.AuthInfo.USERNAME).getBytes());
                    byte[] u = AESUtility
                            .encrypt(key, authData.get(ZyneonAuth.AuthInfo.UUID).getBytes());
                    saver.set("opapi.ms.a", new String(a));
                    saver.set("opapi.ms.r", new String(r));
                    saver.set("opapi.ms.n", new String(n));
                    saver.set("opapi.ms.u", new String(u));
                } catch (Exception ignore) {}
            }
        }
    }

    public void destroy() {
        authInfos = null;
        saveFile = null;
        key = null;
        resolver = null;
        isLoggedIn = null;
        saver = null;
        System.gc();
    }
}