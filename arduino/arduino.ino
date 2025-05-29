#include <Arduino.h>
#include <TimeLib.h>
#include <SoftwareSerial.h>
#include <LiquidCrystal_I2C.h>
#include "PmResult.h"
#include "Sds011SensorHandler.h"

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
  sensorHandler.sendWorkModeCommand();
  delay(45000);  //Wait for sensor stabilization
  sensorHandler.sendQueryDataCommand();
  delay(1000);
  readMeasurementData();
  sensorHandler.sendSleepModeCommand();
  delay(60000);
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
  String pm25Output = "PM2.5:" + String(pmResult.pm25) + "ug/m3";
  String pm10Output = "PM10:" + String(pmResult.pm10) + "ug/m3";

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
