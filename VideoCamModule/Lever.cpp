#include "Timings.h"
#include "Lever.h"

Lever::Lever(int pinNumber, Timings *timings) {
    this->timings = timings;
    this->pinNumber = pinNumber;
    unsigned long nowStamp = millis();
    unsigned long lastTimeChanged = nowStamp;
    unsigned long lastTimeTurnedOn = nowStamp;
    unsigned long lastTimeTurnedOff = nowStamp;
}

Lever::Lever() {}

const Lever &Lever::operator=(const Lever &B) {
    if (this == &B) return *this;
    pinNumber = B.pinNumber;
    timings = B.timings;
    lastTimeChanged = B.lastTimeChanged;
    lastTimeTurnedOn = B.lastTimeTurnedOn;
    lastTimeTurnedOff = B.lastTimeTurnedOff;
    return *this;
}

bool Lever::isOn() {
    return state;
}

bool Lever::isDoubleClicked() {
    return doubleClicked;
}

bool Lever::isDoubleClicking(unsigned long timeStamp) {
    if (lastTimeTurnedOn + timings->REPEATER_DELAY > timeStamp &&
        lastTimeTurnedOn < lastTimeTurnedOff)
        return true;
    else
        return false;
}

unsigned long Lever::getLastTimeTurnedOff() {
    return lastTimeTurnedOff;
}

unsigned long Lever::getLastTimeTurnedOn() {
    return lastTimeTurnedOn;
}

void Lever::checkState() {
    const unsigned long timeStamp = millis();
    const bool stateStamp = digitalRead(pinNumber);
    if (state != stateStamp &&
        lastTimeChanged < timeStamp - timings->BOUNCE_DELAY) {
        state = stateStamp;
        lastTimeChanged = timeStamp;
        isChangedFlag = true;
    } else {
        if (state) lastTimeTurnedOn = timeStamp;
        else lastTimeTurnedOff = timeStamp;
    }
    if (lastTimeChanged == timeStamp) {// it`s time to set on/off stamp
        if (state) {
            doubleClicked = isDoubleClicking(timeStamp);
            lastTimeTurnedOn = timeStamp;
        } else {
            doubleClicked = false;
            lastTimeTurnedOff = timeStamp;
        }
    }
}