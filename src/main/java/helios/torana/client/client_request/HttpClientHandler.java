package helios.torana.client.client_request;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.*;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.time.Duration;

import org.json.simple.JSONObject;

import helios.torana.client.server_authentication.*;

class HttpClientHandlerException extends Exception {
    String msg;

    HttpClientHandlerException(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return msg;
    }
}

class HttpClientHandlerRequestException extends HttpClientHandlerException {

    HttpClientHandlerRequestException(String msg) {
        super(msg);
    }
}

class HttpRequestNotCreatedException extends HttpClientHandlerRequestException {

    HttpRequestNotCreatedException(String msg) {
        super(msg);
    }

}

class RequestCouldNotBeSendException extends Exception {

    RequestCouldNotBeSendException(String msg) {
        super(msg);
    }

}

public class HttpClientHandler {

    private final String URL;
    private final HttpClient httpClient;
    private HttpRequestBuilder httpRequestBuilder;

    HttpClientHandler(String URL) {
        this.URL = URL;
        httpClient = HttpClientBuilder.getHttpClient();
    }

    private static class HttpClientBuilder {
        private static HttpClient.Builder getBuilder() {
            HttpClient.Builder builder = HttpClient.newBuilder();
            builder.version(Version.HTTP_2);
            builder.connectTimeout(Duration.ofMillis(200));
            builder.authenticator(
                    new ServerAuthentication(ServerAuthenticationInfo.userName, ServerAuthenticationInfo.password)
                            .getAuthentication());
            builder.followRedirects(Redirect.NORMAL);
            return builder;
        }

        private static HttpClient getHttpClient() {
            HttpClient httpClient = getBuilder().build();
            return httpClient;
        }
    }

    private class HttpRequestBuilder {
        private final JSONObject body;
        private HttpRequest httpRequest = null;

        private HttpRequestBuilder(JSONObject body) {
            this.body = body;
        }

        private HttpRequest.Builder getBuilder() throws URISyntaxException {
            HttpRequest.Builder builder = HttpRequest.newBuilder();
            setURI(builder);
            setMethod(builder);
            return builder;
        }

        private HttpRequest.Builder setURI(HttpRequest.Builder builder) throws URISyntaxException {
            return builder.uri(new URI(URL));
        }

        private HttpRequest.Builder setMethod(HttpRequest.Builder builder) {
            builder.POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()));
            return builder;
        }

        private void buildRequest() throws URISyntaxException {
            if (httpRequest == null) {
                HttpRequest.Builder builder = getBuilder();
                httpRequest = builder.build();
            }
        }

        private HttpRequest getRequest() {
            return httpRequest;
        }
    }

    public void createRequest(JSONObject body) throws URISyntaxException {
        httpRequestBuilder = new HttpRequestBuilder(body);
        httpRequestBuilder.buildRequest();
    }

    public HttpResponse<String> sendRequest() throws HttpRequestNotCreatedException, RequestCouldNotBeSendException {
        if (httpRequestBuilder == null) {
            throw new HttpRequestNotCreatedException("http request not created");
        } else {
            try {
                HttpResponse<String> httpResponse = httpClient.send(httpRequestBuilder.getRequest(),
                        HttpResponse.BodyHandlers.ofString());
                return httpResponse;
            } catch (IOException | InterruptedException e) {
                throw new RequestCouldNotBeSendException("request could not be send");
            }
        }
    }

}
