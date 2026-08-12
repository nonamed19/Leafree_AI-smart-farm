/* USER CODE BEGIN Header */
/* USER CODE END Header */

/* Includes ------------------------------------------------------------------*/
#include "FreeRTOS.h"
#include "task.h"
#include "main.h"
#include "cmsis_os.h"

/* Private includes ----------------------------------------------------------*/
/* USER CODE BEGIN Includes */
#include "DHT.h"
#include "usart.h"
#include "tim.h"
#include <stdio.h>
#include <string.h>
#include <stdbool.h>
#include "BH1750.h"
#include "i2c.h"
#include "WS2812B.h"
#include "queue.h"
#include "rtc.h"


uint8_t frameBuffer[3 * 24];  // LED 60개 분량
/* USER CODE END Includes */

/* Private typedef -----------------------------------------------------------*/
/* USER CODE BEGIN PTD */
/* USER CODE END PTD */

/* Private define ------------------------------------------------------------*/
/* USER CODE BEGIN PD */
/* USER CODE END PD */

/* Private macro -------------------------------------------------------------*/
/* USER CODE BEGIN PM */
/* USER CODE END PM */

/* Private variables ---------------------------------------------------------*/
/* USER CODE BEGIN Variables */
volatile float g_temp = 0.0f;
volatile int g_humidity = 0;
volatile int g_lux = 0;
volatile int g_co2 = 0;

volatile float std_temp = 18.0f;
volatile int std_humidity = 65; //라파에서 받은 기준
volatile int std_lux = 1500;
volatile uint8_t std_light_hours = 12;
volatile int std_co2 = 0;

typedef struct __attribute__((packed)) {
    uint8_t  cmd;           // 명령 코드 (예: 0x01)
    int16_t  temp10;        // 기준 온도 (섭씨 ×10, 예: 23.5도 → 235, -3.2도 → -32)
    uint8_t  humidity;      // 기준 습도 (0~100 %RH)
    uint16_t lux;           // 기준 조도 (0~65535 lux)
    uint16_t co2;           // 기준 CO2 (ppm)
    uint8_t  light_hours;   // 하루 필요 조광 시간 (단위: 시간)
} AirCtl_t;

typedef struct __attribute__((packed)) {
    int16_t temp10;   // 현재 온도 (×10)
    uint8_t humidity; // 현재 습도
    uint16_t lux;     // 현재 조도
    uint16_t co2;       // 추가
} AirStat_t;

typedef struct __attribute__((packed)) {
    uint8_t  cmd;       // 0x02
    uint8_t  hour;      // 0~23
    uint8_t  minute;    // 0~59
    uint8_t  second;    // 0~59
    uint8_t  day;       // 1~31
    uint8_t  month;     // 1~12
    uint8_t  year;      // 0~99 (2000 기준)
    uint8_t  reserved[2]; // 정렬 맞춤용 패딩 (총 9바이트로 맞추기 위함)
} RtcSetPacket_t;

RTC_TimeTypeDef sTime;
RTC_DateTypeDef sDate;

// 수신 버퍼
uint8_t rxBuf[9];  // 가장 큰 구조체보다 살짝 여유 있게

// 수신 구조체 큐
QueueHandle_t xAirCtlQ;
QueueHandle_t xRtcSetQ;

/* USER CODE END Variables */
/* Definitions for defaultTask */
osThreadId_t defaultTaskHandle;
const osThreadAttr_t defaultTask_attributes = {
  .name = "defaultTask",
  .stack_size = 64 * 4,
  .priority = (osPriority_t) osPriorityNormal,
};

/* Private function prototypes -----------------------------------------------*/
/* USER CODE BEGIN FunctionPrototypes */
//void StartBlinkTask(void *argument);
void StartSensorTask(void *argument);  // 통합된 Tas
void StartFanControlTask(void *argument);
void StartLightControlTask(void *argument);
void StartUartCtlTask(void *argument);
void StartRtcMonitorTask(void *argument);
/* USER CODE END FunctionPrototypes */

void StartDefaultTask(void *argument);

void MX_FREERTOS_Init(void); /* (MISRA C 2004 rule 8.1) */

/**
  * @brief  FreeRTOS initialization
  * @param  None
  * @retval None
  */
