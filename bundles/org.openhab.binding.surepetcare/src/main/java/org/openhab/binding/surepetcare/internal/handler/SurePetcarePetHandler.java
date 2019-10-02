/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.surepetcare.internal.handler;

import static org.openhab.binding.surepetcare.internal.SurePetcareConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.surepetcare.internal.SurePetcareAPIHelper;
import org.openhab.binding.surepetcare.internal.SurePetcareApiException;
import org.openhab.binding.surepetcare.internal.data.SurePetcarePet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SurePetcarePetHandler} is responsible for handling the things created to represent Sure Petcare pets.
 *
 * @author Rene Scherer - Initial Contribution
 */
@NonNullByDefault
public class SurePetcarePetHandler extends SurePetcareBaseObjectHandler {

    private final Logger logger = LoggerFactory.getLogger(SurePetcarePetHandler.class);

    public SurePetcarePetHandler(Thing thing, SurePetcareAPIHelper petcareAPI) {
        super(thing, petcareAPI);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("DeviceHandler handleCommand called with command: {}", command.toString());

        if (command instanceof RefreshType) {
            updateThing();
        } else {
            switch (channelUID.getId()) {
                case PET_CHANNEL_LOCATION:
                    String location = command.toFullString();
                    logger.debug("received location update command: {}", location);
                    if (command instanceof DecimalType) {
                        // binding specific logic goes here
                        // updateDeviceState(deviceSwitchState);
                        SurePetcarePet pet = petcareAPI.retrievePet(thing.getUID().getId());
                        Integer newLocationId = ((DecimalType) command).intValue();
                        logger.debug("received new location: {}", newLocationId);
                        if (pet != null) {
                            try {
                                petcareAPI.setPetLocation(pet, newLocationId);
                                updateState(PET_CHANNEL_LOCATION, new StringType(pet.getLocation().getWhere().toString()));
                                updateState(PET_CHANNEL_LOCATION_CHANGED,
                                        new DateTimeType(pet.getLocation().getLocationChanged()));
                            } catch (SurePetcareApiException e) {
                                logger.warn("Error from SurePetcare API. Can't update location {} for pet {}",
                                        newLocationId, pet.toString());
                            }
                        }
                    }
                    break;
                default:
                    logger.warn("Update on unsupported channel {}", channelUID.getId());
            }
        }
    }

    @Override
    public void updateThing() {
        SurePetcarePet pet = petcareAPI.retrievePet(thing.getUID().getId());
        if (pet != null) {
            logger.debug("Updating all thing channels for pet : {}", pet.toString());
            updateState(PET_CHANNEL_ID, new DecimalType(pet.getId()));
            updateState(PET_CHANNEL_NAME, new StringType(pet.getName()));
            updateState(PET_CHANNEL_COMMENT, new StringType(pet.getComments()));
            updateState(PET_CHANNEL_GENDER, new StringType(pet.getGenderId().toString()));
            updateState(PET_CHANNEL_BREED, new StringType(pet.getBreedId().toString()));
            updateState(PET_CHANNEL_SPECIES, new StringType(pet.getSpeciesId().toString()));
            updateState(PET_CHANNEL_PHOTO_URL, new StringType(pet.getPhoto().getLocation()));
            updateState(PET_CHANNEL_LOCATION, new StringType(pet.getLocation().getWhere().toString()));
            updateState(PET_CHANNEL_LOCATION_CHANGED, new DateTimeType(pet.getLocation().getLocationChanged()));
            // updateState(PET_CHANNEL_BIRTHDAY, new DateTimeType(pet.getBirthday()));
            updateState(PET_CHANNEL_WEIGHT, new StringType(pet.getWeight()));
            updateState(PET_CHANNEL_TAG_IDENTIFIER, new StringType(pet.getTagIdentifier().getTag()));
        }
    }

    public void updatePetLocation() {
        SurePetcarePet pet = petcareAPI.retrievePet(thing.getUID().getId());
        if (pet != null) {
            updateState(PET_CHANNEL_LOCATION, new StringType(pet.getLocation().getWhere().toString()));
            updateState(PET_CHANNEL_LOCATION_CHANGED, new DateTimeType(pet.getLocation().getLocationChanged()));
        }
    }
}
