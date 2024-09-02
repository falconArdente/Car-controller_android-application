#pragma once

#ifndef CONTR_FOR_CU_H
#define CONTR_FOR_CU_H

class ControllerForCommUnitInterface {
public:
    virtual void setCommunicationDevice(CommunicationUnit network);

    virtual void updateTimings(Timings newTimings);

    virtual void executeCommand(CommunicationUnit::ControlCommandSet command);

    virtual void sendUpTimings();
};

#endif
