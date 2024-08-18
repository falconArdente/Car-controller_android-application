#include "Timings.h"
#include "Lever.h"

Lever::Lever(int pinNumber, Timings timings, bool isLowLevelToTurnOn = false) {
    this->timings = &timings;
    this->pinNumber = pinNumber;
    pinMode(pinNumber, INPUT);
    this->isLowLevelToTurnOn = isLowLevelToTurnOn;
}

bool Lever::isOn() {
    return state;
}

bool Lever::isDoubleClicked() {
    return doubleClicked;
}

unsigned long Lever::getLastTimeTurnedOff() {
    return lastTimeTurnedOff;
}

void Lever::checkState() {
    if (isLowLevelToTurnOn)state = !state;
    unsigned long timeStamp = millis();
    if (state != digitalRead(pinNumber) &&
        lastTimeChanged <= timeStamp - timings->BOUNCE_DELAY) {
        state = digitalRead(pinNumber);
        lastTimeChanged = timeStamp;
    }
    if (isLowLevelToTurnOn)state = !state;
    if (lastTimeChanged == timeStamp) {// is time to set on/off stamp
        if (state) {
            doubleClicked = isDoubleClicking(timeStamp);
            lastTimeTurnedOn = timeStamp;
        } else {
            lastTimeTurnedOff = timeStamp;
        }
    }
}

bool Lever::isDoubleClicking(unsigned long timeStamp) {
    if (lastTimeTurnedOn + timings->REPEATER_DELAY < timeStamp &&
        lastTimeTurnedOn < lastTimeTurnedOff)
        return true;
    else
        return false;
}