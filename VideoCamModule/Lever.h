#pragma once

#include "Timings.h"
#include <Arduino.h>

#ifndef LEVER_H
#define LEVER_H

class Lever {
public:
    Lever(int pinNumber, Timings timings, bool isLowLevelToTurnOn = false);

    bool isOn();

    bool isDoubleClicked();

    unsigned long getLastTimeTurnedOff();

    void checkState();

private:
    Timings *timings;
    bool state = false;
    bool doubleClicked = false;
    int pinNumber;
    unsigned long lastTimeChanged = 0;
    unsigned long lastTimeTurnedOn = 0;
    unsigned long lastTimeTurnedOff = 0;
    bool isLowLevelToTurnOn;

    bool isDoubleClicking(unsigned long timeStamp);
};

#endif