package com.lordgiacomos.cachedauth.old.api;

import com.lordgiacomos.cachedauth.config.CachedAuthConfig;
import com.lordgiacomos.cachedauth.config.CachedAuthConfigManager;
public class ApiTestingMain {
    public static void main(String[] args) {
        CachedAuthConfigManager.load();
        if ("true".equalsIgnoreCase(args[0])) {
            System.out.println("from refresh token");
            Authenticator.fromRefreshTokenAuth(CachedAuthConfig.getAuthenticationProfiles().get(0));
        } else {
            System.out.println("from device code flow (default)");
            Authenticator.testDeviceFlow();
        }
        CachedAuthConfigManager.save();
    }
}