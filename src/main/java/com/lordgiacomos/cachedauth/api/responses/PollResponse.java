package com.lordgiacomos.cachedauth.api.responses;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PollResponse extends GenericResponse {
    public String tokenType;
    public String scope;
    public int expiresInSeconds;
    public String accessToken;
    public String refreshToken;
    public String idToken;



    public PollResponse(int statusCode) {
        super(statusCode);
    }

    public PollResponse(int statusCode, String responseString) {
        super(statusCode);
        parseResponseString(responseString);
    }

    private void parseResponseString(String responseString) {
        JsonObject json = JsonParser.parseString(responseString).getAsJsonObject();
        tokenType = json.get("token_type").getAsString();
        scope = json.get("scope").getAsString();
        expiresInSeconds = json.get("expires_in").getAsInt();
        accessToken = json.get("access_token").getAsString();
        refreshToken = json.get("refresh_token").getAsString();
        idToken = json.get("id_token").getAsString();
    }

}
