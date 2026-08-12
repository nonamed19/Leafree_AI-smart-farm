/* USER CODE BEGIN Header */
/**
  ******************************************************************************
  * File Name          : freertos.c
  * Description        : Code for freertos applications
  ******************************************************************************
  * @attention
  *
  * Copyright (c) 2025 STMicroelectronics.
  * All rights reserved.
  *
  * This software is licensed under terms that can be found in the LICENSE file
  * in the root directory of this software component.
  * If no LICENSE file comes with this software, it is provided AS-IS.
  *
  ******************************************************************************
  */
/* USER CODE END Header */

/* Includes ------------------------------------------------------------------*/
#include "FreeRTOS.h"
#include "task.h"
#include "main.h"
#include "cmsis_os.h"

/* Private includes ----------------------------------------------------------*/
/* USER CODE BEGIN Includes */
#include "rtos_handler.h"
#include "adc.h"
#include "dma.h"
#include "rtc.h"
#include "tim.h"
#include "usart.h"
#include "gpio.h"
#include <stdarg.h>  // 반드시 포함하세요
#include <stdio.h>   // vsnprintf
#include <string.h>  // strlen
#include <stdbool.h>

/* USER CODE END Includes */

/* Private typedef -----------------------------------------------------------*/
/* USER CODE BEGIN PTD */

/* USER CODE END PTD */

/* Private define ------------------------------------------------------------*/
/* USER CODE BEGIN PD */
#define TEST_SHORT_INTERVAL_SEC 0
#define DEBUG_BUF_SIZE 128
/* USER CODE END PD */

/* Private macro -------------------------------------------------------------*/
/* USER CODE BEGIN PM */

/* USER CODE END PM */

/* Private variables ---------------------------------------------------------*/
/* USER CODE BEGIN Variables */
QueueHandle_t xAdcQ      = NULL;   /* uint16_t[2] – DMA 결과 */
QueueHandle_t xCtlQ      = NULL;   /* uint32_t      – CTL word */
QueueHandle_t xDebugQ    = NULL;   /* char[64]      – log msg */

SemaphoreHandle_t xSensorMux;      /* binary mutex  – g_sensor 보호 */
TaskHandle_t xWateringTaskHandle = NULL;
volatile bool g_manual_water_request = false;
uint32_t gPeriodHours = 8;

SensorData_t        g_sensor;
WaterCfg_t          g_cfg = { .set_plant = 0,
															.interval_h = 48,
															.target_adc = 2400,
															.min_level_adc = 200,
															.max_pump_ms = 1000,
															.last_water_tick = 0 };

volatile uint16_t   adc_dma_buf[ADC_BUF_LEN];
uint8_t rxBuf[RX_BUF_SZ];
/* USER CODE END Variables */
osThreadId defaultTaskHandle;

/* Private function prototypes -----------------------------------------------*/
/* USER CODE BEGIN FunctionPrototypes */
void vWateringTask(void *arg);
void vSensingTask(void *arg);
void vWaterCTLTask(void *arg);
void vDebugTask(void *arg);

void Debug_Print(const char *fmt, ...);
/* USER CODE END FunctionPrototypes */

void StartDefaultTask(void const * argument);

void MX_FREERTOS_Init(void); /* (MISRA C 2004 rule 8.1) */

/* GetIdleTaskMemory prototype (linked to static allocation support) */
void vApplicationGetIdleTaskMemory( StaticTask_t **ppxIdleTaskTCBBuffer, StackType_t **ppxIdleTaskStackBuffer, uint32_t *pulIdleTaskStackSize );

/* USER CODE BEGIN GET_IDLE_TASK_MEMORY */
static StaticTask_t xIdleTaskTCBBuffer;
static StackType_t xIdleStack[configMINIMAL_STACK_SIZE];

void vApplicationGetIdleTaskMemory( StaticTask_t **ppxIdleTaskTCBBuffer, StackType_t **ppxIdleTaskStackBuffer, uint32_t *pulIdleTaskStackSize )
{
  *ppxIdleTaskTCBBuffer = &xIdleTaskTCBBuffer;
  *ppxIdleTaskStackBuffer = &xIdleStack[0];
  *pulIdleTaskStackSize = configMINIMAL_STACK_SIZE;
  /* place for user code */
}
/* USER CODE END GET_IDLE_TASK_MEMORY */

/**
  * @brief  FreeRTOS initialization
  * @param  None
  * @retval None
  */
