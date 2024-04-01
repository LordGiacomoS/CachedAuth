package com.lordgiacomos.cachedauth.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.lordgiacomos.cachedauth.api.responses.MSAResponse;
import com.lordgiacomos.cachedauth.api.responses.PollResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.lordgiacomos.cachedauth.api.responses.*;


public class Authenticator { //bunch of stuff here uses `sout` rather than logger, fine for while I'm just testing api, but once Fabric gets involved with this code, it'll yell at me
    public static final String TENANT = "consumers"; // can't use common bc xbox doesn't like that
    public static final String MSA_URI = "https://login.microsoftonline.com/" + TENANT + "/oauth2/v2.0/devicecode";
    public static final String POLL_URI = "https://login.microsoftonline.com/" + TENANT + "/oauth2/v2.0/token";
    public static final String CLIENT_ID = System.getenv("CLIENT_ID"); //figure out way to obfuscate this beyond env variable, to allow distribution
    public static final String SCOPES = "user.read offline_access XBoxLive.signin"; // need to add `XboxLive.signin` scope here... I think

    public static URI setupMsaUri() throws URISyntaxException {
        return new URIBuilder(MSA_URI)
                .addParameter("client_id", CLIENT_ID)
                .addParameter("scope", SCOPES)
                .build();
    }

    public static HttpPost setupPollPost(MSAResponse msaInfo) {
        HttpPost post = new HttpPost(POLL_URI);
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setEntity(
                new StringEntity(
                        "grant_type=urn:ietf:params:oauth:grant-type:device_code&client_id=" + CLIENT_ID + "&device_code=" + msaInfo.deviceCode,
                        ContentType.APPLICATION_FORM_URLENCODED
                )
        );
        return post;
    }

    public static MSAResponse getMSAResponse(CloseableHttpClient client) throws Exception {
        HttpGet request = new HttpGet(setupMsaUri());
        request.addHeader("Content-Type", "x-www-form-urlencoded");
        try (CloseableHttpResponse response = client.execute(request)) {
            System.out.println(response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                return new MSAResponse(
                        response.getStatusLine().getStatusCode(),
                        EntityUtils.toString(entity)
                );
            } else {
                throw new Exception("MSA response invalid"); //need to figure out what should go here
            }
        }
    }





    /*public static PollResponse pollOnce(CloseableHttpClient client, HttpPost pollingPost) throws Exception {
        CloseableHttpResponse request = client.execute(pollingPost);
        System.out.println(request.getStatusLine().getStatusCode());
        if (request.getStatusLine().getStatusCode() == 200) {
            PollResponse response = new PollResponse(request.getStatusLine().getStatusCode())

        } else {
            try {
                ErrorResponse response = new ErrorResponse(request.getStatusLine().getStatusCode());

            } catch (Exception e) {
                System.out.println(e.getClass());
            }
            throw new Exception("issues with polling");
        }
    }*/


    public static PollResponse pollForAuth(MSAResponse msaInfo, CloseableHttpClient client) throws Exception {
        HttpPost pollingPost = setupPollPost(msaInfo);
        PollResponse response = null;
        for (int i = msaInfo.expiresInSeconds; i > 0; i=i- msaInfo.interval) {
            CloseableHttpResponse request = client.execute(pollingPost);
            int statusCode = request.getStatusLine().getStatusCode();
            System.out.println(statusCode);
            try {
                if (statusCode == 200) {
                    response = new PollResponse(statusCode, pollingPost.getEntity().toString());
                    break;
                } else {
                    ErrorResponse errorResponse = new ErrorResponse(statusCode, pollingPost.getEntity().toString());
                    if ("authorization_pending" != errorResponse.error) {
                        throw new Exception(
                                "Issue with authorization checking: `" + errorResponse.error + "`: `" + errorResponse.errorDescription + "`"
                        ); //figure out what specific exception I should use here
                    }
                }
            } catch (JsonSyntaxException ex) {
                System.out.println("issues with parsing error response");
                System.out.println(ex.getMessage());
            }
            Thread.sleep(msaInfo.interval*1000L);
        }
        //ideally I'd move the poll request to a separate method, but doing so would complicate error handling significantly
        return response;
    }




    public static void test() { //need to clean up this messy error handling at some point
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            MSAResponse msa = getMSAResponse(client);
            PollResponse polled = pollForAuth(msa, client);
            //


        } catch (URISyntaxException ex) {
            System.out.println("uri syntax problems");
            System.out.println(ex.getMessage());
        } catch (IOException ex2) {
            System.out.println("io problems");
            System.out.println(ex2.getMessage());
        } catch (InterruptedException e) {
            System.out.println("interruption problems");
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("other problems");
            System.out.println(e.getMessage());
        }


    }
}
