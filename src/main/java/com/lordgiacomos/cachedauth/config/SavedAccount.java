package com.lordgiacomos.cachedauth.config;

import com.google.gson.JsonObject;

public class SavedAccount {
    //constant data
    String refreshToken;

    //need to add more data here, then make sure ive also added matching stuff in json output


    // account session data --







    public JsonObject getJsonOutput() {
        JsonObject output = new JsonObject();
        output.addProperty("refreshToken", refreshToken); //according to quick google, camelCase is fine under json specs

        return output;
    }

    public SavedAccount (JsonObject savedJson) {
        this.refreshToken = savedJson.get("refreshToken").getAsString();

    }



}
