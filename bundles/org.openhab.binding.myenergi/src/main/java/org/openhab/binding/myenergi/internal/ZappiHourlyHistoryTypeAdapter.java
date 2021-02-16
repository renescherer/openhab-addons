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

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.openhab.binding.myenergi.internal.dto.ZappiHourlyHistory;
import org.openhab.binding.myenergi.internal.dto.ZappiHourlyHistoryEntry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * @author scherer
 *
 */
public class ZappiHourlyHistoryTypeAdapter implements JsonDeserializer<ZappiHourlyHistory> {

    /*
     * {"U17028110":[{"dow":"Sat","dom":28,"mon":11,"yr":2020,"imp":1691700},
     * {"hr":1,"dow":"Sat","dom":28,"mon":11,"yr":2020,"imp":1652160}]}
     */
    @Override
    public ZappiHourlyHistory deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        ZappiHourlyHistory history = new ZappiHourlyHistory();

        JsonObject parentJsonObject = element.getAsJsonObject();
        Map.Entry<String, JsonElement> field = parentJsonObject.entrySet().iterator().next();
        history.id = field.getKey();
        Type listType = new TypeToken<List<ZappiHourlyHistoryEntry>>() {
        }.getType();
        history.entries = MyEnergiBindingConstants.GSON.fromJson(field.getValue(), listType);
        return history;
    }
}
