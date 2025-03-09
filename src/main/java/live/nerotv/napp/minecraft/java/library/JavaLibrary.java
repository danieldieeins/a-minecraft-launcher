package live.nerotv.napp.minecraft.java.library;

import com.zyneonstudios.nexus.application.api.LibraryAPI;
import com.zyneonstudios.nexus.application.api.library.LibraryInstance;
import com.zyneonstudios.nexus.application.api.library.zyndex.ZyndexLibrary;
import com.zyneonstudios.nexus.instance.ReadableZynstance;
import com.zyneonstudios.nexus.modules.ReadableModule;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;

import java.util.ArrayList;
import java.util.HashMap;

public class JavaLibrary extends ZyndexLibrary {

    private static JavaLibrary library = null;

    public static JavaLibrary getInstance() {
        return library;
    }

    private final HashMap<String, JavaInstance> instances;

    public JavaLibrary(JsonStorage json) {
        super(json, "a-minecraft-module");
        if (library != null) {
            throw new RuntimeException("Java library already initialized, use JavaLibrary.getInstance() instead.");
        } else {
            library = this;
            instances = new HashMap<>();

            initZyndex();

            setName("Minecraft: Java Edition");
            LibraryAPI.addLibrary("a-minecraft-module",this);
        }
    }

    public boolean addJavaInstance(JavaInstance instance, boolean overwrite) {
        if(overwrite||!instances.containsKey(instance.getId())) {
            instances.put(instance.getId(), instance);
            syncZyndex();
            return true;
        }
        return false;
    }

    public boolean removeJavaInstance(JavaInstance instance) {
        return removeJavaInstance(instance.getId());
    }

    public boolean removeJavaInstance(String instanceID) {
        if(instances.containsKey(instanceID)) {
            instances.remove(instanceID);
            syncZyndex();
            return true;
        }
        return false;
    }

    public JavaInstance getJavaInstance(String instanceID) {
        if(instances.containsKey(instanceID)) {
            return instances.get(instanceID);
        }
        return null;
    }

    public JavaInstance[] getJavaInstances() {
        return instances.values().toArray(new JavaInstance[0]);
    }

    public HashMap<String, JavaInstance> getJavaInstancesById() {
        return new HashMap<>(instances);
    }

    private void initZyndex() {

    }

    private void syncZyndex() {
        getJson().set("instances",getJavaInstances());
    }

    @Override
    @Deprecated
    public LibraryInstance getLibraryInstance(String id) {
        if (instances.containsKey(id)) {
            return instances.get(id);
        }
        return null;
    }

    @Override
    @Deprecated
    public LibraryInstance[] getLibraryInstances() {
        return instances.values().toArray(new LibraryInstance[0]);
    }

    @Override
    @Deprecated
    public void removeLibraryInstance(String instanceID) {
        instances.remove(instanceID);
    }

    @Override
    @Deprecated
    public void removeLibraryInstance(LibraryInstance instance) {
        instances.remove(instance.getId());
    }

    @Override
    @Deprecated
    public void addLibraryInstance(LibraryInstance instance) {
        if (!instances.containsKey(instance.getId())) {
            instances.put(instance.getId(), (JavaInstance) instance);
        }
    }

    @Override
    @Deprecated
    public ArrayList<ReadableZynstance> getInstances() {
        return new ArrayList<>(this.instances.values());
    }

    @Override
    @Deprecated
    public HashMap<String, ReadableZynstance> getInstancesById() {
        return new HashMap<>(instances);
    }

    @Override
    @Deprecated
    public void setInstances(ArrayList<ReadableZynstance> instances) {
        this.instances.clear();
        for (ReadableZynstance instance : instances) {
            this.instances.put(instance.getId(), (JavaInstance) instance);
        }
    }

    @Override
    @Deprecated
    public void addInstance(ReadableZynstance instance) {
        if (!instances.containsKey(instance.getId())) {
            instances.put(instance.getId(), (JavaInstance) instance);
        }
    }

    @Override
    @Deprecated
    public void removeInstance(ReadableZynstance instance) {
        instances.remove(instance.getId());
    }

    @Override @Deprecated
    public void addInstance(ReadableModule module) {

    }
}