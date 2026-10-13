package com.tradejapan.part2.config;

import java.util.List;

/**
 * The fixed list of 28 vehicle options (shown as ON / OFF on the vehicle page and
 * as toggle switches in the admin Edit Vehicle > Features tab). The order here is the display order.
 */
public final class FeatureCatalog {

    private FeatureCatalog() {}

    public static final List<String> NAMES = List.of(
            "Airbag", "Anti-Lock Brakes", "Power Steering", "A/C",
            "Remote Keyless Entry", "Navigation System", "Cruise Control", "Digital Meter",
            "Tilt Wheel", "CD Player", "AM/FM Radio", "DVD",
            "Power Windows", "Rear Window Defroster", "Tinted Glass", "Rear Window Wiper",
            "Power Door Locks", "Alloy Wheels", "Power Mirrors", "Sunroof",
            "Third Row Seats", "Power Slide Door", "Leather Seats", "Power Seats",
            "TV", "Fog Light", "Back Camera", "Push Start");
}