void MX_FREERTOS_Init(void) {
  /* USER CODE BEGIN Init */
	xSensorMux = xSemaphoreCreateMutex();
	xAdcQ   = xQueueCreate(8, sizeof(uint16_t[ADC_BUF_LEN]));
	xCtlQ   = xQueueCreate(6, sizeof(uint32_t));
	xDebugQ = xQueueCreate(8, sizeof(char[DEBUG_BUF_SIZE]));
	configASSERT(xSensorMux != NULL && xAdcQ != NULL && xCtlQ != NULL && xDebugQ != NULL);

	HAL_ADCEx_Calibration_Start(&hadc1);
	HAL_ADC_Start_DMA(&hadc1, (uint32_t *)adc_dma_buf, ADC_BUF_LEN);
	HAL_UART_Receive_DMA(&huart1, rxBuf, RX_BUF_SZ);

  /* USER CODE END Init */

  /* USER CODE BEGIN RTOS_MUTEX */
  /* add mutexes, ... */

  /* USER CODE END RTOS_MUTEX */

  /* USER CODE BEGIN RTOS_SEMAPHORES */
  /* add semaphores, ... */

  /* USER CODE END RTOS_SEMAPHORES */

  /* USER CODE BEGIN RTOS_TIMERS */
  /* start timers, add new ones, ... */
  /* USER CODE END RTOS_TIMERS */

  /* USER CODE BEGIN RTOS_QUEUES */
  /* add queues, ... */

  /* USER CODE END RTOS_QUEUES */

  /* Create the thread(s) */
  /* definition and creation of defaultTask */
  osThreadDef(defaultTask, StartDefaultTask, osPriorityNormal, 0, 128);
  defaultTaskHandle = osThreadCreate(osThread(defaultTask), NULL);

  /* USER CODE BEGIN RTOS_THREADS */
  /* add threads, ... */
	xTaskCreate(vSensingTask, "SENSOR", 128, NULL, tskIDLE_PRIORITY + 1, NULL);
	xTaskCreate(vWateringTask, "WATERING", 128, NULL, tskIDLE_PRIORITY + 1, &xWateringTaskHandle);
	xTaskCreate(vWaterCTLTask, "CONTROL", 256, NULL, tskIDLE_PRIORITY + 1, NULL);
	xTaskCreate(vDebugTask, "DEBUG", 256, NULL, tskIDLE_PRIORITY + 1, NULL);

  /* USER CODE END RTOS_THREADS */

}

/* USER CODE BEGIN Header_StartDefaultTask */
/**
  * @brief  Function implementing the defaultTask thread.
  * @param  argument: Not used
  * @retval None
  */
/* USER CODE END Header_StartDefaultTask */
void StartDefaultTask(void const * argument)
{
  /* USER CODE BEGIN StartDefaultTask */
  uint8_t circulation_done_today = 0;  // 오늘 순환 했는지 플래그
	//HAL_GPIO_WritePin(GPIOA, motor2_Pin, GPIO_PIN_RESET);

  for (;;)
  {
      RTC_TimeTypeDef t;
      RTC_DateTypeDef d;
      HAL_RTC_GetTime(&hrtc, &t, RTC_FORMAT_BIN);
      HAL_RTC_GetDate(&hrtc, &d, RTC_FORMAT_BIN);

      // 08:30일 때, 아직 오늘 순환을 안 했으면 펌프 ON
      if (t.Hours == 8 && t.Minutes == 30 && circulation_done_today == 0)
      {
          // 펌프 ON
				 HAL_GPIO_WritePin(motor2_GPIO_Port, motor2_Pin, GPIO_PIN_RESET);

          // 원하는 동작 시간 만큼 대기 (예: 1분)
          osDelay(60000);

          // 펌프 OFF
          HAL_GPIO_WritePin(motor2_GPIO_Port, motor2_Pin, GPIO_PIN_SET);

          // 오늘은 이미 순환 실행했음 표시
          circulation_done_today = 1;
      }

      // 자정이 되면 플래그 초기화 (날짜 넘어가면 다시 순환 가능)
      if (t.Hours == 0 && t.Minutes == 0)
      {
          circulation_done_today = 0;
      }

      osDelay(1000);  // 1초에 한 번만 체크 (부하 줄이기)
  }
  /* USER CODE END StartDefaultTask */
}

/* Private application code --------------------------------------------------*/
/* USER CODE BEGIN Application */
void vWaterCTLTask(void *arg)
{
	uint32_t rx_word;
	for (;;)
	{

		if (xQueueReceive(xCtlQ, &rx_word, portMAX_DELAY) == pdPASS)
		{
			WaterCtl_t ctl = { .raw = rx_word };
			Debug_Print("CTL : %d\r\n", ctl.cmd);
			switch (ctl.cmd)
			{
			case 0x01: /* Set Param */
				g_cfg.interval_h    = ctl.interval_h;
				g_cfg.target_adc    = ctl.target_moist;
				g_cfg.last_water_tick = 0;
				break;

			case 0x02: /* Get Param */
				HAL_UART_Transmit(&huart1, (uint8_t *)&g_cfg, sizeof g_cfg, HAL_MAX_DELAY);
				break;

			case 0x03: /* Manual Pump */
				if (ctl.trig != 0U) {
					g_manual_water_request = true;
					xTaskNotifyGive(xWateringTaskHandle);
				}
				break;

			case 0x04:
				HAL_GPIO_TogglePin(GPIOA, LD2_Pin);

				xSemaphoreTake(xSensorMux, portMAX_DELAY);
				HAL_UART_Transmit(&huart1, (uint8_t *)&g_sensor, sizeof g_sensor, HAL_MAX_DELAY);
				xSemaphoreGive(xSensorMux);
			default:
				break;
			}
		}

	}
}

