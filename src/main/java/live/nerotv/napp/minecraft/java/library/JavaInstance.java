package live.nerotv.napp.minecraft.java.library;

import com.zyneonstudios.nexus.application.api.library.zyndex.ZyndexInstance;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;

public class JavaInstance extends ZyndexInstance {

    public JavaInstance(JsonStorage config) {
        super(config);
    }

    @Override
    public void enableLaunch(boolean b) {

    }

    @Override
    public boolean isLaunchEnabled() {
        return false;
    }

    @Override
    public void launch() {

    }
}
