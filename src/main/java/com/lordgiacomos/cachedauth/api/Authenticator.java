package com.lordgiacomos.cachedauth.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Authenticator { //bunch of stuff here uses `sout` rather than logger, fine for while I'm just testing api, but once Fabric gets involved with this code, it'll yell at me
    public static final String URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode";
    public static final String CLIENT_ID = System.getenv("CLIENT_ID"); //figure out way to obfuscate this beyond env variable, to allow distribution
    public static final String SCOPES = "user.read"; // need to add `XboxLive.signin` scope here... I think

    public static URI setupURI() throws URISyntaxException {
        return new URIBuilder(URL)
                .addParameter("client_id", CLIENT_ID)
                .addParameter("scope", SCOPES)
                .build();
    }

    public static String getResponse(URI uri, CloseableHttpClient client) throws IOException {
        String result = "";
        HttpGet request = new HttpGet(uri);
        try (CloseableHttpResponse response = client.execute(request)) {
            System.out.println(response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        }
        return result;
    }

    public static void test() { //need to clean up this messy error handling at some point
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            URI uri = setupURI();
            String result = getResponse(uri, client);
            MSAResponse parsed = new MSAResponse(result);
        } catch (URISyntaxException ex) {
            System.out.println("uri syntax problems");
            System.out.println(ex.getMessage());
        } catch (IOException ex2) {
            System.out.println("io problems");
            System.out.println(ex2.getMessage());
        }
    }
}
