/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.myenergi.internal.dto.CommandStatus;
import org.openhab.binding.myenergi.internal.dto.DeviceSummary;
import org.openhab.binding.myenergi.internal.dto.DeviceSummaryList;
import org.openhab.binding.myenergi.internal.dto.HarviSummary;
import org.openhab.binding.myenergi.internal.dto.MyEnergiData;
import org.openhab.binding.myenergi.internal.dto.ZappiBoostTimeSlot;
import org.openhab.binding.myenergi.internal.dto.ZappiBoostTimes;
import org.openhab.binding.myenergi.internal.dto.ZappiHourlyHistory;
import org.openhab.binding.myenergi.internal.dto.ZappiMinuteHistory;
import org.openhab.binding.myenergi.internal.dto.ZappiSummary;
import org.openhab.binding.myenergi.internal.exception.ApiException;
import org.openhab.binding.myenergi.internal.exception.AuthenticationException;
import org.openhab.binding.myenergi.internal.exception.RecordNotFoundException;
import org.openhab.binding.myenergi.internal.util.ZappiBoostMode;
import org.openhab.binding.myenergi.internal.util.ZappiChargingMode;
import org.openhab.core.io.net.http.HttpClientFactory;
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

    private static final int SLEEP_BEFORE_REINIT_MS = 3000;

    private static final String API_USER_AGENT = "Mozilla/5.0 (Linux; Android 7.0; SM-G930F Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/64.0.3282.137 Mobile Safari/537.36";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final Logger logger = LoggerFactory.getLogger(MyEnergiApiClient.class);

    private MyEnergiData data = new MyEnergiData();

    private @Nullable HttpClientFactory httpClientFactory;
    private @Nullable HttpClient httpClient;

    // API
    private String host = "";
    private @Nullable URL baseURL;
    private String username = "";
    private String password = "";

    /**
     * Sets the httpClientFactory object to be used to get httpClients.
     *
     * @param httpClientFactory the client to be used.
     */
    public void setHttpClientFactory(@Nullable HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    /**
     * Sets the credentials (username/password) to be used for API calls.
     *
     * @param hubSerialNumber the serial number of the myenergi hub
     * @param password the password for this hub in the myenergi mobile app.
     * @throws MyEnergiApiException
     */
    public void initialize(final String hubSerialNumber, final String password) throws ApiException {
        this.username = hubSerialNumber;
        this.password = password;
        HttpClientFactory factory = httpClientFactory;
        if (factory == null) {
            throw new ApiException("No HttpClientFactory provided");
        } else {
            HttpClient client = this.httpClient;
            // close down existing client
            stop();

            // create a new httpClient, so that we can add our own digest authentication
            client = factory.createHttpClient(MyEnergiApiClient.class.getSimpleName());
            AuthenticationStore auth = client.getAuthenticationStore();
            auth.clearAuthentications();
            auth.clearAuthenticationResults();
            if ("".equals(host)) {
                host = new MyEnergiGetHostFromDirector().getHostName(client, hubSerialNumber);
            }
            try {
                URL baseURL = new URL("https", host, "/");
                logger.debug("API base URL: {}", baseURL.toString());

                client.getAuthenticationStore().addAuthentication(
                        new DigestAuthentication(baseURL.toURI(), Authentication.ANY_REALM, hubSerialNumber, password));
                this.baseURL = baseURL;
                logger.debug("Digest authentication added: {}", hubSerialNumber);
                if (!client.isStarted()) {
                    client.start();
                }
                httpClient = client;
            } catch (MalformedURLException | URISyntaxException e) {
                throw new ApiException("Invalid URL for API call", e);
            } catch (Exception e) {
                throw new ApiException("Could not start httpClient", e);
            }
        }
    }

    public void stop() {
        HttpClient client = httpClient;
        if (client != null) {
            try {
                client.stop();
            } catch (Exception e) {
                logger.debug("Existing httpClient could not be stopped", e);
            }
            httpClient = null;
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

    public CommandStatus setZappiMinimumGreenLevel(long zappiSerialNumber, int newLevel) throws ApiException {
        String response = executeApiCall("/cgi-set-min-green-Z" + zappiSerialNumber + "-" + newLevel);
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

    public ZappiBoostTimes getZappiBoostTimes(long zappiSerialNumber) throws ApiException {
        String response = executeApiCall("/cgi-boost-time-Z" + zappiSerialNumber);
        try {
            ZappiBoostTimes result = MyEnergiBindingConstants.GSON.fromJson(response, ZappiBoostTimes.class);
            if (result != null) {
                return result;
            } else {
                throw new ApiException("Unexpected JSON response: " + response);
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    // cgi-boost-time-Z???-{slot}-{bsh}-{bdh}-{bdd}
    // Slot is one of 11,12,13,14
    // Start time is in 24 hour clock, 15 minute intervals.
    // Duration is hoursminutes and is less than 10 hours.

    public ZappiBoostTimes setZappiBoostTimes(long zappiSerialNumber, ZappiBoostTimeSlot slot) throws ApiException {
        if (slot.durationHour >= 8) {
            slot.durationHour = 8;
            slot.durationMinute = 0;
        }
        if (slot.startHour == 24 && slot.startMinute > 0) {
            slot.startMinute = 0;
        }
        String uri = String.format("/cgi-boost-time-Z%s-%d-%02d%02d-%d%02d-%s", zappiSerialNumber, slot.slotId,
                slot.startHour, slot.startMinute, slot.durationHour, slot.durationMinute, slot.daysOfTheWeekMap);
        String response = executeApiCall(uri);
        try {
            ZappiBoostTimes result = MyEnergiBindingConstants.GSON.fromJson(response, ZappiBoostTimes.class);
            if (result != null && result.boostTimes.size() > 0) {
                return result;
            } else {
                CommandStatus status = MyEnergiBindingConstants.GSON.fromJson(response, CommandStatus.class);

                throw new ApiException(
                        "Unexpected JSON response: " + response + "Status: " + status.status + "\nURI: " + uri);
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    private CommandStatus setZappiBoostMode(long serialNumber, ZappiBoostMode mode, int energyKiloWattHours,
            int endTimeHour, int endTimeMinute) throws ApiException {
        StringBuilder uriStr = new StringBuilder("/cgi-zappi-mode-Z");
        uriStr.append(serialNumber);
        uriStr.append('-');
        uriStr.append(ZappiChargingMode.BOOST.getIntValue());
        uriStr.append("-0-"); // Slot is always
        uriStr.append(mode.getIntValue());
        uriStr.append('-');
        uriStr.append(energyKiloWattHours);
        uriStr.append('-');
        uriStr.append(String.format("%02d", endTimeHour));
        uriStr.append(String.format("%02d", endTimeMinute));

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

    public CommandStatus setZappiManualBoost(Long serialNumber, int energyKiloWattHours) throws ApiException {
        return setZappiBoostMode(serialNumber, ZappiBoostMode.MANUAL, energyKiloWattHours, 0, 0);
    }

    public CommandStatus setZappiSmartBoost(Long serialNumber, int energyKiloWattHours, int endTimeHour,
            int endTimeMinute) throws ApiException {
        return setZappiBoostMode(serialNumber, ZappiBoostMode.SMART, energyKiloWattHours, endTimeHour, endTimeMinute);
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
            try {
                int lastResponseStatus = 0;
                String lastResponseReason = "";
                int outerLoop = 0;
                while (outerLoop < 3) {
                    outerLoop++;
                    try {
                        int innerLoop = 0;
                        while ((innerLoop < 2) && !client.isStopped()) {
                            innerLoop++;
                            Request request = client.newRequest(url.toString()).method(HttpMethod.GET);
                            request.header(HttpHeader.ACCEPT, "application/json, text/plain, */*");
                            request.header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate");
                            request.header(HttpHeader.CONNECTION, "keep-alive");
                            request.header(HttpHeader.CONTENT_TYPE, "application/json; utf-8");
                            request.header(HttpHeader.USER_AGENT, API_USER_AGENT);

                            logger.debug("sending API request attempt# {}: {}", innerLoop, url.toString());

                            ContentResponse response = request.send();
                            lastResponseStatus = response.getStatus();
                            lastResponseReason = response.getReason();
                            logger.debug("HTTP response code: {}, reason: {}", lastResponseStatus, lastResponseReason);
                            if (logger.isTraceEnabled()) {
                                for (HttpField field : response.getHeaders()) {
                                    logger.trace("HTTP header: {}", field.toString());
                                }
                            }
                            if ((lastResponseStatus == HttpURLConnection.HTTP_OK)
                                    || (lastResponseStatus == HttpURLConnection.HTTP_CREATED)) {
                                String apiResponse = response.getContentAsString();
                                logger.debug("Api response: {}", apiResponse);
                                return apiResponse;
                            } else {
                                if (lastResponseStatus == HttpURLConnection.HTTP_UNAUTHORIZED) {
                                    throw new AuthenticationException(
                                            "Http error: " + response.getStatus() + " - " + response.getReason());
                                } else {
                                    logger.debug("Retrying Api request after code: {}, reason: {}", lastResponseStatus,
                                            lastResponseReason);
                                }
                            }
                        }
                        logger.info("Re-initializing Api connection after code: {}, reason: {}", lastResponseStatus,
                                lastResponseReason);
                    } catch (ExecutionException e) {
                        logger.info("Re-initializing Api connection after exception caught", e);
                    }
                    // reset connection and try again
                    Thread.sleep(SLEEP_BEFORE_REINIT_MS);
                    initialize(username, password);
                }
                throw new ApiException(
                        "Http error after several attemps: " + lastResponseStatus + " - " + lastResponseReason);
            } catch (InterruptedException | TimeoutException e) {
                throw new ApiException("Exception caught during API execution" + e);
            }
        } else {
            throw new ApiException("httpClient is null");
        }
    }
}