void MX_FREERTOS_Init(void) {
  /* USER CODE BEGIN Init */
	xAirCtlQ = xQueueCreate(4, sizeof(AirCtl_t));
	xRtcSetQ = xQueueCreate(1, sizeof(RtcSetPacket_t));
	configASSERT(xAirCtlQ != NULL && xRtcSetQ != NULL);
	HAL_UART_Receive_DMA(&huart3, rxBuf, sizeof(rxBuf));
  /* USER CODE END Init */

  /* USER CODE BEGIN RTOS_MUTEX */
  /* USER CODE END RTOS_MUTEX */

  /* USER CODE BEGIN RTOS_SEMAPHORES */
  /* USER CODE END RTOS_SEMAPHORES */

  /* USER CODE BEGIN RTOS_TIMERS */
  /* USER CODE END RTOS_TIMERS */

  /* USER CODE BEGIN RTOS_QUEUES */
  /* USER CODE END RTOS_QUEUES */

  /* Create the thread(s) */
  /* creation of defaultTask */
  defaultTaskHandle = osThreadNew(StartDefaultTask, NULL, &defaultTask_attributes);

  /* USER CODE BEGIN RTOS_THREADS */
	  osThreadId_t sensorTaskHandle;
	  const osThreadAttr_t sensorTask_attributes = {
		.name = "sensorTask",
		.priority = (osPriority_t) osPriorityNormal,
		.stack_size = 70 * 4 //출력하면 256 출력안하면 70
	  };
	  sensorTaskHandle = osThreadNew(StartSensorTask, NULL, &sensorTask_attributes);
////
	  osThreadId_t fanTaskHandle;
	  const osThreadAttr_t fanTask_attributes = {
	    .name = "fanTask",
	    .priority = (osPriority_t) osPriorityNormal,
	    .stack_size = 64 * 4 //미출력시 64
	  };
	  fanTaskHandle = osThreadNew(StartFanControlTask, NULL, &fanTask_attributes);
//
	  osThreadId_t lightTaskHandle;
	  const osThreadAttr_t lightTask_attributes = {
	      .name = "lightTask",
	      .priority = (osPriority_t) osPriorityNormal,
	      .stack_size = 80 * 4 //미출력시 80
	  };
	  lightTaskHandle = osThreadNew(StartLightControlTask, NULL, &lightTask_attributes);


	  osThreadId_t ctlTaskHandle;
	  osThreadAttr_t ctlTask_attributes = {
	      .name = "uartCtlTask",
	      .priority = osPriorityNormal,
	      .stack_size = 60 * 4 //미출력시 60 출력시 256?
	  };
	  ctlTaskHandle = osThreadNew(StartUartCtlTask, NULL, &ctlTask_attributes);
////
	  osThreadId_t rtcMonitorTaskHandle;
	  osThreadAttr_t rtcTask_attributes = {
	      .name = "rtcMonitorTask",
	      .priority = osPriorityNormal,
	      .stack_size = 128 * 4 //미출력시 60 출력시 128
	  };
	  rtcMonitorTaskHandle = osThreadNew(StartRtcMonitorTask, NULL, &rtcTask_attributes);
  /* USER CODE END RTOS_THREADS */

  /* USER CODE BEGIN RTOS_EVENTS */
  /* add events, ... */
  /* USER CODE END RTOS_EVENTS */

}

/* USER CODE BEGIN Header_StartDefaultTask */
/* USER CODE END Header_StartDefaultTask */
void StartDefaultTask(void *argument)
{
  /* USER CODE BEGIN StartDefaultTask */
	for(;;){
		osDelay(500);
	}
  /* USER CODE END StartDefaultTask */
}

