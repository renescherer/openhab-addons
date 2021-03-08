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
tbd
```
