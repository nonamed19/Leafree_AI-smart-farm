package com.leafree.backend.domain.crop.service;

import com.leafree.backend.domain.crop.dto.CropEnvironmentDto;
import com.leafree.backend.domain.crop.dto.CropEnvironmentWrapperDto;
import com.leafree.backend.mqtt.MqttPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class CropInfoClient {

    @Value("${fast-api-baseurl}")
    private String fastApiUrl;

    private final RestTemplate restTemplate;

    public CropEnvironmentWrapperDto getCropInfo(String cropType) {
        try {
            String encoded = UriUtils.encode(cropType, StandardCharsets.UTF_8);

            URI uri = UriComponentsBuilder
                    .fromHttpUrl(fastApiUrl)
                    .pathSegment("data", encoded)
                    .build(true)
                    .toUri();

            ResponseEntity<CropEnvironmentWrapperDto> resp = restTemplate.getForEntity(uri, CropEnvironmentWrapperDto.class);
            return resp.getBody();

        } catch (HttpClientErrorException e) {
            log.warn("식물 정보를 찾을 수 없습니다. cropType='{}', status={}, body={}",
                    cropType, e.getStatusCode(), e.getResponseBodyAsString());
            return null;

        } catch (Exception e) {
            log.error("FastAPI 요청 중 알 수 없는 오류 발생: cropType='{}'", cropType, e);
            return null;
        }
    }

    public byte[] getCropImage(String cropType) {
        try {
            // cropType을 URI의 Path로 인코딩해서 넣기
            String encodedCropType = UriUtils.encode(cropType, StandardCharsets.UTF_8);

            URI uri = UriComponentsBuilder
                    .fromHttpUrl(fastApiUrl)
                    .pathSegment("image", encodedCropType)
                    .build(true)
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON); // 없어도 무방
            HttpEntity<Void> request = new HttpEntity<>(headers); // 바디는 없음

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    request,
                    byte[].class
            );

            byte[] imageBytes = response.getBody();
            if (imageBytes == null) {
                log.warn("FastAPI 응답은 성공했지만 이미지 바디가 비어 있음: cropType='{}'", cropType);
                return null;
            }

            return imageBytes;

        } catch (HttpClientErrorException e) {
            log.warn("이미지 요청 실패: cropType='{}', status={}, body={}", cropType, e.getStatusCode(), e.getResponseBodyAsString());
            return null;

        } catch (Exception e) {
            log.error("FastAPI 이미지 요청 중 오류 발생: cropType='{}'", cropType, e);
            return null;
        }
    }





}