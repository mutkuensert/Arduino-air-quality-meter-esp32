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

unsigned long measurement_start_time = 0;
bool is_work_mode_active = false;

String pm25Output = "";
String pm10Output = "";

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
  sensorHandler.sendActiveReportModeCommand();
  delay(1000);
}

void loop() {
  if (millis() - measurement_start_time > 60000) {
    sensorHandler.sendQueryReportModeCommand();
    delay(1000);
    sensorHandler.sendSleepModeCommand();
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print(pm25Output);
    lcd.setCursor(0, 1);
    lcd.print(pm10Output + "slp");
    delay(60000);
    measurement_start_time = millis();
    is_work_mode_active = false;
  } else {
    if (is_work_mode_active == false) {
      sensorHandler.sendWorkModeCommand();
      delay(1000);
      sensorHandler.sendActiveReportModeCommand();
      delay(1000);
      is_work_mode_active = true;
    }
    readMeasurementData();
  }
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
  pm25Output = "PM2.5:" + String(pmResult.pm25) + "ug/m3";
  pm10Output = "PM10:" + String(pmResult.pm10) + "ug/m3";

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
