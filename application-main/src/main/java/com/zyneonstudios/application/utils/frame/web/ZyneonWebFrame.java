package com.zyneonstudios.application.utils.frame.web;

import com.google.gson.JsonObject;
import com.zyneonstudios.ApplicationMain;
import com.zyneonstudios.application.utils.backend.Connector;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefDisplayHandlerAdapter;

import java.awt.*;
import java.util.HashMap;

public class ZyneonWebFrame extends WebFrame {

    private final Connector connector;
    private final HashMap<String,JsonObject> notifications = new HashMap<>();

    public ZyneonWebFrame(String url) {
        super(url, ApplicationMain.getDirectoryPath()+"libs/jcef");
        connector = new Connector(this);
        getClient().addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public boolean onConsoleMessage(CefBrowser browser, CefSettings.LogSeverity level, String message, String source, int line) {
                    if(message.contains("[Launcher-Bridge] ")) {
                        String request = message.replace("[Launcher-Bridge] ","");
                        ApplicationMain.getLogger().deb("[CONNECTOR] Received request: \""+request+"\"");
                        connector.resolveRequest(request);
                    }
                return super.onConsoleMessage(browser, level, message, source, line);
            }
        });
    }

    public Connector getConnector() {
        return connector;
    }

    public void setTitlebar(String title, Color background, Color foreground) {
        setTitle(title);
        setTitleBackground(background);
        setTitleForeground(foreground);
    }

    public void setTitleBackground(Color color) {
        setBackground(color);
        getRootPane().putClientProperty("JRootPane.titleBarBackground", color);
    }

    public void setTitleForeground(Color color) {
        getRootPane().putClientProperty("JRootPane.titleBarForeground", color);
    }

    public void sendNotification(String title,String message, String actions, String id, boolean save) {
        if(save) {
            JsonObject notification = new JsonObject();
            notification.addProperty("id", id);
            notification.addProperty("title", title);
            notification.addProperty("message", message);
            notification.addProperty("actions", actions);
            notifications.put(id,notification);
        }
        executeJavaScript("sendNotification(\""+title.replace("\"","\\\"")+"\",\""+message.replace("\"","\\\"")+"\",\""+actions.replace("\"","\\\"")+"\",\""+id.replace("\"","\\\"")+"\");");
    }

    public void removeNotification(String id) {
        notifications.remove(id);
    }

    public void syncNotifications() {
        for(int i = notifications.size() - 1; i >= 0; i--) {
            JsonObject notification = notifications.values().stream().toList().get(i);
            String id = notification.get("id").getAsString();
            String title = notification.get("title").getAsString();
            String message = notification.get("message").getAsString();
            String actions = notification.get("actions").getAsString();
            executeJavaScript("sendNotification(\""+title.replace("\"","\\\"")+"\",\""+message.replace("\"","\\\"")+"\",\""+actions.replace("\"","\\\"")+"\",\""+id.replace("\"","\\\"")+"\");");

        }
    }
}