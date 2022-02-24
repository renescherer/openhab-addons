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
package org.openhab.binding.myenergi.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.myenergi.internal.exception.ApiException;

/**
 * The {@link ZappiChargingMode} enumeration is used to model the various Zappi charging modes.
 *
 * @author Rene Scherer - Initial contribution
 * @author Volkmar Nissen - added charging mode stop and fromInteger method
 */
@NonNullByDefault
public enum ZappiChargingMode {
    BOOST(0),
    FAST(1),
    ECO(2),
    ECO_PLUS(3),
    STOP(4);

    private final int intValue;

    ZappiChargingMode(final int intValue) {
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public static ZappiChargingMode fromInteger(final int intValue) throws ApiException {
        for (ZappiChargingMode m : ZappiChargingMode.values()) {
            if (m.getIntValue() == intValue) {
                return m;
            }
        }
        throw new ApiException("Unknown Zappi Charging Mode " + intValue);
    }
}
