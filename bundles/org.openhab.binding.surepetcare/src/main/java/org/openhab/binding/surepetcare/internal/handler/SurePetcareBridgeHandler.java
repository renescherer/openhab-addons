/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.surepetcare.internal.handler;

import static org.openhab.binding.surepetcare.internal.SurePetcareConstants.*;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.surepetcare.internal.AuthenticationException;
import org.openhab.binding.surepetcare.internal.SurePetcareAPIHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SurePetcareBridgeHandler} is responsible for handling the bridge things created to use the Sure Petcare
 * API. This way, the user credentials may be entered only once.
 *
 * It also spawns 2 background polling threads to update the more static data (topology) and the pet locations at
 * different time intervals.
 *
 * @author Rene Scherer - Initial Contribution
 */
@NonNullByDefault
public class SurePetcareBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SurePetcareBridgeHandler.class);

    private SurePetcareAPIHelper petcareAPI;
    private @Nullable ScheduledFuture<?> topologyPollingJob = null;
    private @Nullable ScheduledFuture<?> petLocationPollingJob = null;

    public SurePetcareBridgeHandler(Bridge bridge, SurePetcareAPIHelper petcareAPI) {
        super(bridge);
        this.petcareAPI = petcareAPI;
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        logger.debug("Initializing Sure Petcare bridge handler.");
        Configuration config = getThing().getConfiguration();
        updateState(BRIDGE_CHANNEL_ONLINE, OnOffType.OFF);
        // Check if username and password have been provided during the bridge creation
        if ((StringUtils.trimToNull((String) config.get(PASSWORD)) == null)
                || (StringUtils.trimToNull((String) config.get(USERNAME)) == null)) {
            logger.warn("Setting thing '{}' to OFFLINE: Parameter 'password' and 'username' must be configured.",
                    getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-password");
        } else {
            String username = (String) config.get(USERNAME);
            String password = (String) config.get(PASSWORD);
            updateStatus(ThingStatus.UNKNOWN);
            try {
                logger.debug("Login to SurePetcare API with username: {}", username);
                petcareAPI.login(username, password);
                logger.debug("Login successful, updating topology cache");
                petcareAPI.updateTopologyCache();
                logger.debug("Cache update successful, setting bridge status to ONLINE");
                updateStatus(ThingStatus.ONLINE);
                updateState(BRIDGE_CHANNEL_ONLINE, OnOffType.ON);
                updateThings();

            } catch (AuthenticationException e) {
                updateStatus(ThingStatus.OFFLINE);
            }
        }

        try {
            long refreshIntervalTopology = ((BigDecimal) config.get(REFRESH_INTERVAL_TOPOLOGY)).longValueExact();
            long refreshIntervalLocation = ((BigDecimal) config.get(REFRESH_INTERVAL_LOCATION)).longValueExact();

            if (topologyPollingJob == null || topologyPollingJob.isCancelled()) {
                topologyPollingJob = scheduler.scheduleWithFixedDelay(() -> {
                    petcareAPI.updateTopologyCache();
                    updateThings();
                }, refreshIntervalTopology, refreshIntervalTopology, TimeUnit.SECONDS);
                logger.debug("Bridge topology polling job every {} seconds", refreshIntervalTopology);
            }
            if (petLocationPollingJob == null || petLocationPollingJob.isCancelled()) {
                petLocationPollingJob = scheduler.scheduleWithFixedDelay(() -> {
                    pollAndUpdatePetLocations();
                }, refreshIntervalLocation, refreshIntervalLocation, TimeUnit.SECONDS);
                logger.debug("Bridge location polling job every {} seconds", refreshIntervalLocation);
            }
        } catch (ArithmeticException e) {
            logger.warn("Invalid settings for refresh intervals [{},{}]", config.get(REFRESH_INTERVAL_TOPOLOGY),
                    config.get(REFRESH_INTERVAL_LOCATION));
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-invalid-refresh-intervals");
        }

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
        if (petLocationPollingJob != null && !petLocationPollingJob.isCancelled()) {
            petLocationPollingJob.cancel(true);
            petLocationPollingJob = null;
            logger.debug("Stopped pet location background polling process");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("SurePetcareBridgeHandler handleCommand called with command: {}", command.toString());
        if (command instanceof RefreshType) {
            updateState(BRIDGE_CHANNEL_ONLINE, OnOffType.from(petcareAPI.isOnline()));
            updateState(BRIDGE_CHANNEL_REFRESH, OnOffType.OFF);
        } else {
            switch (channelUID.getId()) {
                case BRIDGE_CHANNEL_REFRESH:
                    if ("ON".equals(command.toString())) {
                        petcareAPI.updateTopologyCache();
                        updateThings();
                        updateState(BRIDGE_CHANNEL_REFRESH, OnOffType.OFF);
                    }
                    break;
            }
        }
    }

    private synchronized void updateThings() {
        logger.debug("Updating {} connected things", ((Bridge) thing).getThings().size());
        // update existing things
        for (Thing th : ((Bridge) thing).getThings()) {
            logger.debug("Thing: {}, id: {}", th.getThingTypeUID().getAsString(), th.getUID().getId());
            ThingHandler handler = th.getHandler();
            if (handler != null) {
                if (th.getThingTypeUID().equals(THING_TYPE_PET)) {
                    ((SurePetcarePetHandler) handler).updateThing();
                } else if (th.getThingTypeUID().equals(THING_TYPE_HOUSEHOLD)) {
                    ((SurePetcareHouseholdHandler) handler).updateThing();
                } else if ((th.getThingTypeUID().equals(THING_TYPE_HUB_DEVICE))
                        || (th.getThingTypeUID().equals(THING_TYPE_FLAP_DEVICE))) {
                    ((SurePetcareDeviceHandler) handler).updateThing();
                }
            }
        }
    }

    private synchronized void pollAndUpdatePetLocations() {
        petcareAPI.updatePetLocations();
        for (Thing th : ((Bridge) thing).getThings()) {
            if (th.getThingTypeUID().equals(THING_TYPE_PET)) {
                logger.debug("updating pet location for: {}", th.getUID().getId());
                ThingHandler handler = th.getHandler();
                if (handler != null) {
                    ((SurePetcarePetHandler) handler).updatePetLocation();
                }
            }
        }
    }

}
