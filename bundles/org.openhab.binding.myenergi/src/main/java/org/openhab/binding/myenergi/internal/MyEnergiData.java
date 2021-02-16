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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myenergi.internal.dto.EddiSummary;
import org.openhab.binding.myenergi.internal.dto.HarviSummary;
import org.openhab.binding.myenergi.internal.dto.ZappiSummary;

/**
 * @author scherer
 *
 */
@NonNullByDefault
public class MyEnergiData {

    private List<HarviSummary> harvis = new ArrayList<HarviSummary>();
    private List<ZappiSummary> zappis = new ArrayList<ZappiSummary>();
    private List<EddiSummary> eddis = new ArrayList<EddiSummary>();
    private String asn = "";

    public final List<HarviSummary> getHarvis() {
        return harvis;
    }

    public final void setHarvis(List<HarviSummary> harvis) {
        this.harvis = harvis;
    }

    public final List<ZappiSummary> getZappis() {
        return zappis;
    }

    public final void setZappis(List<ZappiSummary> zappis) {
        this.zappis = zappis;
    }

    public final List<EddiSummary> getEddis() {
        return eddis;
    }

    public final void setEddis(List<EddiSummary> eddis) {
        this.eddis = eddis;
    }

    public final String getAsn() {
        return asn;
    }

    public final void setAsn(String asn) {
        this.asn = asn;
    }

    public @Nullable HarviSummary getHarviBySerialNumber(final String serialNumber) {
        for (HarviSummary device : harvis) {
            if (serialNumber.equals(device.serialNumber)) {
                return device;
            }
        }
        return null;
    }

    public @Nullable ZappiSummary getZappiBySerialNumber(final String serialNumber) {
        for (ZappiSummary device : zappis) {
            if (serialNumber.equals(device.serialNumber)) {
                return device;
            }
        }
        return null;
    }

    public @Nullable EddiSummary getEddiBySerialNumber(final String serialNumber) {
        for (EddiSummary device : eddis) {
            if (serialNumber.equals(device.serialNumber)) {
                return device;
            }
        }
        return null;
    }

    public void clear() {
        harvis = new ArrayList<HarviSummary>();
        zappis = new ArrayList<ZappiSummary>();
        asn = "";
    }

    public void addHarvi(HarviSummary device) {
        harvis.add(device);
    }

    public void addZappi(ZappiSummary device) {
        zappis.add(device);
    }

    public void addEddi(EddiSummary device) {
        eddis.add(device);
    }

    public void addAllHarvis(List<HarviSummary> list) {
        harvis.addAll(list);
    }

    public void addAllZappis(List<ZappiSummary> list) {
        zappis.addAll(list);
    }

    public void addAllEddis(List<EddiSummary> list) {
        eddis.addAll(list);
    }
}
