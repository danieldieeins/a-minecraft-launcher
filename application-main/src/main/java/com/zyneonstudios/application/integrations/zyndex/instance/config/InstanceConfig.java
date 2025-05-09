package com.zyneonstudios.application.integrations.zyndex.instance.config;

import com.zyneonstudios.nexus.utilities.storage.JsonStorage;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Deprecated
public class InstanceConfig extends JsonStorage implements Comparable<InstanceConfig>{

    @Deprecated
    public InstanceConfig(File file) {
        super(file);
    }

    @Override @Deprecated
    public int compareTo(@NotNull InstanceConfig o) {
        return getString("instance.info.name").compareTo(o.getString("instance.info.name"));
    }
}
