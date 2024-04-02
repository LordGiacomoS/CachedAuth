package com.lordgiacomos.cachedauth.config;

import java.util.ArrayList;

public class CachedAuthConfig {
    private static boolean modEnabled = true;
    private static ArrayList<SavedAccount> savedAccounts = new ArrayList<>();



    //config property setters
    public static void setModEnabled(boolean value) {
        modEnabled = value;
    }
    public static void setSavedAccounts(ArrayList<SavedAccount> value) {
        savedAccounts = value;
    } //add ordering of accounts later



    //config property getters
    public static boolean getModEnabled() {
        return modEnabled;
    }
    public static ArrayList<SavedAccount> getSavedAccounts() {
        return savedAccounts;
    }
}
