package com.lordgiacomos.cachedauth.api.responses;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;


public class ErrorResponse extends GenericResponse {
    public String error;
    public String errorDescription;
    public ArrayList<Integer> errorCodes;
    public String timestamp;
    public String traceId;
    public String correlationId;
    public URI errorUri;

    public ErrorResponse(int statusCode) {
        super(statusCode);
    }
    public ErrorResponse(int statusCode, String responseString) {
        super(statusCode);
        JsonObject json = JsonParser.parseString(responseString).getAsJsonObject();
        error = json.get("error").getAsString();
        errorDescription = json.get("error_description").getAsString();
        errorCodes = new ArrayList<>() {{
            for (JsonElement element : json.getAsJsonArray("error_codes")) {
                add(element.getAsInt());
            }
        }};
        timestamp = json.get("timestamp").getAsString();
        traceId = json.get("trace_id").getAsString();
        correlationId = json.get("correlation_id").getAsString();
        try {
            errorUri = new URI(json.get("error_uri").getAsString());
        } catch (URISyntaxException e) {
            System.out.println("could not parse error URI");
            System.out.println(e.getMessage());
            //throw new CachedAuthException("Could not parse ErrorResponse URI", e);
            //error uri not that important so I can get away with ignoring it unless I'm debugging this
        }
    }
}
