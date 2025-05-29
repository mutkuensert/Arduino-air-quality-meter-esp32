#include "Sds011SensorHandler.h"

Sds011SensorHandler::Sds011SensorHandler(SoftwareSerial& sensorSerial)
  : sensorSerial(sensorSerial),
    SLEEP_MODE_COMMAND{ 0xAA, 0xB4, 0x06, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0x05, 0xAB },
    WORK_MODE_COMMAND{ 0xAA, 0xB4, 0x06, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0x06, 0xAB },
    QUERY_REPORT_MODE_COMMAND{ 0xAA, 0xB4, 0x02, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0x02, 0xAB },
    QUERY_DATA_COMMAND{ 0xAA, 0xB4, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0x02, 0xAB },
    ACTIVE_REPORT_MODE_COMMAND{ 0xAA, 0xB4, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0x01, 0xAB },
    SENSOR_DATA_HEAD(0xAA),
    SENSOR_MEASUREMENT_ANSWER_ID(0xC0) {}

void Sds011SensorHandler::sendCommand(uint8_t* cmd, size_t len) {
  for (size_t i = 0; i < len; ++i) {
    sensorSerial.write(cmd[i]);
  }
}

void Sds011SensorHandler::sendSleepModeCommand() {
  sendCommand(SLEEP_MODE_COMMAND, sizeof(SLEEP_MODE_COMMAND));
}

void Sds011SensorHandler::sendWorkModeCommand() {
  sendCommand(WORK_MODE_COMMAND, sizeof(WORK_MODE_COMMAND));
}

void Sds011SensorHandler::sendQueryReportModeCommand() {
  sendCommand(QUERY_REPORT_MODE_COMMAND, sizeof(QUERY_REPORT_MODE_COMMAND));
}

void Sds011SensorHandler::sendActiveReportModeCommand() {
  sendCommand(ACTIVE_REPORT_MODE_COMMAND, sizeof(ACTIVE_REPORT_MODE_COMMAND));
}

void Sds011SensorHandler::sendQueryDataCommand() {
  sendCommand(QUERY_DATA_COMMAND, sizeof(QUERY_DATA_COMMAND));
}

bool Sds011SensorHandler::isSensorDataAvailable() {
  return sensorSerial.available() >= 10;
}

void Sds011SensorHandler::waitUntilSensorDataIsAvailable() {
  while (sensorSerial.available() < 10) {
    delay(1);
  }
}

void Sds011SensorHandler::readUntilDataHead() {
  while (sensorSerial.read() != SENSOR_DATA_HEAD) {
    delay(1);
  }
}

bool Sds011SensorHandler::isSerialDataSensorMeasurement() {
  return sensorSerial.read() == SENSOR_MEASUREMENT_ANSWER_ID;
}

PmResult Sds011SensorHandler::readPmResult() {
  waitUntilSensorDataIsAvailable();
  readUntilDataHead();

  if (isSerialDataSensorMeasurement()) {
    int pm25Low = sensorSerial.read();
    int pm25High = sensorSerial.read();
    int pm10Low = sensorSerial.read();
    int pm10High = sensorSerial.read();
    float pm25 = convertHighLowByteToDecimal(pm25High, pm25Low) / 10.0;
    float pm10 = convertHighLowByteToDecimal(pm10High, pm10Low) / 10.0;

    return PmResult(pm25, pm10, pm25Low, pm25High, pm10Low, pm10High);
  }

  return PmResult(0, 0, 0, 0, 0, 0);
}

int Sds011SensorHandler::convertHighLowByteToDecimal(uint8_t high, uint8_t low) {
  return (high << 8) | low;
}

uint8_t Sds011SensorHandler::mod256(uint16_t value) {
  return value % 256;
}

void Sds011SensorHandler::readRawCommandResponse() {
  while (sensorSerial.available()) {
    uint8_t byte = sensorSerial.read();
    Serial.print("0x");
    Serial.print(byte, HEX);
    Serial.print(" ");
  }
  Serial.println();
}