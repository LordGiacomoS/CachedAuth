package com.lordgiacomos.cachedauth.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Authenticator { //bunch of stuff here uses `sout` rather than logger, fine for while I'm just testing api, but once Fabric gets involved with this code, it'll yell at me
    public static final String URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode";
    public static final String CLIENT_ID = System.getenv("CLIENT_ID"); //figure out way to obfuscate this beyond env variable, to allow distribution
    public static final String SCOPES = "user.read"; // need to add `XboxLive.signin` scope here... I think

    public static URI setupMsaUri() throws URISyntaxException {
        return new URIBuilder(URL)
                .addParameter("client_id", CLIENT_ID)
                .addParameter("scope", SCOPES)
                .build();
    }

    public static String getMSAResponse1(URI uri, CloseableHttpClient client) throws IOException {
        String result = "";
        HttpGet request = new HttpGet(uri);
        request.addHeader("Content-Type", "x-www-form-urlencoded");
        try (CloseableHttpResponse response = client.execute(request)) {
            System.out.println(response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        }
        return result;
    }

    public static MSAResponse msaDeviceCode(CloseableHttpClient client) throws URISyntaxException, IOException {
        //msa lvl 1
        URI uri = setupMsaUri();
        String result = getMSAResponse1(uri, client);
        return new MSAResponse(result);
    }


    //public static String pollOnce(HttpPost post, CloseableHttpClient client) throws IOException {
    //    String result = "";

        //post.addHeader("Content-Type", "application/json");
        //post.addHeader("Accept", "application/json");
    //}

    public static void pollForAuth(MSAResponse msaInfo, CloseableHttpClient client) throws InterruptedException, URISyntaxException, IOException {
        String result = "";

        //incredibly messy, need to make this url stuff nicer
        URI pollUri = new URIBuilder("https://login.microsoftonline.com/consumers/oauth2/v2.0/token")
                .setCustomQuery(
                        new StringBuilder("grant_type=urn:ietf:params:oauth:grant-type:device_code&clientid=")
                                .append(CLIENT_ID)
                                .append("&device_code=")
                                .append(msaInfo.deviceCode)
                                .toString())
                .build();

                //.addParameter("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
                //.addParameter("client_id", CLIENT_ID)
                //.addParameter("device_code", msaInfo.deviceCode)
                //.build();
        System.out.println(pollUri);
        HttpPost post = new HttpPost(pollUri);
        post.addHeader("Content-Type", "x-www-form-urlencoded");

        //for (int i = msaInfo.expiresInSeconds; i > 0; i=i- msaInfo.interval) {
        CloseableHttpResponse response = client.execute(post);
        System.out.println(response.getStatusLine().getStatusCode());
        result = EntityUtils.toString(response.getEntity());
        System.out.println(JsonParser.parseString(result).toString());

        //for some reason my poll attempt is considered an invalid request because I don't have `grant_type` parameter
        // in url, even though I do. I really hate oauth sometimes. Time to try and get working prototype in python because
        // that language allows better rapid prototyping of http request stuff

        //    Thread.sleep(msaInfo.interval* 1000L);
        //}
    }




    public static void test() { //need to clean up this messy error handling at some point
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            MSAResponse response = msaDeviceCode(client);
            pollForAuth(response, client);

        } catch (URISyntaxException ex) {
            System.out.println("uri syntax problems");
            System.out.println(ex.getMessage());
        } catch (IOException ex2) {
            System.out.println("io problems");
            System.out.println(ex2.getMessage());
        } catch (InterruptedException e) {
            System.out.println("interruption problems");
            System.out.println(e.getMessage());
        }


    }
}
