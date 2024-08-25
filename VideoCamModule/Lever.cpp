#include "Timings.h"
#include "Lever.h"

Lever::Lever(int pinNumber, Timings *timings, bool isLowLevelToTurnOn = false){
    this->timings=timings;
    this->pinNumber=pinNumber;
    this->isLowLevelToTurnOn = isLowLevelToTurnOn;
    unsigned long nowStamp=millis();
    unsigned long lastTimeChanged = nowStamp;
    unsigned long lastTimeTurnedOn = nowStamp;
    unsigned long lastTimeTurnedOff = nowStamp;
 }
Lever::Lever(){}
const Lever& Lever::operator=(const Lever &B){
    Serial.print("pinNumber = ");
    Serial.println(B.pinNumber);
    
    if (this == &B)    return *this;
    pinNumber=B.pinNumber;
    timings=B.timings;
    isLowLevelToTurnOn=B.isLowLevelToTurnOn;
    Serial.print("deBounce = ");
    Serial.println(timings->BOUNCE_DELAY);
    
  return *this;
}

bool Lever::isOn() {
    return state==true;
}

bool Lever::isDoubleClicked() {
    return  doubleClicked;
}

unsigned long Lever::getLastTimeTurnedOff() {
    return lastTimeTurnedOff;
}

void Lever::checkState() {
     bool tempState=state;
    if (isLowLevelToTurnOn)tempState = !tempState;
   const unsigned long timeStamp = millis();
   const bool stateStamp=digitalRead(pinNumber);
    if (tempState != stateStamp &&
       lastTimeChanged < timeStamp - timings->BOUNCE_DELAY) {
        tempState = stateStamp;
        Serial.print("t ");
        Serial.print(timeStamp);
        Serial.print(" :");
        Serial.print(this->pinNumber);
        Serial.print(" is ");
        Serial.print(state);
        Serial.print(" delay* ");Serial.print(timings->REAR_CAM_SHOWTIME_DELAY);
        Serial.println();

        lastTimeChanged = timeStamp;
      }
    
    if (lastTimeChanged == timeStamp) {// it`s time to set on/off stamp
        if (state) {
            doubleClicked = isDoubleClicking(timeStamp);
            lastTimeTurnedOn = timeStamp;
        } else{ 
          doubleClicked=false;
        lastTimeTurnedOff = timeStamp;
        }
    }
    if (isLowLevelToTurnOn)tempState = !tempState;
    state=tempState;
}

bool Lever::isDoubleClicking(unsigned long timeStamp) {
    if (lastTimeTurnedOn + timings->REPEATER_DELAY > timeStamp &&
        lastTimeTurnedOn < lastTimeTurnedOff){
            Serial.print(this->pinNumber);
            Serial.print(" doubleClicked lastTimeOn:");        
            Serial.print(lastTimeTurnedOn);
            Serial.print(" timeStamp^");        
            Serial.println(timeStamp);
        return true;
        }    else
        return false;
}
