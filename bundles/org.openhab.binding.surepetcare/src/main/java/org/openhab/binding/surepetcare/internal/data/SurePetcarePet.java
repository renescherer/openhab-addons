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
package org.openhab.binding.surepetcare.internal.data;

import java.util.Arrays;
import java.util.Map;
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
//import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
//import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link SurePetcarePet} is a DTO class used to represent a pet. It's used to deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcarePet extends SurePetcareBaseObject {

    // {
    // "id":34675,
    // "name":"Cat",
    // "gender":0,
    // "comments":"",
    // "household_id":87435,
    // "breed_id":382,
    // "photo_id":23412,
    // "species_id":1,
    // "tag_id":234523,
    // "version":"MQ==",
    // "created_at":"2019-09-02T09:27:17+00:00",
    // "updated_at":"2019-09-02T09:31:08+00:00",
    // "photo":{
    // "id":23412,
    // "location":"https:\/\/surehub.s3.amazonaws.com\/user-photos\/thm\/56231\/z70LUtqLKJGKJHgjkGLyhuiykfKJhgkjghfptCgeZU.jpg",
    // "uploading_user_id":52815,
    // "version":"MA==",
    // "created_at":"2019-09-02T09:31:07+00:00",
    // "updated_at":"2019-09-02T09:31:07+00:00"
    // },
    // "position":{
    // "tag_id":234523,
    // "device_id":876348,
    // "where":2,
    // "since":"2019-09-11T09:24:13+00:00"
    // },
    // "status":{
    // "activity":{
    // "tag_id":234523,
    // "device_id":318966,
    // "where":2,
    // "since":"2019-09-11T09:24:13+00:00"
    // }
    // }
    // }

    public enum PetGender {

        UNKNONWN(-1, "@text/unknown"),
        FEMALE(0, "@text/genderFemale"),
        MALE(1, "@text/genderMale");

        private final Integer genderId;
        private final String name;

        private PetGender(int locationId, String name) {
            this.genderId = locationId;
            this.name = name;
        }

        public Integer getGenderId() {
            return genderId;
        }

        public String getName() {
            return name;
        }

        public static PetGender findByTypeId(final int genderId) {
            return Arrays.stream(values()).filter(value -> value.genderId.equals(genderId)).findFirst()
                    .orElse(UNKNONWN);
        }
    }

    public enum PetSpecies {

        UNKNONWN(0, "@text/unknown"),
        CAT(1, "@text/speciesCat"),
        DOG(2, "@text/speciesDog");

        private final Integer speciesId;
        private final String name;

        private PetSpecies(int speciesId, String name) {
            this.speciesId = speciesId;
            this.name = name;
        }

        public Integer getGenderId() {
            return speciesId;
        }

        public String getName() {
            return name;
        }

        public static PetSpecies findByTypeId(final int speciesId) {
            return Arrays.stream(values()).filter(value -> value.speciesId.equals(speciesId)).findFirst()
                    .orElse(UNKNONWN);
        }
    }

    private String name = "";
    private Integer gender = 0;
    //private Date dateOfBirth;
    private String weight = "";
    private String comments = "";
    private Integer householdId = 0;
    private Integer breedId = 0;
    private Integer photoId = 0;
    private Integer speciesId = 0;
    private Integer tagId = 0;
    private SurePetcarePhoto photo = new SurePetcarePhoto();
    private SurePetcarePetLocation position = new SurePetcarePetLocation();
    private SurePetcareTag tagIdentifier = new SurePetcareTag();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    /*public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }*/

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Integer getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(Integer householdId) {
        this.householdId = householdId;
    }

    public Integer getBreedId() {
        return breedId;
    }

    public void setBreedId(Integer breedId) {
        this.breedId = breedId;
    }

    public Integer getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Integer photoId) {
        this.photoId = photoId;
    }

    public Integer getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(Integer speciesId) {
        this.speciesId = speciesId;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public SurePetcarePhoto getPhoto() {
        return photo;
    }

    public void setPhoto(SurePetcarePhoto photo) {
        this.photo = photo;
    }

    public SurePetcarePetLocation getLocation() {
        return position;
    }

    public void setPosition(SurePetcarePetLocation position) {
        this.position = position;
    }

    public SurePetcareTag getTagIdentifier() {
        return tagIdentifier;
    }

    public void setTagIdentifier(SurePetcareTag tagIdentifier) {
        this.tagIdentifier = tagIdentifier;
    }

    public String getGenderName() {
        return PetGender.findByTypeId(gender).getName();
    }

    public String getSpeciesName() {
        return PetSpecies.findByTypeId(speciesId).getName();
    }

    public String getBreedName() {
        return breedId.toString();
    }

    @Override
    public String toString() {
        return "Pet [id=" + id + ", name=" + name + "]";
    }

    @Override
    public Map<String, Object> getThingProperties() {
        Map<String, Object> properties = super.getThingProperties();
        properties.put("householdId", householdId.toString());
        return properties;
    }

    /*public @NonNull ZonedDateTime getBirthday() {
        return dateOfBirth.toInstant().atZone(ZoneId.systemDefault());
    }*/
}