/* Private application code --------------------------------------------------*/
/* USER CODE BEGIN Application */
void StartSensorTask(void *argument)
{
    float BH1750_lux;
    DHT_data dht;
    uint8_t size; //미출력 주석
//    char buffer[120]; //미출력 주석
//    char timeBuf[64]; //미출력 주석
    //온습도
    static DHT_sensor bedRoom = {GPIOA, Port_DHT22_Pin, DHT22, GPIO_PULLUP};

    //조도
    if (BH1750_Init(&hi2c2) != BH1750_OK) {
        vTaskDelete(NULL);
    }
    BH1750_SetMode(CONTINUOUS_HIGH_RES_MODE_2);

    for (;;)
    {
    	//온습도
        dht = DHT_getData(&bedRoom);
        g_humidity = (int)(dht.hum + 0.5f);
        g_temp = dht.temp;

        //조도
        if (BH1750_OK == BH1750_ReadLight(&BH1750_lux)) {
            g_lux = (int)(BH1750_lux + 0.5f);
        }

        //co2
        uint8_t mhz_cmd[9] = {0xFF, 0x01, 0x86, 0, 0, 0, 0, 0, 0x79};
        uint8_t mhz_resp[9] = {0};
        HAL_UART_Transmit(&huart1, mhz_cmd, 9, 100);
        HAL_UART_Receive(&huart1, mhz_resp, 9, 100);

        if (mhz_resp[0] == 0xFF && mhz_resp[1] == 0x86) {
            g_co2 = (mhz_resp[2] << 8) | mhz_resp[3];
        } else {
            g_co2 = 0;  // invalid readings are not sent as a misleading unsigned value
        }

//        size = snprintf(buffer, sizeof(buffer),
//        "[현재] T: %.1f°C, H: %d%%, L: %d, CO2: %d ppm | [기준] T: %.1f°C, H: %d%%, L: %d\r\n", g_temp, g_humidity, g_lux, g_co2, std_temp, std_humidity, std_lux);
//        HAL_UART_Transmit(&huart2, (uint8_t *)buffer, size, HAL_MAX_DELAY);

//        snprintf(timeBu f, sizeof(timeBuf),
//        "[RTC] %02d:%02d:%02d %02d/%02d/20%02d\r\n", sTime.Hours, sTime.Minutes, sTime.Seconds, sDate.Date, sDate.Month, sDate.Year);
//        HAL_UART_Transmit(&huart2, (uint8_t*)timeBuf, strlen(timeBuf), HAL_MAX_DELAY);

        osDelay(1000);  // 1초마다 반복
    }
}


void StartFanControlTask(void *argument)
{
    for (;;)
    {
        if (g_humidity > std_humidity || g_temp > std_temp)
        {
            HAL_GPIO_WritePin(GPIOB, GPIO_PIN_5, GPIO_PIN_RESET); // 릴레이 ON
        }
        else
        {
            HAL_GPIO_WritePin(GPIOB, GPIO_PIN_5, GPIO_PIN_SET); // 릴레이 OFF
        }
        osDelay(1000); // 1초 주기로 확인
    }
}

//
//void StartLightControlTask(void *argument)
//{
//    WS2812B_Init();
//
//    for (;;)
//    {
//        if (g_lux < std_lux)
//        {
//            for (int i = 0; i < WS2812_LED_COUNT; i++)
//                WS2812B_SetColor(i, 255, 0, 50);  // 핑크색
//        }
//        else
//        {
//            for (int i = 0; i < WS2812_LED_COUNT; i++)
//                WS2812B_SetColor(i, 0, 0, 0);     // OFF
//        }
//
//        WS2812B_Show();
//        osDelay(1000);
//    }
//}

void StartLightControlTask(void *argument)
{
    WS2812B_Init();
    const uint8_t startHour = 8;

    for (;;)
    {
        uint8_t hour = sTime.Hours;
        uint8_t lightHours = std_light_hours > 24 ? 24 : std_light_hours;
        bool in_light_period;

        // 조광 시간이 1 이상인 경우만 판단
        uint8_t endHour = (startHour + lightHours) % 24;
        in_light_period = lightHours == 24 ||
                (lightHours > 0 && (endHour > startHour
                        ? hour >= startHour && hour < endHour
                        : hour >= startHour || hour < endHour));

        if (in_light_period && g_lux < std_lux)
        {
            // 조도가 부족할 때만 생장등 ON
            for (int i = 0; i < WS2812_LED_COUNT; i++)
                WS2812B_SetColor(i, 255, 0, 50);  // 핑크색
        }
        else
        {
            // 그 외에는 OFF
            for (int i = 0; i < WS2812_LED_COUNT; i++)
                WS2812B_SetColor(i, 0, 0, 0);     // OFF
        }
        WS2812B_Show();
        osDelay(1000);
    }
}

