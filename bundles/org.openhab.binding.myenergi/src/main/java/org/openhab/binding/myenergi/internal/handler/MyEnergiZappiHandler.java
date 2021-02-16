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
import static org.openhab.core.library.unit.Units.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.myenergi.internal.MyEnergiApiClient;
import org.openhab.binding.myenergi.internal.dto.ZappiSummary;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyEnergiZappiHandler} is responsible for handling things created to represent Zappis.
 *
 * @author Rene Scherer - Initial Contribution
 */
@NonNullByDefault
public class MyEnergiZappiHandler extends MyEnergiBaseDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(MyEnergiZappiHandler.class);

    public MyEnergiZappiHandler(Thing thing, MyEnergiApiClient apiClient) {
        super(thing, apiClient);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateThingCache.getValue();
        } else {
            switch (channelUID.getId()) {
                case ZAPPI_CHANNEL_CHARGING_MODE:
            }
        }
    }

    @Override
    protected void updateThing() {
        ZappiSummary device = apiClient.getData().getZappiBySerialNumber(thing.getUID().getId());
        if (device != null) {
            logger.debug("Updating all thing channels for device : {}", device.serialNumber);

            updateDateTimeState(ZAPPI_CHANNEL_LAST_UPDATED_TIME, device.getLastUpdateTime());
            updateElectricPotentialState(ZAPPI_CHANNEL_SUPPLY_VOLTAGE, device.supplyVoltage, VOLT);
            updateFrequencyState(ZAPPI_CHANNEL_SUPPLY_FREQUENCY, device.supplyFrequency, HERTZ);

            updateIntegerState(ZAPPI_CHANNEL_NUMBER_OF_PHASES, device.numberOfPhases);
            updateIntegerState(ZAPPI_CHANNEL_LOCKING_MODE, device.lockingMode);
            updateIntegerState(ZAPPI_CHANNEL_CHARGING_MODE, device.chargingMode);
            updateIntegerState(ZAPPI_CHANNEL_STATUS, device.status);
            updateStringState(ZAPPI_CHANNEL_PLUG_STATUS, device.plugStatus);

            updateIntegerState(ZAPPI_CHANNEL_COMMAND_TRIES, device.commandTries);
            updateIntegerState(ZAPPI_CHANNEL_DIVERTER_PRIORITY, device.diverterPriority);
            updateIntegerState(ZAPPI_CHANNEL_MINIMUM_GREEN_LEVEL, device.minimumGreenLevel);

            updatePowerState(ZAPPI_CHANNEL_GRID_POWER, device.gridPower, WATT);
            updatePowerState(ZAPPI_CHANNEL_GENERATED_POWER, device.generatedPower, WATT);
            updatePowerState(ZAPPI_CHANNEL_DIVERTED_POWER, device.divertedPower, WATT);

            updateEnergyState(ZAPPI_CHANNEL_CHARGE_ADDED, device.chargeAdded, KILOWATT_HOUR);

            updateStringState(ZAPPI_CHANNEL_SMART_BOOST_TIME, device.smartBoostHour + ":" + device.smartBoostMinute);
            updateEnergyState(ZAPPI_CHANNEL_SMART_BOOST_CHARGE, device.smartBoostCharge, KILOWATT_HOUR);
            updateStringState(ZAPPI_CHANNEL_TIMED_BOOST_TIME, device.timedBoostHour + ":" + device.timedBoostMinute);
            updateEnergyState(ZAPPI_CHANNEL_TIMED_BOOST_CHARGE, device.timedBoostCharge, KILOWATT_HOUR);

            updateStringState(ZAPPI_CHANNEL_CLAMP_NAME_1, device.clampName1);
            updateStringState(ZAPPI_CHANNEL_CLAMP_NAME_2, device.clampName2);
            updateStringState(ZAPPI_CHANNEL_CLAMP_NAME_3, device.clampName3);
            updateStringState(ZAPPI_CHANNEL_CLAMP_NAME_4, device.clampName4);
            updateStringState(ZAPPI_CHANNEL_CLAMP_NAME_5, device.clampName5);
            updateStringState(ZAPPI_CHANNEL_CLAMP_NAME_6, device.clampName6);

            updatePowerState(ZAPPI_CHANNEL_CLAMP_POWER_1, device.clampPower1, WATT);
            updatePowerState(ZAPPI_CHANNEL_CLAMP_POWER_2, device.clampPower2, WATT);
            updatePowerState(ZAPPI_CHANNEL_CLAMP_POWER_3, device.clampPower3, WATT);
            updatePowerState(ZAPPI_CHANNEL_CLAMP_POWER_4, device.clampPower4, WATT);
            updatePowerState(ZAPPI_CHANNEL_CLAMP_POWER_5, device.clampPower5, WATT);
            updatePowerState(ZAPPI_CHANNEL_CLAMP_POWER_6, device.clampPower6, WATT);
        } else

        {
            logger.debug("Trying to update unknown device: {}", thing.getUID().getId());
        }
    }
}
