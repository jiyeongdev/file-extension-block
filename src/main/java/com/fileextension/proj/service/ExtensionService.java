package com.fileextension.proj.service;

import com.fileextension.proj.dto.ApiResponseDto;
import com.fileextension.proj.dto.CustomExtensionDto;
import com.fileextension.proj.dto.ExtensionRequestDto;
import com.fileextension.proj.dto.FixedExtensionDto;

import java.util.List;

public interface ExtensionService {
    
    // 고정 확장자 관련
    List<FixedExtensionDto> getAllFixedExtensions();
    ApiResponseDto<FixedExtensionDto> updateFixedExtensionStatus(Long id, boolean isBlocked);
    
    // 커스텀 확장자 관련
    List<CustomExtensionDto> getAllCustomExtensions();
    ApiResponseDto<CustomExtensionDto> addCustomExtension(ExtensionRequestDto request);
    ApiResponseDto<Void> deleteCustomExtension(Long id);
    //커스텀 확장자 갯수 조회
    long getCustomExtensionCount();
    
    // 유효성 검사
    boolean isValidExtensionName(String extensionName);
    boolean isDuplicateExtension(String extensionName);
    boolean isFixedExtension(String extensionName);
    // 확장자 차단 여부 확인 (고정 + 커스텀)
    boolean isExtensionBlocked(String extensionName);
} 