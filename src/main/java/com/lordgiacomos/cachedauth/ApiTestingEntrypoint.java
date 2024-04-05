package com.lordgiacomos.cachedauth;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lordgiacomos.cachedauth.config.AuthenticationProfile;
import com.lordgiacomos.cachedauth.config.CachedAuthConfig;
import com.lordgiacomos.cachedauth.config.CachedAuthConfigManager;

public class ApiTestingEntrypoint {
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();
    public static final String CLIENT_ID = System.getenv("CLIENT_ID"); //figure out way to obfuscate this beyond env variable, to allow distribution

    public static void main(String[] args) throws CachedAuthException {
        CachedAuthConfigManager.load();
        try {
            if ("true".equalsIgnoreCase(args[0])) {
                System.out.println("from refresh token");
                AuthenticationProfile profile = CachedAuthConfig.getAuthenticationProfiles().get(0);
                profile.initializeSession();
            } else {
                System.out.println("from device code flow (default)");
                AuthenticationProfile profile = new AuthenticationProfile();
                profile.initializeSession();
            }
        } catch (Exception e) {
            CachedAuthConfigManager.save();
            throw e;
        }
        CachedAuthConfigManager.save();
    }
}
