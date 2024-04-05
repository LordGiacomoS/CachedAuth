package com.lordgiacomos.cachedauth.config;

import com.google.gson.JsonObject;
import com.lordgiacomos.cachedauth.CachedAuthException;
import com.lordgiacomos.cachedauth.api.HttpDriver;
import com.lordgiacomos.cachedauth.ApiTestingEntrypoint;
import net.minecraft.util.JsonHelper;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

public class AuthenticationProfile {
    public static final String TENANT = "consumers"; // can't use common bc xbox doesn't like that
    public static final String MSA_CODE_URI = "https://login.microsoftonline.com/" + TENANT + "/oauth2/v2.0/devicecode";
    public static final String MSA_TOKEN_URI = "https://login.microsoftonline.com/" + TENANT + "/oauth2/v2.0/token";
    public static final String XBOX_AUTH_URI = "https://user.auth.xboxlive.com/user/authenticate";
    public static final String XBOX_XSTS_URI = "https://xsts.auth.xboxlive.com/xsts/authorize";
    public static final String MINECREAFT_AUTH_URI = "https://api.minecraftservices.com/authentication/login_with_xbox";
    //public static final String ApiTestingEntrypoint.CLIENT_ID = System.getenv("CLIENT_ID"); //figure out way to obfuscate this beyond env variable, to allow distribution
    public static final String SCOPES = "XBoxLive.signin offline_access";

    //cached data
    String refreshToken;
    //probs should store stuff like uuid, skins & capes


    // active session data
    String accessToken;
    String xblToken; //Xbox Live token
    String userhash;
    String xstsToken;

    //<some sorta datetime object> authedTokenExpiresAt; ?
    String jwtMcToken;


    protected void updateTokens(JsonObject responseJson) {
        this.refreshToken = responseJson.get("refresh_token").getAsString();
        this.accessToken = responseJson.get("access_token").getAsString();
    }

    protected void fromRefreshToken() throws CachedAuthException {
        try (CloseableHttpResponse response = HttpDriver.postRequest(
                MSA_TOKEN_URI,
                String.format(
                        "grant_type=refresh_token&client_id=%s&refresh_token=%s",
                        ApiTestingEntrypoint.CLIENT_ID,
                        this.refreshToken
                ), ContentType.APPLICATION_FORM_URLENCODED
        )) {
            JsonObject responseJson = JsonHelper.deserialize(EntityUtils.toString(response.getEntity()));
            updateTokens(responseJson);
        } catch (ClientProtocolException e) {
            throw new CachedAuthException("Refresh Token usage post request returned error status code", e);
            //need to amend this to actually send in status code, maybe move
        } catch (IOException e) {
            throw new CachedAuthException("Could not parse response while refreshing auth token.", e);
        }
    }

    protected JsonObject getDeviceCode() throws CachedAuthException {
        try (CloseableHttpResponse response = HttpDriver.getRequest(
                MSA_CODE_URI,
                new HashMap<>() {{
                    put("client_id", ApiTestingEntrypoint.CLIENT_ID);
                    put("scope", SCOPES);
                }},
                ContentType.APPLICATION_FORM_URLENCODED
        )) {
            JsonObject responseJson = JsonHelper.deserialize(EntityUtils.toString(response.getEntity()));
            System.out.println(responseJson.get("user_code").getAsString());
            return responseJson;
        } catch (ClientProtocolException e) {
            throw new CachedAuthException("Device Code get request returned error status code.", e);
        } catch (IOException e) {
            throw new CachedAuthException("Could not parse response while requesting device code.", e);
        } catch (URISyntaxException e) {
            throw new CachedAuthException("Encountered issue while assembling device code request uri.", e);
        }
    }

    protected void pollMicrosoftLoop() throws CachedAuthException {
        JsonObject deviceCodeResponseJson = getDeviceCode();
        HttpPost draftedPost = HttpDriver.draftPostRequest(
                MSA_TOKEN_URI,
                String.format(
                        "grant_type=urn:ietf:params:oauth:grant-type:device_code&client_id=%s&device_code=%s",
                        ApiTestingEntrypoint.CLIENT_ID,
                        deviceCodeResponseJson.get("device_code").getAsString()
                ),
                ContentType.APPLICATION_FORM_URLENCODED,
                false
        );
        try {
            int msaInterval = deviceCodeResponseJson.get("interval").getAsInt();
            for (int i=deviceCodeResponseJson.get("expires_in").getAsInt(); i>0; i=i-msaInterval) {
                try (CloseableHttpResponse pollResponse = HttpDriver.postRequest(draftedPost)) {
                    JsonObject pollResponseJson = JsonHelper.deserialize(EntityUtils.toString(pollResponse.getEntity()));
                    String pollResponseError;
                    if (pollResponse.getStatusLine().getStatusCode() == 200) {
                        updateTokens(pollResponseJson);
                        break;
                    } else if ("authorization_pending".equals((pollResponseError = pollResponseJson.get("error").getAsString()))) {
                        Thread.sleep(deviceCodeResponseJson.get("interval").getAsInt() * 1000L);
                    } else {
                        throw new CachedAuthException(String.format(
                                "Microsoft poller experienced error `%s`: `%s`.",
                                pollResponseError,
                                pollResponseJson.get("error_description").getAsString()
                        ));
                    }
                } catch (ClientProtocolException e) {
                    throw new CachedAuthException("MSA Poller recieved error status code.", e);
                } catch (IOException e) {
                    throw new CachedAuthException("Could not parse response from MSA poll request.", e);
                }
            }
        } catch (InterruptedException e) {
            throw new CachedAuthException("MSA Poller experienced unexpected interruption.", e);
        }
    }

