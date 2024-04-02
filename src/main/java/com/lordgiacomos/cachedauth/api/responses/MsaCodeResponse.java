package com.lordgiacomos.cachedauth.api.responses;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URISyntaxException;

public class MsaCodeResponse extends GenericResponse { //should be able to slim this code down and bury it in another class once I'm done with development
    public String userCode;
    public String deviceCode;
    public URI verificationUri;
    public int expiresInSeconds;
    public int interval; // how often (in seconds), client should check if code has been submitted through microsoft account
    public String message;

    public MsaCodeResponse(int statusCode) {
        super(statusCode);
    }

    public MsaCodeResponse(int statusCode, String responseString) {
        super(statusCode);
        parseResponseString(responseString);
    }

    private void parseResponseString(String responseString) {
        JsonObject json = JsonParser.parseString(responseString).getAsJsonObject();
        this.userCode = json.get("user_code").getAsString();
        this.deviceCode = json.get("device_code").getAsString();
        try {
            this.verificationUri = new URI(json.get("verification_uri").getAsString());
        } catch (URISyntaxException e) {
            System.out.println("could not parse msa verification uri");
            System.out.println(e.getMessage());
            //throw new CachedAuthException("Could not parse MsaCodeResponse verificationUri", e);
            this.verificationUri = null;
            //almost certainly going to be `https://microsoft.com/link` so I should be fine to ignore it
        }
        this.expiresInSeconds = json.get("expires_in").getAsInt();
        this.interval = json.get("interval").getAsInt();
        this.message = json.get("message").getAsString();
    }
}