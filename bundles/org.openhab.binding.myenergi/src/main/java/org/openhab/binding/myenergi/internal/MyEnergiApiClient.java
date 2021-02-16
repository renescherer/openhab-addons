/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.myenergi.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.HttpCookieStore;
import org.openhab.binding.myenergi.internal.dto.CommandStatus;
import org.openhab.binding.myenergi.internal.dto.DeviceSummary;
import org.openhab.binding.myenergi.internal.dto.DeviceSummaryList;
import org.openhab.binding.myenergi.internal.dto.ZappiHourlyHistory;
import org.openhab.binding.myenergi.internal.dto.ZappiMinuteHistory;
import org.openhab.binding.myenergi.internal.utils.ZappiBoostMode;
import org.openhab.binding.myenergi.internal.utils.ZappiChargingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyEnergiApiClient} is a helper class to abstract the myenergi API. It handles authentication and
 * all JSON API calls. If an API call fails it automatically refreshes the authentication token and retries.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class MyEnergiApiClient {

    private static final String API_USER_AGENT = "Mozilla/5.0 (Linux; Android 7.0; SM-G930F Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/64.0.3282.137 Mobile Safari/537.36";

    private final Logger logger = LoggerFactory.getLogger(MyEnergiApiClient.class);

    private boolean online = false;

    private MyEnergiData data = new MyEnergiData();

    private @Nullable HttpClient httpClient;

    // API
    private String username = "";
    private String password = "";
    private String host = "";
    private @Nullable URL baseURL;

    /**
     * Sets the httpClient object to be used for API calls.
     *
     * @param httpClient the client to be used.
     */
    public void setHttpClient(@Nullable HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Sets the credentials (username/password) to be used for API calls.
     *
     * @param username the username to be used.
     * @param password the password to be used.
     * @throws MyEnergiApiException
     */
    public final void setCredentials(final String username, final String password) throws MyEnergiApiException {
        this.username = username;
        this.password = password;
        if (httpClient != null) {
            HttpClient client = httpClient;
            client.getAuthenticationStore().clearAuthentications();
            client.getAuthenticationStore().clearAuthenticationResults();
            client.setCookieStore(new HttpCookieStore.Empty());
            client.setUserAgentField(new HttpField(HttpHeader.USER_AGENT, "curl/7.58.0"));

            if (host.equals("")) {
                host = "s" + username.charAt(username.length() - 1) + ".myenergi.net";
            }
            try {
                baseURL = new URL("https", host, "/");
                logger.debug("API base URL: {}", baseURL.toString());

                client.getAuthenticationStore().addAuthentication(
                        new DigestAuthentication(baseURL.toURI(), Authentication.ANY_REALM, username, password));
                logger.debug("Digest authentication added: {}, {}", username, password);
                if (!client.isStarted()) {
                    client.start();

                }
            } catch (MalformedURLException e) {
                throw new MyEnergiApiException("Invalid URL for API call", e);
            } catch (Exception e) {
                logger.warn("could not start httpClient - {}", e);
            }
        }
    }

    public boolean isOnline() {
        return online;
    }

    public final MyEnergiData getData() {
        return data;
    }

    public synchronized void updateTopologyCache() throws MyEnergiApiException {
        for (DeviceSummary summary : getDeviceSummaryList()) {
            if (summary.asn != null) {
                data.setAsn(summary.asn);
            }
            data.addAllHarvis(summary.harvis);
            data.addAllZappis(summary.zappis);
            data.addAllEddis(summary.eddis);
        }
    }

    public DeviceSummaryList getDeviceSummaryList() throws MyEnergiApiException {
        try {
            String response = executeApiCall("/cgi-jstatus-*");
            logger.trace("Api Response: {}", response);
            DeviceSummaryList summaryList = MyEnergiBindingConstants.GSON.fromJson(response, DeviceSummaryList.class);
            if (summaryList != null) {
                logger.trace("getDeviceSummaryList - summaryList: {} - {}", summaryList.size(), summaryList.toString());
                return summaryList;
            } else {
                return new DeviceSummaryList();
            }
        } catch (Exception e) {
            throw new MyEnergiApiException(e);
        }
    }

    public void getZappiHistoryByHour(String serialNumber, ZonedDateTime date) {
        try {
            String response = executeApiCall("/cgi-jdayhour-Z" + serialNumber + "-" + getFormattedDate(date));
            logger.info("Api Response: {}", response);
            ZappiHourlyHistory history = MyEnergiBindingConstants.GSON.fromJson(response, ZappiHourlyHistory.class);
            if (history != null) {
                logger.info(history.toString());
            } else {
                throw new MyEnergiApiException("Unable to deserialize JSON reponse: " + response);
            }

            // hourlyHistory.toLogger();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void getZappiHistoryByMinute(String serialNumber, ZonedDateTime date) {
        try {
            String response = executeApiCall("/cgi-jday-Z" + serialNumber + "-" + getFormattedDate(date));
            logger.info("Api Response: {}", response);
            ZappiMinuteHistory history = MyEnergiBindingConstants.GSON.fromJson(response, ZappiMinuteHistory.class);
            if (history != null) {
                logger.info(history.toString());
            } else {
                throw new MyEnergiApiException("Unable to deserialize JSON reponse: " + response);
            }

            // hourlyHistory.toLogger();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setZappiChargingMode(String serialNumber, ZappiChargingMode mode) {
        try {
            String response = executeApiCall(
                    "/cgi-zappi-mode-Z" + serialNumber + "-" + mode.getIntValue() + "-0-0-0000");
            logger.info("Api Response: {}", response);
            CommandStatus status = MyEnergiBindingConstants.GSON.fromJson(response, CommandStatus.class);
            if (status != null) {
                logger.info(status.toString());
            } else {
                throw new MyEnergiApiException("Unable to deserialize JSON reponse: " + response);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setZappiBoostMode(String serialNumber, ZappiBoostMode mode, int energyKiloWattHours,
            @Nullable String departureTime) throws MyEnergiApiException {
        StringBuilder uriStr = new StringBuilder("/cgi-zappi-mode-Z");
        uriStr.append(serialNumber);
        uriStr.append('-');
        uriStr.append(ZappiChargingMode.BOOST.getIntValue());
        uriStr.append('-');
        uriStr.append(mode.getIntValue());
        uriStr.append('-');
        uriStr.append(energyKiloWattHours);
        if (departureTime == null) {
            uriStr.append("-0000");
        } else {
            uriStr.append('-');
            uriStr.append(departureTime);
        }
        String response = executeApiCall(uriStr.toString());
        logger.info("Api Response: {}", response);
        CommandStatus status = MyEnergiBindingConstants.GSON.fromJson(response, CommandStatus.class);
        if (status != null) {
            logger.info(status.toString());
        } else {
            throw new MyEnergiApiException("Unable to deserialize JSON reponse: " + response);
        }
    }

    private String executeApiCall(String path) throws MyEnergiApiException {
        String result = "";
        try {
            URL url = new URL(baseURL, path);
            logger.info("executeApiCall - url: {}", url.toString());
            result = executeApiCallJavaHttpClient(url);
            // result = test_Authentication(url.toURI());
        } catch (MalformedURLException e) {
            throw new MyEnergiApiException("Invalid URL", e);
        } catch (Exception e) {
            throw new MyEnergiApiException("Digest not working", e);
        }
        return result;
    }

    private String executeApiCallJavaHttpClient(URL url) throws MyEnergiApiException {
        String result = "";
        try {
            java.net.Authenticator.setDefault(new java.net.Authenticator() {
                @Override
                protected java.net.PasswordAuthentication getPasswordAuthentication() {
                    logger.info("requesting authentication details");
                    return new java.net.PasswordAuthentication(username, password.toCharArray());
                }
            });

            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .authenticator(new java.net.Authenticator() {
                        @Override
                        protected java.net.PasswordAuthentication getPasswordAuthentication() {
                            logger.info("requesting authentication details");
                            return new java.net.PasswordAuthentication(username, password.toCharArray());
                        }
                    }).connectTimeout(Duration.ofSeconds(30)).executor(Executors.newFixedThreadPool(2))
                    .followRedirects(java.net.http.HttpClient.Redirect.NEVER)
                    .sslContext(javax.net.ssl.SSLContext.getDefault()).version(java.net.http.HttpClient.Version.HTTP_2)
                    .sslParameters(new javax.net.ssl.SSLParameters()).build();

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder().GET().uri(url.toURI())
                    .timeout(Duration.ofSeconds(15)).build();

            logger.info("sending API request: {}", request.uri().toString());

            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            logger.info("received response: {} - {}", response.statusCode(), response.body());
            Map<String, List<String>> headers = response.headers().map();
            for (String header : headers.keySet()) {
                for (String value : headers.get(header)) {
                    logger.info("received headers: {} - {}", header, value);
                }
            }
            String digest = getDigestHeader(headers.get("www-authenticate").get(0), request.method(), request.uri());
            logger.info("sending header: {}", digest);

            request = java.net.http.HttpRequest.newBuilder().GET().uri(url.toURI()).timeout(Duration.ofSeconds(15))
                    .header(HttpHeaders.AUTHORIZATION, digest).build();
            logger.info("sending API request: {}", request.uri().toString());

            response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            logger.info("received response: {} - {}", response.statusCode(), response.body());

            result = response.body();
        } catch (IOException | InterruptedException | URISyntaxException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return result;
    }

    private String getDigestHeader(String requestHeader, String method, URI uri) {
        // Digest realm="MyEnergi
        // Telemetry",qop="auth",nonce="00206c600008ae8300085b947a4bc7e60fde598df17c99a08ca0df6b233967fe4376603059535dbee0dc8452ecceab9e",opaque="000000100000000100000000f72ea4230000565f576cb0b94ff6170cae32a33f76b23322d11cd8dfc63069c84317f3a4",Stale="false",algorithm="MD5"
        // return null;
        Map<String, String> responseHeaders = new HashMap<String, String>();
        final Properties p = new Properties();
        try {
            p.load(new StringReader(requestHeader.replace(',', '\n').replace("Digest ", "")));
            for (Object key : p.keySet()) {
                responseHeaders.put(key.toString(), p.get(key).toString().replace("\"", ""));
            }
            for (String h : responseHeaders.keySet()) {
                logger.debug("digest header : {}={}", h, responseHeaders.get(h));
            }
            String ha1 = DigestUtils.md5Hex(username + ":" + responseHeaders.get("realm") + ":" + password);
            logger.debug("HA1: {}", ha1);

            String ha2 = DigestUtils.md5Hex(method + ":" + uri.toString());
            logger.debug("HA2: {}", ha2);

            String nonceCount = "00000001";
            String clientNonce = "ZDYyMjdiODI1ZTA3NDg2NzdiYWY4YTA3OWNkMDZhYjI=";

            String response = DigestUtils.md5Hex(ha1 + ":" + responseHeaders.get("nonce") + ":" + nonceCount + ":"
                    + clientNonce + ":" + responseHeaders.get("qop") + ":" + ha2);
            // Authorization: Digest username="14042540", realm="MyEnergi Telemetry",
            // nonce="0012e0700007d5c400078b307a4bc7e60e68091ac6c0faf660e17c1981cdba13bdf13339717bcf2aed8d567e5df471b9",
            // uri="/cgi-jstatus-*", cnonce="ZDYyMjdiODI1ZTA3NDg2NzdiYWY4YTA3OWNkMDZhYjI=", nc=00000001, qop=auth,
            // response="56b729e75d5b98e490c6a17c97dac041",
            // opaque="000000100000000100000000f72ea4230000565f576cb0b94ff6170cae32a33f76b23322d11cd8dfc63069c84317f3a4",
            // algorithm="MD5"

            StringBuilder responseBuilder = new StringBuilder("Digest username=\"");
            responseBuilder.append(username);
            responseBuilder.append("\", realm=\"");
            responseBuilder.append(responseHeaders.get("realm"));
            responseBuilder.append("\", nonce=\"");
            responseBuilder.append(responseHeaders.get("nonce"));
            responseBuilder.append("\", uri=\"");
            responseBuilder.append(uri.toString());
            responseBuilder.append("\", cnonce=\"");
            responseBuilder.append(clientNonce);
            responseBuilder.append("\", nc=\"");
            responseBuilder.append(nonceCount);
            responseBuilder.append("\", qop=\"");
            responseBuilder.append(responseHeaders.get("qop"));
            responseBuilder.append("\", response=\"");
            responseBuilder.append(response);
            responseBuilder.append("\", opaque=\"");
            responseBuilder.append(responseHeaders.get("opaque"));
            responseBuilder.append("\", algorithm=\"MD5\"");
            return responseBuilder.toString();

        } catch (IOException e) {
            logger.warn("Invalid digest header : {}", requestHeader);
        }
        return "";
    }

    private String executeApiCallHttpClient(URL url) throws MyEnergiApiException {
        if (httpClient != null) {
            Request request = httpClient.newRequest(url.toString()).method(HttpMethod.GET);
            int retries = 3;
            while (retries > 0) {
                try {
                    request.header(HttpHeader.ACCEPT, "application/json, text/plain, */*");
                    request.header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate");
                    request.header(HttpHeader.CONNECTION, "keep-alive");
                    request.header(HttpHeader.CONTENT_TYPE, "application/json; utf-8");
                    request.header(HttpHeader.USER_AGENT, API_USER_AGENT);

                    AuthenticationStore authenticationStore = httpClient.getAuthenticationStore();
                    authenticationStore.addAuthentication(
                            new DigestAuthentication(url.toURI(), Authentication.ANY_REALM, username, password));

                    logger.info("sending API request: {}", url.toString());

                    ContentResponse response = request.send();
                    if ((response.getStatus() == HttpURLConnection.HTTP_OK)
                            || (response.getStatus() == HttpURLConnection.HTTP_CREATED)) {
                    } else {
                        logger.info("HTTP Response Code: {}", response.getStatus());
                        logger.info("HTTP Response Msg: {}", response.getReason());
                        if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            throw new MyEnergiApiException(
                                    "Http error: " + response.getStatus() + " - " + response.getReason());
                        }
                        return response.getContentAsString();
                    }
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    throw new MyEnergiApiException("Exception caught during API execution" + e);
                } catch (URISyntaxException e) {
                    throw new MyEnergiApiException("Can't convert URL to URI" + e);
                }
            }
            throw new MyEnergiApiException("Can't execute API after 3 retries");
        } else {
            throw new MyEnergiApiException("httpClient is null");
        }
    }

    private String executeApiCallCurl(URL url) throws MyEnergiApiException {
        try {
            Process process = new ProcessBuilder()
                    .command(new String[] { "curl", "--digest", "-u", username + ":" + password, url.toString() })
                    .start();
            InputStream stdout = process.getInputStream();
            InputStream stderr = process.getErrorStream();
            String stdoutText = new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8)).lines()
                    .collect(Collectors.joining("\n"));
            String stderrText = new BufferedReader(new InputStreamReader(stderr, StandardCharsets.UTF_8)).lines()
                    .collect(Collectors.joining("\n"));
            process.waitFor();
            int exitCode = process.exitValue();
            process.destroy();

            logger.info("executeApiCallCurl - exit code: {}", exitCode);
            if (exitCode != 0) {
                online = false;
                return stderrText;
            } else {
                online = true;
                return stdoutText;
            }
        } catch (IOException | InterruptedException e) {
            throw new MyEnergiApiException("Unable to call API", e);
        }
    }

    private final String getFormattedDate(ZonedDateTime date) {
        DateTimeFormatter formmat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
        return formmat1.format(date);
    }

    private String test_Authentication(URI uri) throws Exception {
        String result = "";

        final AtomicReference<CountDownLatch> requests = new AtomicReference<>(new CountDownLatch(2));
        Request.Listener.Adapter requestListener = new Request.Listener.Adapter() {
            @Override
            public void onSuccess(@Nullable Request request) {
                requests.get().countDown();
                logger.info("onSuccess response, {} to go", requests.get().getCount());
            }
        };
        httpClient.getRequestListeners().add(requestListener);

        logger.info("first authenticated request to {}", uri);
        // Request without Authentication causes a 401
        Request request = httpClient.newRequest(uri);
        request.header(HttpHeader.ACCEPT, "*/*");
        request.getHeaders().remove(HttpHeader.ACCEPT_ENCODING);
        request.header(HttpHeader.ACCEPT_ENCODING, null);
        for (HttpField h : request.getHeaders()) {
            logger.info("header: {} - {}", h.getName(), h.getValue());
        }
        // request.header(HttpHeader.ACCEPT_ENCODING, "*");
        // request.header(HttpHeader.ACCEPT_ENCODING, null);
        // request.header(HttpHeader.CONNECTION, "keep-alive");
        // request.header(HttpHeader.CONTENT_TYPE, "application/json; utf-8");
        // request.header(HttpHeader.USER_AGENT, "curl/7.58.0");
        // request.header(HttpHeader.REFERER, "https://surepetcare.io/");
        // request.header("Origin", "https://surepetcare.io");
        // request.header("Referer", "https://surepetcare.io");
        // request.header("X-Requested-With", "com.sureflap.surepetcare");

        request.timeout(5, TimeUnit.SECONDS).send(new Response.Listener() {
            @Override
            public void onBegin(@Nullable Response response) {
                logger.info("onBegin response: {} - {}", response.getStatus(), response.toString());
            }

            @Override
            public boolean onHeader(@Nullable Response response, @Nullable HttpField field) {
                logger.info("onHeader response: {} - {}", response.getStatus(), response.toString());
                return true;
            }

            @Override
            public void onHeaders(@Nullable Response response) {
                logger.info("onHeaders response: {} - {}", response.getStatus(), response.toString());
            }

            @Override
            public void onContent(@Nullable Response response, @Nullable ByteBuffer content) {
                logger.info("onContent response: {} - {}", response.getStatus(), response.toString());
            }

            @Override
            public void onContent(@Nullable Response response, @Nullable ByteBuffer content,
                    @Nullable Callback callback) {
                logger.info("onContent response: {} - {}", response.getStatus(), response.toString());
            }

            @Override
            public void onSuccess(@Nullable Response response) {
                logger.info("onSuccess response: {} - {}", response.getStatus(), response.toString());
            }

            @Override
            public void onFailure(@Nullable Response response, @Nullable Throwable failure) {
                logger.info("onFailure response: {} - {}", response.getStatus(), response.toString());
            }

            @Override
            public void onComplete(@Nullable Result result) {
                Response response = result.getResponse();
                logger.info("received response: {} - {}", response.getStatus(), response.toString());
            }
        });
        Thread.sleep(10000);
        logger.info("all done");
        // logger.info("received response: {} - {}", response.getStatus(), response.getContentAsString());
        httpClient.getRequestListeners().remove(requestListener);

        return result;
    }

    public String calculateDigestResponse(String method, String username, String password, String realm, String qop,
            String reqURI, String nonce, String nonceCount, String clientNonce) {
        String ha1 = DigestUtils.md5Hex(username + ":" + realm + ":" + password);
        logger.debug("HA1: {}", ha1);

        String ha2 = DigestUtils.md5Hex(method + ":" + reqURI);
        logger.debug("HA2: {}", ha2);

        // String nonceCount = headerValues.get("nc");
        // String clientNonce = headerValues.get("cnonce");

        String response = DigestUtils
                .md5Hex(ha1 + ":" + nonce + ":" + nonceCount + ":" + clientNonce + ":" + qop + ":" + ha2);
        logger.debug("response: {}", response);
        return response;
    }
}
