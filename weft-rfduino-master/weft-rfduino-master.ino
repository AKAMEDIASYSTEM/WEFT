#include <Wire.h>
#define TEENSY_ADDR 8

/*
The sketch is an empty template for Bluetooth Low Energy 4.
Simply remove what you dont need, and fill in the rest.
*/

/*
 Copyright (c) 2014 OpenSourceRF.com.  All right reserved.

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

/*
 * This is dumb firmware that just relays BLE commands from the RFduino module to the Teensy over i2c
 * 
 */

#include <RFduinoBLE.h>

void setup()
{
  Wire.begin();
  RFduinoBLE.deviceName = "WEFT-c";
  RFduinoBLE.advertisementData = "HI-WEFT";
  RFduinoBLE.advertisementInterval = MILLISECONDS(300);
  RFduinoBLE.txPowerLevel = -20;  // (-20dbM to +4 dBm)

  // start the BLE stack
  RFduinoBLE.begin();
//  Wire.beginTransmission(8);
//  Wire.write(0);
//  Wire.write(255);
//  Wire.endTransmission();
//  RFduinoBLE.send('Z');
  Serial.begin(9600);
  Serial.print("i am awake");
}

void loop()
{
  // switch to lower power mode
  //  RFduino_ULPDelay(INFINITE);

  // to send one char
  // RFduinoBLE.send((char)temp);

  // to send multiple chars
  // RFduinBLE.send(&data, len);
}


