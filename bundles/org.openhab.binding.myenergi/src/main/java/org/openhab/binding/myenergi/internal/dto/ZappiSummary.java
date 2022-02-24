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
package org.openhab.binding.myenergi.internal.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ZappiSummary} is a DTO class used to represent a high level summary of a Zappi device. It's used to
 * deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 * @author Volkmar Nissen - Update due to changes of the zaopi API
 *
 */
public class ZappiSummary extends BaseSummary {

    private final Logger logger = LoggerFactory.getLogger(ZappiSummary.class);

    // {"dat":"27-11-2020","tim":"16:02:06","ectp2":843,"ectt1":"Internal
    // Load","ectt2":"Grid","ectt3":"None","frq":50.12,"grd":841,"pha":1,"sno":17028110,"sta":1,"vol":235.0,"pri":1,"cmt":254,"zmo":1,"tbk":5,"che":0.00,"pst":"A","mgl":50,"sbh":17,"sbk":5,"ectt4":"None","ectt5":"None","ectt6":"None","fwv":"3560S3.054","dst":1,"lck":16}
    // new Feb 2022:
    // "bsm" Boost 1 = manual Boost
    // "bst" Boost time 0 ???
    // "tz" 0 ???
    // "zs" 0 ???
    // Removed in Feb 22
    // tbh
    // tbm

    @SerializedName("vol")
    public Float supplyVoltageInTenthVolt;

    @SerializedName("frq")
    public Float supplyFrequency;
    @SerializedName("pha")
    public Integer numberOfPhases;
    @SerializedName("lck") // Bit 0: Locked Now, Bit 1: Lock when plugged in, Bit 2: Lock when unplugged., Bit 3: Charge
                           // when locked., Bit 4: Charge Session Allowed (Even if locked)
    public Integer lockingMode;
    @SerializedName("zmo")
    public Integer chargingMode; // Zappi Mode - 1=Fast, 2=Eco, 3=Eco+
    @SerializedName("sta")
    public Integer status; // 0 Starting, 1 Waiting for export, 2 DSR, 3 Diverting, 4 Boosting, 5 Charge Complete
    @SerializedName("pst")
    public String plugStatus; // Status A=EV Disconnected, B1=EV Connected, B2=Waiting for EV, C1=Charging, C2= Charging
                              // Max Power, F=Fault/Restart
    @SerializedName("cmt")
    public Integer commandTries; // 0-10 Trying, 253 Acked & Failed, 254 Acked & OK, 255 No command has ever been sent
    @SerializedName("pri")
    public Integer diverterPriority;
    @SerializedName("mgl")
    public Integer minimumGreenLevel;

    // Overall Measures
    @SerializedName("grd")
    public Integer gridPower; // Grid consumption
    @SerializedName("gen")
    public Integer generatedPower; // Generated Watts
    @SerializedName("div")
    public Integer divertedPower; // Diversion amount Watts
    @SerializedName("che")
    public Double chargeAdded; // Charge added in KWh

    // Smart Boost
    @SerializedName("sbh")
    public Integer smartBoostHour;
    @SerializedName("sbm")
    public Integer smartBoostMinute;
    @SerializedName("sbk")
    public Double smartBoostCharge;
    @SerializedName("bss")
    public int smartBoost;

    public boolean getSmartBoost() {
        return (smartBoost != 0);
    }

    // Timed Boost Removed Feb 22
    @SerializedName("tbh")
    public Integer timedBoostHour;
    @SerializedName("tbm")
    public Integer timedBoostMinute;

    // tbk is only not null during manual boost
    @SerializedName("tbk")
    public Double manualBoostCharge; // planned boost Energy for manual boost - Note charge remaining for boost =
                                     // tbk-che

    @SerializedName("bsm")
    public int manualBoostInt; // 1 manual Boost

    public boolean getManualBoost() {
        return (manualBoostInt != 0);
    }; // 1 manual Boost

    @SerializedName("bst")
    public Integer bst;

    @SerializedName("tz")
    public Integer tz;
    @SerializedName("zs")
    public Integer zs;

    // CT Clamps
    @SerializedName("ectt4")
    public String clampName1;
    @SerializedName("ectt5")
    public String clampName2;
    @SerializedName("ectt6")
    public String clampName3;

    @SerializedName("ectp4")
    public Integer clampPower1; // in Watts
    @SerializedName("ectp5")
    public Integer clampPower2;
    @SerializedName("ectp6")
    public Integer clampPower3;

    public ZappiSummary(long serialNumber) {
        super(serialNumber);
    }

    @Override
    public String toString() {
        return "ZappiSummary [serialNumber=" + serialNumber + ", dat=" + dat + ", tim=" + tim + ", dst=" + dst
                + ", supplyVoltage=" + supplyVoltageInTenthVolt + ", supplyFrequency=" + supplyFrequency
                + ", numberOfPhases=" + numberOfPhases + ", lockingMode=" + lockingMode + ", chargingMode="
                + chargingMode + ", status=" + status + ", plugStatus=" + plugStatus + ", commandTries=" + commandTries
                + ", diverterPriority=" + diverterPriority + ", minimumGreenLevel=" + minimumGreenLevel + ", gridPower="
                + gridPower + ", generatedPower=" + generatedPower + ",  divertedPower=" + divertedPower
                + ", chargeAdded=" + chargeAdded + ", smartBoostHour=" + smartBoostHour + ", smartBoostMinute="
                + smartBoostMinute + ", smartBoostCharge=" + smartBoostCharge + ", timedBoostHour=" + timedBoostHour
                + ", timedBoostMinute=" + timedBoostMinute + ", timedBoostCharge=" + manualBoostCharge + ", clampName1="
                + clampName1 + ", clampName2=" + clampName2 + ", clampName3=" + clampName3 + ", clampPower1="
                + clampPower1 + ", clampPower2=" + clampPower2 + ", clampPower3=" + clampPower3 + ", firmwareVersion="
                + firmwareVersion + "]";
    }

    public void toLogger() {
        logger.info("ZappiSummary:");
        logger.info("serialNumber={}", serialNumber);
        logger.info("date/time={} {}, dst={}", dat, tim, dst);
        logger.info("supplyVoltage={}, supplyFrequency={}, numberOfPhases={}", supplyVoltageInTenthVolt,
                supplyFrequency, numberOfPhases);
        logger.info("lockingMode={}", lockingMode);
        logger.info("chargingMode={}", chargingMode);
        logger.info("status={}", status);
        logger.info("plugStatus={}", plugStatus);
        logger.info("commandTries={}", commandTries);
        logger.info("diverterPriority={}", diverterPriority);
        logger.info("minimumGreenLevel={}", minimumGreenLevel);
        logger.info("gridPower={}, generatedPower={}, divertedPower={}", gridPower, generatedPower, divertedPower);
        logger.info("chargeAdded={}", chargeAdded);
        logger.info("smartBoostTime={}:{}, Charge={}", smartBoostHour, smartBoostMinute, smartBoostCharge);
        logger.info("manualBoost={}", getManualBoost());
        logger.info("timedBoostTime={}:{}, Charge={}", timedBoostHour, timedBoostMinute, manualBoostCharge);
        logger.info("clamp1={}, power={}", clampName1, clampPower1);
        logger.info("clamp2={}, power={}", clampName2, clampPower2);
        logger.info("clamp3={}, power={}", clampName3, clampPower3);
        logger.info("firmwareVersion={}", firmwareVersion);
    }
}
