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
package org.openhab.binding.myenergi.internal.handler;

import static org.openhab.binding.myenergi.internal.MyEnergiBindingConstants.*;
import static org.openhab.core.library.unit.Units.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.myenergi.internal.MyEnergiApiClient;
import org.openhab.binding.myenergi.internal.dto.ZappiBoostTimeSlot;
import org.openhab.binding.myenergi.internal.dto.ZappiBoostTimes;
import org.openhab.binding.myenergi.internal.dto.ZappiSummary;
import org.openhab.binding.myenergi.internal.exception.ApiException;
import org.openhab.binding.myenergi.internal.exception.RecordNotFoundException;
import org.openhab.binding.myenergi.internal.util.ZappiChargingMode;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerService;
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

    final String dayNames[] = { ZAPPI_CHANNEL_TIMED_BOOST_MONDAY, ZAPPI_CHANNEL_TIMED_BOOST_TUESDAY,
            ZAPPI_CHANNEL_TIMED_BOOST_WEDNESDAY, ZAPPI_CHANNEL_TIMED_BOOST_THURSDAY, ZAPPI_CHANNEL_TIMED_BOOST_FRIDAY,
            ZAPPI_CHANNEL_TIMED_BOOST_SATURDAY, ZAPPI_CHANNEL_TIMED_BOOST_SUNDAY };

    double newManualBoostCharge = 0;

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(MyEnergiZappiActions.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {

            if (command instanceof RefreshType) {
                updateThingCache.getValue();
            } else {
                if (channelUID.getId().startsWith(ZAPPI_CHANNEL_GROUP_TIMED_BOOST_SLOT)) {
                    handleCommandStartTimedBoost(channelUID, command);
                } else {
                    switch (channelUID.getId()) {
                        case ZAPPI_CHANNEL_CHARGING_MODE:
                            apiClient.setZappiChargingMode(serialNumber,
                                    ZappiChargingMode.fromInteger(Integer.parseInt(command.toString())));
                            break;
                        case ZAPPI_CHANNEL_MINIMUM_GREEN_LEVEL:
                            apiClient.setZappiMinimumGreenLevel(serialNumber, Integer.parseInt(command.toString()));
                            break;
                        case ZAPPI_CHANNEL_NEW_MANUAL_BOOST_CHARGE:
                            newManualBoostCharge = Double.parseDouble(command.toString());
                        case ZAPPI_CHANNEL_MANUAL_BOOST_STATUS:
                            apiClient.setZappiManualBoost(serialNumber, (int) newManualBoostCharge);
                    }
                }
            }
        } catch (NumberFormatException | ApiException e) {
            logger.error("unable to execute command channel:{} command{}: serialNumber: {} message: {}",
                    channelUID.getId(), command.toString(), serialNumber, e.getMessage());
        }
    }

    private void handleCommandStartTimedBoost(ChannelUID channelUID, Command command) throws ApiException {
        int slotIndex = channelUID.getId().indexOf('#') - 1;
        char slot = (char) (channelUID.getId().charAt(slotIndex) - '0');
        // String channelPrefix = ZAPPI_CHANNEL_GROUP_TIMED_BOOST_SLOT + Character.toString(slot) + "#";
        String channel = channelUID.getId().substring(slotIndex + 1);
        ZappiBoostTimes boostTimes = apiClient.getZappiBoostTimes(serialNumber);
        Iterator<ZappiBoostTimeSlot> iter = boostTimes.boostTimes.iterator();
        while (iter.hasNext()) {
            ZappiBoostTimeSlot s = iter.next();
            if (s.slotId == slot - '0' + 10) {
                switch (channel) {
                    case ZAPPI_CHANNEL_TIMED_BOOST_MONDAY:
                    case ZAPPI_CHANNEL_TIMED_BOOST_TUESDAY:
                    case ZAPPI_CHANNEL_TIMED_BOOST_WEDNESDAY:
                    case ZAPPI_CHANNEL_TIMED_BOOST_THURSDAY:
                    case ZAPPI_CHANNEL_TIMED_BOOST_FRIDAY:
                    case ZAPPI_CHANNEL_TIMED_BOOST_SATURDAY:
                    case ZAPPI_CHANNEL_TIMED_BOOST_SUNDAY:

                        StringBuilder sb = new StringBuilder(s.daysOfTheWeekMap);
                        int dayIdx = -1;
                        for (int i = 0; i < dayNames.length && dayIdx < 0; i++) {
                            if (dayNames[i] == channel) {
                                dayIdx = i;
                            }
                        }
                        if (dayIdx >= 0) {
                            sb.setCharAt(dayIdx, command.toString() == "ON" ? '1' : '0');
                        } else {
                            throw new ApiException(
                                    "invalid dayOfWeekMap map:" + s.daysOfTheWeekMap + "day: {}" + channel);
                        }
                        s.daysOfTheWeekMap = sb.toString();
                        break;
                    case ZAPPI_CHANNEL_TIMED_BOOST_START_HOUR:
                        s.startHour = Integer.parseInt(command.toString());
                        break;
                    case ZAPPI_CHANNEL_TIMED_BOOST_START_MINUTE:
                        s.startMinute = Integer.parseInt(command.toString());
                        break;
                    case ZAPPI_CHANNEL_TIMED_BOOST_DURATION:
                        s.durationHour = Integer.parseInt(command.toString());
                        s.durationMinute = (int) ((Double.parseDouble(command.toString()) % 1) * 60);
                        break;
                    case ZAPPI_CHANNEL_TIMED_BOOST_CANCEL:
                        s.daysOfTheWeekMap = "";
                        s.startHour = 0;
                        s.startMinute = 0;
                        s.durationHour = 0;
                        s.durationMinute = 0;
                        break;
                    default:
                        logger.warn("unknown channel for update timed boost {}", channel);
                        return;
                }
                apiClient.setZappiBoostTimes(serialNumber, s);
            }
        }
    }

    @Override
    protected void updateThing() {
        try {
            logger.debug("Updating all thing channels for device : {}", serialNumber);
            ZappiSummary device = apiClient.getData().getZappiBySerialNumber(serialNumber);
            // Device group ===============================================
            updateDateTimeState(ZAPPI_CHANNEL_LAST_UPDATED_TIME, device.getLastUpdateTime());

            updateIntegerState(ZAPPI_CHANNEL_NUMBER_OF_PHASES, device.numberOfPhases, false);
            updateIntegerState(ZAPPI_CHANNEL_LOCKING_MODE, device.lockingMode, false);
            updateStringState(ZAPPI_CHANNEL_CHARGING_MODE, device.chargingMode.toString());
            updateStringState(ZAPPI_CHANNEL_STATUS, device.status.toString());
            updateStringState(ZAPPI_CHANNEL_PLUG_STATUS, device.plugStatus);

            updateIntegerState(ZAPPI_CHANNEL_COMMAND_TRIES, device.commandTries, false);
            updateIntegerState(ZAPPI_CHANNEL_DIVERTER_PRIORITY, device.diverterPriority, false);
            updateIntegerState(ZAPPI_CHANNEL_MINIMUM_GREEN_LEVEL, device.minimumGreenLevel, false);

            updateElectricPotentialState(ZAPPI_CHANNEL_SUPPLY_VOLTAGE, device.supplyVoltageInTenthVolt / 10.0f, VOLT);
            updateFrequencyState(ZAPPI_CHANNEL_SUPPLY_FREQUENCY, device.supplyFrequency, HERTZ);

            updatePowerState(ZAPPI_CHANNEL_GRID_POWER, device.gridPower, WATT);
            updatePowerState(ZAPPI_CHANNEL_GENERATED_POWER, device.generatedPower, WATT);
            updatePowerState(ZAPPI_CHANNEL_DIVERTED_POWER, device.divertedPower, WATT);
            int consumedPower = ((device.gridPower != null) ? device.gridPower : 0)
                    + ((device.generatedPower != null) ? device.generatedPower : 0);
            updatePowerState(ZAPPI_CHANNEL_CONSUMED_POWER, consumedPower, WATT);

            updateEnergyState(ZAPPI_CHANNEL_CHARGE_ADDED, device.chargeAdded, KILOWATT_HOUR);
            // smart boost group ===============================================
            updateIntegerState(ZAPPI_CHANNEL_SMART_BOOST_END_TIME_HOUR, device.smartBoostHour, false);
            updateIntegerState(ZAPPI_CHANNEL_SMART_BOOST_END_TIME_MINUTE, device.smartBoostMinute, false);
            updateEnergyState(ZAPPI_CHANNEL_SMART_BOOST_CHARGE, device.smartBoostCharge, KILOWATT_HOUR);
            updateIntegerState(ZAPPI_CHANNEL_SMART_BOOST_STOPALL, 0, false);
            // manual Boost group ===============================================
            updateIntegerState(ZAPPI_CHANNEL_MANUAL_BOOST_STATUS, device.getManualBoost() ? 1 : 0, false);
            updateEnergyState(ZAPPI_CHANNEL_MANUAL_BOOST_CHARGE, device.manualBoostCharge, KILOWATT_HOUR);
            if (newManualBoostCharge == 0) {
                newManualBoostCharge = device.manualBoostCharge;
            }
            updateEnergyState(ZAPPI_CHANNEL_NEW_MANUAL_BOOST_CHARGE, device.manualBoostCharge, KILOWATT_HOUR);
            updateIntegerState(ZAPPI_CHANNEL_MANUAL_BOOST_STOPALL, 0, false);
            // clamp group ===============================================
            updateStringState(ZAPPI_CHANNEL_CLAMP_NAME_1, device.clampName1);
            updateStringState(ZAPPI_CHANNEL_CLAMP_NAME_2, device.clampName2);
            updateStringState(ZAPPI_CHANNEL_CLAMP_NAME_3, device.clampName3);

            updatePowerState(ZAPPI_CHANNEL_CLAMP_POWER_1, device.clampPower1, WATT);
            updatePowerState(ZAPPI_CHANNEL_CLAMP_POWER_2, device.clampPower2, WATT);
            updatePowerState(ZAPPI_CHANNEL_CLAMP_POWER_3, device.clampPower3, WATT);
            updateBoostTimeSlots();

        } catch (RecordNotFoundException | ApiException e) {
            logger.debug("Trying to update unknown device: {}", thing.getUID().getId());
        }
    }

    private void updateBoostTimeSlots() throws ApiException {
        ZappiBoostTimes boostTimes = apiClient.getZappiBoostTimes(serialNumber);
        Iterator<ZappiBoostTimeSlot> iter = boostTimes.boostTimes.iterator();
        while (iter.hasNext()) {
            ZappiBoostTimeSlot slot = iter.next();
            String channelGroupPrefix = ZAPPI_CHANNEL_GROUP_TIMED_BOOST_SLOT + (slot.slotId - 10) + "#";
            String weekMap = slot.daysOfTheWeekMap;
            int idx = 0;
            for (byte c : weekMap.getBytes()) {
                updateIntegerState(channelGroupPrefix + dayNames[idx], c - '0', false);
            }
            updateIntegerState(channelGroupPrefix + ZAPPI_CHANNEL_TIMED_BOOST_START_HOUR, slot.startHour, false);
            updateIntegerState(channelGroupPrefix + ZAPPI_CHANNEL_TIMED_BOOST_START_MINUTE, slot.startMinute, false);
            updateDoubleState(channelGroupPrefix + ZAPPI_CHANNEL_TIMED_BOOST_DURATION,
                    slot.durationHour + ((double) slot.durationMinute) / 60);
            updateIntegerState(channelGroupPrefix + ZAPPI_CHANNEL_TIMED_BOOST_CANCEL, 0, false);
        }
    }

    @Override
    protected void refreshMeasurements() throws ApiException {
        try {
            apiClient.updateZappiSummary(serialNumber);
        } catch (RecordNotFoundException e) {
            logger.warn("invalid serial number: {}", serialNumber, e);
        }
    }
}
