#ifndef INC_DHT_H_
#define INC_DHT_H_

#include "main.h"

#define DHT_TIMEOUT 				10000
#define DHT_POLLING_CONTROL			1
#define DHT_POLLING_INTERVAL_DHT11	2000
#define DHT_POLLING_INTERVAL_DHT22	1000
#define DHT_IRQ_CONTROL

typedef struct {
	float hum;
	float temp;
} DHT_data;

typedef enum {
	DHT11,
	DHT22
} DHT_type;

typedef struct {
	GPIO_TypeDef *DHT_Port;
	uint16_t DHT_Pin;
	DHT_type type;
	uint8_t pullUp;


	#if DHT_POLLING_CONTROL == 1
	uint32_t lastPollingTime;
	float lastTemp;
	float lastHum;
	#endif
} DHT_sensor;


DHT_data DHT_getData(DHT_sensor *sensor);

#endif /* INC_DHT_H_ */
