package com.lordgiacomos.cachedauth.config;

import com.google.gson.JsonObject;
import com.lordgiacomos.cachedauth.api.Authentication;
import com.lordgiacomos.cachedauth.api.HttpDriver;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class AuthenticationProfile {
    public static final String MSA_CODE_URI = "https://login.microsoftonline.com/" + TENANT + "/oauth2/v2.0/devicecode";
    public static final String MSA_TOKEN_URI = "https://login.microsoftonline.com/" + TENANT + "/oauth2/v2.0/token";
    public static final String XBOX_AUTH_URI = "https://user.auth.xboxlive.com/user/authenticate";
    public static final String XBOX_XSTS_URI = "https://xsts.auth.xboxlive.com/xsts/authorize";
    public static final String MINECREAFT_AUTH_URI = "https://api.minecraftservices.com/authentication/login_with_xbox";
    public static final String CLIENT_ID = System.getenv("CLIENT_ID"); //figure out way to obfuscate this beyond env variable, to allow distribution
    public static final String SCOPES = "XBoxLive.signin offline_access";
    //cached data

    String refreshToken;
    //probs should store stuff like uuid, skins & capes


    // active session data
    String accessToken;
    String xblToken; //Xbox Live token
    String userhash;
    String xstsToken;


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



    public static void fromRefreshToken(String refreshToken) {
        HttpDriver.postRequest();
    }


    // config import/export stuff
    public JsonObject getJsonOutput() {
        JsonObject output = new JsonObject();
        output.addProperty("refreshToken", refreshToken);

        return output;
    }

    public AuthenticationProfile(JsonObject savedJson) throws IOException {
        this.refreshToken = savedJson.get("refreshToken").getAsString();
        try (CloseableHttpClient client = HttpClients.createDefault()) {


        }
        // need to take refresh token and use it with oauth flow to get access token & stuff
    }

    //create new Auth Profile
    public AuthenticationProfile() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {

        }
    }

    public AuthenticationProfile(CloseableHttpClient client, String accessToken, String refreshToken) {

    }

}
