#pragma once
#include <Arduino.h>
struct Timings {
    const uint16_t BOUNCE_DELAY;
    const uint16_t REPEATER_DELAY; // millis to discover double click
    const uint16_t FRONT_CAM_SHOWTIME_DELAY; //millis to show front cam after signal gone off
    const uint16_t REAR_CAM_SHOWTIME_DELAY;
};