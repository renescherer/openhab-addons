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

import java.net.URL;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.myenergi.internal.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyEnergiGetHostFromDirector} is a helper class to get the hostname on
 * myenergi.net for the the myenergi API.
 * It finds the server for a given hub serial number
 *
 * @author Volkmar Nissen - Initial contribution
 */
@NonNullByDefault
public class MyEnergiGetHostFromDirector {
    private static final int SLEEP_BEFORE_REINIT_MS = 3000;
    public static final String MY_ENERGI_RESPONSE_FIELD = "X_MYENERGI-asn";

    private final Logger logger = LoggerFactory.getLogger(MyEnergiGetHostFromDirector.class);

    /**
     * Finds the server for a given hub serial number
     *
     * @param httpClientFactory the client to be used.
     * @throws ApiException
     */

    public String getHostName(@NotNull HttpClient httpClient, @NotNull String hubSerialNumber) throws ApiException {
        String directorHostname = "director.myenergi.net";
        try {
            URL directorURL = new URL("https", directorHostname, "/");
            // No password is needed at director.myenergie.net
            httpClient.getAuthenticationStore().addAuthentication(
                    new DigestAuthentication(directorURL.toURI(), Authentication.ANY_REALM, hubSerialNumber, ""));
            int lastResponseStatus = 0;
            String lastResponseReason = "";
            int outerLoop = 0;
            while (outerLoop < 3) {
                outerLoop++;
                try {
                    int innerLoop = 0;
                    while ((innerLoop < 2)) {
                        innerLoop++;
                        if (!httpClient.isStarted()) {
                            httpClient.start();
                        }

                        Request request = httpClient.newRequest(directorURL.toString()).method(HttpMethod.GET);

                        logger.info("sending API request attempt# {}: {}", innerLoop, directorURL.toString());

                        ContentResponse response = request.send();
                        String hostname = response.getHeaders().get(MY_ENERGI_RESPONSE_FIELD);
                        if (null != hostname) {
                            return hostname;
                        }

                        logger.debug("HTTP response code: {}, reason: {}", lastResponseStatus, lastResponseReason);
                        if (logger.isTraceEnabled()) {
                            for (HttpField field : response.getHeaders()) {
                                logger.trace("HTTP header: {}", field.toString());
                            }
                        }
                    }
                    logger.info("Re-initializing Api missing host name in response header");
                } catch (Exception e) {
                    logger.info("Re-initializing Api connection after exception caught", e);
                }
                // reset connection and try again
                Thread.sleep(SLEEP_BEFORE_REINIT_MS);
            }
            throw new ApiException("Director returned no host name after several attempts");
        } catch (Exception e) {
            throw new ApiException("Exception caught during API execution", e);
        }
    }
}
