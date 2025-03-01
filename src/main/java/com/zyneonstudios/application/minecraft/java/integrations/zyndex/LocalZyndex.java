package com.zyneonstudios.application.minecraft.java.integrations.zyndex;

import com.google.gson.JsonArray;
import com.zyneonstudios.nexus.index.Zyndex;
import com.zyneonstudios.nexus.instance.ReadableZynstance;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class LocalZyndex extends Zyndex {

    private HashMap<LocalInstance, String> instances;

    public LocalZyndex(JsonStorage config) {
        super(config);
        instances = new HashMap<>();
    }

    public LocalZyndex(File json) {
        super(json);
        instances = new HashMap<>();
    }

    public void setInstances(HashMap<LocalInstance, String> instances) {
        getJson().delete("instances");
        JsonArray array = new JsonArray();
        if(!instances.isEmpty()) {
            for (LocalInstance zynstance : instances.keySet()) {
                array.add(instances.get(zynstance));
            }
        }
        this.instances = instances;
        getJson().set("instances",array);
        System.gc();
    }

    public void addInstance(LocalInstance zynstance, String path) {
        HashMap<LocalInstance, String> instances = this.instances;
        if(!instances.containsKey(zynstance)) {
            if(!instances.containsValue(path)) {
                instances.put(zynstance, path);
            }
        }
        setInstances(instances);
    }

    public void removeInstance(LocalInstance zynstance, String path) {
        HashMap<LocalInstance, String> instances = this.instances;
        instances.remove(zynstance,path);
        instances.remove(zynstance);
        setInstances(instances);
    }

    public String getPath(ReadableZynstance instance) {
        if(instances.containsKey(instance)) {
            return instances.get(instance);
        }
        return null;
    }

    public ArrayList<LocalInstance> getLocalInstances() {
        return new ArrayList<>(instances.keySet());
    }

    public HashMap<String, LocalInstance> getLocalInstancesById() {
        HashMap<String, LocalInstance> zynstances = new HashMap<>();
        for(LocalInstance zynstance:instances.keySet()) {
            zynstances.put(zynstance.getId(),zynstance);
        }
        return zynstances;
    }

    @Override @Deprecated
    public ArrayList<ReadableZynstance> getInstances() {
        ArrayList<ReadableZynstance> instances = new ArrayList<>();
        for(LocalInstance instance:this.instances.keySet()) {
            instances.add(new ReadableZynstance(instance.getInstanceFile()));
        }
        return instances;
    }

    @Override @Deprecated
    public HashMap<String, ReadableZynstance> getInstancesById() {
        HashMap<String, ReadableZynstance> zynstances = new HashMap<>();
        for(LocalInstance instance:instances.keySet()) {
            ReadableZynstance zynstance = new ReadableZynstance(instance.getInstanceFile());
            zynstances.put(zynstance.getId(),zynstance);
        }
        return zynstances;
    }

    @Override @Deprecated
    public void setInstances(ArrayList<ReadableZynstance> instances) {}

    @Override @Deprecated
    public void addInstance(ReadableZynstance zynstance) {}

    @Override @Deprecated
    public void removeInstance(ReadableZynstance zynstance) {}
}
