package com.leafree.backend.domain.event.enums;

import java.util.Map;

public enum EventContentCode {

    // === ACTUATING ===
    TEMP_CHANGED("Changed temperature from {from}°C to {to}°C"),
    HUM_CHANGED("Changed humidity from {from}% to {to}%"),
    LIGHT_CHANGED("Changed light intensity from {from}lx to {to}lx"),
    CO2_PPM_CHANGED("Changed CO2 level from {from}ppm to {to}ppm"),
    LIGHT_DURATION_CHANGED("Changed light duration from {from} min to {to} min"),
    WATERING_INTERVAL_CHANGED("Changed watering interval from {from} min to {to} min"),
    SOIL_HUMI_CHANGED("Changed soil humidity from {from} to {to}"),

    // === WARNING ===
    TEMP_HIGH("온도가 너무 높아요! 현재 온도 : {value}°C"),
    TEMP_LOW("온도가 너무 낮아요! 현재 온도 : {value}°C"),
    HUM_HIGH("습도가 너무 높아요! 현재 습도: {value}%"),
    HUM_LOW("습도가 너무 낮아요! 현재 습도: {value}%"),
    WATER_LOW("물 탱크에 물이 부족해요! 현재 물량 : {value}%"),

    // === NOTIFY ===
    CROP_REGISTERED("{cropType}을(를) 심었어요!"),
    CROP_DELETED("{cropType} 기르기를 마무리했어요!"),
    CROP_HARVESTED("Crop harvested: {cropType}");

    private final String template;

    EventContentCode(String template) {
        this.template = template;
    }

    public String format(Map<String, Object> args) {
        String result = template;
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return result;
    }

    public String getTemplate() {
        return template;
    }
}

