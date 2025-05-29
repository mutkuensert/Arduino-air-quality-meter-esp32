#ifndef PM_RESULT_H
#define PM_RESULT_H

class PmResult {
public:
  float pm25;
  float pm10;
  uint8_t pm25_low;
  uint8_t pm25_high;
  uint8_t pm10_low;
  uint8_t pm10_high;

  PmResult(float pm25, float pm10, uint8_t pm25_low, uint8_t pm25_high, uint8_t pm10_low, uint8_t pm10_high)
    : pm25(pm25), pm10(pm10), pm25_low(pm25_low), pm25_high(pm25_high),
      pm10_low(pm10_low), pm10_high(pm10_high) {}
};

#endif