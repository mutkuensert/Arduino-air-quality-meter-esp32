#include <Arduino.h>
#include <TimeLib.h>
#include <SoftwareSerial.h>
#include <LiquidCrystal_I2C.h>
#include "PmResult.h"
#include "Sds011SensorHandler.h"

constexpr int DATA_READY_SIGNAL_PIN = 2;
constexpr int SENSOR_RECEIVER_PIN = 3;
constexpr int SENSOR_TRANSMITTER_PIN = 4;
constexpr uint8_t ESP32_DATA_HEAD = 0xAA;

SoftwareSerial sensorSerial(SENSOR_RECEIVER_PIN, SENSOR_TRANSMITTER_PIN);
LiquidCrystal_I2C lcd(0x27, 16, 2);
Sds011SensorHandler sensorHandler(sensorSerial);

unsigned long measurementStartTime = 0;
bool isWorkModeActive = false;

String pm25Output = "";
String pm10Output = "";

void setup() {
  Serial.begin(115200);
  sensorSerial.begin(9600);
  pinMode(DATA_READY_SIGNAL_PIN, OUTPUT);

  lcd.begin(16, 2);
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Starting...");

  sensorHandler.sendQueryReportModeCommand();
  delay(1000);
  sensorHandler.sendActiveReportModeCommand();
  delay(1000);
}

void loop() {
  if (millis() - measurementStartTime > 60000) {
    sensorHandler.sendQueryReportModeCommand();
    delay(1000);
    sensorHandler.sendSleepModeCommand();

    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print(pm25Output);
    lcd.setCursor(0, 1);
    lcd.print(pm10Output + "slp");

    delay(60000);
    measurementStartTime = millis();
    isWorkModeActive = false;
  } else {
    if (isWorkModeActive == false) {
      sensorHandler.sendWorkModeCommand();
      delay(1000);
      sensorHandler.sendActiveReportModeCommand();
      delay(1000);
      isWorkModeActive = true;
    }
    readMeasurementData();
  }
}

void readMeasurementData() {
  PmResult pmResult = sensorHandler.readPmResult();
  pm25Output = "PM2.5:" + String(pmResult.pm25) + "ug/m3";
  pm10Output = "PM10:" + String(pmResult.pm10) + "ug/m3";

  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print(pm25Output);
  lcd.setCursor(0, 1);
  lcd.print(pm10Output);

  Serial.write(ESP32_DATA_HEAD);
  Serial.write(pmResult.pm25_low);
  Serial.write(pmResult.pm25_high);
  Serial.write(pmResult.pm10_low);
  Serial.write(pmResult.pm10_high);
  sendDataIsReadySignalForEsp32();
}

void sendDataIsReadySignalForEsp32() {
  digitalWrite(DATA_READY_SIGNAL_PIN, HIGH);  // Data is ready signal for esp32
  delay(10);                                  // Short amount of time for signal detection
  digitalWrite(DATA_READY_SIGNAL_PIN, LOW);   // Reset signal
}
