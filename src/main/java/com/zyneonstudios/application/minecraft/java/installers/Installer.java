package com.zyneonstudios.application.minecraft.java.installers;

public interface Installer {

    default boolean install() {
        return false;
    }
}