#include <Arduino.h>
#include <TimeLib.h>

#define sensor_pin A0

void setup() {
  Serial.begin(115200);
  Serial.println("Sensor isitiliyor...");
  delay(5000);
  setTime(21, 49, 0, 25, 5, 2025);  // Set current time for arduino
  pinMode(2, OUTPUT);
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

void loop() {
  int sensorValue = analogRead(sensor_pin);
  String sensorValueAndDate = "Sensör değeri: " + String(sensorValue) + " Tarih: " + getDateTime();
  Serial.println(sensorValueAndDate);
  digitalWrite(2, HIGH);  // Data is ready signal for esp32
  delay(10);              // Short amount of time for signal detection
  digitalWrite(2, LOW);   // Reset signal
  delay(1000);
}
