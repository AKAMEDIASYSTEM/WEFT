void receiveEvent(int howMany) {
  //  digitalWrite(ledPin,HIGH);
  byte c = 0;
  int d = 255;
  while (Wire.available()) {
    c = Wire.read(); // mode byte, like encMode  {"FREQ", "AMP", "WAVE", "DUTY"};
    d = int(Wire.read()); // value byte, 0-255
  }

  switch (int(c)) {
    case 0: // FREQ
      phaseOffset = constrain(floatmap(d, 0, 255, minFreq, maxFreq), minFreq, maxFreq);
      break;
    case 1: // AMP
      DACamplitude = constrain(floatmap(d, 0.0, 255, minAmpl, maxAmpl), minAmpl, maxAmpl);
      break;
    case 2: // WAVE
      waveType = constrain(d, 0, 4); // here d must be between 0 and 4 {"SINE", "SQUARE", "D_SAW", "A_SAW", "NOISE"}
      break;
    case 3: // DUTY
      dutyCycle = constrain(floatmap(d, 0.0, 255, minDuty, maxDuty), minDuty, maxDuty);
      break;
    default:
      phaseOffset = constrain(floatmap(d, 0, 255, minFreq, maxFreq), minFreq, maxFreq);
      break;
  }
}
