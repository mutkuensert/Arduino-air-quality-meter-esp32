#ifndef SDS011_SENSOR_HANDLER_H
#define SDS011_SENSOR_HANDLER_H

#include <Arduino.h>
#include <SoftwareSerial.h>
#include "PmResult.h"

class Sds011SensorHandler {
public:
  Sds011SensorHandler();
  void setSensorSerial(SoftwareSerial * serial);
  void waitUntilReady();
  void sendSleepModeCommand();
  void sendWorkModeCommand();
  void sendQueryReportModeCommand();
  void sendActiveReportModeCommand();
  void sendQueryDataCommand();
  PmResult readPmResult();

private:
  SoftwareSerial * sensorSerial;

  void sendCommand(uint8_t* cmd, size_t len);
  bool isSensorDataAvailable();
  void waitUntilSensorDataIsAvailable();
  void readUntilDataHead();
  bool isSerialDataSensorMeasurement();
  void readRawCommandResponse();
  int convertHighLowByteToDecimal(uint8_t high, uint8_t low);
  uint8_t mod256(uint16_t value);

  uint8_t sleep_mode_command[19];
  uint8_t work_mode_command[19];
  uint8_t query_report_mode_command[19];
  uint8_t query_data_command[19];
  uint8_t active_report_mode_command[19];
  uint8_t sensor_data_head;
  uint8_t sensor_measurement_answer_id;
};

#endif