package com.lordgiacomos.cachedauth.old.api.responses;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;


public class ErrorResponse {
    public String error;
    public String errorDescription;
    public ArrayList<Integer> errorCodes;
    public String timestamp;
    public String traceId;
    public String correlationId;
    public URI errorUri;

    public ErrorResponse(String responseString) {
        parseResponseString(responseString);
    }

    private void parseResponseString(String responseString) {
        JsonObject json = JsonParser.parseString(responseString).getAsJsonObject();
        this.error = json.get("error").getAsString();
        this.errorDescription = json.get("error_description").getAsString();
        this.errorCodes = new ArrayList<>() {{
            for (JsonElement element : json.getAsJsonArray("error_codes")) {
                add(element.getAsInt());
            }
        }};
        this.timestamp = json.get("timestamp").getAsString();
        this.traceId = json.get("trace_id").getAsString();
        this.correlationId = json.get("correlation_id").getAsString();
        try {
            this.errorUri = new URI(json.get("error_uri").getAsString());
        } catch (URISyntaxException e) {
            System.out.println("could not parse error URI");
            System.out.println(e.getMessage());
            this.errorUri = null;
            //throw new CachedAuthException("Could not parse ErrorResponse URI", e);
            //error uri not that important so I can get away with ignoring it unless I'm debugging this
        }
    }
}
