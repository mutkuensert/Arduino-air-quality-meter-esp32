#include <Arduino.h>
#include <TimeLib.h>
#include <SoftwareSerial.h>
#include <LiquidCrystal_I2C.h>

int data_ready_signal_pin = 2;
int sensor_receiver_pin = 3;
int sensor_transmitter_pin = 4;
uint8_t sensor_data_head = 0xAA;
uint8_t sensor_measurement_answer_id = 0xC0;

uint8_t set_query_mode_command[] = {
  0xAA, 0xB4, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00,
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xA1,
  0x60, 0x05, 0xAB
};

SoftwareSerial sensorSerial(sensor_receiver_pin, sensor_transmitter_pin);
LiquidCrystal_I2C lcd(0x27, 16, 2);

void setup() {
  Serial.begin(115200);
  sensorSerial.begin(9600);
  setTime(13, 01, 0, 27, 5, 2025);  // Set current time for arduino
  pinMode(data_ready_signal_pin, OUTPUT);
  lcd.begin(16, 2);
}

void sendCommand(uint8_t* cmd, size_t len) {
  for (size_t i = 0; i < len; ++i) {
    sensorSerial.write(cmd[i]);
  }
}

void requestData(uint8_t command) {
  sendCommand(command, sizeof(command));
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
  measureParticle();
}

void measureParticle() {
  if (sensorSerial.available() >= 10) {
    while (sensorSerial.read() != sensor_data_head)
      ;
    if (sensorSerial.read() == sensor_measurement_answer_id) {
      int pm25Low = sensorSerial.read();
      int pm25High = sensorSerial.read();
      int pm10Low = sensorSerial.read();
      int pm10High = sensorSerial.read();
      float pm25 = convertHighLowByteToDecimal(pm25High, pm25Low) / 10.0;
      float pm10 = convertHighLowByteToDecimal(pm10High, pm10Low) / 10.0;

      String pm25Output = "PM2.5: " + String(pm25) + " ug/m3";
      Serial.println(pm25Output);
      String pm10Output = "PM10: " + String(pm10) + " ug/m3";
      Serial.println(pm10Output);
      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print(pm25Output);
      lcd.setCursor(0, 1);
      lcd.print(pm10Output);

      Serial.println(getDateTime() + ":" + "\n" + pm25Output + "\n" + pm10Output);
      sendDataIsReadySignal();
    }
  }

  delay(1000);
}

void sendDataIsReadySignal() {
  digitalWrite(data_ready_signal_pin, HIGH);  // Data is ready signal for esp32
  delay(10);                                  // Short amount of time for signal detection
  digitalWrite(data_ready_signal_pin, LOW);   // Reset signal
}

int convertHighLowByteToDecimal(uint8_t high, uint8_t low) {
  return (high << 8) | low;
}

void scanScreenAddress() {
  Wire.begin();
  Serial.begin(4800);
  while (!Serial)
    ;  // Needed for some cards, No problem for Nano

  Serial.println("Scanning I2C devices...");

  byte count = 0;
  for (byte addr = 1; addr < 127; addr++) {
    Wire.beginTransmission(addr);
    if (Wire.endTransmission() == 0) {
      Serial.print("Device found: 0x");
      Serial.println(addr, HEX);
      count++;
    }
  }

  if (count == 0) {
    Serial.println("No I2C device found.");
  } else {
    Serial.println("Scan has been finished.");
  }
}
