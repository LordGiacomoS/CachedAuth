package com.lordgiacomos.cachedauth.old.api.responses;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

public class AuthMinecraftResponse extends GenericResponse {
    public String username; //not the minecraft user uuid, something else apparently (maybe microsoft or xbox account uuid -- that would likely be different but related)
    public ArrayList<?> roles;
    public String accessToken;
    public String tokenType;
    public int expiresInSeconds;


    public AuthMinecraftResponse(int statusCode) {
        super(statusCode);
    }
    public AuthMinecraftResponse(int statusCode, String responseString) {
        super(statusCode);
        parseResponseString(responseString);
    }

    private void parseResponseString(String responseString) {
        JsonObject json = JsonParser.parseString(responseString).getAsJsonObject();
        this.username = json.get("username").getAsString();
        this.roles = new ArrayList<>() {{ //need to figure out exact type to expect from roles list
            for (JsonElement role : json.getAsJsonArray("roles")) {
                //add();
            }
        }};
        this.accessToken = json.get("access_token").getAsString();
        this.tokenType = json.get("token_type").getAsString();
        this.expiresInSeconds = json.get("expires_in").getAsInt();

    }

}
