// 
/*
// WEFT driver for implementation of REVEL and TeslaTouch papers by Disney Research
// for teensy 3.1
// Digital pin 4 sends hi-freq PWM (altered to send at 375kHz) to a boost converter circuit
// Analog pin 14 (it's a real DAC on the teensy 3.1) sends variable-freq sine wave to a hi-voltage transistor circuit
// that modulates the boosted supply voltage
// freqPot on pin A7 sets the signal frequency
// SDA on pin A4
// SCL on pin A5

// TODO: implement goal-seeking feedback to keep constant current despite changing impedances
// TODO: figure out where in the circuit the current-sensing occurs (pretty sure we're measuring voltage drop across precision resistor?)

//  phase = phase + 0.2; // about 600Hz
//  phase = phase + 0.05; // about 150Hz
//  phase = phase + 0.025; // about 75Hz
*/

#include <SPI.h>
#include <Wire.h>

#define pwmOut 4
#define LED 13
#define SINE 0
#define SQUARE 1
#define SAW_DESC 2
#define SAW_ASC 3
#define NOISE 4

#define waveMax 4
#define waveMin 0

float minFreq = 0.016 * 1000.0; // 0.0125 is too low for some people to feel, trying 0.016 now
float maxFreq = 0.4 * 1000.0;

float minDuty = 0.3;
float maxDuty = 0.95;
float dutyCycle = 0.3;

float minAmpl = 0.0;
float maxAmpl = 2000.0;

float DACamplitude = 2000.0;
int waveType = 1; // sine, square (default), saw descending, saw ascending, noise

float phase = 0.01;
float twopi = 3.14159 * 2;
float phaseOffset = 10; //just an initial condition, this used to be 0.05

void setup() {
  // 0 - 4095 pwm values if res set to 12-bit
  Wire.begin(8);
  Wire.onReceive(receiveEvent);
  analogWriteResolution(12);
  analogWriteFrequency(pwmOut, 375000);
  pinMode(pwmOut, OUTPUT);
  pinMode(LED, OUTPUT);
  digitalWrite(LED,HIGH);

}

void loop() {

  analogWrite(pwmOut, int(4096 * dutyCycle));

  float DACval = 0;
  phase = phase + (phaseOffset / 1000.0);
  if (phase >= twopi) {
    phase = 0;
  }

  switch (waveType) {
    case SINE:
      DACval = sin(phase) * DACamplitude + 2050.0;
      // 2050.0 is DC offset?
      break;
    case SQUARE:
      // if phase > pi then 1 else 0
      (phase > twopi / 2) ? (DACval = (DACamplitude / maxAmpl) * 4095.0) : (DACval = 0.0);
      break;
    case SAW_DESC:
      // phase itself is linearly ramping
      DACval = floatmap(phase, 0, twopi, 0.0, 1.0) * (DACamplitude / maxAmpl) * 4095.0;
      break;
    case SAW_ASC:
      // phase itself is linearly ramping
      DACval = floatmap(phase, 0, twopi, 1.0, 0.0) * (DACamplitude / maxAmpl) * 4095.0;
      break;
    case NOISE:
      (random(0, 9) > 4.5) ? (DACval = (DACamplitude / maxAmpl) * 4095.0) : (DACval = 0.0);
      break;
    default:
      DACval = sin(phase) * DACamplitude + 2050.0;
      break;
  }

  DACval = 4095.0 - DACval;
  analogWrite(A14, (int)DACval);

}

// native map func uses int math
float floatmap(float x, float in_min, float in_max, float out_min, float out_max)
{
  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}
