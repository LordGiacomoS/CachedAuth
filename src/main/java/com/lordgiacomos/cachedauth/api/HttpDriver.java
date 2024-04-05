package com.lordgiacomos.cachedauth.api;

import com.lordgiacomos.cachedauth.CachedAuthException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.AbstractHttpMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;


public class HttpDriver {
    private static CloseableHttpClient client;
    //public final String APPLICATION_JSON = "application/json";
    //public final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    public static void startClient() {
        client = HttpClients.createDefault();
    }
    public static void stopClient() throws CachedAuthException {
        try {
            client.close();
        } catch (IOException e) {
            throw new CachedAuthException("Encountered problem trying to end http client.", e);
        }
    }

    private static URI addQueryParameters(String url, Map<String,String> queryParameters) throws URISyntaxException {
        return new URIBuilder(url) {{ queryParameters.forEach(this::addParameter); }}.build();
    }
    private static void addHeaders(AbstractHttpMessage request, Map<String,String> headers) {
        headers.forEach(request::addHeader);
    }

    /*public static CloseableHttpResponse getRequest(CloseableHttpClient client, String url, Map<String,String> queryParameters, Map<String,String> headers) throws URISyntaxException, IOException {
        //code to convert queryParameters
//        URI queryUri = new URIBuilder(url) {{ queryParameters.forEach(this::addParameter); }}.build();
//        HttpGet request = new HttpGet(queryUri) {{
//            headers.forEach(this::addHeader);
//        }};
//        return client.execute(request);
        return client.execute(
            new HttpGet(addQueryParameters(url, queryParameters)) {{
                addHeaders(this, headers);
            }}
        );
    }*/

    protected static CloseableHttpResponse getRequest(URI uri, Map<String,String> headers) throws ClientProtocolException, CachedAuthException {
        try {
            return client.execute(
                    new HttpGet(uri) {{
                        addHeaders(this, headers);
                    }}
            );
        } catch (IOException e) {
            if (e.getClass() == ClientProtocolException.class) {
                throw (ClientProtocolException) e;
            } else {
                throw new CachedAuthException("Encountered connection issues while sending request.", e);
            }
        }
    }

    /*public static CloseableHttpResponse getRequest(String url, Map<String,String> queryParameters, Map<String,String> headers, ContentType contentType) throws URISyntaxException, CachedAuthException, ClientProtocolException {
        headers.put("Content-Type", contentType.getMimeType());
        return getRequest(
                addQueryParameters(url, queryParameters),
                headers
        );
    }*/ //surprise tool that'll help me later (for checking game ownership once I get api approval)
    public static CloseableHttpResponse getRequest(String url, Map<String,String> queryParameters, ContentType contentType) throws URISyntaxException, ClientProtocolException, CachedAuthException {
        return getRequest(
                addQueryParameters(url, queryParameters),
                new HashMap<>() {{
                    put("Content-Type", contentType.getMimeType());
                }});
    }

    public static HttpPost draftPostRequest(String url, String postBody, ContentType contentType, boolean includeAccept) {
        return new HttpPost(url) {{
            addHeaders(this, new HashMap<>() {{
                put("Content-Type", contentType.getMimeType());
                if (includeAccept) {
                    put("Accept", contentType.getMimeType());
                }
            }});
            setEntity(new StringEntity(postBody, contentType));
        }};
    }
    public static CloseableHttpResponse postRequest(HttpPost draftedPost) throws ClientProtocolException, CachedAuthException {
        try {
            return client.execute(draftedPost);
        } catch (IOException e) {
            if (e.getClass() == ClientProtocolException.class) {
                throw (ClientProtocolException) e;
            } else {
                throw new CachedAuthException("Encountered connection issues while sending request.", e);
            }
        }
    }
    public static CloseableHttpResponse postRequest(String url, String postBody, ContentType contentType) throws ClientProtocolException, CachedAuthException {
        try {
            return postRequest(url, postBody, contentType, false);
        } catch (IOException e) {
            if (e.getClass() == ClientProtocolException.class) {
                throw (ClientProtocolException) e;
            } else {
                throw new CachedAuthException("Encountered connection issues while sending request.", e);
            }
        }
    }
    public static CloseableHttpResponse postRequest(String url, String postBody, ContentType contentType, boolean includeAccept) throws ClientProtocolException, CachedAuthException {
        try {
            return client.execute(
                    new HttpPost(url) {{
                        addHeaders(this, new HashMap<>() {{
                            put("Content-Type", contentType.getMimeType());
                            if (includeAccept) {
                                put("Accept", contentType.getMimeType());
                            }
                        }});
                        setEntity(new StringEntity(postBody, contentType));
                    }});
        } catch (IOException e) {
            if (e.getClass() == ClientProtocolException.class) {
                throw (ClientProtocolException) e;
            } else {
                throw new CachedAuthException("Encountered connection issues while sending request.", e);
            }
        }
    }

    /* dont delete, may need custom headers list insertion still
    public static CloseableHttpResponse postRequest(CloseableHttpClient client, String url, StringEntity postBody, Map<String,String> headers) throws IOException {
        return client.execute(
                new HttpPost(url) {{
                    addHeaders(this, headers);
                    setEntity(postBody);
                }}
        );

    }*/
}