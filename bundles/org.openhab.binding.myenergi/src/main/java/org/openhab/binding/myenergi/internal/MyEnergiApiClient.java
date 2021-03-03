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
import java.net.URISyntaxException;
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
import org.openhab.binding.myenergi.internal.dto.HarviSummary;
import org.openhab.binding.myenergi.internal.dto.MyEnergiData;
import org.openhab.binding.myenergi.internal.dto.ZappiHourlyHistory;
import org.openhab.binding.myenergi.internal.dto.ZappiMinuteHistory;
import org.openhab.binding.myenergi.internal.dto.ZappiSummary;
import org.openhab.binding.myenergi.internal.exception.ApiException;
import org.openhab.binding.myenergi.internal.exception.AuthenticationException;
import org.openhab.binding.myenergi.internal.exception.RecordNotFoundException;
import org.openhab.binding.myenergi.internal.util.ZappiBoostMode;
import org.openhab.binding.myenergi.internal.util.ZappiChargingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

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
    public void setCredentials(final String username, final String password) throws ApiException {
        HttpClient client = httpClient;
        if (client != null) {
            client.getAuthenticationStore().clearAuthentications();
            client.getAuthenticationStore().clearAuthenticationResults();
            if (host.equals("")) {
                host = "s" + username.charAt(username.length() - 1) + ".myenergi.net";
            }
            try {
                URL baseURL = new URL("https", host, "/");
                logger.debug("API base URL: {}", baseURL.toString());

                client.getAuthenticationStore().addAuthentication(
                        new DigestAuthentication(baseURL.toURI(), Authentication.ANY_REALM, username, password));
                this.baseURL = baseURL;
                logger.debug("Digest authentication added: {}", username);
                if (!client.isStarted()) {
                    client.start();
                }
            } catch (MalformedURLException | URISyntaxException e) {
                throw new ApiException("Invalid URL for API call", e);
            } catch (Exception e) {
                throw new ApiException("Could not start httpClient", e);
            }
        }
    }

    public MyEnergiData getData() {
        return data;
    }

    public synchronized void updateTopologyCache() throws ApiException {
        data.clear();
        for (DeviceSummary summary : getDeviceSummaryList()) {
            if (summary.activeServer != null) {
                data.setActiveServer(summary.activeServer);
                data.setFirmwareVersion(summary.firmwareVersion);
                host = summary.activeServer;
            }
            data.addAllHarvis(summary.harvis);
            data.addAllZappis(summary.zappis);
            data.addAllEddis(summary.eddis);
        }
    }

    public synchronized ZappiSummary updateZappiSummary(long serialNumber)
            throws ApiException, RecordNotFoundException {
        String response = executeApiCall("/cgi-jstatus-Z" + serialNumber);
        try {
            DeviceSummary ds = MyEnergiBindingConstants.GSON.fromJson(response, DeviceSummary.class);
            if (ds == null) {
                throw new ApiException("Unexpected JSON response: " + response);
            } else if (ds.zappis.isEmpty()) {
                throw new RecordNotFoundException("No Zappi with serial number: " + serialNumber);
            } else {
                ZappiSummary sum = ds.zappis.get(0);
                data.updateZappi(sum);
                return sum;
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    public synchronized HarviSummary updateHarviSummary(long serialNumber)
            throws ApiException, RecordNotFoundException {
        String response = executeApiCall("/cgi-jstatus-H" + serialNumber);
        try {
            DeviceSummary ds = MyEnergiBindingConstants.GSON.fromJson(response, DeviceSummary.class);
            if (ds == null) {
                throw new ApiException("Unexpected JSON response: " + response);
            } else if (ds.harvis.isEmpty()) {
                throw new RecordNotFoundException("No Harvi with serial number: " + serialNumber);
            } else {
                HarviSummary sum = ds.harvis.get(0);
                data.updateHarvi(sum);
                return sum;
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    public DeviceSummaryList getDeviceSummaryList() throws ApiException {
        String response = executeApiCall("/cgi-jstatus-*");
        try {
            DeviceSummaryList summaryList = MyEnergiBindingConstants.GSON.fromJson(response, DeviceSummaryList.class);
            if (summaryList != null) {
                logger.trace("getDeviceSummaryList - summaryList: {} - {}", summaryList.size(), summaryList.toString());
                return summaryList;
            } else {
                return new DeviceSummaryList();
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    public ZappiHourlyHistory getZappiHistoryByHour(long zappiSerialNumber, ZonedDateTime date) throws ApiException {
        String response = executeApiCall("/cgi-jdayhour-Z" + zappiSerialNumber + "-" + DATE_FORMATTER.format(date));
        try {
            ZappiHourlyHistory history = MyEnergiBindingConstants.GSON.fromJson(response, ZappiHourlyHistory.class);
            if (history != null) {
                return history;
            } else {
                throw new ApiException("Unexpected JSON response: " + response);
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    public ZappiMinuteHistory getZappiHistoryByMinute(long zappiSerialNumber, ZonedDateTime date) throws ApiException {
        String response = executeApiCall("/cgi-jday-Z" + zappiSerialNumber + "-" + DATE_FORMATTER.format(date));
        try {
            ZappiMinuteHistory history = MyEnergiBindingConstants.GSON.fromJson(response, ZappiMinuteHistory.class);
            if (history != null) {
                return history;
            } else {
                throw new ApiException("Unexpected JSON response: " + response);
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    public CommandStatus setZappiChargingMode(long zappiSerialNumber, ZappiChargingMode mode) throws ApiException {
        String response = executeApiCall(
                "/cgi-zappi-mode-Z" + zappiSerialNumber + "-" + mode.getIntValue() + "-0-0-0000");
        try {
            CommandStatus status = MyEnergiBindingConstants.GSON.fromJson(response, CommandStatus.class);
            if (status != null) {
                return status;
            } else {
                throw new ApiException("Unexpected JSON response: " + response);
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    public CommandStatus setZappiBoostMode(String serialNumber, ZappiBoostMode mode, int energyKiloWattHours,
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
        try {
            CommandStatus status = MyEnergiBindingConstants.GSON.fromJson(response, CommandStatus.class);
            if (status != null) {
                return status;
            } else {
                throw new ApiException("Unexpected JSON response: " + response);
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    private String executeApiCall(String path) throws ApiException {
        String result = "";
        try {
            URL url = new URL(baseURL, path);
            logger.debug("executeApiCall - url: {}", url.toString());
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

                logger.debug("sending API request: {}", url.toString());

                ContentResponse response = request.send();
                logger.debug("HTTP Response Code: {}", response.getStatus());
                logger.debug("HTTP Response Msg: {}", response.getReason());
                if ((response.getStatus() == HttpURLConnection.HTTP_OK)
                        || (response.getStatus() == HttpURLConnection.HTTP_CREATED)) {
                    String apiResponse = response.getContentAsString();
                    logger.debug("Api Response: {}", apiResponse);
                    return apiResponse;
                } else {
                    if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        throw new AuthenticationException(
                                "Http error: " + response.getStatus() + " - " + response.getReason());
                    } else {
                        throw new ApiException("Http error: " + response.getStatus() + " - " + response.getReason());
                    }
                }
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
