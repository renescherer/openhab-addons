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

import static org.openhab.core.thing.ThingStatus.ONLINE;

import java.time.ZonedDateTime;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myenergi.internal.MyEnergiApiClient;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyEnergiBaseDeviceHandler} is an abstract base class for any MyEnergi things. It contains an expiring
 * cache which ensures thing properties are not unnecessarily updated.
 *
 * @author Rene Scherer - Initial Contribution
 */
@NonNullByDefault
public abstract class MyEnergiBaseDeviceHandler extends BaseThingHandler {

    private static final int UPDATE_THING_CACHE_TIMEOUT = 3000; // 3 secs

    private final Logger logger = LoggerFactory.getLogger(MyEnergiBaseDeviceHandler.class);

    protected MyEnergiApiClient apiClient;

    protected ExpiringCache<Integer> updateThingCache = new ExpiringCache<Integer>(UPDATE_THING_CACHE_TIMEOUT,
            this::refreshCache);

    public MyEnergiBaseDeviceHandler(Thing thing, MyEnergiApiClient apiClient) {
        super(thing);
        this.apiClient = apiClient;
    }

    @Override
    public void initialize() {
        updateStatus(ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateThingCache.getValue();
        }
    }

    @Override
    public void updateProperties(Map<String, String> properties) {
        logger.debug("Updating thing properties");
        super.updateProperties(properties);
    }

    private Integer refreshCache() {
        logger.debug("cache has timed out, we refresh the values in the thing");
        updateThing();
        // we don't care about the cache content, we just return a zero
        return 0;
    }

    protected void updatePowerState(final String channelId, @Nullable Integer value, Unit<Power> unit) {
        QuantityType<Power> quantity;
        if (value != null) {
            quantity = new QuantityType<>(value, unit);

        } else {
            quantity = new QuantityType<>(0, unit);
        }
        updateState(channelId, quantity);
    }

    protected void updateEnergyState(final String channelId, @Nullable Double value, Unit<Energy> unit) {
        QuantityType<Energy> quantity;
        if (value != null) {
            quantity = new QuantityType<>(value, unit);
        } else {
            quantity = new QuantityType<>(0, unit);
        }
        updateState(channelId, quantity);
    }

    protected void updateElectricPotentialState(final String channelId, @Nullable Float value,
            Unit<ElectricPotential> unit) {
        QuantityType<ElectricPotential> quantity;
        if (value != null) {
            quantity = new QuantityType<>(value, unit);
        } else {
            quantity = new QuantityType<>(0, unit);
        }
        updateState(channelId, quantity);
    }

    protected void updateFrequencyState(final String channelId, @Nullable Float value, Unit<Frequency> unit) {
        QuantityType<Frequency> quantity;
        if (value != null) {
            quantity = new QuantityType<>(value, unit);
        } else {
            quantity = new QuantityType<>(0, unit);
        }
        updateState(channelId, quantity);
    }

    protected void updateStringState(final String channelId, @Nullable String value) {
        if (value != null) {
            updateState(channelId, new StringType(value));
        }
    }

    protected void updateIntegerState(final String channelId, @Nullable Integer value) {
        if (value != null) {
            updateState(channelId, new DecimalType(value));
        }
    }

    protected void updateDateTimeState(final String channelId, @Nullable ZonedDateTime value) {
        if (value != null) {
            updateState(channelId, new DateTimeType(value));
        }
    }

    /**
     * Updates all channels of a thing.
     */
    protected abstract void updateThing();
}
