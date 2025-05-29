#include <WiFi.h>
#include "time.h"
#include <Arduino.h>
#include "Credentials.h"

constexpr char* NTP_SERVER = "pool.ntp.org";
constexpr long GMT_OFFSET_SEC = 3 * 3600;
constexpr int DAYLIGHT_OFFSET_SEC = 0;

constexpr int SIGNAL_PIN = 15;

IPAddress localIp(192, 168, 0, 184);
IPAddress gateway(192, 168, 0, 1);
IPAddress subnet(255, 255, 255, 0);

WiFiServer server(80);
long rssi = 0;

volatile bool isDataReady = false;
constexpr uint8_t DATA_HEAD = 0xAA;

String htmlData = "";
String jsonData = "";

void IRAM_ATTR setDataReady() {
  isDataReady = true;
}

struct tm getLocalTime() {
  struct tm timeinfo;

  if (!getLocalTime(&timeinfo)) {
    Serial.println("Time info could not be retrieved.");
    // Set all fields to zero to return invalid time
    memset(&timeinfo, 0, sizeof(struct tm));
  }

  return timeinfo;
}

void setup() {
  pinMode(SIGNAL_PIN, INPUT);
  attachInterrupt(digitalPinToInterrupt(SIGNAL_PIN), setDataReady, RISING);

  Serial.begin(115200);

  delay(10);

  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.config(localIp, gateway, subnet);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected.");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.println("Signal strength (RSSI):");
  Serial.println(WiFi.RSSI());

  configTime(GMT_OFFSET_SEC, DAYLIGHT_OFFSET_SEC, NTP_SERVER);

  server.begin();
}

void flushSerial() {
  while (Serial.available() > 0) {
    Serial.read();
  }
}

void waitForData() {
  while (Serial.available() < 4) {
    delay(1);
  }
}

void readUntilDataHead() {
  while (Serial.read() != DATA_HEAD) {
    delay(1);
  }
}

int convertHighLowByteToDecimal(uint8_t high, uint8_t low) {
  return (high << 8) | low;
}

float* getSensorData() {
  isDataReady = false;
  static float data[2];
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
  rssi = WiFi.RSSI();
  WiFiClient client = server.available();

  if (isDataReady) {
    waitForData();
    float* data = getSensorData();
    htmlData = String(
      "Wifi strength: " + String(rssi) + "<br>"
      + "PM2.5: " + String(data[0]) + "<br>"
      + "PM10: " + String(data[1]));

    jsonData = String("{\"pm2.5\":") + String(data[0]) + "," + String("\"pm10\":") + String(data[1]) + String("}");

    Serial.println("Received data:");
    Serial.println(data[0]);
    Serial.println(data[1]);
  }

  if (client) {
    Serial.println("New Client.");
    String currentLine = "";
    bool isEndOfRequest = false;
    while (client.connected()) {
      if (client.available()) {
        char c = client.read();

        if (c != '\r') {
          currentLine += c;
        }

        if (currentLine == "\n") {
          isEndOfRequest = true;
        } else if (c == '\n' && currentLine.length() != 0) {
          currentLine = "";
        }

        Serial.write(c);

        if (currentLine.startsWith("GET /json")) {
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:application/json; charset=UTF-8");
          client.println();
          client.println(jsonData);
          client.println();
          break;
        }

        if (isEndOfRequest) {
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:text/html; charset=UTF-8");
          client.println();
          //struct tm currentTime = getLocalTime();
          //client.println(&currentTime, "%Y-%m-%d %H:%M:%S");

          client.println(htmlData);
          client.println();
          break;
        }
      }
    }

    delay(1);  // give the web browser time to receive the data
    client.stop();
    Serial.println("Client Disconnected.");
  }
}
