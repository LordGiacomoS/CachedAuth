package com.lordgiacomos.cachedauth.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.lordgiacomos.cachedauth.CachedAuthException;
import com.lordgiacomos.cachedauth.api.responses.MsaCodeResponse;
import com.lordgiacomos.cachedauth.api.responses.MsaTokenResponse;
import com.lordgiacomos.cachedauth.config.AuthenticationProfile;
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
    public static final String MSA_CODE_URI = "https://login.microsoftonline.com/" + TENANT + "/oauth2/v2.0/devicecode";
    public static final String MSA_TOKEN_URI = "https://login.microsoftonline.com/" + TENANT + "/oauth2/v2.0/token";
    public static final String XBOX_AUTH_URI = "https://user.auth.xboxlive.com/user/authenticate";
    public static final String CLIENT_ID = System.getenv("CLIENT_ID"); //figure out way to obfuscate this beyond env variable, to allow distribution
    public static final String SCOPES = "XBoxLive.signin offline_access";//"user.read profile openid offline_access XBoxLive.signin"; // need to add `XboxLive.signin` scope here... I think

    public static HttpPost setupRefreshPost(String refreshToken) {
        HttpPost post = new HttpPost(MSA_TOKEN_URI);
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setEntity(
                new StringEntity(
                        "grant_type=refresh_token&client_id=" + CLIENT_ID + "&refresh_token=" + refreshToken,
                        ContentType.APPLICATION_FORM_URLENCODED
                )
        );
        return post;
    }

    public static HttpGet setupMsaRequest() throws CachedAuthException {
        try {
            HttpGet request = new HttpGet(
                new URIBuilder(MSA_CODE_URI)
                    .addParameter("client_id", CLIENT_ID)
                    .addParameter("scope", SCOPES)
                    .build()
            );
            request.addHeader("Content-Type", "x-www-form-urlencoded");
            return request;
        } catch (URISyntaxException e) {
            throw new CachedAuthException("Issue with setting up request for device code", e);
        }
    }

    public static HttpPost setupPollPost(MsaCodeResponse msaInfo) {
        HttpPost post = new HttpPost(MSA_TOKEN_URI);
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setEntity(
                new StringEntity(
                        "grant_type=urn:ietf:params:oauth:grant-type:device_code&client_id=" + CLIENT_ID + "&device_code=" + msaInfo.deviceCode,
                        ContentType.APPLICATION_FORM_URLENCODED
                )
        );
        return post;
    }

    public static HttpPost setupXboxLiveAuth(MsaTokenResponse pollInfo) {
        HttpPost post = new HttpPost(XBOX_AUTH_URI);
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
                        postJson.toString(),
                        ContentType.APPLICATION_JSON
                )
        );
        return post;
    }


    public static MsaTokenResponse refreshFromToken(String refreshToken, CloseableHttpClient client) throws CachedAuthException {
        try (CloseableHttpResponse request = client.execute(setupRefreshPost(refreshToken))) {
            int statusCode = request.getStatusLine().getStatusCode();
            System.out.println(statusCode);
            String output = EntityUtils.toString(request.getEntity());

            System.out.println(output);
            MsaTokenResponse response = new MsaTokenResponse(statusCode, output);
            if (statusCode == 200) {
                return response;
            } else {
                throw new CachedAuthException(
                        "Issue with authorization checking: `" + response.errorResponse.error + "`: `" + response.errorResponse.errorDescription + "`"
                ); //figure out what specific exception I should use here
            }
        } catch (IOException e) {
            throw new CachedAuthException("Could not establish contact with Microsoft.", e);
        }
    }

    public static MsaCodeResponse getMsaResponse(CloseableHttpClient client) throws CachedAuthException {
        try (CloseableHttpResponse request = client.execute(setupMsaRequest())) {
            int statusCode = request.getStatusLine().getStatusCode();
            System.out.println(statusCode);
            HttpEntity entity = request.getEntity();

            if (entity != null) {
                String output = EntityUtils.toString(entity);
                System.out.println(output);
                return new MsaCodeResponse(statusCode, output);
            } else {
                throw new CachedAuthException("MSA request invalid"); //need to figure out what Exception should actually go here
            }
        } catch (IOException e) {
            throw new CachedAuthException("Could not establish contact with Microsoft", e);
        }
    }

    public static MsaTokenResponse pollMicrosoftOnce(HttpPost pollingPost, CloseableHttpClient client) throws CachedAuthException {
        try (CloseableHttpResponse request = client.execute(pollingPost)) {
            int statusCode = request.getStatusLine().getStatusCode();
            //System.out.println(statusCode);
            String output = EntityUtils.toString(request.getEntity());
            System.out.println(output);
            return new MsaTokenResponse(statusCode, output);
        } catch (JsonSyntaxException e) {
            throw new CachedAuthException("Issue with parsing response from MSA Token polling.", e);
        } catch (IOException e) {
            throw new CachedAuthException("Could not establish contact with Microsoft.", e);
        }
    }
    public static MsaTokenResponse pollForMicrosoftAuth(MsaCodeResponse msaInfo, CloseableHttpClient client) throws CachedAuthException {
        try {
            HttpPost pollingPost = setupPollPost(msaInfo);
            //System.out.println(msaInfo.expiresInSeconds);
            //System.out.println(msaInfo.interval);
            for (int i = msaInfo.expiresInSeconds; i > 0; i=i-msaInfo.interval) {
                System.out.println(i);
                MsaTokenResponse response = pollMicrosoftOnce(pollingPost, client);
                if (response.statusCode == 200) {
                    return response;
                } else if ("authorization_pending".equals(response.errorResponse.error)) {
                    Thread.sleep(msaInfo.interval * 1000L); //im sure there's a better way to do timing stuff using fabric, maybe ClientTickEvent
                } else {
                    throw new CachedAuthException(
                        "Issue with authorization checking: `" + response.errorResponse.error + "`: `" + response.errorResponse.errorDescription + "`."
                    );
                }
            }
            throw new CachedAuthException("Input Code expired");
        } catch (InterruptedException e) {
            throw new CachedAuthException("Timing of polling was interrupted.", e);
        }
    }

    public static XblResponse authXboxLive(MsaTokenResponse pollInfo, CloseableHttpClient client) throws CachedAuthException {
        try (CloseableHttpResponse request = client.execute(setupXboxLiveAuth(pollInfo))) {
            int statusCode = request.getStatusLine().getStatusCode();
            System.out.println(statusCode);
            String output = EntityUtils.toString(request.getEntity());

            if (statusCode == 200) {
                System.out.println(output);
                return new XblResponse(statusCode, output);
            } else {
                System.out.println(output);
                System.out.println(request);
                throw new CachedAuthException("Xbox Live not happy");
            }
        } catch (IOException e) {
            throw new CachedAuthException("Could not establish contact with XBox Live.");
        }
    }


    public static void testDeviceFlow() { //need to clean up this messy error handling at some point
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            MsaCodeResponse msa = getMsaResponse(client);
            MsaTokenResponse polled = pollForMicrosoftAuth(msa, client);
            XblResponse xbl = authXboxLive(polled, client);
        } catch (CachedAuthException e) {
            System.out.println("problems");
            System.out.println(e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    public static void fromRefreshTokenAuth(AuthenticationProfile authenticationProfile) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            MsaTokenResponse refreshed = refreshFromToken(authenticationProfile.refreshToken, client);
            //update authentication profile info here
            authenticationProfile.setAccessToken(refreshed.accessToken);
            authenticationProfile.setRefreshToken(refreshed.refreshToken);
            XblResponse xbl = authXboxLive(refreshed, client);
        } catch (CachedAuthException e) {
            System.out.println("problems");
            System.out.println(e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
