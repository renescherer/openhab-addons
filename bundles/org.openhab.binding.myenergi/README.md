# myenergi Binding

This binding allows openHAB to communicate with the public API from myenergi Ltd. (https://myenergi.info), a manufacturer of electric vehicle chargers (Zappi) and solar energy diverters (Eddi):

- Retrieval of current energy readings (grid, solar, EV charger)
- Control charging status and boost modes for Zappi

So far, this binding has only been tested with a Harvi and a Zappi. I would welcome testers who have an Eddi installed.

## Supported Things

This binding supports the following thing types

| Thing  | Thing Type | Discovery | Description                                    |
|--------|------------|-----------|------------------------------------------------|
| bridge | Bridge     | Manual    |  A single connection to the myenergi API |
| eddi   | Thing      | Automatic |  A solar energy diverter                 |
| zappi  | Thing      | Automatic |  An EV Charger (EVSE)                    |
| harvi  | Thing      | Automatic |  A remote power clamp reader             |


## Discovery

Once the bridge is configured with myenergi username and password, the various devices will be discovered automatically and added to the Inbox.
 
## Thing Configuration

#### Manual configuration

For the identifier of the devices, the corresponding serial number is used.

```
Bridge myenergi:bridge:api "MyEnergi API Bridge" [ username="<my username>", password="<my password>", refreshInterval=24 ] {
  Thing zappi 21287642 "MyEnergi Zappi" [ refreshInterval=30 ]
  Thing harvi 87263212 "MyEnergi Harvi" [ refreshInterval=30 ]
}
```

## Channels

The following channels are defined. 

####  Bridge

none

####  Zappi

The Zappi channels are divided into  some channel groups.

|Group| Description|
|-----|------------|
|Device|Contains all settings specific to this zappi device|
|Zappi CT|Contains the channels for reading the CT attached to this Zappi|
|Manual Boost|Contains the channels to configure a manual boost|
|Smart Boost Slot|Contains the channels to configure a smart boost|
|Timed Boost Slot|Contains the channels to configure a timed boost. There are 4 slots available|

| channel         | type          | description                                                     |read only|
|-----------------|---------------|-----------------------------------------------------------------|--------|
|<strong>Device</strong>|||
| lastUpdatedTime|DateTime|Last Updated Time<br>The time when the device was last updated|X|
| numberOfPhases|Number|Number of Phases<br>The number of phases in the installation|X|
| lockingMode|Number|Locking Mode<br>The current locking mode of the device as bitmap.<br>Bit 0: Locked Now,<br>Bit 1: Lock when plugged in,<br>Bit 2: Lock when unplugged,<br>Bit 3: Charge when locked,<br>Bit 4: Charge Session Allowed (Even if locked)|X|
| chargingMode|String|Charging Mode<br>The current charging mode of the device (One of 0=Boost, 1=Fast, 2=Eco, 3=Eco+ or 4=Stop)||
| status|String|Status<br>The current status of the device (E.g. Starting, Waiting for Export ...)|X|
| plugStatus|String|Plug Status<br>The current plug status of the device (E.g. Disconnected. Awaiting Surplus, Charge Complete ...)|X|
| commandTries|Number|Command Status/Retries<br>0-10 Trying, 253 Acked and Failed, 254 Acked and OK, 255 No command has ever been sent|X|
| diverterPriority|Number|Diverter Priority<br>The diverter priority|X|
| minimumGreenLevel|Number|Minimum Green Level<br>The minimum percentage of green energy to allow charging||
| supplyVoltage|Number:ElectricPotential|Voltage<br>Supply voltage to the unit|X|
| supplyFrequency|Number:Frequency|Frequency<br>Grid frequency|X|
| gridPower|Number:Power|Imported or Exported Grid Power<br>|X|
| generatedPower|Number:Power|Generated Power(Solar or Wind)<br>|X|
| divertedPower|Number:Power|Diverted Power<br>|X|
| consumedPower|Number:Power|Consumed Power<br>|X|
| chargeAdded|Number:Power|Charge Added<br>|X|
|<strong>Zappi CT</strong>|||
| clampName1|String|CT Type L1<br>|X|
| clampName2|String|CT Type L2<br>|X|
| clampName3|String|CT Type L3<br>|X|
| clampPower1|Number:Power|CT Power L1<br>The measured power at the CT L1|X|
| clampPower2|Number:Power|CT Power L1<br>The measured power at the CT L1|X|
| clampPower3|Number:Power|CT Power L1<br>The measured power at the CT L1|X|
|<strong>Manual Boost</strong>|||
| boostStatus|Switch|Boost Status<br>Is ON if boost is active. Switch it to ON to start a new manual boost.||
| manualBoostCharge|Number:Energy|Manual Boost Charge<br>Energy supplied to the EV during current charge session|X|
| newManualBoostCharge|Number:Energy|Energy to be supplied to the EV in the next charge session||
| stopAll|Switch|Stop all Boosts<br>||
|<strong>Smart Boost Slot</strong>|||
| boostStatus|Switch|Boost Status<br>Is ON if boost is active. Switch it to ON to start a new manual boost.||
| endTimeHour|Number|Previous Ending Time (Hour)<br>||
| endTimeMinute|Number|Previous Ending Time (Minute)<br>||
| smartBoostCharge|Number:Energy|Previous Energy to Charge<br>Energy supplied to the EV during current charge session|X|
| newSmartBoostCharge|Number:Energy|Next Energy to Charge<br>Energy supplied to the EV during current charge session||
| newEndTimeHour|Number|Next Ending Time (Hour)<br>||
| newEndTimeMinute|Number|Next Ending Time (Minute)<br>||
| stopAll|Switch|Stop all Boosts<br>||
|<strong>Timed Boost Slot</strong>|||
| monday|Switch|Monday<br>||
| tuesday|Switch|Tuesday<br>||
| wednesday|Switch|Wednesday<br>||
| thursday|Switch|Thursday<br>||
| friday|Switch|Friday<br>||
| saturday|Switch|Saturday<br>||
| sunday|Switch|Sunday<br>||
| startHour|Number|Start Hour<br>||
| startMinute|Number|Start Minute<br>||
| duration|Number|Duration in Hours<br>E.g. 1.5. Maximum duration is 10h||

##### Zappi Items

The read only items don't need to be changed when importing the channels from the thing.
The writable number items must be changed as follows:

| Channel | Item Type(Metadata)| Min/Max/Steps|
|---------|----------|--------------|
|writable charge values | Knob (cell)| Min=0, Max=100, Step=1|
|writable hour values| Knob (cell)|Min=0, Max=23, Step=1|
|writable minute values|Knob (cell)|Min=0, Max=45, Step=15|
|Minimum Green Level|Knob (cell) or Slider|Min=0, Max=100, Step=1|
|writable Duration value|Knob (cell)|Min=0, Max=8, Step=0.25|


####  Harvi

| channel         | type          | description                                                     |
|-----------------|---------------|-----------------------------------------------------------------|
| lastUpdatedTime | DateTime      | The time the readings have last been updated.                   |
| clampName1      | String        | A name for the clamp (set through app, e.g. "Solar Generation". |
| clampPower1     | Number:Energy | The amount of energy measured by the clamp.                     |
| clampPhase1     | Number        | The identifier of the phase (for 3-phase installations).        |


## Actions

tbd

####  Electricity Meter Point

tbd

## Examples

myenergi.things

```
Bridge myenergi:bridge:api "MyEnergi API Bridge" [ username="<my username>", password="<my password>", refreshInterval=24 ] {
  Thing zappi 21287642 "MyEnergi Zappi" [ refreshInterval=30 ]
  Thing harvi 87263212 "MyEnergi Harvi" [ refreshInterval=30 ]
}
```

myenergi.items

```
Group                    dgMyEnergi [ "Equipment" ]

DateTime                 Zappi_LastUpdatedTime    "MyEnergi Zappi Last Updated Time [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]" <time>    (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:lastUpdatedTime" }
Number:ElectricPotential Zappi_SupplyVoltage      "MyEnergi Zappi Supply Voltage [%.1f %unit%]"                            <energy>  (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:supplyVoltage" }
Number:Frequency         Zappi_SupplyFrequency    "MyEnergi Zappi Supply Frequency [%.1f %unit%]"                          <energy>  (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:supplyFrequency" }
Number                   Zappi_NumberOfPhases     "MyEnergi Zappi Number of Phases [%d]"                                   <energy>  (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:numberOfPhases" }
Number                   Zappi_LockingMode        "MyEnergi Zappi Locking Mode [%d]"                                                 (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:lockingMode" }
String                   Zappi_ChargingMode       "MyEnergi Zappi Charging Mode [%s]"                                                (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:chargingMode" }
String                   Zappi_Status             "MyEnergi Zappi Status [%s]"                                                       (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:status" }
String                   Zappi_PlugStatus         "MyEnergi Zappi Plug Status [%s]"                                                  (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:plugStatus" }
Number                   Zappi_CommandTries       "MyEnergi Zappi Command Tries [%d]"                                                (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:commandTries" }
Number                   Zappi_DiverterPriority   "MyEnergi Zappi Diverter Priority [%d]"                                            (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:diverterPriority" }
Number                   Zappi_MinimumGreenLevel  "MyEnergi Zappi Minimum Green Level [%d %%]"                                       (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:minimumGreenLevel" }
Number:Power             Zappi_GridPower          "MyEnergi Zappi Grid Power [%.1f %unit%]"                                <energy>  (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:gridPower" }
Number:Power             Zappi_GeneratedPower     "MyEnergi Zappi Generated Power [%.1f %unit%]"                           <energy>  (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:generatedPower" }
Number:Power             Zappi_DivertedPower      "MyEnergi Zappi Diverted Power [%.1f %unit%]"                            <energy>  (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:divertedPower" }
Number:Energy            Zappi_ChargeAdded        "MyEnergi Zappi Charge Added [%.1f %unit%]"                              <energy>  (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:chargeAdded" }
String                   Zappi_SmartBoostTime     "MyEnergi Zappi Smart Boost Time [%s]"                                   <time>    (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:smartBoostTime" }
Number:Energy            Zappi_SmartBoostCharge   "MyEnergi Zappi Smart Boost Charge [%.1f %unit%]"                        <energy>  (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:smartBoostCharge" }
String                   Zappi_TimedBoostTime     "MyEnergi Zappi Timed Boost Time [%s]"                                   <time>    (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:timedBoostTime" }
Number:Energy            Zappi_TimedBoostCharge   "MyEnergi Zappi Timed Boost Charge [%.1f %unit%]"                        <energy>  (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:timedBoostCharge" }
String                   Zappi_ClampName1         "MyEnergi Zappi Clamp Name 1 [%s]"                                                 (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:clampName1" }
Number:Power             Zappi_ClampPower1        "MyEnergi Zappi Clamp Power 1 [%.1f %unit%]"                             <energy>  (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:clampPower1" }
String                   Zappi_ClampName2         "MyEnergi Zappi Clamp Name 2 [%s]"                                                 (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:clampName2" }
Number:Power             Zappi_ClampPower2        "MyEnergi Zappi Clamp Power 2 [%.1f %unit%]"                             <energy>  (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:clampPower2" }
String                   Zappi_ClampName3         "MyEnergi Zappi Clamp Name 3 [%s]"                                                 (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:clampName3" }
Number:Power             Zappi_ClampPower3        "MyEnergi Zappi Clamp Power 3 [%.1f %unit%]"                             <energy>  (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:clampPower3" }
String                   Zappi_ClampName4         "MyEnergi Zappi Clamp Name 4 [%s]"                                                 (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:clampName4" }
Number:Power             Zappi_ClampPower4        "MyEnergi Zappi Clamp Power 4 [%.1f %unit%]"                             <energy>  (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:clampPower4" }
String                   Zappi_ClampName5         "MyEnergi Zappi Clamp Name 5 [%s]"                                                 (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:clampName5" }
Number:Power             Zappi_ClampPower5        "MyEnergi Zappi Clamp Power 5 [%.1f %unit%]"                             <energy>  (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:clampPower5" }
String                   Zappi_ClampName6         "MyEnergi Zappi Clamp Name 6 [%s]"                                                 (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:clampName6" }
Number:Power             Zappi_ClampPower6        "MyEnergi Zappi Clamp Power 6 [%.1f %unit%]"                             <energy>  (dgMyEnergi)  { channel="myenergi:zappi:api:21287642:clampPower6" }

DateTime                 Harvi_LastUpdatedTime    "MyEnergi Harvi Last Updated Time [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]" <time>    (dgMyEnergi)  { channel="myenergi:harvi:api:87263212:lastUpdatedTime" }
String                   Harvi_ClampName1         "MyEnergi Harvi Clamp Name 1 [%s]"                                                 (dgMyEnergi)  { channel="myenergi:harvi:api:87263212:clampName1" }
Number:Power             Harvi_ClampPower1        "MyEnergi Harvi Clamp Power 1 [%.1f %unit%]"                             <energy>  (dgMyEnergi)  { channel="myenergi:harvi:api:87263212:clampPower1" }
Number                   Harvi_ClampPhase1        "MyEnergi Harvi Clamp Phase 1 [%d]"                                      <energy>  (dgMyEnergi)  { channel="myenergi:harvi:api:87263212:clampPhase1" }
String                   Harvi_ClampName2         "MyEnergi Harvi Clamp Name 2 [%s]"                                                 (dgMyEnergi)  { channel="myenergi:harvi:api:87263212:clampName2" }
Number:Power             Harvi_ClampPower2        "MyEnergi Harvi Clamp Power 2 [%.1f %unit%]"                             <energy>  (dgMyEnergi)  { channel="myenergi:harvi:api:87263212:clampPower2" }
Number                   Harvi_ClampPhase2        "MyEnergi Harvi Clamp Phase 2 [%d]"                                      <energy>  (dgMyEnergi)  { channel="myenergi:harvi:api:87263212:clampPhase2" }
String                   Harvi_ClampName3         "MyEnergi Harvi Clamp Name 3 [%s]"                                                 (dgMyEnergi)  { channel="myenergi:harvi:api:87263212:clampName3" }
Number:Power             Harvi_ClampPower3        "MyEnergi Harvi Clamp Power 3 [%.1f %unit%]"                             <energy>  (dgMyEnergi)  { channel="myenergi:harvi:api:87263212:clampPower3" }
Number                   Harvi_ClampPhase3        "MyEnergi Harvi Clamp Phase 3 [%d]"                                      <energy>  (dgMyEnergi)  { channel="myenergi:harvi:api:87263212:clampPhase3" }

```
