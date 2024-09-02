#pragma once

#include <Arduino.h>
#include "CommunicationUnit.h"
#include "ControllerForCommUnitInterface.h"

#ifndef DXBT18_H
#define DXBT18_H

class DxBT18BluetoothModule : public CommunicationUnit {
public:
    DxBT18BluetoothModule(ControllerForCommUnitInterface *hostPointer) {
        hostObject = hostPointer;
    }

    DxBT18BluetoothModule() {}

    ~DxBT18BluetoothModule() {}
};

#endif
