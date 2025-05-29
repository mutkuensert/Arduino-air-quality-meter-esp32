#include <WiFi.h>
#include "time.h"
#include <Arduino.h>

const char* ssid = "ssid";
const char* password = "pass";

const char* ntpServer = "pool.ntp.org";
const long gmtOffset_sec = 3 * 3600;
const int daylightOffset_sec = 0;

IPAddress local_IP(192, 168, 0, 184);
IPAddress gateway(192, 168, 0, 1);
IPAddress subnet(255, 255, 255, 0);

WiFiServer server(80);
volatile bool isDataReady = false;
String htmlData = "";
String jsonData = "";
uint8_t data_head = 0xAA;

void IRAM_ATTR setDataReady() {
  isDataReady = true;
}

struct tm getLocalTime() {
  struct tm timeinfo;

  if (!getLocalTime(&timeinfo)) {
    Serial.println("Time info could not be retrieved.");
    // Geçersiz zaman döndürmek için tüm alanları sıfırla
    memset(&timeinfo, 0, sizeof(struct tm));
  }

  return timeinfo;
}

void setup() {
  pinMode(15, INPUT);  // Arduino'dan gelen sinyal pini
  attachInterrupt(digitalPinToInterrupt(15), setDataReady, RISING);

  Serial.begin(115200);

  delay(10);

  // We start by connecting to a WiFi network

  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.config(local_IP, gateway, subnet);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected.");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);

  server.begin();
}

void flushSerial() {
  while (Serial.available() > 0) {
    Serial.read();
  }
}

bool isDataAvailable() {
  return Serial.available() >= 5;
}

void readUntilDataHead() {
  while (Serial.read() != data_head)
    ;
}

int convertHighLowByteToDecimal(uint8_t high, uint8_t low) {
  return (high << 8) | low;
}

float* getSensorData() {
  isDataReady = false;
  static float data[4];
  String currentLine = "";
  int lineIndex = 0;

  readUntilDataHead();
  int pm25Low = Serial.read();
  int pm25High = Serial.read();
  int pm10Low = Serial.read();
  int pm10High = Serial.read();
  float pm25 = convertHighLowByteToDecimal(pm25High, pm25Low) / 10.0;
  float pm10 = convertHighLowByteToDecimal(pm10High, pm10Low) / 10.0;

  data[0] = pm25;
  data[1] = pm10;

  return data;
}

void loop() {
  WiFiClient client = server.available();
  if (isDataReady && isDataAvailable()) {
    float* data = getSensorData();
    htmlData = String(String(data[0]) + "<br>" + String(data[1]) + "<br>" + htmlData);
    Serial.println("Received data: " + String(data[0]) + "\n" + String(data[1]));
  }

  if (client) {
    Serial.println("New Client.");
    String currentLine = "";
    while (client.connected()) {
      if (client.available()) {
        char c = client.read();
        Serial.write(c);
        if (c == '\n') {

          if (currentLine.length() == 0) {
            client.println("HTTP/1.1 200 OK");
            client.println("Content-type:text/html; charset=UTF-8");
            client.println();
            //struct tm currentTime = getLocalTime();
            //client.println(&currentTime, "%Y-%m-%d %H:%M:%S");

            client.println(htmlData);
            client.println();
            break;
          } else {
            currentLine = "";
          }
        } else if (c != '\r') {
          currentLine += c;
        }

        if (currentLine.endsWith("GET /json")) {
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:text/html; charset=UTF-8");
          client.println();
        }
      }
    }
    client.stop();
    Serial.println("Client Disconnected.");
  }
}
