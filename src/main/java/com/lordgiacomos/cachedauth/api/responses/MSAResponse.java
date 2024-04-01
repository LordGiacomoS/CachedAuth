package com.lordgiacomos.cachedauth.api.responses;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URISyntaxException;

public class MSAResponse extends GenericResponse { //should be able to slim this code down and bury it in another class once I'm done with development
    public String userCode;
    public String deviceCode;
    public URI verificationUri;
    public int expiresInSeconds;
    public int interval; // how often (in seconds), client should check if code has been submitted through microsoft account
    public String message;


    public MSAResponse(int statusCode) {
        super(statusCode);
    }

    public MSAResponse(int statusCode, String responseString) throws URISyntaxException {
        super(statusCode);
        JsonObject json = JsonParser.parseString(responseString).getAsJsonObject();
        userCode = json.get("user_code").getAsString();
        deviceCode = json.get("device_code").getAsString();
        verificationUri = new URI(json.get("verification_uri").getAsString());
        expiresInSeconds = json.get("expires_in").getAsInt();
        interval = json.get("interval").getAsInt();
        message = json.get("message").getAsString();
    }
}