void HAL_UART_RxCpltCallback(UART_HandleTypeDef *huart)
{
    if (huart == &huart3)
    {
        BaseType_t hp = pdFALSE;
        uint8_t cmd = rxBuf[0];

        if (cmd == 0x01 || cmd == 0x04)  // 센서 제어
        {
            AirCtl_t pkt;
            memcpy(&pkt, rxBuf, sizeof(AirCtl_t));
            if (xAirCtlQ != NULL) {
                xQueueSendFromISR(xAirCtlQ, &pkt, &hp);
            }
        }
        else if (cmd == 0x02)  // RTC 설정
        {
            RtcSetPacket_t rtc;
            memcpy(&rtc, rxBuf, sizeof(RtcSetPacket_t));
            if (xRtcSetQ != NULL) {
                xQueueSendFromISR(xRtcSetQ, &rtc, &hp);
            }
        }

        portYIELD_FROM_ISR(hp);

        // 다음 수신 준비 (어떤 패킷이 올지 모르니 가장 큰 것으로)
        HAL_UART_Receive_DMA(&huart3, rxBuf, sizeof(rxBuf));
    }
}


//void StartUartCtlTask(void *argument)
//{
//    AirCtl_t pkt;
//
//    for (;;)
//    {
//        if (xQueueReceive(xAirCtlQ, &pkt, portMAX_DELAY) == pdPASS)
//        {
//            if (pkt.cmd == 0x01)
//            {
//                std_temp     = (float)pkt.temp10 / 10.0f;
//                std_humidity = pkt.humidity;
//                std_lux      = pkt.lux;
//
//                char msg[64];
//                snprintf(msg, sizeof(msg), "[CMD 0x01] 설정: %.1f°C, %d%%, %d lux\r\n",
//                         std_temp, std_humidity, std_lux);
//                HAL_UART_Transmit(&huart2, (uint8_t*)msg, strlen(msg), HAL_MAX_DELAY);
//            }
//            else if (pkt.cmd == 0x02)
//            {
//                RTC_TimeTypeDef sTime;
//                RTC_DateTypeDef sDate;
//
//                sTime.Hours = pkt.hour;
//                sTime.Minutes = pkt.minute;
//                sTime.Seconds = pkt.second;
//
//                sDate.Date = pkt.day;
//                sDate.Month = pkt.month;
//                sDate.Year = pkt.year;  // 24면 2024년
//
//                HAL_RTC_SetTime(&hrtc, &sTime, RTC_FORMAT_BIN);
//                HAL_RTC_SetDate(&hrtc, &sDate, RTC_FORMAT_BIN);
//
//                HAL_UART_Transmit(&huart2, (uint8_t *)"[CMD 0x02] RTC 시간 설정 완료\r\n", 31, HAL_MAX_DELAY);
//            }
//            else if (pkt.cmd == 0x04)
//            {
//                AirStat_t stat;
//                stat.temp10  = (int16_t)(g_temp * 10.0f);
//                stat.humidity = (uint8_t)g_humidity;
//                stat.lux      = (uint16_t)g_lux;
//
//                HAL_UART_Transmit(&huart3, (uint8_t*)&stat, sizeof(stat), HAL_MAX_DELAY);
//
//                // 디버깅 출력
//                char msg[64];
//                snprintf(msg, sizeof(msg), "[CMD 0x04] 전송: %.1f°C, %d%%, %d lux\r\n",
//                         g_temp, g_humidity, g_lux);
//                HAL_UART_Transmit(&huart2, (uint8_t*)msg, strlen(msg), HAL_MAX_DELAY);
//            }
//        }
//    }
//}

