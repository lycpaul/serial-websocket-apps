#include <SoftwareSerial.h>
SoftwareSerial mySerial(D7, D8);//RX, TX
int data[16] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
byte buf[4] = {0, 0, 0, 0};
void setup() {
  Serial.begin(115200);
  mySerial.begin(115200);
}
void loop() {
  while (!mySerial.available()) {}
  if (mySerial.read() == 0x53) {
    byte key = mySerial.read();
    if (key >= 0) {
      if (mySerial.available()) buf[0] = mySerial.read();
      if (mySerial.available()) buf[1] = mySerial.read();
      if (mySerial.available()) buf[2] = mySerial.read();
      if (mySerial.available()) buf[3] = mySerial.read();
      data[key] = buf[0] + buf[1] * 256 + buf[2] * 65536 + buf[3] * 16777216;
    }
  }
  for(int i=0 ; i<15 ; i++){
    Serial.print(String(data[i]) + " ");
  } Serial.println(String(data[15]));
  delay(1);
}

//  byte tx[5] = {10, 0b11110000, 0, 0, 0};
//  mySerial.write(tx, 5);
