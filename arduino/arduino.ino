#include <Arduino.h>
#include <TimeLib.h>
#include <SoftwareSerial.h>
#include <LiquidCrystal_I2C.h>

class PmResult {
public:
  float pm25;
  float pm10;
  uint8_t pm25_low;
  uint8_t pm25_high;
  uint8_t pm10_low;
  uint8_t pm10_high;

  PmResult(float pm25, float pm10, uint8_t pm25_low, uint8_t pm25_high, uint8_t pm10_low, uint8_t pm10_high) {
    this->pm25 = pm25;
    this->pm10 = pm10;
    this->pm25_low = pm25_low;
    this->pm25_high = pm25_high;
    this->pm10_low = pm10_low;
    this->pm10_high = pm10_high;
  }
};

class Sds011SensorHandler {
public:
  SoftwareSerial& sensorSerial;

  Sds011SensorHandler(SoftwareSerial& sensorSerial)
    : sensorSerial(sensorSerial) {}

private:
  uint8_t sleep_mode_command[19] = {
    0xAA, 0xB4, 0x06, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0x05, 0xAB
  };

private:
  uint8_t work_mode_command[19] = {
    0xAA, 0xB4, 0x06, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0x06, 0xAB
  };

private:
  uint8_t query_report_mode_command[19] = {
    0xAA, 0xB4, 0x02, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0x02, 0xAB
  };

private:
  uint8_t query_data_command[19] = {
    0xAA, 0xB4, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0x02, 0xAB
  };

private:
  uint8_t active_report_mode_command[19] = {
    0xAA, 0xB4, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0x01, 0xAB
  };

private:
  uint8_t sensor_data_head = 0xAA;
private:
  uint8_t sensor_measurement_answer_id = 0xC0;

public:
  PmResult readPmResult() {
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
  }

private:
  void sendCommand(uint8_t* cmd, size_t len) {
    for (size_t i = 0; i < len; ++i) {
      sensorSerial.write(cmd[i]);
    }
  }

public:
  void sendSleepModeCommand() {
    sendCommand(sleep_mode_command, sizeof(sleep_mode_command));
  }

public:
  void sendWorkModeCommand() {
    sendCommand(work_mode_command, sizeof(work_mode_command));
  }

public:
  void sendQueryReportModeCommand() {
    sendCommand(query_report_mode_command, sizeof(query_report_mode_command));
  }

public:
  void sendActiveReportModeCommand() {
    sendCommand(active_report_mode_command, sizeof(active_report_mode_command));
  }

public:
  void sendQueryDataCommand() {
    sendCommand(query_data_command, sizeof(query_data_command));
  }

  int convertHighLowByteToDecimal(uint8_t high, uint8_t low) {
    return (high << 8) | low;
  }

private:
  uint8_t mod256(uint16_t value) {
    return value % 256;
  }

private:
  bool isSensorDataAvailable() {
    return sensorSerial.available() >= 10;
  }
private:
  bool waitUntilSensorDataIsAvailable() {
    while (sensorSerial.available() < 10) {
      delay(1);
    }
  }

private:
  void readUntilDataHead() {
    while (sensorSerial.read() != sensor_data_head)
      ;
  }

private:
  bool isSerialDataSensorMeasurement() {
    return sensorSerial.read() == sensor_measurement_answer_id;
  }

private:
  void readRawCommandResponse() {
    while (sensorSerial.available()) {
      uint8_t b = sensorSerial.read();
      Serial.print("0x");
      Serial.print(b, HEX);
      Serial.print(" ");
    }
    Serial.println();
  }
};

int data_ready_signal_pin = 2;
int sensor_receiver_pin = 3;
int sensor_transmitter_pin = 4;

SoftwareSerial sensorSerial(sensor_receiver_pin, sensor_transmitter_pin);
LiquidCrystal_I2C lcd(0x27, 16, 2);
Sds011SensorHandler sensorHandler(sensorSerial);

void setup() {
  Serial.begin(115200);
  sensorSerial.begin(9600);
  setTime(20, 11, 0, 28, 5, 2025);  // Set current time for arduino
  pinMode(data_ready_signal_pin, OUTPUT);
  lcd.begin(16, 2);
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Starting...");
  sensorHandler.sendQueryReportModeCommand();
  delay(1000);
}

void loop() {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Stabilizing...");
  sensorHandler.sendWorkModeCommand();
  delay(30000);  //Wait for sensor stabilization
  sensorHandler.sendQueryDataCommand();
  delay(1000);
  readMeasurementData();
  sensorHandler.sendSleepModeCommand();
  delay(30000);
}

String getDateTime() {
  time_t currentTime = now();
  int yr = year(currentTime);
  int mn = month(currentTime);
  int dy = day(currentTime);
  int hr = hour(currentTime);
  int min = minute(currentTime);
  int sec = second(currentTime);
  char dateTimeBuffer[30];
  sprintf(dateTimeBuffer, "%04d-%02d-%02d %02d:%02d:%02d", yr, mn, dy, hr, min, sec);
  return String(dateTimeBuffer);
}

uint8_t esp32_data_head = 0xAA;

void readMeasurementData() {
  PmResult pmResult = sensorHandler.readPmResult();
  String pm25Output = "PM2.5: " + String(pmResult.pm25) + " ug/m3";
  String pm10Output = "PM10: " + String(pmResult.pm10) + " ug/m3";

  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print(pm25Output);
  lcd.setCursor(0, 1);
  lcd.print(pm10Output);

  Serial.write(esp32_data_head);
  Serial.write(pmResult.pm25_low);
  Serial.write(pmResult.pm25_high);
  Serial.write(pmResult.pm10_low);
  Serial.write(pmResult.pm10_high);
  sendDataIsReadySignalForEsp32();
}

void sendDataIsReadySignalForEsp32() {
  digitalWrite(data_ready_signal_pin, HIGH);  // Data is ready signal for esp32
  delay(10);                                  // Short amount of time for signal detection
  digitalWrite(data_ready_signal_pin, LOW);   // Reset signal
}
