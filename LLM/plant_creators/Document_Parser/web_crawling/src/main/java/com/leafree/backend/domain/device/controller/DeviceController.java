package com.leafree.backend.domain.device.controller;

import com.leafree.backend.domain.crop.service.CropService;
import com.leafree.backend.domain.device.dto.DeviceRegistRequest;
import com.leafree.backend.domain.device.dto.DeviceRegistResponse;
import com.leafree.backend.domain.device.dto.DeviceDetailResponseDto;
import com.leafree.backend.domain.device.dto.UserDeviceResponse;
import com.leafree.backend.domain.device.entity.Device;
import com.leafree.backend.domain.device.service.DeviceService;
import com.leafree.backend.domain.event.dto.EventResponse;
import com.leafree.backend.domain.event.service.EventService;
import com.leafree.backend.domain.setting.dto.SettingResponse;
import com.leafree.backend.domain.setting.dto.UpdateSettingRequest;
import com.leafree.backend.domain.setting.dto.UpdateSettingResponse;
import com.leafree.backend.domain.setting.service.SettingService;
import com.leafree.backend.domain.user.dto.FcmTokenDto;
import com.leafree.backend.domain.user.service.UserService;
import com.leafree.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(name = "Device API", description = "디바이스 관련 API")
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController {

    private final UserService userService;
    private final DeviceService deviceService;
    private final CropService cropService;
    private final SettingService settingService;
    private final EventService eventService;

    @Operation(summary = "사용자 기기목록 조회", description = "userId에 해당하는 기기목록 조회")
    @GetMapping
    public ResponseEntity<List<UserDeviceResponse>> getUserDeviceList(Authentication authentication) {
        Long userId = SecurityUtil.getCurrentUserId(authentication);
        List<UserDeviceResponse> response = deviceService.getAllUserDevice(userId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "기기 상세정보 조회", description = "기기에 현재 심긴 작물 정보")
    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceDetailResponseDto> getDeviceDetail(@PathVariable Long deviceId) {
        DeviceDetailResponseDto response = deviceService.getDeviceDetail(deviceId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "기기에 심긴 작물 이미지 조회", description = "기기에 현재 심긴 작물 이미지")
    @GetMapping("/{deviceId}/image")
    public ResponseEntity<byte[]> getDeviceImage(@PathVariable Long deviceId) {
        byte[] image = cropService.getLatestCropImage(deviceId);

        if (image == null || image.length == 0) {
            return ResponseEntity.notFound().build(); // 404
        }

        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .header("Content-Length", String.valueOf(image.length))
                .body(image);
    }


    @Operation(summary = "사용자 기기 추가", description = "사용자가 기기를 등록합니다.")
    @PostMapping
    public ResponseEntity<DeviceRegistResponse> addDevice(Authentication authentication, @Valid @RequestBody DeviceRegistRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(authentication);

        log.info("userId = " + userId);

        DeviceRegistResponse response = deviceService.addUserDevice(userId, request);

        log.info("response = " + response);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("{deviceId}/crop/{cropId}")
    @Operation(summary = "식물 삭제", description = "기기에서 식물을 제거합니다.")
    public ResponseEntity<Map<String, String>> deleteCrop(@PathVariable Long deviceId, @PathVariable Long cropId) {

        cropService.deleteCrop(deviceId, cropId);

        return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("message", "기기에서 식물 삭제에 성공했습니다."));
    }

    @GetMapping("/{deviceId}/setting")
    @Operation(summary = "기기 설정 정보 조회", description = "기기 설정 정보를 조회합니다.")
    public ResponseEntity<SettingResponse> getDeviceSetting(@PathVariable Long deviceId) {

        SettingResponse response = settingService.getSetting(deviceId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{deviceId}/setting")
    @Operation(summary = "기기 설정 수정", description = "기기 설정을 수동으로 수정합니다.")
    public ResponseEntity<UpdateSettingResponse> updateDeviceSetting(@PathVariable Long deviceId, @RequestBody UpdateSettingRequest request) {

        UpdateSettingResponse response = settingService.updateSetting(deviceId,request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "날짜별 이벤트 조회", description = "특정 날짜(2025-05-13)의 이벤트들을 리스트로 조회")
    @GetMapping("/{deviceId}/event")
    public ResponseEntity<List<EventResponse>> getEventsForDate(@PathVariable Long deviceId, @RequestParam(name = "date") LocalDate date) {

        List<EventResponse> response = eventService.getEvent(deviceId, date);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "FCM 토큰 저장", description = "FCM 알림용 토큰 저장")
    @PostMapping("/{deviceId}/fcm-token")
    public ResponseEntity<Map<String, String>> insertFcmToken(@PathVariable Long deviceId, @RequestBody FcmTokenDto tokenDto, Authentication authentication) {
        Long userId = SecurityUtil.getCurrentUserId(authentication);

        userService.saveFcmToken(tokenDto, userId, deviceId);

        return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("message","fcm 토큰 저장에 완료했습니다."));
    }

    @Operation(summary = "기기 연결 해제", description = "기기를 사용자 기기목록에서 삭제합니다")
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Map<String,String>> deleteUserDevice(Authentication authentication, @PathVariable Long deviceId) {
        Long userId = SecurityUtil.getCurrentUserId(authentication);

        deviceService.deleteUserDevice(userId, deviceId);

        return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("message","사용자의 기기목록에서 기기를 삭제 완료했습니다."));
    }

    @Operation(summary = "백오피스 기기 등록", description = "기기와 기기에 해당하는 UUID 저장")
    @PostMapping("/backoffice")
    public ResponseEntity<DeviceRegistResponse> registDevice() {
        String newDeviceUuid = UUID.randomUUID().toString();

        Device device = deviceService.createDevice(newDeviceUuid);
        DeviceRegistResponse response = DeviceRegistResponse.builder()
                .deviceId(device.getId())
                .deviceUuid(device.getDeviceUuid())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
