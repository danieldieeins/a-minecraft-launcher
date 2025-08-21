package net.nrfy.nexus.launcher.integrations.zyndex.instance;

import java.io.File;

public interface ZInstance extends com.zyneonstudios.nexus.instance.Instance {

    default File getFile() {
        return null;
    }

    default String getPath() {
        return null;
    }

    default ZInstanceSettings getSettings() {
        return null;
    }

    default void setSettings(ZInstanceSettings settings) {

    }
}