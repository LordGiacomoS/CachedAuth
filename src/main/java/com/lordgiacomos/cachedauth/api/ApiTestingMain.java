package com.lordgiacomos.cachedauth.api;

import com.lordgiacomos.cachedauth.config.CachedAuthConfig;
import com.lordgiacomos.cachedauth.config.CachedAuthConfigManager;
import com.lordgiacomos.cachedauth.config.AuthenticationProfile;
public class ApiTestingMain {
    public static void main(String[] args) {
        CachedAuthConfigManager.load();
        if ("true".equalsIgnoreCase(args[0])) {
            Authenticator.fromRefreshTokenAuth(CachedAuthConfig.getAuthenticationProfiles().get(0));
            System.out.println("from refresh token");
        } else {
            Authenticator.testDeviceFlow();
            System.out.println("from device code flow (default)");
        }
        CachedAuthConfigManager.save();
    }
}