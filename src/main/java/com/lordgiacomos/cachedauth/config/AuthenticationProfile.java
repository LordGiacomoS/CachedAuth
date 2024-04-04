package com.lordgiacomos.cachedauth.config;

import com.google.gson.JsonObject;

public class AuthenticationProfile {
    //cached data
    public String refreshToken;
    //probs should store stuff like uuid, skins & capes


    // active session data
    public String accessToken;
    public String xblToken; //Xbox Live token
    public String userhash;
    public String xstsToken;


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

    public void setUserhash(String value) {
        this.userhash = value;
    }

    public void setXstsToken(String value) {
        this.xstsToken = value;
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
