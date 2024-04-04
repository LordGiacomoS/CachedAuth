package com.lordgiacomos.cachedauth.api;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.AbstractHttpMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class HttpDriver {
    //public final String APPLICATION_JSON = "application/json";
    //public final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

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
    


    public static CloseableHttpResponse getRequest(CloseableHttpClient client, String url, Map<String,String> queryParameters, ContentType contentType) throws URISyntaxException, IOException {
        return client.execute(
                new HttpGet(addQueryParameters(url, queryParameters)) {{
                    addHeader("Content-Type", contentType.getMimeType());
                }}
        );
    }

    public static CloseableHttpResponse postRequest(CloseableHttpClient client, String url, String postBody, ContentType contentType) throws IOException {
        return postRequest(client, url, postBody, contentType, false);
    }
    public static CloseableHttpResponse postRequest(CloseableHttpClient client, String url, String postBody, ContentType contentType, boolean includeAccept) throws IOException {
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




        /*return client.execute(
            new HttpPost(url) {{
                addHeader("Content-Type", contentType.getMimeType());
                addHeader("Accept", contentType.getMimeType());
                setEntity(new StringEntity(postBody, contentType));
            }}
        );*/

    /*public static CloseableHttpResponse postFormRequest(CloseableHttpClient client, String url, Map<String,String> postBody, ContentType contentType) throws IOException {
        return postRequest(
            client,
            url,
            new StringEntity(

                contentType
            )
        );
    }*/


}
    
    /*public static CloseableHttpResponse postJsonRequest(CloseableHttpClient client, String url, Map<String,String> postBody, Map<String,String> headers) {
        
        return client.execute()
    }*/
    
    /*public static CloseableHttpResponse postFormRequest(CloseableHttpClient client, String url, String postBody, Map<String,String> headers) {
        
    }*



    //public static void




    //need support for http POST & http GET protocols
    //need way to provide json arguments
    //need way to set headers


//}
