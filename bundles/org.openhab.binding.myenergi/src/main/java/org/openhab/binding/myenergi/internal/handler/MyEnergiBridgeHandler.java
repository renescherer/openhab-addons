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
package org.openhab.binding.myenergi.internal.handler;

import static org.openhab.binding.myenergi.internal.MyEnergiBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myenergi.internal.MyEnergiApiClient;
import org.openhab.binding.myenergi.internal.MyEnergiApiException;
import org.openhab.binding.myenergi.internal.MyEnergiConfiguration;
import org.openhab.binding.myenergi.internal.MyEnergiDiscoveryService;
import org.openhab.binding.myenergi.internal.dto.HarviSummary;
import org.openhab.binding.myenergi.internal.dto.ZappiSummary;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyEnergiBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class MyEnergiBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(MyEnergiBridgeHandler.class);

    private final MyEnergiApiClient apiClient;
    private @Nullable ScheduledFuture<?> topologyPollingJob = null;

    public MyEnergiBridgeHandler(Bridge thing, MyEnergiApiClient apiClient) {
        super(thing);
        this.apiClient = apiClient;
    }

    public ThingUID getUID() {
        return thing.getUID();
    }

    public boolean isOnline() {
        return apiClient.isOnline();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateState(BRIDGE_CHANNEL_ONLINE, OnOffType.from(apiClient.isOnline()));
            updateState(BRIDGE_CHANNEL_REFRESH, OnOffType.OFF);
        } else {
            switch (channelUID.getId()) {
                case BRIDGE_CHANNEL_REFRESH:
                    if ("ON".equals(command.toString())) {
                        try {
                            apiClient.updateTopologyCache();
                        } catch (MyEnergiApiException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        updateState(BRIDGE_CHANNEL_REFRESH, OnOffType.OFF);
                    }
                    break;
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Sure Petcare bridge handler.");
        MyEnergiConfiguration config = getConfigAs(MyEnergiConfiguration.class);
        updateState(BRIDGE_CHANNEL_ONLINE, OnOffType.OFF);

        if (config.username != null && config.password != null) {
            updateStatus(ThingStatus.UNKNOWN);
            try {
                logger.debug("Login to MyEnergi API with username: {}", config.username);
                apiClient.setCredentials(config.username, config.password);
                apiClient.updateTopologyCache();
                logger.debug("Cache update successful, setting bridge status to ONLINE");
                updateStatus(ThingStatus.ONLINE);
                updateState(BRIDGE_CHANNEL_ONLINE, OnOffType.ON);
            } catch (MyEnergiApiException e) {
                logger.warn("Invalid setting for topology refresh interval");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-invalid-refresh-intervals");
            }
        } else {
            logger.warn("Setting thing '{}' to OFFLINE: Parameter 'password' and 'username' must be configured.",
                    getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-username-or-password");
        }

        if (config.refreshIntervalTopology != null) {
            boolean noJob = true;
            if (topologyPollingJob != null) {
                noJob = topologyPollingJob.isCancelled();
            }
            if (noJob) {
                topologyPollingJob = scheduler.scheduleWithFixedDelay(() -> {
                    try {
                        apiClient.updateTopologyCache();
                        if (getThing().getStatus() == ThingStatus.OFFLINE) {
                            updateStatus(ThingStatus.ONLINE);
                            updateState(BRIDGE_CHANNEL_ONLINE, OnOffType.ON);
                        }
                    } catch (MyEnergiApiException e) {
                        logger.warn("Error when updating tolopogy cache");
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "@text/offline.communication-error");
                    }
                }, config.refreshIntervalTopology, config.refreshIntervalTopology, TimeUnit.SECONDS);
                logger.debug("Bridge topology polling job every {} seconds", config.refreshIntervalTopology);
            }
        } else {
            logger.warn("Invalid setting for topology refresh interval");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-invalid-refresh-intervals");
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(MyEnergiDiscoveryService.class);
    }

    @SuppressWarnings("null")
    @Override
    public void dispose() {
        updateState(BRIDGE_CHANNEL_ONLINE, OnOffType.OFF);

        if (topologyPollingJob != null && !topologyPollingJob.isCancelled()) {
            topologyPollingJob.cancel(true);
            topologyPollingJob = null;
            logger.debug("Stopped pet background polling process");
        }
    }

    public Iterable<ZappiSummary> listZappis() {
        return apiClient.getData().getZappis();
    }

    public Iterable<HarviSummary> listHarvis() {
        return apiClient.getData().getHarvis();
    }
}
