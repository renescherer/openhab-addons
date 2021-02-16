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
import static org.openhab.core.library.unit.Units.WATT;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.myenergi.internal.MyEnergiApiClient;
import org.openhab.binding.myenergi.internal.dto.HarviSummary;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyEnergiHarviHandler} is responsible for handling things created to represent Harvis.
 *
 * @author Rene Scherer - Initial Contribution
 */
@NonNullByDefault
public class MyEnergiHarviHandler extends MyEnergiBaseDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(MyEnergiHarviHandler.class);

    public MyEnergiHarviHandler(Thing thing, MyEnergiApiClient apiClient) {
        super(thing, apiClient);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateThingCache.getValue();
        }
    }

    @Override
    protected void updateThing() {
        HarviSummary device = apiClient.getData().getHarviBySerialNumber(thing.getUID().getId());
        if (device != null) {
            logger.debug("Updating all thing channels for device : {}", device.serialNumber);

            updateDateTimeState(HARVI_CHANNEL_LAST_UPDATED_TIME, device.getLastUpdateTime());

            updateStringState(HARVI_CHANNEL_CLAMP_NAME_1, device.clampName1);
            updateStringState(HARVI_CHANNEL_CLAMP_NAME_2, device.clampName2);
            updateStringState(HARVI_CHANNEL_CLAMP_NAME_3, device.clampName3);

            updatePowerState(HARVI_CHANNEL_CLAMP_POWER_1, device.clampPower1, WATT);
            updatePowerState(HARVI_CHANNEL_CLAMP_POWER_2, device.clampPower2, WATT);
            updatePowerState(HARVI_CHANNEL_CLAMP_POWER_3, device.clampPower3, WATT);

            updateIntegerState(HARVI_CHANNEL_CLAMP_PHASE_1, device.clampPhase1);
            updateIntegerState(HARVI_CHANNEL_CLAMP_PHASE_2, device.clampPhase2);
            updateIntegerState(HARVI_CHANNEL_CLAMP_PHASE_3, device.clampPhase3);
        } else {
            logger.debug("Trying to update unknown device: {}", thing.getUID().getId());
        }
    }
}
