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

import java.time.DayOfWeek;
import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myenergi.internal.dto.DaysOfWeekMap;
import org.openhab.binding.myenergi.internal.dto.ZappiBoostTimeSlot;
import org.openhab.binding.myenergi.internal.exception.ApiException;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyEnergiZappiActions} class implements actions on the Zappi EV charger
 *
 * @author Rene Scherer - Initial contribution
 */
@ThingActionsScope(name = "myenergi")
@NonNullByDefault
public class MyEnergiZappiActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(MyEnergiZappiActions.class);

    private @Nullable MyEnergiZappiHandler handler;

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (MyEnergiZappiHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "setTimedBoost", description = "Schedules a timed boost charge.")
    public void setTimedBoost(
            @ActionInput(name = "slot", label = "Slot", description = "The boost time slot (11,12,13,14).") int slot,
            @ActionInput(name = "dayOfWeek", label = "Day of the Week", description = "The day of the week") DayOfWeek dayOfWeek,
            @ActionInput(name = "startHour", label = "Start Hour", description = "The boost time slot (11,12,13,14).") int startHour,
            @ActionInput(name = "startMinute", label = "Start Minute", description = "The boost time slot (11,12,13,14).") int startMinute,
            @ActionInput(name = "duration", label = "Duration", description = "The boost time slot (11,12,13,14).") Duration duration) {
        try {
            // DayOfWeek currentDayOfWeek = DayOfWeek.from(Instant.now());

            DaysOfWeekMap dowm = new DaysOfWeekMap(dayOfWeek);
            ZappiBoostTimeSlot timeSlot = new ZappiBoostTimeSlot(slot, startHour, startMinute, duration.toHoursPart(),
                    duration.toMinutesPart(), dowm);
            MyEnergiZappiHandler h = handler;
            if (h != null) {
                h.apiClient.setZappiBoostTimes(h.serialNumber, timeSlot);
            }
        } catch (ApiException e) {
            logger.warn("Couldn't set boost time slot - {}", e.getMessage());
        }
    }

    public static void setTimedBoost(@Nullable ThingActions actions, int slot, DayOfWeek dayOfWeek, int startHour,
            int startMinute, Duration duration) {
        if (actions instanceof MyEnergiZappiActions) {
            ((MyEnergiZappiActions) actions).setTimedBoost(slot, dayOfWeek, startHour, startMinute, duration);
        } else {
            throw new IllegalArgumentException("Instance is not an OctopusEnergyElectricityMeterPointActions class.");
        }
    }
}
