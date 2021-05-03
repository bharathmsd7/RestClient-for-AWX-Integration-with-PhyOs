package methods;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import play.Logger;
import play.Logger.ALogger;
import play.libs.Json;
import play.libs.ws.WSAuthScheme;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

interface restclient{

    Integer getRequest(String ipAddress, String path) throws InterruptedException, ExecutionException, TimeoutException;
    String getRequestWithJson(String ipAddress, String path) throws InterruptedException, ExecutionException,
            TimeoutException;
    JsonNode getRequestWithJsonAndTakeJson(String ipAddress, String path) throws InterruptedException, ExecutionException,
            TimeoutException;
    String getJsonString(String ipAddress, String path, long timeout) throws InterruptedException,
            ExecutionException, TimeoutException;
    File getFile(String ipAddress, String path, long timeout) throws InterruptedException,
            ExecutionException, TimeoutException, IOException;
    Optional<String> getWithRetry(String ipAddress, String endpoint, int attempts, int timeout);
    String postRequestMultipartFormData(String ipAddress, String path, Map<String, File> fileData, Map<String, String> formData, int timeout, Http.Cookie cookie);
    JsonNode postRequestWithoutData(String ipAddress, String path) throws InterruptedException,
            ExecutionException, TimeoutException;
    boolean isJsonValid(String test);
}

@Singleton
class RestClient implements restclient{

    private static final ALogger LOGGER = Logger.of(RestClient.class);
    private final WSClient wsClient;
    private final String StringConstants = "http://";

    @Inject
    public RestClient(WSClient wsClient) {
        this.wsClient = wsClient;
    }


    public Integer getRequest(String ipAddress, String path) throws InterruptedException, ExecutionException, TimeoutException {
        if (ipAddress == null || ipAddress.isEmpty() || path == null || path.isEmpty()) {
            return -1;
        }
        CompletionStage<WSResponse> response = wsClient.url(ipAddress + path).setContentType("application/json").get();
        if (response == null) {
            return -1;
        }
        int res = response.toCompletableFuture().get(100, TimeUnit.SECONDS).getStatus();//.getStatusText();
        return res;
    }


    public String getRequestWithJson(String ipAddress, String path) throws InterruptedException, ExecutionException,
            TimeoutException {
        if (ipAddress == null || ipAddress.isEmpty() || path == null || path.isEmpty()) {
            return null;
        }
        //  Basic Authorization
        //  wsClient.url(StringConstants + ipAddress + path).setAuth("admin", "password", WSAuthScheme.BASIC).get();
        CompletionStage<WSResponse> response = wsClient.url(ipAddress + path).setAuth("admin", "password", WSAuthScheme.BASIC).setContentType("application/json").get();
        if (response == null) {
            return null;
        }

        WSResponse wsResponse = response.toCompletableFuture().get(1000, TimeUnit.SECONDS);
        String res = wsResponse.getBody();
        LOGGER.debug(new StringBuilder(ipAddress).append(path).append(" ").append(res).toString());
        if(wsResponse.getStatus() == 401){
            return "Unauthorized";
        }
        else if (wsResponse.getStatus() != 200) {
            // FIXME null is not propriate
            LOGGER.debug(String.valueOf(wsResponse.getStatus()));
            return null;
        }
        return res;
    }


    public JsonNode getRequestWithJsonAndTakeJson(String ipAddress, String path) throws InterruptedException, ExecutionException,
            TimeoutException {
        if (ipAddress == null || ipAddress.isEmpty() || path == null || path.isEmpty()) {
            return null;
        }
        //  Basic Authorization
        //  wsClient.url(StringConstants + ipAddress + path).setAuth("admin", "password", WSAuthScheme.BASIC).get();
        CompletionStage<WSResponse> response = wsClient.url(ipAddress + path).setAuth("admin", "password", WSAuthScheme.BASIC).setContentType("application/json").get();
        if (response == null) {
            return null;
        }

        JsonNode res = response.toCompletableFuture().get(1000, TimeUnit.SECONDS).asJson();
        return res;
    }


    public String getJsonString(String ipAddress, String path, long timeout) throws InterruptedException,
            ExecutionException, TimeoutException {
        if (ipAddress == null || ipAddress.isEmpty() || path == null || path.isEmpty()) {
            return null;
        }
        LOGGER.info("sending rest request " + (ipAddress+path));
        CompletionStage<WSResponse> response = wsClient.url(ipAddress + path).setAuth("admin", "password", WSAuthScheme.BASIC).setContentType("application/json").get();
        if (response == null) {
            return null;
        }

        WSResponse wsResponse = response.toCompletableFuture().get(timeout, TimeUnit.SECONDS);
        String res = wsResponse.getBody();
        LOGGER.debug(res);
        if (wsResponse.getStatus() != 200) {
            // FIXME null is not propriate
            LOGGER.debug(new StringBuilder(ipAddress).append(path).append(" ").append(wsResponse.getStatus())
                    .toString());
            return null;
        }
        return res;
    }


