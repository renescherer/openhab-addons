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

The following channels are defined. Except for the bridge refresh channel, all are read-only.

####  Bridge

none

####  Zappi

| channel         | type          | description                                                     |
|-----------------|---------------|-----------------------------------------------------------------|
| lastUpdatedTime | DateTime      | The time the readings have last been updated.                   |
| supplyVoltage      | String        | A name for the clamp (set through app, e.g. "Solar Generation". |
| supplyFrequency     | Number:Energy | The amount of energy measured by the clamp.                     |
| numberOfPhases     | Number        | The identifier of the phase (for 3-phase installations).        |
| lockingMode     | Number        | The identifier of the phase (for 3-phase installations).        |
| chargingMode     | Number        | The identifier of the phase (for 3-phase installations).        |
| status     | Number        | The identifier of the phase (for 3-phase installations).        |
| plugStatus     | Number        | The identifier of the phase (for 3-phase installations).        |
| commandTries     | Number        | The identifier of the phase (for 3-phase installations).        |
| diverterPriority     | Number        | The identifier of the phase (for 3-phase installations).        |
| minimumGreenLevel     | Number        | The identifier of the phase (for 3-phase installations).        |
| gridPower     | Number        | The identifier of the phase (for 3-phase installations).        |
| generatedPower     | Number        | The identifier of the phase (for 3-phase installations).        |
| divertedPower     | Number        | The identifier of the phase (for 3-phase installations).        |
| chargeAdded     | Number        | The identifier of the phase (for 3-phase installations).        |
| smartBoostTime     | Number        | The identifier of the phase (for 3-phase installations).        |
| smartBoostCharge     | Number        | The identifier of the phase (for 3-phase installations).        |
| timedBoostTime     | Number        | The identifier of the phase (for 3-phase installations).        |
| timedBoostCharge     | Number        | The identifier of the phase (for 3-phase installations).        |
| clampName1      | String        | A name for the clamp (set through app, e.g. "Solar Generation". |
| clampPower1     | Number:Energy | The amount of energy measured by the clamp.                     |
| clampName2      | String        | A name for the clamp (set through app, e.g. "Solar Generation". |
| clampPower2     | Number:Energy | The amount of energy measured by the clamp.                     |
| clampName3      | String        | A name for the clamp (set through app, e.g. "Solar Generation". |
| clampPower3     | Number:Energy | The amount of energy measured by the clamp.                     |
| clampName4      | String        | A name for the clamp (set through app, e.g. "Solar Generation". |
| clampPower4     | Number:Energy | The amount of energy measured by the clamp.                     |
| clampName5      | String        | A name for the clamp (set through app, e.g. "Solar Generation". |
| clampPower5     | Number:Energy | The amount of energy measured by the clamp.                     |
| clampName6      | String        | A name for the clamp (set through app, e.g. "Solar Generation". |
| clampPower6     | Number:Energy | The amount of energy measured by the clamp.                     |

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
