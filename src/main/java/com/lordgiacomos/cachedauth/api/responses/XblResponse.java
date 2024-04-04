package com.lordgiacomos.cachedauth.api.responses;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class XblResponse extends GenericResponse {
    public String issueInstant;
    public String notAfter;
    public String token; //xbltoken & xsts token... using this class twice since json is pretty much a duplicate in api
    //Map<String, ArrayList<Map<String, String>>> displayClaims; //there has to be less idiotic way to write this
    public String userhash; //figured out the less idiotic way (for this context)


    public XblResponse(int statusCode) {
        super(statusCode);
    }
    public XblResponse(int statusCode, String responseString) {
        super(statusCode);
        parseResponseString(responseString);
    }

    private void parseResponseString(String responseString) {
        JsonObject json = JsonParser.parseString(responseString).getAsJsonObject();
        this.issueInstant = json.get("IssueInstant").getAsString();
        this.notAfter = json.get("NotAfter").getAsString();
        this.token = json.get("Token").getAsString();
        this.userhash = json.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString();
    }


    //used for xbl auth token & xsts token
}
