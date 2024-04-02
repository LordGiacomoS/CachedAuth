package com.lordgiacomos.cachedauth.api;

import com.lordgiacomos.cachedauth.config.CachedAuthConfig;
import com.lordgiacomos.cachedauth.config.CachedAuthConfigManager;
import com.lordgiacomos.cachedauth.config.AuthenticationProfile;
public class ApiTestingMain {
    public static void main(String[] args) {
        CachedAuthConfigManager.load();
        //Authenticator.testDeviceFlow();
        Authenticator.fromRefreshTokenAuth(CachedAuthConfig.getAuthenticationProfiles().get(0));

        CachedAuthConfigManager.save();
    }
}