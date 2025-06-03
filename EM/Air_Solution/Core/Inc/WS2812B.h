#ifndef WS2812B_H
#define WS2812B_H

#include <stdint.h>
#include <stdbool.h>

#define WS2812_LED_COUNT         48
#define WS2812_RESET_SLOTS       50
#define WS2812_DMA_BUFFER_SIZE   (WS2812_RESET_SLOTS + WS2812_LED_COUNT * 24)

// 초기화 (DMA 버퍼 초기화 및 첫 PWM 송신)
void WS2812B_Init(void);

// 특정 LED 색 설정
void WS2812B_SetColor(uint32_t index, uint8_t r, uint8_t g, uint8_t b);

// 전체 LED 상태 전송
void WS2812B_Show(void);

#endif /* WS2812B_H */
