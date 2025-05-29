#ifndef SDS011_SENSOR_HANDLER_H
#define SDS011_SENSOR_HANDLER_H

#include <Arduino.h>
#include <SoftwareSerial.h>
#include "PmResult.h"

class Sds011SensorHandler {
public:
  SoftwareSerial& sensorSerial;

  Sds011SensorHandler(SoftwareSerial& sensorSerial);

  void sendSleepModeCommand();
  void sendWorkModeCommand();
  void sendQueryReportModeCommand();
  void sendActiveReportModeCommand();
  void sendQueryDataCommand();
  PmResult readPmResult();

private:
  void sendCommand(uint8_t* cmd, size_t len);
  bool isSensorDataAvailable();
  void waitUntilSensorDataIsAvailable();
  void readUntilDataHead();
  bool isSerialDataSensorMeasurement();
  void readRawCommandResponse();
  int convertHighLowByteToDecimal(uint8_t high, uint8_t low);
  uint8_t mod256(uint16_t value);

  uint8_t SLEEP_MODE_COMMAND[19];
  uint8_t WORK_MODE_COMMAND[19];
  uint8_t QUERY_REPORT_MODE_COMMAND[19];
  uint8_t QUERY_DATA_COMMAND[19];
  uint8_t ACTIVE_REPORT_MODE_COMMAND[19];
  uint8_t SENSOR_DATA_HEAD;
  uint8_t SENSOR_MEASUREMENT_ANSWER_ID;
};

#endif