# Leafree 프로젝트 정보 | Project Information

> Sanity `project` 스키마에 바로 옮겨 적을 수 있도록 필드별로 정리한 프로젝트 기술 노트입니다. 개인 역할·대표 이미지처럼 저장소만으로 확인할 수 없는 항목은 비워 두었습니다.

## 기본 정보 | Basic Information

### `title` — Project Name

Leafree: AI 기반 스마트팜 환경 제어 시스템  
*Leafree: AI-Powered Smart Farm Environment Control System*

### `slug` — URL Slug

`leafree-ai-smart-farm`

### `summary` — Summary

Leafree는 식물 이름을 음성 또는 앱으로 입력하면, 작물별 생장 데이터를 바탕으로 온도·습도·조도·토양 수분 등 재배 환경을 자동 설정하고 실시간으로 제어하는 가정용 AI 스마트팜입니다. Android 앱, Spring Boot 백엔드, FastAPI 기반 LLM 서비스, MQTT, STM32·Raspberry Pi 디바이스를 하나의 흐름으로 연결해, 식물을 처음 키우는 사용자도 더 쉽게 재배를 시작할 수 있도록 설계했습니다.  
*Leafree is a home smart-farm system that turns a plant name into actionable growing conditions and real-time device control.*

### `type` — Project Type

`ai-data`

### `status` — Status

`completed`

### `period.startDate` — Start Date

`2025-04-15`

### `period.endDate` — End Date

`2025-05-22`

### `role` — My Role



## 핵심 요약 | Key Highlights

### `highlights` — Key Highlights

- 음성·앱 입력으로 식물명을 받아 작물별 권장 생장 환경을 자동 구성
- 온습도, 조도, CO₂, 토양 수분 센서 데이터를 MQTT로 수집하고 환경 제어에 반영
- STM32 기반 워터·에어 솔루션으로 급수, 조명, 환기 등 액추에이터를 제어
- FastAPI와 LLM/RAG를 이용해 미등록 식물의 생장 정보와 캐릭터 이미지를 생성
- Android 앱의 실시간 모니터링, 재배 일지, FCM 푸시 알림으로 원격 관리 경험 제공

## 상세 내용 | Detailed Content

### 작물을 고르는 경험에서 환경 제어까지

식물을 키우고 싶어도 “물을 얼마나, 언제 줘야 하는지”부터 막막한 경우가 많습니다. Leafree는 이 지점을 줄이기 위해 시작했습니다. 사용자는 음성 또는 앱에서 식물 이름을 선택하거나 입력하고, 시스템은 해당 식물의 온도·습도·조도·토양 수분 기준을 준비합니다. 이후 재배기 내부의 센서 값과 기준을 비교해 필요한 제어를 수행합니다.

*Leafree reduces the setup burden of home growing by translating a plant selection into a concrete environmental profile.*

### 데이터가 없을 때도 멈추지 않는 식물 정보 흐름

작물 정보 API는 먼저 로컬 데이터셋에서 식물명을 찾아 반환합니다. 등록된 정보가 없으면 `PlantDataCreator`가 생성형 AI를 통해 생장 데이터를 만들고, 정규화한 결과를 다시 데이터셋에 저장합니다. 이미지 역시 요청 시 식물별 캐릭터 에셋을 생성해 제공합니다. 정해진 데이터만 보여 주는 대신, 새로운 작물 요청에도 유연하게 대응할 수 있도록 한 구조입니다.

*The FastAPI service follows a dataset-first, generative fallback flow for plant data and character imagery.*

### 디바이스와 애플리케이션을 잇는 제어 구조

센서와 액추에이터는 STM32 NUCLEO-F103RB를 중심으로 구성했습니다. DHT22, BH1750, MH-Z19, 토양 수분 센서에서 환경을 읽고 워터 펌프, 서보 모터, 조명·에어 솔루션을 제어합니다. MQTT는 디바이스, 백엔드, Android 앱 사이의 실시간 상태 전달 채널로 사용했습니다. Raspberry Pi는 Whisper Tiny 기반 음성 인식과 Qt 디스플레이를 담당해, 앱을 열지 않아도 기기 가까이에서 조작하고 상태를 확인할 수 있게 했습니다.

