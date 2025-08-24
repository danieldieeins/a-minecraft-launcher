package live.nerotv.aminecraftlauncher.launcher;

import fr.flowarg.openlauncherlib.NoFramework;

public abstract class MinecraftLauncher {

    private LauncherHook preLaunchHook;
    private LauncherHook postLaunchHook;
    private LauncherHook gameCloseHook;

    public void setGameCloseHook(LauncherHook gameCloseHook) {
        this.gameCloseHook = gameCloseHook;
    }

    public void setPostLaunchHook(LauncherHook postLaunchHook) {
        this.postLaunchHook = postLaunchHook;
    }

    public void setPreLaunchHook(LauncherHook preLaunchHook) {
        this.preLaunchHook = preLaunchHook;
    }

    public LauncherHook getGameCloseHook() {
        return gameCloseHook;
    }

    public LauncherHook getPostLaunchHook() {
        return postLaunchHook;
    }

    public LauncherHook getPreLaunchHook() {
        return preLaunchHook;
    }

    public abstract Process getGameProcess();
    public abstract NoFramework getFramework();
    public abstract boolean isLaunched();
}
