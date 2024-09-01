#include <Arduino.h>

#pragma once

class utils {
public:
    static byte crc8(byte *buffer, byte size) {
        byte crc = 0;
        for (byte i = 0; i < size; i++) {
            byte data = buffer[i];
            for (int j = 8; j > 0; j--) {
                crc = ((crc ^ data) & 1) ? (crc >> 1) ^ 0x8C : (crc >> 1);
                data >>= 1;
            }
        }
        return crc;
    }
};