```text
Voice / Android App
        │ plant selection · control request
        ▼
Spring Boot Backend ─────── FastAPI LLM Service
        │                         │
        │ MQTT                    └─ plant profile / character image
        ▼
STM32 + sensors ───────► pump · servo · light · air solution
        │
        └──► real-time status / FCM notification / growing journal
```

### 사용자가 보는 화면과 운영 흐름

Android 앱은 Jetpack Compose로 구성하고, Retrofit을 통한 API 통신과 Paho MQTT 기반 실시간 수신을 결합했습니다. Firebase Cloud Messaging으로 환경 이상 또는 주요 이벤트를 알리고, 사용자는 앱에서 재배 상태와 일지를 확인할 수 있습니다. 백엔드는 Spring Boot, JPA, Redis, MySQL, JWT 인증을 기반으로 하며 FastAPI 서비스와 WebClient로 연동합니다. 배포 환경은 Docker, Jenkins, Nginx, Certbot을 사용하도록 구성했습니다.

*The mobile app pairs live MQTT telemetry with API-driven history and FCM notifications, while the backend coordinates authentication, persistence, and LLM integration.*

### 다음 단계

향후에는 실제 농장 환경에서 제어 기준을 검증하고, 작물 데이터셋과 추천 기능을 확장할 계획입니다. OpenCV 기반 생장 상태 분석, 고도화된 음성 명령, 다국어 지원도 후속 과제로 남아 있습니다.

## 기술 스택 | Tech Stack

### `techStack` — Tech Stack

- Android / Kotlin / Jetpack Compose / Material 3
- Spring Boot / WebFlux / JPA / Redis / MySQL / JWT
- Python / FastAPI / LangChain / OpenAI API / RAG
- MQTT / Eclipse Paho / Mosquitto
- Firebase Cloud Messaging (FCM)
- STM32 NUCLEO-F103RB / FreeRTOS / Raspberry Pi / Qt / Whisper Tiny
- DHT22 / BH1750 / MH-Z19 / 토양 수분 센서 / OLED / 워터 펌프 / 서보 모터
- Docker / Jenkins / Nginx / Certbot

## 팀 및 기여 | Team & Contributions

### `team.type` — Type

`team`

### `team.size` — Team Size

`6`

### `contributions` — Key Contributions



## 이미지 | Images

### `thumbnail` — Thumbnail Image

추천 파일: `results/Leafree_AIPic.jpg`  
`alt`: Leafree AI 스마트팜 제품 이미지 | *Leafree AI smart-farm product*

### `gallery` — Image·Video Gallery

- `results/architecture.png` — `alt`: Leafree 시스템 아키텍처 다이어그램 | *Leafree system architecture diagram*
- `results/ERD.png` — `alt`: Leafree 데이터베이스 ERD | *Leafree database ERD*
- `results/67405768-0310-4659-AD22-9394E83AD4A8.jpg` — `alt`: Leafree 워터 솔루션 | *Leafree water solution*
- `results/45F70609-09B4-4E1C-8032-90015A9193CE.jpg` — `alt`: Leafree 에어 솔루션 | *Leafree air solution*
- `results/마지막화면.png` — `alt`: Raspberry Pi Qt 디스플레이 화면 | *Raspberry Pi Qt display interface*
- `results/image.png` — `alt`: 생성형 AI 식물 캐릭터 이미지 | *Generative AI plant character image*

## 관련 링크 | Related Links

### `links` — Related Links

- `label`: Leafree 서비스 / 앱 소개 페이지  
  `url`: `https://leafree.kr`
- `label`: Leafree Backend API  
  `url`: `https://leafree.kr/api/`
- `label`: Leafree LLM API  
  `url`: `https://leafree.kr/llm-api/`

### `featured` — Featured

`false`
