#include <WiFi.h>

const char* ssid = "ssid";
const char* password = "pass";

WiFiServer server(80);
volatile bool isDataReady = false;
String lastReceivedData = "";

void IRAM_ATTR setDataReady() {
  isDataReady = true;
}

void setup() {
  pinMode(15, INPUT);  // Signal pin
  attachInterrupt(digitalPinToInterrupt(15), setDataReady, RISING);

  Serial.begin(115200);

  delay(10);

  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected.");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  server.begin();
}

void loop() {
  WiFiClient client = server.available();  // listen for incoming clients
  String data = "";
  if (isDataReady) {
    isDataReady = false;
    if (Serial.available()) {
      data = Serial.readStringUntil('\n');
      lastReceivedData = String(data + "<br>" + lastReceivedData);
      Serial.println("Received data: " + data);
    }
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
            client.println("Received data: " + lastReceivedData);
            client.println();
            break;
          } else {
            currentLine = "";
          }
        } else if (c != '\r') {  
          currentLine += c;
        }
      }
    }
    client.stop();
    Serial.println("Client Disconnected.");
  }
}
