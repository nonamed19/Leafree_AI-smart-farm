#ifndef DATA_H
#define DATA_H

#include <QWidget>

typedef struct __attribute__((__packed__)) {
    uint8_t cmd;
    int16_t temp10;
    uint8_t humidity;
    uint16_t lux;
    uint16_t co2;
    uint8_t light_hours;
} AirCtl_t;

typedef struct __attribute__((__packed__)) {
    int16_t temp10;
    uint8_t humidity;
    uint16_t lux;
    uint16_t co2;
} AirStat_t;

typedef union {
    uint32_t raw;
    struct {
        uint32_t target_moist   : 16;
        uint32_t interval_h     : 8;
        uint32_t cmd            : 8;
    };
} WaterCtl_t;

typedef struct {
    uint16_t soil_adc;
    uint16_t level_adc;
    uint32_t sample_tick;
} SensorData_t;

typedef struct __attribute__((__packed__)) {
    uint8_t cmd;    // 0x02
    uint8_t hour;   // 0~23
    uint8_t minute; // 0~59
    uint8_t second; // 0~59
    uint8_t day;    // 1~31
    uint8_t month;  // 1~12
    uint8_t year;   // 0~99
    uint8_t reserved[2];
} RtcSetPacket_t;

#endif // DATA_H
