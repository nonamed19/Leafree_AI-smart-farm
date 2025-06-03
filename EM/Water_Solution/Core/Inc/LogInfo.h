/*
 * LogInfo.h
 *
 *  Created on: Apr 30, 2025
 *      Author: SSAFY
 */

#ifndef INC_LOGINFO_H_
#define INC_LOGINFO_H_

#include "SettingConfig.h"   /* ★ 추가: 타입 선언 먼저 */
typedef struct {
    WaterCtl_t ctl;          /* 오류 사라짐 */
    /* ... */
} LogInfo_t;

#endif /* INC_LOGINFO_H_ */
