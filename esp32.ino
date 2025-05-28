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

void IRAM_ATTR setDataReady() {
  isDataReady = true;
}

struct tm getLocalTime() {
  struct tm timeinfo;

  if (!getLocalTime(&timeinfo)) {
    Serial.println("Time info could not be retrieved.");
    memset(&timeinfo, 0, sizeof(struct tm));
  }

  return timeinfo;
}

void setup() {
  pinMode(15, INPUT);
  attachInterrupt(digitalPinToInterrupt(15), setDataReady, RISING);

  Serial.begin(115200);

  delay(10);

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

String* getSensorData() {
  isDataReady = false;
  static String data[3] = { "", "", "" };
  String currentLine = "";
  int lineIndex = 0;
  while (Serial.available() > 0) {
    char c = Serial.read();
    currentLine += c;
    if (currentLine == "\r\n") {
      flushSerial();
      break;
    }

    if (lineIndex >= 3) {
      flushSerial();
      break;
    }

    if (c == '\n') {
      data[lineIndex] = currentLine;
      lineIndex++;
      currentLine = "";
    }
  }
  return data;
}

void loop() {
  WiFiClient client = server.available();
  if (isDataReady) {
    String* data = getSensorData();
    htmlData = String(data[1] + "<br>" + data[2] + "<br>" + htmlData);
    Serial.println("Received data: " + data[0] + data[1] + data[2]);
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
