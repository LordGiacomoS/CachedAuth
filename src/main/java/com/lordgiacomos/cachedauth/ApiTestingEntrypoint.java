package com.lordgiacomos.cachedauth;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lordgiacomos.cachedauth.config.CachedAuthConfigManager;

public class ApiTestingEntrypoint {
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();
    public static final String CLIENT_ID = System.getenv("CLIENT_ID");

    public static void main(String[] args) {
        CachedAuthConfigManager.load();
        try {
            if ("true".equalsIgnoreCase(args[0])) {
                System.out.println("from refresh token");



                //Authenticator.fromRefreshTokenAuth(CachedAuthConfig.getAuthenticationProfiles().get(0));
            } else {
                System.out.println("from device code flow (default)");
                //Authenticator.testDeviceFlow();
            }
        } catch (Exception e) {
            CachedAuthConfigManager.save();
            throw e;
        }
        CachedAuthConfigManager.save();
    }
}
