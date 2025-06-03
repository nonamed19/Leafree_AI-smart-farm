/*
 * rtos_handler.h
 *
 *  Created on: May 14, 2025
 *      Author: SSAFY
 */

#ifndef INC_RTOS_HANDLER_H_
#define INC_RTOS_HANDLER_H_

#include "cmsis_os.h"
#include "SettingConfig.h"
#include <stdint.h>
#include <stddef.h>  // size_t 정의

#define ADC_BUF_LEN 2U
#define RX_BUF_SZ 4

/* === FreeRTOS 핸들 선언 === */
extern TaskHandle_t xWateringTaskHandle;
extern QueueHandle_t xAdcQ;
extern QueueHandle_t xCtlQ;
extern QueueHandle_t xDebugQ;
extern SemaphoreHandle_t xSensorMux;
extern osThreadId defaultTaskHandle;
extern SensorData_t        g_sensor;
extern WaterCfg_t          g_cfg;

extern uint8_t  rxBuf[RX_BUF_SZ];
extern volatile uint16_t   adc_dma_buf[ADC_BUF_LEN];
#endif /* INC_RTOS_HANDLER_H_ */
