package com.lordgiacomos.cachedauth.config;

import java.util.ArrayList;

public class CachedAuthConfig {
    private static boolean modEnabled = true;
    private static ArrayList<AuthenticationProfile> authenticationProfiles = new ArrayList<>();



    //config property setters
    public static void setModEnabled(boolean value) {
        modEnabled = value;
    }
    public static void setAuthenticationProfiles(ArrayList<AuthenticationProfile> value) {
        authenticationProfiles = value;
    } //add ordering of accounts later



    //config property getters
    public static boolean getModEnabled() {
        return modEnabled;
    }
    public static ArrayList<AuthenticationProfile> getAuthenticationProfiles() {
        return authenticationProfiles;
    }
}
