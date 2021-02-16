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
package org.openhab.binding.myenergi.internal.dto;

/**
 * The {@link EddiSummary} is a DTO class used to represent a high level summary of an Eddi device. It's used to
 * deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
public class EddiSummary extends BaseSummary {

    @Override
    public String toString() {
        return "EddiSummary [serialNumber=" + serialNumber + ", dat=" + dat + ", tim=" + tim + ", firmwareVersion="
                + firmwareVersion + "]";
    }
}
