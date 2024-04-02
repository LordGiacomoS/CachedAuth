package com.lordgiacomos.cachedauth.api.responses;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lordgiacomos.cachedauth.CachedAuthException;

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
        JsonObject json = JsonParser.parseString(responseString).getAsJsonObject();
        userCode = json.get("user_code").getAsString();
        deviceCode = json.get("device_code").getAsString();
        try {
            verificationUri = new URI(json.get("verification_uri").getAsString());
        } catch (URISyntaxException e) {
            System.out.println("could not parse msa verification uri");
            System.out.println(e.getMessage());
            //throw new CachedAuthException("Could not parse MsaCodeResponse verificationUri", e);

            //almost certainly going to be `https://microsoft.com/link` so I should be fine to ignore it
        }
        expiresInSeconds = json.get("expires_in").getAsInt();
        interval = json.get("interval").getAsInt();
        message = json.get("message").getAsString();
    }
}