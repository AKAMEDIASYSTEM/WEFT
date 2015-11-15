void receiveEvent(int howMany) {
  //  digitalWrite(ledPin,HIGH);
  byte c = 0;
  byte d = 255;
  while (Wire.available()) {
    c = Wire.read(); // mode byte, like encMode  {"FREQ", "AMP", "WAVE", "DUTY"};
    d = Wire.read(); // value, 0-255
  }

  switch (int(c)) {
    case 0: // FREQ
      phaseOffset = constrain(floatmap(int(d), 0, 255, minFreq, maxFreq), minFreq, maxFreq);
      break;
    case 1: // AMP
      DACamplitude = constrain(floatmap(int(d), 0.0, 255, minAmpl, maxAmpl), minAmpl, maxAmpl);
      break;
    case 2: // WAVE
      waveType = constrain(int(d), 0, 4); // here d must be between 0 and 4 {"SINE", "SQUARE", "A_SAW", "D_SAW", "NOISE"}
      break;
    case 3: // DUTY
      dutyCycle = constrain(floatmap(int(d), 0.0, 255, minDuty, maxDuty), minDuty, maxDuty);
      break;
    default:
      phaseOffset = constrain(floatmap(int(d), 0, 255, minFreq, maxFreq), minFreq, maxFreq);
      break;
  }
}
