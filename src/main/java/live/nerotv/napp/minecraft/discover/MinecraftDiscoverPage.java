package live.nerotv.napp.minecraft.discover;

import com.zyneonstudios.nexus.application.api.SharedAPI;
import com.zyneonstudios.nexus.application.api.shared.body.elements.BodyButton;
import com.zyneonstudios.nexus.application.api.shared.body.elements.BodyPage;
import com.zyneonstudios.nexus.application.api.shared.events.ElementActionEvent;

public class MinecraftDiscoverPage extends BodyPage {

    public MinecraftDiscoverPage() {
        setTitle("Minecraft");
        setId("a-minecraft-module");
        setActive(true);

        BodyButton openLibrary = new BodyButton();
        openLibrary.setText("Open library");
        openLibrary.addActionEvent(new ElementActionEvent() {
            @Override
            public boolean onAction() {
                SharedAPI.openAppPage(SharedAPI.AppPage.LIBRARY);
                return true;
            }
        });
    }
}