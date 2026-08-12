/* WS2812B.c */
#include "WS2812B.h"
#include "tim.h"
#include "dma.h"
#include "main.h"
#include <string.h>

extern TIM_HandleTypeDef htim4;
extern DMA_HandleTypeDef hdma_tim4_ch1;

static uint8_t ws2812b_dma_buffer[WS2812_DMA_BUFFER_SIZE];

void WS2812B_Init(void) {
    memset(ws2812b_dma_buffer, 0, sizeof(ws2812b_dma_buffer));
    HAL_TIM_PWM_Start_DMA(&htim4, TIM_CHANNEL_1, (uint32_t *)ws2812b_dma_buffer, WS2812_DMA_BUFFER_SIZE);
}

void WS2812B_SetColor(uint32_t index, uint8_t r, uint8_t g, uint8_t b) {
    if (index >= WS2812_LED_COUNT) return;
    uint32_t offset = WS2812_RESET_SLOTS + index * 24;
    for (int i = 0; i < 8; i++) {
        ws2812b_dma_buffer[offset + i] = (g & (1 << (7 - i))) ? 56 : 28;
        ws2812b_dma_buffer[offset + 8 + i] = (r & (1 << (7 - i))) ? 56 : 28;
        ws2812b_dma_buffer[offset + 16 + i] = (b & (1 << (7 - i))) ? 56 : 28;
    }
}

void WS2812B_Show(void) {
    HAL_TIM_PWM_Start_DMA(&htim4, TIM_CHANNEL_1, (uint32_t *)ws2812b_dma_buffer, WS2812_DMA_BUFFER_SIZE);
}
