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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.myenergi.internal.dto.ZappiHourlyHistory;
import org.openhab.binding.myenergi.internal.util.ZappiHourlyHistoryTypeAdapter;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link myenergiBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class MyEnergiBindingConstants {

    private static final String BINDING_ID = "myenergi";

    public static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(ZappiHourlyHistory.class, new ZappiHourlyHistoryTypeAdapter()).create();

    // List all Thing Type UIDs, related to the binding
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_ZAPPI = new ThingTypeUID(BINDING_ID, "zappi");
    public static final ThingTypeUID THING_TYPE_EDDI = new ThingTypeUID(BINDING_ID, "eddi");
    public static final ThingTypeUID THING_TYPE_HARVI = new ThingTypeUID(BINDING_ID, "harvi");

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_BRIDGE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(THING_TYPE_ZAPPI, THING_TYPE_EDDI, THING_TYPE_HARVI));

    // Zappi Channel Names
    public static final String ZAPPI_CHANNEL_LAST_UPDATED_TIME = "device#lastUpdatedTime";
    public static final String ZAPPI_CHANNEL_NUMBER_OF_PHASES = "device#numberOfPhases";
    public static final String ZAPPI_CHANNEL_LOCKING_MODE = "device#lockingMode";
    public static final String ZAPPI_CHANNEL_CHARGING_MODE = "device#chargingMode";
    public static final String ZAPPI_CHANNEL_STATUS = "device#status";
    public static final String ZAPPI_CHANNEL_PLUG_STATUS = "device#plugStatus";
    public static final String ZAPPI_CHANNEL_COMMAND_TRIES = "device#commandTries";
    public static final String ZAPPI_CHANNEL_DIVERTER_PRIORITY = "device#diverterPriority";
    public static final String ZAPPI_CHANNEL_MINIMUM_GREEN_LEVEL = "device#minimumGreenLevel";
    public static final String ZAPPI_CHANNEL_SUPPLY_VOLTAGE = "device#supplyVoltage";
    public static final String ZAPPI_CHANNEL_SUPPLY_FREQUENCY = "device#supplyFrequency";
    public static final String ZAPPI_CHANNEL_GRID_POWER = "device#gridPower";
    public static final String ZAPPI_CHANNEL_GENERATED_POWER = "device#generatedPower";
    public static final String ZAPPI_CHANNEL_DIVERTED_POWER = "device#divertedPower";
    public static final String ZAPPI_CHANNEL_CONSUMED_POWER = "device#consumedPower";
    public static final String ZAPPI_CHANNEL_CHARGE_ADDED = "device#chargeAdded";

    public static final String ZAPPI_CHANNEL_SMART_BOOST_END_TIME_HOUR = "smartBoost#endTimeHour";
    public static final String ZAPPI_CHANNEL_SMART_BOOST_END_TIME_MINUTE = "smartBoost#endTimeMinute";
    public static final String ZAPPI_CHANNEL_SMART_BOOST_CHARGE = "smartBoost#smartBoostCharge";
    public static final String ZAPPI_CHANNEL_SMART_BOOST_STATUS = "smartBoost#boostStatus";
    public static final String ZAPPI_CHANNEL_SMART_BOOST_STOPALL = "smartBoost#stopAll";

    public static final String ZAPPI_CHANNEL_MANUAL_BOOST_STATUS = "manualBoost#boostStatus";
    public static final String ZAPPI_CHANNEL_MANUAL_BOOST_CHARGE = "manualBoost#manualBoostCharge";
    public static final String ZAPPI_CHANNEL_NEW_MANUAL_BOOST_CHARGE = "manualBoost#newManualBoostCharge";
    public static final String ZAPPI_CHANNEL_MANUAL_BOOST_STOPALL = "manualBoost#stopAll";

    public static final String ZAPPI_CHANNEL_CLAMP_NAME_1 = "clamp#clampName1";
    public static final String ZAPPI_CHANNEL_CLAMP_NAME_2 = "clamp#clampName2";
    public static final String ZAPPI_CHANNEL_CLAMP_NAME_3 = "clamp#clampName3";
    public static final String ZAPPI_CHANNEL_CLAMP_POWER_1 = "clamp#clampPower1";
    public static final String ZAPPI_CHANNEL_CLAMP_POWER_2 = "clamp#clampPower2";
    public static final String ZAPPI_CHANNEL_CLAMP_POWER_3 = "clamp#clampPower3";
    public static final String ZAPPI_CHANNEL_GROUP_TIMED_BOOST_SLOT = "timedBoostSlot";
    public static final String ZAPPI_CHANNEL_TIMED_BOOST_MONDAY = "monday";
    public static final String ZAPPI_CHANNEL_TIMED_BOOST_TUESDAY = "tuesday";
    public static final String ZAPPI_CHANNEL_TIMED_BOOST_WEDNESDAY = "wednesday";
    public static final String ZAPPI_CHANNEL_TIMED_BOOST_THURSDAY = "thursday";
    public static final String ZAPPI_CHANNEL_TIMED_BOOST_FRIDAY = "friday";
    public static final String ZAPPI_CHANNEL_TIMED_BOOST_SATURDAY = "saturday";
    public static final String ZAPPI_CHANNEL_TIMED_BOOST_SUNDAY = "sunday";
    public static final String ZAPPI_CHANNEL_TIMED_BOOST_START_HOUR = "startHour";
    public static final String ZAPPI_CHANNEL_TIMED_BOOST_START_MINUTE = "startMinute";
    public static final String ZAPPI_CHANNEL_TIMED_BOOST_DURATION = "duration";
    public static final String ZAPPI_CHANNEL_TIMED_BOOST_CANCEL = "cancel";

    // Harvi Channel Names
    public static final String HARVI_CHANNEL_LAST_UPDATED_TIME = "lastUpdatedTime";
    public static final String HARVI_CHANNEL_CLAMP_NAME_1 = "clampName1";
    public static final String HARVI_CHANNEL_CLAMP_NAME_2 = "clampName2";
    public static final String HARVI_CHANNEL_CLAMP_NAME_3 = "clampName3";
    public static final String HARVI_CHANNEL_CLAMP_POWER_1 = "clampPower1";
    public static final String HARVI_CHANNEL_CLAMP_POWER_2 = "clampPower2";
    public static final String HARVI_CHANNEL_CLAMP_POWER_3 = "clampPower3";
    public static final String HARVI_CHANNEL_CLAMP_PHASE_1 = "clampPhase1";
    public static final String HARVI_CHANNEL_CLAMP_PHASE_2 = "clampPhase2";
    public static final String HARVI_CHANNEL_CLAMP_PHASE_3 = "clampPhase3";
}
