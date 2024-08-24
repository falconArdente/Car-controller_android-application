#include "Timings.h"
#include "Lever.h"

Lever::Lever(int pinNumber, Timings appTimings, bool isLowLevelToTurnOn = false) {
    //Serial.begin(9600);
    this->timings = &appTimings;
    this->pinNumber = pinNumber;
    pinMode(pinNumber, INPUT);
    // delay(10);
    Serial.print(pinNumber);
    Serial.println(" inititate");
    Serial.print("timing lever");
    Serial.println(timings->BOUNCE_DELAY);
    //*this->timings->BOUNCE_DELAY;
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
        Serial.print("t ");
        Serial.print(timeStamp);
        Serial.print(" :");
        Serial.print(this->pinNumber);
        Serial.print(" is ");
        Serial.print(state);
        Serial.println();

        lastTimeChanged = timeStamp;
        delay(10);
    }
    if (isLowLevelToTurnOn)state = !state;
    if (lastTimeChanged == timeStamp) {// is time to set on/off stamp
        if (state) {
            doubleClicked = isDoubleClicking(timeStamp);
            lastTimeTurnedOn = timeStamp;

            //Serial.print(this->pinNumber);
            //Serial.println(" doubleClicked");
            //delay(10);

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
