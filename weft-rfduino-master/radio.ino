void RFduinoBLE_onAdvertisement(bool start)
{
}

void RFduinoBLE_onConnect()
{
  Serial.println("connected");
}

void RFduinoBLE_onDisconnect()
{
  Serial.println("disconnected");
}

// returns the dBm signal strength indicated by the receiver after connection (-0dBm to -127dBm)
void RFduinoBLE_onRSSI(int rssi)
{
}

void RFduinoBLE_onReceive(byte *data, int len)
{
  Serial.print("received " + len);
  if (len == 2) {
    Serial.print("len is 2");
    Wire.beginTransmission(8);
    Wire.write(data[0]);
    Wire.write(data[1]);
    Wire.endTransmission();
    //  Serial.println(data[0]);
    //  RFduinoBLE.send(data[0]);
    RFduinoBLE.send(data[1]); //confirm message
  }
}
