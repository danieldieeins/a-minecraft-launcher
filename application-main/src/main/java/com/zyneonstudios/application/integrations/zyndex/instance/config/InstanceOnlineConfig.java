package com.zyneonstudios.application.integrations.zyndex.instance.config;

import com.zyneonstudios.nexus.utilities.storage.ReadableJsonStorage;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class InstanceOnlineConfig extends ReadableJsonStorage implements Comparable<InstanceOnlineConfig>{

    @Deprecated
    public InstanceOnlineConfig(String url) {
        super(url);
    }

    @Override @Deprecated
    public int compareTo(@NotNull InstanceOnlineConfig o) {
        return getString("instance.info.name").compareTo(o.getString("instance.info.name"));
    }
}