    protected void authXboxLive() throws CachedAuthException {
        try(CloseableHttpResponse response = HttpDriver.postRequest(
                XBOX_AUTH_URI,
                String.format(
                        "{\"Properties\": {\"AuthMethod\": \"RPS\", \"SiteName\": \"user.auth.xboxlive.com\", \"RpsTicket\": \"d=%s\"}, \"RelyingParty\": \"http://auth.xboxlive.com\", \"TokenType\": \"JWT\"}",
                        accessToken
                ),
                ContentType.APPLICATION_JSON,
                true
        )) {
            if (response.getStatusLine().getStatusCode() == 200) {
                JsonObject responseJson = JsonHelper.deserialize(EntityUtils.toString(response.getEntity()));
                this.xblToken = responseJson.get("Token").getAsString();
                this.userhash = responseJson.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString();
            } else {
                throw new CachedAuthException("Unknown issue with XBox Live Request.");
            }
        } catch (ClientProtocolException e) {
            throw new CachedAuthException("XBL token post request returned error status code.", e);
        } catch (IOException e) {
            throw new CachedAuthException("Could not parse while authenticating with XBoxLive.", e);
        }
    }

    protected void fetchXstsToken() throws CachedAuthException { //need to add handling for known xerr codes
        try (CloseableHttpResponse response = HttpDriver.postRequest(
                XBOX_XSTS_URI,
                String.format(
                        "{\"Properties\": {\"SandboxId\": \"RETAIL\", \"UserTokens\": [\"%s\"]}, \"RelyingParty\": \"rp://api.minecraftservices.com/\", \"TokenType\": \"JWT\"}",
                        xblToken
                ),
                ContentType.APPLICATION_JSON,
                true
        )) {
            if (response.getStatusLine().getStatusCode() == 200) {
                JsonObject responseJson = JsonHelper.deserialize(EntityUtils.toString(response.getEntity()));
                this.xstsToken = responseJson.get("Token").getAsString();
                if (!responseJson.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString().equals(this.userhash)) {
                    throw new CachedAuthException("XSTS userhash does not match XBL userhash.");
                }
            } else {
                throw new CachedAuthException("Unknown issue with XBox Live Request.");
            }
        } catch (ClientProtocolException e) {
            throw new CachedAuthException("XSTS Token post request returned error status code.", e);
        } catch (IOException e) {
            throw new CachedAuthException("Could not parse response while fetching XSTS token.", e);
        }
    }

    protected void authMinecraft() throws CachedAuthException {
        try (CloseableHttpResponse response = HttpDriver.postRequest(
                MINECREAFT_AUTH_URI,
                String.format(
                        "{\"identityToken\": \"XBL3.0 x=%s;%s\"}",
                        userhash,
                        xstsToken
                ),
                ContentType.APPLICATION_JSON
        )) {
            JsonObject responseJson = JsonHelper.deserialize(EntityUtils.toString(response.getEntity()));
            String errorMessage;
            if (response.getStatusLine().getStatusCode() == 200) {
                this.jwtMcToken = responseJson.get("access_token").getAsString();
            } else if ("Invalid app registration, see https://aka.ms/AppRegInfo for more information".equals((errorMessage = responseJson.get("errorMessage").getAsString()))) {
                throw new CachedAuthException("Invalid app registration. The ApiTestingEntrypoint.CLIENT_ID provided is not currently authorized to access Mojang APIs.");
            } else if (errorMessage != null) {
                throw new CachedAuthException(String.format("Encountered error while authenticating with Mojang: `%s`.", errorMessage));
            } else {
                throw new CachedAuthException("Encountered unknown error while authenticating with Mojang.");
            }
        } catch (ClientProtocolException e) {
            throw new CachedAuthException("Minecraft authentication request returned error status code.", e);
        } catch (IOException e) {
            throw new CachedAuthException("Could not parse response while authenticating with Mojang");
        }
    }


    // config import/export stuff
    public JsonObject getJsonOutput() {
        JsonObject output = new JsonObject();
        output.addProperty("refreshToken", refreshToken);

        return output;
    }

    public AuthenticationProfile(JsonObject savedJson) {
        this.refreshToken = savedJson.get("refreshToken").getAsString();
    }

    public AuthenticationProfile() {} //exists for creating new auth profile


    public void initializeSession() throws CachedAuthException {

        HttpDriver.startClient(); //only starting http client when actively needing to make series of requests
        if (this.refreshToken != null) {
            fromRefreshToken();
        } else {
            pollMicrosoftLoop();
        }
        authXboxLive();
        fetchXstsToken();
        authMinecraft();

        HttpDriver.stopClient();
    }


}
