#pragma once

#include <Arduino.h>

struct Timings {
     int BOUNCE_DELAY=0;
     int REPEATER_DELAY=0; // millis to discover double click
     int FRONT_CAM_SHOWTIME_DELAY=0; //millis to show front cam after signal gone off
     int REAR_CAM_SHOWTIME_DELAY=0;
     byte crc=0;
      Timings(){}
      Timings(int bounce,
              int repeater,
              int font_delay,
              int rear_delay)
              {
          BOUNCE_DELAY =bounce;
          REPEATER_DELAY = repeater;
          FRONT_CAM_SHOWTIME_DELAY = font_delay;
          REAR_CAM_SHOWTIME_DELAY = rear_delay;
      }
    const Timings operator=(const Timings &B) {
    if (this == &B) return *this;
    BOUNCE_DELAY = B.BOUNCE_DELAY;
    REPEATER_DELAY = B.REPEATER_DELAY;
    FRONT_CAM_SHOWTIME_DELAY = B.FRONT_CAM_SHOWTIME_DELAY;
    REAR_CAM_SHOWTIME_DELAY = B.REAR_CAM_SHOWTIME_DELAY;
    crc=B.crc;
    return *this;
}
bool operator == (const Timings& b) {
  if (BOUNCE_DELAY==b.BOUNCE_DELAY &&
  REPEATER_DELAY==b.REPEATER_DELAY&&
  FRONT_CAM_SHOWTIME_DELAY==b.FRONT_CAM_SHOWTIME_DELAY&&
  REAR_CAM_SHOWTIME_DELAY==b.REAR_CAM_SHOWTIME_DELAY)
  return true;
  else return false;
  }
};
