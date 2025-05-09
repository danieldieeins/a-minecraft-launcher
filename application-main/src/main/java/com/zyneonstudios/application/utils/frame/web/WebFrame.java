package com.zyneonstudios.application.utils.frame.web;

import com.zyneonstudios.ApplicationMain;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefBeforeDownloadCallback;
import org.cef.callback.CefDownloadItem;
import org.cef.callback.CefDownloadItemCallback;
import org.cef.handler.CefDownloadHandler;
import org.cef.handler.CefFocusHandlerAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

public class WebFrame extends JFrame {

    private CefClient client;
    private CefBrowser browser;
    private boolean browserFocus;

    public WebFrame(String url, String jcefPath) {
        try {
            init(url, jcefPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public WebFrame getInstance() {
        return this;
    }

    public CefClient getClient() {
        return client;
    }

    public CefBrowser getBrowser() {
        return browser;
    }

    private void init(String url, String jcefPath) throws UnsupportedPlatformException, IOException, CefInitializationException, InterruptedException {
        browserFocus = true;
        File installDir = new File(jcefPath);
        ApplicationMain.getLogger().deb("[WEBFRAME] Is jCef installed: "+!installDir.mkdirs());
        CefAppBuilder builder = new CefAppBuilder();
        builder.setAppHandler(new MavenCefAppHandlerAdapter() {
            @Override @Deprecated
            public void stateHasChanged(CefApp.CefAppState state) {
                if (state == CefApp.CefAppState.TERMINATED) System.exit(0);
            }
        });
        builder.getCefSettings().cache_path = jcefPath+"/cache";
        builder.getCefSettings().log_severity = CefSettings.LogSeverity.LOGSEVERITY_DISABLE;
        builder.getCefSettings().persist_session_cookies = true;
        builder.setInstallDir(installDir);
        builder.install();
        builder.getCefSettings().windowless_rendering_enabled = false;
        CefApp app = builder.build();
        client = app.createClient();
        CefMessageRouter messageRouter = CefMessageRouter.create();
        client.addMessageRouter(messageRouter);
        client.addDownloadHandler(new CefDownloadHandler() {
            @Override
            public boolean onBeforeDownload(CefBrowser browser, CefDownloadItem item, String s, CefBeforeDownloadCallback callback) {
                callback.Continue(s,true);
                return false;
            }

            @Override
            public void onDownloadUpdated(CefBrowser cefBrowser, CefDownloadItem cefDownloadItem, CefDownloadItemCallback cefDownloadItemCallback) {

            }
        });
        browser = client.createBrowser(url, false, false);
        client.addDragHandler((cefBrowser, dragData, i) -> dragData.isFile());
        Component ui = browser.getUIComponent();
        client.addFocusHandler(new CefFocusHandlerAdapter() {
            @Override
            public void onGotFocus(CefBrowser browser) {
                if (browserFocus) return;
                browserFocus = true;
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
                browser.setFocus(true);
            }
            @Override
            public void onTakeFocus(CefBrowser browser, boolean next) {
                browserFocus = false;
            }
        });
        getContentPane().add(ui,BorderLayout.CENTER);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                CefApp.getInstance().dispose();
                dispose();
            }
        });
    }

    public void executeJavaScript(String command) {
        browser.executeJavaScript(command,browser.getURL(),5);
    }
}