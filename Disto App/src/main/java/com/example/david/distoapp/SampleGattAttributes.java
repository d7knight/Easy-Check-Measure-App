/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.david.distoapp;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String DISTO_SERVICE = "3ab10100-f831-4395-b29d-570977d5bf94";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String DISTO_CHARACTERISTIC_DISTANCE ="3ab10101-f831-4395-b29d-570977d5bf94";
    public static String DISTO_CHARACTERISTIC_DISTANCE_DISPLAY_UNIT = "3ab10102-f831-4395-b29d-570977d5bf94";
    public static String DISTO_CHARACTERISTIC_INCLINATION = "3ab10103-f831-4395-b29d-570977d5bf94";
    public static String DISTO_CHARACTERISTIC_INCLINATION_DISPLAY_UNIT = "3ab10104-f831-4395-b29d-570977d5bf94";
    public static String DISTO_CHARACTERISTIC_GEOGRAPHIC_DIRECTION = "3ab10105-f831-4395-b29d-570977d5bf94";
    public static String DISTO_CHARACTERISTIC_GEOGRAPHIC_DIRECTION_DISPLAY_UNIT ="3ab10106-f831-4395-b29d-570977d5bf94";
    public static String DISTO_CHARACTERISTIC_HORIZONTAL_INCLINE = "3ab10107-f831-4395-b29d-570977d5bf94";
    public static String DISTO_CHARACTERISTIC_VERTICAL_INCLINE = "3ab10108-f831-4395-b29d-570977d5bf94";
    public static String DISTO_CHARACTERISTIC_COMMAND = "3ab10109-f831-4395-b29d-570977d5bf94";
    public static String DISTO_CHARACTERISTIC_STATE_RESPONSE = "3ab1010A-f831-4395-b29d-570977d5bf94";
    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        // Disto Characteristics

        attributes.put(DISTO_SERVICE, "Disto Service");
        attributes.put(DISTO_CHARACTERISTIC_DISTANCE, "Distance Measurement");
        attributes.put(DISTO_CHARACTERISTIC_DISTANCE_DISPLAY_UNIT,"Display Unit");
        attributes.put(DISTO_CHARACTERISTIC_INCLINATION,"Inclination");
        attributes.put(DISTO_CHARACTERISTIC_INCLINATION_DISPLAY_UNIT,"Inclination Unit");
        attributes.put(DISTO_CHARACTERISTIC_GEOGRAPHIC_DIRECTION,"Geographic Direction");
        attributes.put(DISTO_CHARACTERISTIC_GEOGRAPHIC_DIRECTION_DISPLAY_UNIT,"Geographic Direction Display Unit");
        attributes.put(DISTO_CHARACTERISTIC_HORIZONTAL_INCLINE,"Horizontal Incline");
        attributes.put(DISTO_CHARACTERISTIC_VERTICAL_INCLINE,"Vertical Incline");
        attributes.put(DISTO_CHARACTERISTIC_COMMAND,"Command");
        attributes.put(DISTO_CHARACTERISTIC_STATE_RESPONSE ,"State Response");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
