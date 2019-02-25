package com.xpenatan.gradle.locallib;

import com.xpenatan.gradle.locallib.internal.LocalLib;

import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.api.plugins.PluginAware;

public class LocalLibPlugin implements Plugin<PluginAware> {

    @Override
    public void apply(PluginAware target) {
        if(target instanceof Settings) {
            LocalLib localLib = new LocalLib();
            localLib.init((Settings)target);
        }
    }
}