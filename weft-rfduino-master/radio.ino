void RFduinoBLE_onAdvertisement(bool start)
{
}

void RFduinoBLE_onConnect()
{
RFduinoBLE.send('X');
}

void RFduinoBLE_onDisconnect()
{
}

// returns the dBm signal strength indicated by the receiver after connection (-0dBm to -127dBm)
void RFduinoBLE_onRSSI(int rssi)
{
}

void RFduinoBLE_onReceive(byte *data, int len)
{
  if(len==2){
  Wire.beginTransmission(8);
  Wire.write(data[0]);
  Wire.write(data[1]);
  Wire.endTransmission();
  RFduinoBLE.send(data[1]); //confirm message
  }
}
