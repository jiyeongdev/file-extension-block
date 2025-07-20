package com.fileextension.proj.controller;

import com.fileextension.proj.dto.ApiResponseDto;
import com.fileextension.proj.dto.CustomExtensionDto;
import com.fileextension.proj.dto.ExtensionRequestDto;
import com.fileextension.proj.dto.FixedExtensionDto;
import com.fileextension.proj.service.ExtensionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/extensions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ExtensionController {

    private final ExtensionService extensionService;

    // 고정 확장자 목록 조회
    @GetMapping("/fixed")
    public ResponseEntity<ApiResponseDto<List<FixedExtensionDto>>> getFixedExtensions() {
        try {
            List<FixedExtensionDto> extensions = extensionService.getAllFixedExtensions();
            return ResponseEntity.ok(ApiResponseDto.success(extensions));
        } catch (Exception e) {
            log.error("고정 확장자 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDto.error("고정 확장자 목록 조회에 실패했습니다."));
        }
    }



    // 고정 확장자 상태 업데이트
    @PutMapping("/fixed/{id}/status")
    public ResponseEntity<ApiResponseDto<FixedExtensionDto>> updateFixedExtensionStatus(
            @PathVariable Long id,
            @RequestParam boolean isBlocked) {
        try {
            ApiResponseDto<FixedExtensionDto> response = extensionService.updateFixedExtensionStatus(id, isBlocked);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("고정 확장자 상태 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDto.error("고정 확장자 상태 업데이트에 실패했습니다."));
        }
    }

    // 커스텀 확장자 목록 조회
    @GetMapping("/custom")
    public ResponseEntity<ApiResponseDto<List<CustomExtensionDto>>> getCustomExtensions() {
        try {
            List<CustomExtensionDto> extensions = extensionService.getAllCustomExtensions();
            return ResponseEntity.ok(ApiResponseDto.success(extensions));
        } catch (Exception e) {
            log.error("커스텀 확장자 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDto.error("커스텀 확장자 목록 조회에 실패했습니다."));
        }
    }

    // 커스텀 확장자 추가
    @PostMapping("/custom")
    public ResponseEntity<ApiResponseDto<CustomExtensionDto>> addCustomExtension(
            @Valid @RequestBody ExtensionRequestDto request) {
        try {
            ApiResponseDto<CustomExtensionDto> response = extensionService.addCustomExtension(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("커스텀 확장자 추가 실패: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDto.error("커스텀 확장자 추가에 실패했습니다."));
        }
    }

    // 커스텀 확장자 삭제
    @DeleteMapping("/custom/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteCustomExtension(@PathVariable Long id) {
        try {
            ApiResponseDto<Void> response = extensionService.deleteCustomExtension(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("커스텀 확장자 삭제 실패: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDto.error("커스텀 확장자 삭제에 실패했습니다."));
        }
    }




} 