/*
	DMA + Timer를 통해 100ms 마다 토양 수분 값과 현재 물양을 측정해주는 Task
*/
void vSensingTask(void * arg)
{
	uint16_t pair[ADC_BUF_LEN];
	for (;;)
	{
		if (xQueueReceive(xAdcQ, pair, portMAX_DELAY) == pdPASS)
		{
			xSemaphoreTake(xSensorMux, portMAX_DELAY);
			g_sensor.soil_adc    = pair[0] & 0x0FFFU;
			g_sensor.level_adc   = pair[1] & 0x0FFFU;
			g_sensor.sample_tick = xTaskGetTickCount();
			Debug_Print("SOIL : %d, LEVEL : %d\r\n", g_sensor.soil_adc, g_sensor.level_adc);
			xSemaphoreGive(xSensorMux);


			// 토양의 수분 정도가 적다면 워터링 테스크 신호
			if(g_sensor.soil_adc > g_cfg.target_adc)
			{
				xTaskNotifyGive(xWateringTaskHandle);
			}
			// 수위센서의 값이 특정 값보다 작다면 라즈베리파이에 UART로 알림 전송
		}
	}
}

/*
	일정 주기마다 물을 자동으로 주는 Task
*/
void vWateringTask(void *arg)
{
	HAL_GPIO_WritePin(GPIOA, motor1_Pin, GPIO_PIN_SET);
	HAL_GPIO_WritePin(GPIOA, motor2_Pin, GPIO_PIN_SET);
	//for(g.cfg = )
	for (;;)
	{
		/* Do not energize the pump until a manual, sensor, or timer trigger arrives. */
		(void)ulTaskNotifyTake(pdTRUE, portMAX_DELAY);
		bool manualRequest = g_manual_water_request;
		g_manual_water_request = false;

		SensorData_t before;
		xSemaphoreTake(xSensorMux, portMAX_DELAY);
		before = g_sensor;
		xSemaphoreGive(xSensorMux);
		if (before.level_adc < g_cfg.min_level_adc ||
			(!manualRequest && before.soil_adc <= g_cfg.target_adc)) {
			Debug_Print("WATER skipped: soil=%u level=%u\\r\\n", before.soil_adc, before.level_adc);
			continue;
		}

		HAL_GPIO_WritePin(GPIOA, motor1_Pin, GPIO_PIN_RESET); /* Pump ON */
		TickType_t start = xTaskGetTickCount();
		for (;;)
		{
			SensorData_t s;
			xSemaphoreTake(xSensorMux, portMAX_DELAY);
			s = g_sensor;
			xSemaphoreGive(xSensorMux);
//			if ((s.soil_adc  < g_cfg.target_adc) ||
//				(s.level_adc <  g_cfg.min_level_adc) ||
//				((xTaskGetTickCount() - start) >= pdMS_TO_TICKS(g_cfg.max_pump_ms)))
//			{
			if (s.soil_adc <= g_cfg.target_adc ||
				(s.level_adc < g_cfg.min_level_adc) ||
				((xTaskGetTickCount() - start) >= pdMS_TO_TICKS(g_cfg.max_pump_ms)))
			{
				break;
			}
			vTaskDelay(pdMS_TO_TICKS(200));
		}
		HAL_GPIO_WritePin(GPIOA, motor1_Pin, GPIO_PIN_SET); /* Pump OFF */
		g_cfg.last_water_tick = xTaskGetTickCount();

#if TEST_SHORT_INTERVAL_SEC > 0
		set_next_alarm_sec(TEST_SHORT_INTERVAL_SEC);
#else
    set_next_alarm_hours(g_cfg.interval_h);
#endif
	}
}

void vDebugTask(void *arg)
{
    char dbg[128];

    for (;;)
    {
        if (xQueueReceive(xDebugQ, &dbg, portMAX_DELAY) == pdPASS)
        {
            HAL_UART_Transmit(&huart2, (uint8_t *)dbg, strlen(dbg), HAL_MAX_DELAY);
        }
    }
}

void Debug_Print(const char *fmt, ...)
{
    char dbg[DEBUG_BUF_SIZE];
    va_list args;
    va_start(args, fmt);
    vsnprintf(dbg, DEBUG_BUF_SIZE, fmt, args);  // 바로 dbg 사용
    va_end(args);

    if (xDebugQ != NULL)
    {
        xQueueSend(xDebugQ, &dbg, 0);  // 바로 버퍼 사용
    }
}

/* USER CODE END Application */