void StartUartCtlTask(void *argument)
{
    AirCtl_t airPkt;
    RtcSetPacket_t rtcPkt;

    for (;;)
    {
        if (xQueueReceive(xAirCtlQ, &airPkt, 0) == pdPASS)
        {
            if (airPkt.cmd == 0x01)
            {
                std_temp     = (float)airPkt.temp10 / 10.0f;
                std_humidity = airPkt.humidity;
                std_lux      = airPkt.lux;
                std_co2 = airPkt.co2;

//                char msg[64];
//                snprintf(msg, sizeof(msg), "[CMD 0x01] 설정: %.1f°C, %d%%, %d lux\r\n",
//                         std_temp, std_humidity, std_lux);
//                HAL_UART_Transmit(&huart2, (uint8_t*)msg, strlen(msg), HAL_MAX_DELAY);
            }
            else if (airPkt.cmd == 0x04)
            {
                AirStat_t stat;
                stat.temp10  = (int16_t)(g_temp * 10.0f);
                stat.humidity = (uint8_t)g_humidity;
                stat.lux      = (uint16_t)g_lux;
                stat.co2 = (uint16_t)g_co2;

                HAL_UART_Transmit(&huart3, (uint8_t*)&stat, sizeof(stat), HAL_MAX_DELAY);
//
//                char msg[64];
//                snprintf(msg, sizeof(msg), "[CMD 0x04] 전송: %.1f°C, %d%%, %d lux\r\n",
//                         g_temp, g_humidity, g_lux);
//                HAL_UART_Transmit(&huart2, (uint8_t*)msg, strlen(msg), HAL_MAX_DELAY);
            }
        }
        if (xQueueReceive(xRtcSetQ, &rtcPkt, 0) == pdPASS)
        {
            RTC_TimeTypeDef sTime;
            RTC_DateTypeDef sDate;

            sTime.Hours = rtcPkt.hour;
            sTime.Minutes = rtcPkt.minute;
            sTime.Seconds = rtcPkt.second;

            sDate.Date = rtcPkt.day;
            sDate.Month = rtcPkt.month;
            sDate.Year = rtcPkt.year;

            HAL_RTC_SetTime(&hrtc, &sTime, RTC_FORMAT_BIN);
            HAL_RTC_SetDate(&hrtc, &sDate, RTC_FORMAT_BIN);

//            HAL_UART_Transmit(&huart2, (uint8_t *)"[CMD 0x02] RTC 시간 설정 완료\r\n", strlen("[CMD 0x02] RTC 시간 설정 완료\r\n"), HAL_MAX_DELAY);
        }

        osDelay(10);  // 짧게 sleep 주기
    }
}



void StartRtcMonitorTask(void *argument)
{
    char timeBuf[64];
    for (;;)
    {
        HAL_RTC_GetTime(&hrtc, &sTime, RTC_FORMAT_BIN);
        HAL_RTC_GetDate(&hrtc, &sDate, RTC_FORMAT_BIN);  // 꼭 호출해야 시간 정상 갱신됨

//        snprintf(timeBuf, sizeof(timeBuf),
//                 "[RTC] %02d:%02d:%02d %02d/%02d/20%02d\r\n",
//                 sTime.Hours, sTime.Minutes, sTime.Seconds,
//                 sDate.Date, sDate.Month, sDate.Year);
//
//        HAL_UART_Transmit(&huart2, (uint8_t*)timeBuf, strlen(timeBuf), HAL_MAX_DELAY);
        osDelay(1000);
    }
}


//void StartBlinkTask(void *argument)
//{
//  for(;;)
//  {
//	HAL_GPIO_WritePin(LD2_GPIO_Port, LD2_Pin, 1);
//    osDelay(1000);
//	HAL_GPIO_WritePin(LD2_GPIO_Port, LD2_Pin, 0);
//    osDelay(1000); // 500ms 대기
//  }
//}

//void uart_send(char *msg)
//{
//    HAL_UART_Transmit(&huart2, (uint8_t *)msg, strlen(msg), HAL_MAX_DELAY);
//}

//void StartDHT22Task(void *argument)

//void StartBH1750Task(void *argument)
//{
//	  float BH1750_lux;
//	  char buffer[40];
//	  uint8_t size;
//
//  BH1750_Init(&hi2c2);
//  BH1750_SetMode(CONTINUOUS_HIGH_RES_MODE_2);  for (;;)
//  {
//		sprintf(buffer, "ok");
//		HAL_UART_Transmit(&huart2, (uint8_t*)buffer, strlen(buffer), 0xFF);
//    if (BH1750_OK == BH1750_ReadLight(&BH1750_lux))
//    {
//      size = sprintf(buffer, "BH1750 Lux: %.2f\r\n", BH1750_lux);
//      HAL_UART_Transmit(&huart2, (uint8_t *)buffer, size, 100);
//    }
//    osDelay(200);  // Delay for 200ms
//  }
//}

/* USER CODE END Application */

