package net.nrfy.nexus.launcher.integrations.zyndex.instance;

import com.zyneonstudios.nexus.utilities.storage.JsonStorage;

import java.io.File;

public class InstanceSettings extends JsonStorage {

    private int memory = 1024;

    public InstanceSettings(Instance instance) {
        super(new File(instance.getPath()+"meta/instanceSettings.json"));
        if(get("settings.memory")!=null) {
            memory = getInt("settings.memory");
        }
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
        set("settings.memory",memory);
    }
}