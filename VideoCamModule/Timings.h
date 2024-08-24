#pragma once

#include <Arduino.h>

struct Timings {
    const int BOUNCE_DELAY;
    const int REPEATER_DELAY; // millis to discover double click
    const int FRONT_CAM_SHOWTIME_DELAY; //millis to show front cam after signal gone off
    const int REAR_CAM_SHOWTIME_DELAY;
};
