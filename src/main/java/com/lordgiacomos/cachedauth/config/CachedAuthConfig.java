package com.lordgiacomos.cachedauth.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class CachedAuthConfig {
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();
    private static boolean modEnabled = true;
    private static ArrayList<AuthenticationProfile> authenticationProfiles = new ArrayList<>();
    //private static int selectedAuthenticationProfile = 0;


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
