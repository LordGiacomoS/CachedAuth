package com.lordgiacomos.cachedauth.api;

import java.io.IOException;
import java.net.URISyntaxException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.lordgiacomos.cachedauth.api.responses.MSAResponse;
import com.lordgiacomos.cachedauth.api.responses.PollResponse;
import org.apache.http.HttpEntity;
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
    public static final String XBOX_URI = "https://user.auth.xboxlive.com/user/authenticate";
    public static final String CLIENT_ID = System.getenv("CLIENT_ID"); //figure out way to obfuscate this beyond env variable, to allow distribution
    public static final String SCOPES = "user.read offline_access XBoxLive.signin"; // need to add `XboxLive.signin` scope here... I think



    public static HttpGet setupMsaRequest() throws URISyntaxException {
        HttpGet request = new HttpGet(
                new URIBuilder(MSA_URI)
                        .addParameter("client_id", CLIENT_ID)
                        .addParameter("scope", SCOPES)
                        .build()
        );
        request.addHeader("Content-Type", "x-www-form-urlencoded");
        return request;
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

    public static HttpPost setupXboxLiveAuth(PollResponse pollInfo) {
        HttpPost post = new HttpPost(XBOX_URI);
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Accept", "application/json");

        JsonObject postJson = new JsonObject();
        JsonObject properties = new JsonObject();
        properties.addProperty("AuthMethod", "RPS");
        properties.addProperty("SiteName", "user.auth.xboxlive.com");
        properties.addProperty("RpsTicket", "d="+pollInfo.accessToken);

        postJson.add("Properties", properties);
        postJson.addProperty("RelyingParty", "http://user.auth.xboxlive.com");
        postJson.addProperty("TokenType", "JWT");

        post.setEntity(
                new StringEntity(
                        postJson.getAsString(),
                        ContentType.APPLICATION_JSON
                )
        );
        return post;
    }

    public static MSAResponse getMSAResponse(CloseableHttpClient client) throws Exception {
        try (CloseableHttpResponse request = client.execute(setupMsaRequest())) {
            int statusCode = request.getStatusLine().getStatusCode();
            System.out.println(statusCode);
            HttpEntity entity = request.getEntity();

            if (entity != null) {
                String output = EntityUtils.toString(entity);
                System.out.println(output);
                return new MSAResponse(
                        statusCode,
                        output
                );
            } else {
                throw new Exception("MSA request invalid"); //need to figure out what Exception should actually go here
            }
        }
    }

    public static PollResponse pollForMicrosoftAuth(MSAResponse msaInfo, CloseableHttpClient client) throws IOException, URISyntaxException, Exception {
        HttpPost pollingPost = setupPollPost(msaInfo);
        PollResponse response = null;
        for (int i = msaInfo.expiresInSeconds; i > 0; i=i- msaInfo.interval) {
            CloseableHttpResponse request = client.execute(pollingPost);
            int statusCode = request.getStatusLine().getStatusCode();
            System.out.println(statusCode);
            String output = EntityUtils.toString(request.getEntity());
            System.out.println(output);
            try {
                if (statusCode == 200) {
                    response = new PollResponse(statusCode, output);
                    break;
                } else {
                    ErrorResponse errorResponse = new ErrorResponse(statusCode, output);
                    if ("authorization_pending" != errorResponse.error) {
                        throw new Exception(
                                "Issue with authorization checking: `" + errorResponse.error + "`: `" + errorResponse.errorDescription + "`"
                        ); //figure out what specific exception I should use here
                    }
                }
            } catch (JsonSyntaxException ex) {
                System.out.println("issues with parsing error response");
                System.out.println(ex.getMessage());
                System.out.println(output);
            }
            Thread.sleep(msaInfo.interval*1000L);
        } //im sure there's a better way to do timing stuff using fabric
        //ideally I'd move the poll request to a separate method, but doing so would complicate error handling significantly
        return response;
    }


    public static void authXboxLive(PollResponse pollInfo, CloseableHttpClient client) throws IOException {
        try (CloseableHttpResponse request = client.execute(setupXboxLiveAuth(pollInfo))) {
            int statusCode = request.getStatusLine().getStatusCode();
            System.out.println(EntityUtils.toString(request.getEntity()));
        }
    }

    public static void test() { //need to clean up this messy error handling at some point
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            MSAResponse msa = getMSAResponse(client);
            PollResponse polled = pollForMicrosoftAuth(msa, client);
            authXboxLive(polled, client);


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
