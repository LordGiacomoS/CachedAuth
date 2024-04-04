package com.lordgiacomos.cachedauth.old.api.responses;

public class GenericResponse {
    public int statusCode;
    public ErrorResponse errorResponse;


    public GenericResponse(int statusCode) {
        this.statusCode = statusCode;
    }
    /*public GenericResponse(int statusCode, String responseString) {
        this.statusCode = statusCode;
        this.errorResponse = new ErrorResponse(responseString);
    }*/
    public void setErrorResponse(String responseString) {
        this.errorResponse = new ErrorResponse(responseString);
    }
}
