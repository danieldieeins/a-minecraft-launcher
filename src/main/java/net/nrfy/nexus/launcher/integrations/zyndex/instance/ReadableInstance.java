package net.nrfy.nexus.launcher.integrations.zyndex.instance;

import com.zyneonstudios.nexus.instance.ReadableZynstance;

import java.io.File;

public class ReadableInstance extends ReadableZynstance implements Instance {

    private File file;
    private String path;
    private InstanceSettings settings;

    public ReadableInstance(File file) {
        super(file);
        init(file);
    }

    public ReadableInstance(String path) {
        super(new File(path));
        init(new File(path));
    }

    private void init(File file) {
        this.file = file;
        this.path = file.getParent()+"/";
        this.settings = new InstanceSettings(this);
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public InstanceSettings getSettings() {
        return this.settings;
    }

    @Override
    public void setSettings(InstanceSettings settings) {
        this.settings = settings;
    }
}