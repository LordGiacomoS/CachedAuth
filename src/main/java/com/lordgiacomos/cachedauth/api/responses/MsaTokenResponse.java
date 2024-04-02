package com.lordgiacomos.cachedauth.api.responses;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MsaTokenResponse extends GenericResponse {
    public String tokenType;
    public String scope;
    public int expiresInSeconds;
    public String accessToken;
    public String refreshToken;
    public String idToken;


    public MsaTokenResponse(int statusCode) {
        super(statusCode);
    }


    public MsaTokenResponse(int statusCode, String responseString) {
        super(statusCode);
        if (statusCode == 200) {
            parseResponseString(responseString);
        } else {
            setErrorResponse(responseString);
        }
    }

    private void parseResponseString(String responseString) {
        JsonObject json = JsonParser.parseString(responseString).getAsJsonObject();
        this.tokenType = json.get("token_type").getAsString();
        this.scope = json.get("scope").getAsString();
        this.expiresInSeconds = json.get("expires_in").getAsInt();
        this.accessToken = json.get("access_token").getAsString();
        this.refreshToken = json.get("refresh_token").getAsString();
        //this.idToken = json.get("id_token").getAsString(); //not using openid anymore so this isn't needed
    }

}
