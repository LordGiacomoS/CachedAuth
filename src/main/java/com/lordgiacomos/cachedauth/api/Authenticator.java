package com.lordgiacomos.cachedauth.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Arrays;

import com.google.gson.JsonSyntaxException;
import com.lordgiacomos.cachedauth.CachedAuthException;
import com.lordgiacomos.cachedauth.api.responses.MsaCodeResponse;
import com.lordgiacomos.cachedauth.api.responses.MsaTokenResponse;
import com.lordgiacomos.cachedauth.config.AuthenticationProfile;
import com.lordgiacomos.cachedauth.config.CachedAuthConfig;
import com.lordgiacomos.cachedauth.config.CachedAuthConfigManager;
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
    public static final String XBOX_XSTS_URI = "https://xsts.auth.xboxlive.com/xsts/authorize";
    public static final String MINECREAFT_AUTH_URI = "https://api.minecraftservices.com/authentication/login_with_xbox";
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

    public static HttpPost setupXboxLiveAuthenticationPost(MsaTokenResponse pollInfo) throws UnsupportedEncodingException {
        HttpPost post = new HttpPost(XBOX_AUTH_URI);
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Accept", "application/json");
        post.setEntity(
            new StringEntity(
                String.format("""
                    {
                        "Properties": {
                            "AuthMethod": "RPS",
                            "SiteName": "user.auth.xboxlive.com",
                            "RpsTicket": "d=%s"
                        },
                        "RelyingParty": "http://auth.xboxlive.com",
                        "TokenType": "JWT"
                    }""", pollInfo.accessToken)
            ));
        // apparently "SiteName" needs to be "user.auth.xboxlive.com" but "RelyingParty" needs to be "http://auth.xboxlive.com"
        //   thats like 8 hours of me trying to bugfix something incredibly stupid because xbox api gave no info about what i was doing wrong lol
        return post;
    }

    public static HttpPost setupXstsFetchingPost(XblResponse xblAuth) throws UnsupportedEncodingException {
        HttpPost post = new HttpPost(XBOX_XSTS_URI);
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Accept", "application/json");
        post.setEntity(
            new StringEntity(
                String.format("""
                    {
                        "Properties": {
                            "SandboxId": "RETAIL",
                            "UserTokens": [
                                "%s"
                            ]
                        },
                        "RelyingParty": "rp://api.minecraftservices.com/",
                        "TokenType": "JWT"
                    }""", xblAuth.token)
        ));
        return post;
    }

    public static HttpPost setupMinecraftAuthenticationPost(XblResponse xblXsts) throws UnsupportedEncodingException {
        HttpPost post = new HttpPost(MINECREAFT_AUTH_URI);
        post.addHeader("Content-Type", "application/json");
        //post.addHeader("Accept", "application/json"); this one doesnt need this weirdly
        post.setEntity(
            new StringEntity(
                String.format("""
                    {
                        "identityToken": "XBL3.0 x=%s;%s"
                    }""", xblXsts.userhash, xblXsts.token)
        ));
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
                return new MsaCodeResponse(statusCode, output);
            } else {
                throw new CachedAuthException("MSA request invalid"); //need to figure out what Exception should actually go here
            }
        } catch (IOException e) {
            throw new CachedAuthException("Could not establish contact with Microsoft.", e);
        }
    }

    public static MsaTokenResponse pollMicrosoftOnce(HttpPost pollingPost, CloseableHttpClient client) throws CachedAuthException {
        try (CloseableHttpResponse request = client.execute(pollingPost)) {
            int statusCode = request.getStatusLine().getStatusCode();
            String output = EntityUtils.toString(request.getEntity());
            //System.out.println(output);
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
            throw new CachedAuthException("Repeated checking for code input was interrupted.", e);
        }
    }

    public static XblResponse authXboxLive(MsaTokenResponse pollInfo, CloseableHttpClient client) throws CachedAuthException {
        try (CloseableHttpResponse request = client.execute(setupXboxLiveAuthenticationPost(pollInfo))) {
            int statusCode = request.getStatusLine().getStatusCode();
            System.out.println(statusCode);
            String output = EntityUtils.toString(request.getEntity());

            if (statusCode == 200) {
                //System.out.println(output);
                return new XblResponse(statusCode, output);
            } else {
                System.out.println(output);
                System.out.println(request);
                throw new CachedAuthException("Xbox Live not happy"); // give more detail about what went wrong if possible (though the api's response to errors makes that nearly impossible)
            }
        } catch (IOException e) { //this also catches wrongly formed http post stuff so need to subdivide and catch UnsupportedEncodingException separately
            throw new CachedAuthException("Could not establish contact with XBox Live.", e);
        }
    }

    public static XblResponse getXstsToken(XblResponse xblAuth, CloseableHttpClient client) throws CachedAuthException {
        try(CloseableHttpResponse request = client.execute(setupXstsFetchingPost(xblAuth))) {
            int statusCode = request.getStatusLine().getStatusCode();
            System.out.println(statusCode);
            String output = EntityUtils.toString(request.getEntity());

            if (statusCode == 200) {
                return new XblResponse(statusCode, output);
            } else { //add handling for xboxlive errors due to age & stuff
                System.out.println(output);
                System.out.println(request);
                throw new CachedAuthException("XBox Live not happy");
            }

        } catch (IOException e) { //this also catches wrongly formed http post stuff so need to subdivide and catch UnsupportedEncodingException separately
            throw new CachedAuthException("Could not establish contact with XBox Live.", e);
        }
    }

    public static AuthMinecraftResponse authMinecraft(XblResponse xblXsts, CloseableHttpClient client) throws CachedAuthException {
        try(CloseableHttpResponse request = client.execute(setupMinecraftAuthenticationPost(xblXsts))) {
            int statusCode = request.getStatusLine().getStatusCode();
            System.out.println(statusCode);
            String output = EntityUtils.toString(request.getEntity());

            if (statusCode == 200) {
                System.out.println(output);
                return new AuthMinecraftResponse(statusCode, output);
            } else { //add check mentioning that if using custom oauth client, you need to fill out and get approved the form at `https://aka.ms/mce-reviewappid` -- see console output on next line to know what to look for
                /*403
{
  "path" : "/authentication/login_with_xbox",
  "errorMessage" : "Invalid app registration, see https://aka.ms/AppRegInfo for more information"
}
HttpResponseProxy{HTTP/1.1 403 Forbidden [Date: Thu, 04 Apr 2024 15:36:20 GMT, Content-Type: application/json, Content-Length: 147, Connection: keep-alive, x-minecraft-request-id: ccb5ef8bf380b174, Cache-Control: no-store, x-azure-ref: 20240404T153620Z-179b4d87857tgwfxsr6mt1naxg000000063g000000006v0c, X-Cache: TCP_MISS] ResponseEntityProxy{[Content-Type: application/json,Content-Length: 147,Chunked: false]}}
problems
minecraft services api not happy*/
                System.out.println(output);
                System.out.println(request);
                throw new CachedAuthException("minecraft services api not happy");
            }
        } catch (IOException e) { //this also catches wrongly formed http post stuff so need to subdivide and catch UnsupportedEncodingException separately
            throw new CachedAuthException("Could not establish contact with `api.minecraftservices.com`.");
        }
    }



    public static void continueWithMergedFlow(AuthenticationProfile authenticationProfile, CloseableHttpClient client, MsaTokenResponse msaTokenResponse) throws CachedAuthException {
        authenticationProfile.setAccessToken(msaTokenResponse.accessToken);
        authenticationProfile.setRefreshToken(msaTokenResponse.refreshToken);
        XblResponse xblAuth = authXboxLive(msaTokenResponse, client);
        authenticationProfile.setXblToken(xblAuth.token);
        XblResponse xblXsts = getXstsToken(xblAuth, client);
        authenticationProfile.setUserhash(xblXsts.userhash);
        // may wanna add failsafe check that `xblAuth.userhash == xblXsts.userhash` are the same even though they shouldn't ever be different, and if they were, it would probably give another error later
        // -- add this as debug mode feature?
        //System.out.println(CachedAuthConfig.GSON.toJson(xblXsts));
        authenticationProfile.setXstsToken(xblXsts.token);
        AuthMinecraftResponse authMinecraftResponse = authMinecraft(xblXsts, client);
    }

    public static void testDeviceFlow() { //need to clean up this messy error handling at some point
        AuthenticationProfile authenticationProfile = CachedAuthConfig.getAuthenticationProfiles().get(0);
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            MsaCodeResponse msa = getMsaResponse(client);
            System.out.println(msa.userCode);
            MsaTokenResponse polled = pollForMicrosoftAuth(msa, client);
            continueWithMergedFlow(authenticationProfile, client, polled);
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
            continueWithMergedFlow(authenticationProfile, client, refreshed);
        } catch (CachedAuthException e) {
            System.out.println("problems");
            System.out.println(e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
        } catch (Exception e) {
            CachedAuthConfigManager.save();
            throw e;
        }
    }
}
