package com.lordgiacomos.cachedauth.api.responses;

import java.util.ArrayList;
import java.util.Map;

public class XblResponse extends GenericResponse {
    String issueInstant;
    String notAfter;
    String token;
    Map<String, ArrayList<Map<String, String>>> displayClaims; //there has to be less idiotic way to write this




    public XblResponse(int statusCode) {
        super(statusCode);
    }
    public XblResponse(int statusCode, String responseString) {
        super(statusCode);
        parseResponseString(responseString);
    }

    private void parseResponseString(String responseString) {

    }
}
