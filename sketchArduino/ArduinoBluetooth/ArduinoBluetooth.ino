#include <Adafruit_NeoPixel.h>

#define PIXEL_PIN 10 // pin IO used for pilot the Neopixel led
#define PIXEL_COUNT 20 // number Neopixel led present in strip
int valueRead = 0; // variable used for read the serial data
byte data_in[3] {0, 0, 0}; // variable used for write data read of the serial

// create the object strip of type Adafruit_NeoPixel
// Parameter 1 = number of pixels in strip
// Parameter 2 = Arduino pin number (most are valid)
// Parameter 3 = pixel type flags, add together as needed:
//   NEO_KHZ800  800 KHz bitstream (most NeoPixel products w/WS2812 LEDs)
//   NEO_KHZ400  400 KHz (classic 'v1' (not v2) FLORA pixels, WS2811 drivers)
//   NEO_GRB     Pixels are wired for GRB bitstream (most NeoPixel products)
//   NEO_RGB     Pixels are wired for RGB bitstream (v1 FLORA pixels, not v2)
Adafruit_NeoPixel strip = Adafruit_NeoPixel(PIXEL_COUNT, PIXEL_PIN, NEO_GRB + NEO_KHZ400);

void setup() {
  Serial.begin(9600);
  // Initialize all pixels to 'off
  strip.begin();
  strip.show();
}
void loop() {
  // save data in data_in
  if (Serial.available() > 2) {
    for (int i = 0; i < 3; i++) {
      valueRead = Serial.read();
      data_in[i] = valueRead;
    }
  }
  single_color();
}

// animation signle color
void single_color() {
  for (int i = 0; i < PIXEL_COUNT ; i++)
    // strip.setPixelColor(n, red, green, blue);
    // The first argument — n in this example — is the pixel number along the strip, starting from 0 closest to the Arduino.
    // If you have a strip of 30 pixels, they’re numbered 0 through 29.
    // The next three arguments are the pixel color, expressed as red, green and blue brightness levels,
    // where 0 is dimmest (off) and 255 is maximum brightness.
    strip.setPixelColor(i, (int)data_in[0], (int)data_in[1], (int)data_in[2]);
  strip.show();
}
