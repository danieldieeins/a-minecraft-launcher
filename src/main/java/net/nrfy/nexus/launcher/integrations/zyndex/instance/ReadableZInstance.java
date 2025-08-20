package net.nrfy.nexus.launcher.integrations.zyndex.instance;

import com.zyneonstudios.nexus.instance.ReadableZynstance;

import java.io.File;

public class ReadableZInstance extends ReadableZynstance implements ZInstance {

    private File file;
    private String path;
    private ZInstanceSettings settings;

    public ReadableZInstance(File file) {
        super(file);
        init(file);
    }

    public ReadableZInstance(String path) {
        super(new File(path));
        init(new File(path));
    }

    private void init(File file) {
        this.file = file;
        this.path = file.getParent()+"/";
        this.settings = new ZInstanceSettings(this);
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
    public ZInstanceSettings getSettings() {
        return this.settings;
    }

    @Override
    public void setSettings(ZInstanceSettings settings) {
        this.settings = settings;
    }
}