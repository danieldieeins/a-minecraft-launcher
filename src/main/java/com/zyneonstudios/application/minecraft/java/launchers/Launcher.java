package com.zyneonstudios.application.minecraft.java.launchers;

public interface Launcher {

    default boolean launch() {
        return false;
    }
}