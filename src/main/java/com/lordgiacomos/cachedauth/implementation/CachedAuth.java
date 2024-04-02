package com.lordgiacomos.cachedauth.implementation;

import net.fabricmc.api.ClientModInitializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CachedAuth implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    // implement config storage

    @Override
    public void onInitializeClient() {
        //load config here

    }


}
