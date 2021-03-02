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

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.myenergi.internal.dto.CommandStatus;
import org.openhab.binding.myenergi.internal.dto.DeviceSummary;
import org.openhab.binding.myenergi.internal.dto.DeviceSummaryList;
import org.openhab.binding.myenergi.internal.dto.ZappiHourlyHistory;
import org.openhab.binding.myenergi.internal.dto.ZappiMinuteHistory;
import org.openhab.binding.myenergi.internal.exception.ApiException;
import org.openhab.binding.myenergi.internal.util.ZappiBoostMode;
import org.openhab.binding.myenergi.internal.util.ZappiChargingMode;
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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final Logger logger = LoggerFactory.getLogger(MyEnergiApiClient.class);

    private MyEnergiData data = new MyEnergiData();

    private @Nullable HttpClient httpClient;

    // API
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
    public final void setCredentials(final String username, final String password) throws ApiException {
        if (httpClient != null) {
            HttpClient client = httpClient;
            client.getAuthenticationStore().clearAuthentications();
            client.getAuthenticationStore().clearAuthenticationResults();
            if (host.equals("")) {
                host = "s" + username.charAt(username.length() - 1) + ".myenergi.net";
            }
            try {
                URL baseURL = new URL("https", host, "/");
                logger.info("API base URL: {}", baseURL.toString());

                client.getAuthenticationStore().addAuthentication(
                        new DigestAuthentication(baseURL.toURI(), Authentication.ANY_REALM, username, password));
                this.baseURL = baseURL;
                logger.info("Digest authentication added: {}, {}", username, password);
                if (!client.isStarted()) {
                    client.start();

                }
            } catch (MalformedURLException e) {
                throw new ApiException("Invalid URL for API call", e);
            } catch (Exception e) {
                logger.warn("could not start httpClient - {}", e);
            }
        }
    }

    public final MyEnergiData getData() {
        return data;
    }

    public synchronized void updateTopologyCache() throws ApiException {
        for (DeviceSummary summary : getDeviceSummaryList()) {
            if (summary.asn != null) {
                data.setAsn(summary.asn);
            }
            data.addAllHarvis(summary.harvis);
            data.addAllZappis(summary.zappis);
            data.addAllEddis(summary.eddis);
        }
    }

    public DeviceSummaryList getDeviceSummaryList() throws ApiException {
        try {
            String response = executeApiCall("/cgi-jstatus-*");

            DeviceSummaryList summaryList = MyEnergiBindingConstants.GSON.fromJson(response, DeviceSummaryList.class);
            if (summaryList != null) {
                logger.trace("getDeviceSummaryList - summaryList: {} - {}", summaryList.size(), summaryList.toString());
                return summaryList;
            } else {
                return new DeviceSummaryList();
            }

        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    public void getZappiHistoryByHour(String serialNumber, ZonedDateTime date) {
        try {
            String response = executeApiCall("/cgi-jdayhour-Z" + serialNumber + "-" + DATE_FORMATTER.format(date));
            ZappiHourlyHistory history = MyEnergiBindingConstants.GSON.fromJson(response, ZappiHourlyHistory.class);
            if (history != null) {
                logger.info(history.toString());
            } else {
                throw new ApiException("Unable to deserialize JSON reponse: " + response);
            }

            // hourlyHistory.toLogger();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void getZappiHistoryByMinute(String serialNumber, ZonedDateTime date) {
        try {
            String response = executeApiCall("/cgi-jday-Z" + serialNumber + "-" + DATE_FORMATTER.format(date));
            ZappiMinuteHistory history = MyEnergiBindingConstants.GSON.fromJson(response, ZappiMinuteHistory.class);
            if (history != null) {
                logger.info(history.toString());
            } else {
                throw new ApiException("Unable to deserialize JSON reponse: " + response);
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
            CommandStatus status = MyEnergiBindingConstants.GSON.fromJson(response, CommandStatus.class);
            if (status != null) {
                logger.info(status.toString());
            } else {
                throw new ApiException("Unable to deserialize JSON reponse: " + response);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setZappiBoostMode(String serialNumber, ZappiBoostMode mode, int energyKiloWattHours,
            @Nullable String departureTime) throws ApiException {
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
        CommandStatus status = MyEnergiBindingConstants.GSON.fromJson(response, CommandStatus.class);
        if (status != null) {
            logger.info(status.toString());
        } else {
            throw new ApiException("Unable to deserialize JSON reponse: " + response);
        }
    }

    private String executeApiCall(String path) throws ApiException {
        String result = "";
        try {
            URL url = new URL(baseURL, path);
            logger.info("executeApiCall - url: {}", url.toString());
            result = executeApiCallHttpClient(url);
        } catch (MalformedURLException e) {
            throw new ApiException("Invalid URL", e);
        }
        return result;
    }

    private String executeApiCallHttpClient(URL url) throws ApiException {
        HttpClient client = httpClient;
        if (client != null) {
            Request request = client.newRequest(url.toString()).method(HttpMethod.GET);
            try {
                request.header(HttpHeader.ACCEPT, "application/json, text/plain, */*");
                request.header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate");
                request.header(HttpHeader.CONNECTION, "keep-alive");
                request.header(HttpHeader.CONTENT_TYPE, "application/json; utf-8");
                request.header(HttpHeader.USER_AGENT, API_USER_AGENT);

                logger.info("sending API request: {}", url.toString());

                ContentResponse response = request.send();
                logger.info("HTTP Response Code: {}", response.getStatus());
                logger.info("HTTP Response Msg: {}", response.getReason());
                if ((response.getStatus() == HttpURLConnection.HTTP_OK)
                        || (response.getStatus() == HttpURLConnection.HTTP_CREATED)) {
                } else {
                    // if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new ApiException("Http error: " + response.getStatus() + " - " + response.getReason());
                    // }
                }
                String apiResponse = response.getContentAsString();
                logger.info("Api Response: {}", apiResponse);
                return apiResponse;
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new ApiException("Exception caught during API execution" + e);
            }
            // catch (URISyntaxException e) {
            // throw new MyEnergiApiException("Can't convert URL to URI" + e);
            // }
        } else {
            throw new ApiException("httpClient is null");
        }
    }

}
