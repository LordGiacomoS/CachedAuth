package com.lordgiacomos.cachedauth.api.responses;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Map;

public class XblResponse extends GenericResponse {
    /* syntax of response according to wiki.vg
     {
       "IssueInstant":"2020-12-07T19:52:08.4463796Z",
       "NotAfter":"2020-12-21T19:52:08.4463796Z",
       "Token":"token", // save this, this is your xbl token
       "DisplayClaims":{
         "xui":[
           {
             "uhs":"userhash" // save this
           }
         ]
       }
     }
     */

    String issueInstant;
    String notAfter;
    String token;
    //Map<String, ArrayList<Map<String, String>>> displayClaims; //there has to be less idiotic way to write this
    String userhash; //figured out how to intelligently save it lol


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
}
