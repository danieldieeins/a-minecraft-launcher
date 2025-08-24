package live.nerotv.aminecraftlauncher.launcher;

public abstract class LauncherHook {

    private final MinecraftLauncher launcher;

    public LauncherHook(MinecraftLauncher launcher) {
        this.launcher = launcher;
    }

    public MinecraftLauncher getLauncher() {
        return launcher;
    }

    public abstract void run();
}