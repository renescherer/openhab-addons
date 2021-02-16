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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.myenergi.internal.dto.ZappiHourlyHistory;
import org.openhab.core.thing.ThingTypeUID;

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
            .registerTypeAdapter(ZappiHourlyHistory.class, new ZappiHourlyHistoryTypeAdapter()).create();

    // List all Thing Type UIDs, related to the binding
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_ZAPPI = new ThingTypeUID(BINDING_ID, "zappi");
    public static final ThingTypeUID THING_TYPE_EDDI = new ThingTypeUID(BINDING_ID, "eddi");
    public static final ThingTypeUID THING_TYPE_HARVI = new ThingTypeUID(BINDING_ID, "harvi");

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_BRIDGE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(THING_TYPE_ZAPPI, THING_TYPE_EDDI, THING_TYPE_HARVI));

    // Bridge Channel Names
    public static final String BRIDGE_CHANNEL_ONLINE = "online";
    public static final String BRIDGE_CHANNEL_REFRESH = "refresh";

    // Zappi Channel Names
    public static final String ZAPPI_CHANNEL_LAST_UPDATED_TIME = "lastUpdatedTime";
    public static final String ZAPPI_CHANNEL_SUPPLY_VOLTAGE = "supplyVoltage";
    public static final String ZAPPI_CHANNEL_SUPPLY_FREQUENCY = "supplyFrequency";
    public static final String ZAPPI_CHANNEL_NUMBER_OF_PHASES = "numberOfPhases";
    public static final String ZAPPI_CHANNEL_LOCKING_MODE = "lockingMode";
    public static final String ZAPPI_CHANNEL_CHARGING_MODE = "chargingMode";
    public static final String ZAPPI_CHANNEL_STATUS = "status";
    public static final String ZAPPI_CHANNEL_PLUG_STATUS = "plugStatus";
    public static final String ZAPPI_CHANNEL_COMMAND_TRIES = "commandTries";
    public static final String ZAPPI_CHANNEL_DIVERTER_PRIORITY = "diverterPriority";
    public static final String ZAPPI_CHANNEL_MINIMUM_GREEN_LEVEL = "minimumGreenLevel";
    public static final String ZAPPI_CHANNEL_GRID_POWER = "gridPower";
    public static final String ZAPPI_CHANNEL_GENERATED_POWER = "generatedPower";
    public static final String ZAPPI_CHANNEL_DIVERTED_POWER = "divertedPower";
    public static final String ZAPPI_CHANNEL_CHARGE_ADDED = "chargeAdded";
    public static final String ZAPPI_CHANNEL_SMART_BOOST_TIME = "smartBoostTime";
    public static final String ZAPPI_CHANNEL_SMART_BOOST_CHARGE = "smartBoostCharge";
    public static final String ZAPPI_CHANNEL_TIMED_BOOST_TIME = "timedBoostTime";
    public static final String ZAPPI_CHANNEL_TIMED_BOOST_CHARGE = "timedBoostCharge";
    public static final String ZAPPI_CHANNEL_CLAMP_NAME_1 = "clampName1";
    public static final String ZAPPI_CHANNEL_CLAMP_NAME_2 = "clampName2";
    public static final String ZAPPI_CHANNEL_CLAMP_NAME_3 = "clampName3";
    public static final String ZAPPI_CHANNEL_CLAMP_NAME_4 = "clampName4";
    public static final String ZAPPI_CHANNEL_CLAMP_NAME_5 = "clampName5";
    public static final String ZAPPI_CHANNEL_CLAMP_NAME_6 = "clampName6";
    public static final String ZAPPI_CHANNEL_CLAMP_POWER_1 = "clampPower1";
    public static final String ZAPPI_CHANNEL_CLAMP_POWER_2 = "clampPower2";
    public static final String ZAPPI_CHANNEL_CLAMP_POWER_3 = "clampPower3";
    public static final String ZAPPI_CHANNEL_CLAMP_POWER_4 = "clampPower4";
    public static final String ZAPPI_CHANNEL_CLAMP_POWER_5 = "clampPower5";
    public static final String ZAPPI_CHANNEL_CLAMP_POWER_6 = "clampPower6";

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
