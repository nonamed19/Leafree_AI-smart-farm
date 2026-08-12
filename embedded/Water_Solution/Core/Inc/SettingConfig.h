/*
 * SettingCofig.h
 *
 *  Created on: Apr 30, 2025
 *      Author: SSAFY
 */

#ifndef INC_SETTINGCONFIG_H_

/* === CTL 비트필드 === */
typedef union {
    uint32_t raw;
    struct {
        uint32_t trig          : 1;
        uint32_t rsv           : 5;
        uint32_t target_moist  :10;
        uint32_t interval_h    : 8;
        uint32_t cmd           : 8;
    };
} WaterCtl_t;

/* === 런타임 파라미터 구조체 === */
typedef struct {
		uint8_t   set_plant;
    uint8_t   interval_h;
    uint16_t  target_adc;
    uint16_t  min_level_adc;   /* ★ 새 필드 */
    uint32_t  max_pump_ms;     /* ★ 새 필드 */
    TickType_t last_water_tick;
} WaterCfg_t;


typedef struct {
    uint16_t soil_adc;        // 토양 수분 (0–4095)
    uint16_t level_adc;       // 물탱크 수위
    TickType_t sample_tick;
} SensorData_t;


#define INC_SETTINGCONFIG_H_



#endif /* INC_SETTINGCOFIG_H_ */
