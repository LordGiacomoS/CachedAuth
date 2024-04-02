package com.lordgiacomos.cachedauth.config;

import com.google.gson.JsonObject;

public class AuthenticationProfile {
    //constant data
    public String refreshToken;
    //need to add more data here, then make sure ive also added matching stuff in json output


    // active session data
    public String accessToken;
    public String xblToken; //Xbox Live token
    //String userhash;
    //String xstsToken;


    // building stuff (for when going through flow)
    public void setRefreshToken(String value) {
        this.refreshToken = value;
    }

    public void setAccessToken(String value) {
        this.accessToken = value;
    }
    public void setXblToken(String value) {
        this.xblToken = value;
    }





    // config import/export stuff
    public JsonObject getJsonOutput() {
        JsonObject output = new JsonObject();
        output.addProperty("refreshToken", refreshToken);

        return output;
    }

    public AuthenticationProfile(JsonObject savedJson) {
        this.refreshToken = savedJson.get("refreshToken").getAsString();
        // need to take refresh token and use it with oauth flow to get access token & stuff


    }


    //public class AuthenticationProfileBuilder {}
}