    public File getFile(String ipAddress, String path, long timeout) throws InterruptedException,
            ExecutionException, TimeoutException, IOException {
        if (ipAddress == null || ipAddress.isEmpty() || path == null || path.isEmpty()) {
            return null;
        }
        LOGGER.info("sending rest request " + (ipAddress+path));
        CompletionStage<WSResponse> response = wsClient.url(StringConstants + ipAddress + path).setContentType("application/json").get();
        if (response == null) {
            return null;
        }

        WSResponse wsResponse = response.toCompletableFuture().get(timeout, TimeUnit.SECONDS);
        InputStream res = wsResponse.getBodyAsStream();
        if (wsResponse.getStatus() != 200) {
            // FIXME null is not propriate
            LOGGER.debug(new StringBuilder(ipAddress).append(path).append(" ").append(wsResponse.getStatus())
                    .toString());
            return null;
        }
        Optional<String> cdHeader = wsResponse.getSingleHeader("Content-Disposition");

        String filename="NcmsTDI.xls";
        if(cdHeader.isPresent()){
            filename = cdHeader.get().substring(cdHeader.get().indexOf("filename=")+9);
        }
        File file = new File(filename);
        FileUtils.copyInputStreamToFile(res, file);
        return file;
    }

    public Optional<String> getWithRetry(String ipAddress, String endpoint, int attempts, int timeout) {
        int attemptCount = 0;
        Optional<String> result = Optional.empty();
        while (!result.isPresent() && attemptCount < attempts) {
            attemptCount++;
            result = getResult(ipAddress, endpoint, attempts, timeout, attemptCount);
        }
        return result;
    }

    private Optional<String> getResult(String ipAddress, String endpoint, int attempts, int timeout, int attemptCount) {
        Optional<String> result = Optional.empty();
        try {
            result = Optional.ofNullable(getJsonString(StringConstants + ipAddress, endpoint, timeout));
        } catch (Exception e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
            if (attemptCount == attempts) {
                LOGGER.error(StringConstants + ipAddress + endpoint, e);
            }
        }
        return result;
    }

    public JsonNode postRequest(String ipAddress, String path, String data, int timeout) throws IOException {

        if (ipAddress == null || ipAddress.isEmpty() || path == null || path.isEmpty() || data == null) {
            return null;
        }

        BufferedReader responseBuffer = null;
        try {

            URL targetUrl = new URL(StringConstants + ipAddress + path);

            HttpURLConnection httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Content-Type", "application/json");
            httpConnection.setReadTimeout(timeout * 1000);
            String input = data;
            OutputStream outputStream = httpConnection.getOutputStream();
            outputStream.write(input.getBytes(Charset.forName("UTF-8")));
            outputStream.flush();

            responseBuffer = new BufferedReader(new InputStreamReader((httpConnection.getInputStream()),
                    Charset.forName("UTF-8")));
            String output;
            StringBuilder result = new StringBuilder();

            while ((output = responseBuffer.readLine()) != null) {
                result.append(output);

            }
            responseBuffer.close();
            httpConnection.disconnect();
            if (isJsonValid(result.toString())) {
                return Json.parse(result.toString());
            }

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        } finally {

            if (responseBuffer != null) {
                responseBuffer.close();
            }

        }

        return null;

    }

    public String postRequestMultipartFormData(String ipAddress, String path, Map<String, File> fileData, Map<String, String> formData, int timeout, Http.Cookie cookie) {

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        for(Map.Entry<String, File> entry : fileData.entrySet()){
            FileBody fileBody = new FileBody(entry.getValue(), ContentType.DEFAULT_BINARY);
            builder.addPart(entry.getKey(), fileBody);
        }
        for(Map.Entry<String, String> entry : formData.entrySet()){
            StringBody stringBody = new StringBody(entry.getValue(), ContentType.TEXT_PLAIN);
            builder.addPart(entry.getKey(), stringBody);
        }
        LOGGER.info("sending multipart/form-data request, formPart: {}", formData);
        HttpEntity entity = builder.build();

        HttpPost request = new HttpPost(StringConstants + ipAddress + path);
        request.setEntity(entity);
        request.setConfig(RequestConfig.custom().build());
        request.setHeader("Cookie", cookie.name()+"="+cookie.value());

        HttpClient client = HttpClientBuilder.create().build();
        try {
            HttpResponse response = client.execute(request);
            HttpEntity result = response.getEntity();
            return EntityUtils.toString(result);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JsonNode postRequestWithoutData(String ipAddress, String path) throws InterruptedException,
            ExecutionException, TimeoutException {
        if (ipAddress == null || ipAddress.isEmpty() || path == null || path.isEmpty()) {
            return null;
        }
        CompletionStage<WSResponse> response = wsClient.url(ipAddress + path).setAuth("admin", "password", WSAuthScheme.BASIC).setContentType("application/json")
                .post("");
        if (response == null) {
            return null;
        }
        JsonNode res = response.toCompletableFuture().get(1000, TimeUnit.SECONDS).asJson();
        return res;
    }

    public boolean isJsonValid(String test) {
        try {
            Json.parse(test);
        } catch (RuntimeException ex) {
            if (ex.getCause() != null && ex.getCause() instanceof com.fasterxml.jackson.core.JsonParseException) {
                return false;
            }

            throw ex;
        }
        return true;
    }

    public JsonNode postRequestWithData(String ipAddress, String path, String data, String username, String password) throws InterruptedException,
            ExecutionException, TimeoutException {
        if (ipAddress == null || ipAddress.isEmpty() || path == null || path.isEmpty() || data == null || data.isEmpty() || username == null || username.isEmpty() || password == null || password.isEmpty()){
            return null;
        }
        CompletionStage<WSResponse> response = wsClient.url(ipAddress + path).setAuth(username, password, WSAuthScheme.BASIC).setContentType("application/json")
                .post(data);
        if (response == null) {
            return null;
        }
        JsonNode res = response.toCompletableFuture().get(1000, TimeUnit.SECONDS).asJson();
        return res;
    }

}