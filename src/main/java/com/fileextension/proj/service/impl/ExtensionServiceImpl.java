package com.fileextension.proj.service.impl;

import com.fileextension.proj.dto.ApiResponseDto;
import com.fileextension.proj.dto.CustomExtensionDto;
import com.fileextension.proj.dto.ExtensionRequestDto;
import com.fileextension.proj.dto.FixedExtensionDto;
import com.fileextension.proj.entity.CustomExtension;
import com.fileextension.proj.entity.FixedExtension;
import com.fileextension.proj.repository.CustomExtensionQueryRepository;
import com.fileextension.proj.repository.CustomExtensionRepository;
import com.fileextension.proj.repository.FixedExtensionQueryRepository;
import com.fileextension.proj.repository.FixedExtensionRepository;
import com.fileextension.proj.service.ExtensionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExtensionServiceImpl implements ExtensionService {

    private final FixedExtensionRepository fixedExtensionRepository;
    private final FixedExtensionQueryRepository fixedExtensionQueryRepository;
    private final CustomExtensionRepository customExtensionRepository;
    private final CustomExtensionQueryRepository customExtensionQueryRepository;

    private static final int MAX_CUSTOM_EXTENSIONS = 200;
    private static final int MAX_EXTENSION_LENGTH = 20;

    @Override
    @Transactional(readOnly = true)
    public List<FixedExtensionDto> getAllFixedExtensions() {
        return fixedExtensionQueryRepository.findAllOrderByDisplayName()
                .stream()
                .map(this::convertToFixedExtensionDto)
                .collect(Collectors.toList());
    }

    @Override
    public ApiResponseDto<FixedExtensionDto> updateFixedExtensionStatus(Long id, boolean isBlocked) {
        try {
            FixedExtension fixedExtension = fixedExtensionRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("고정 확장자를 찾을 수 없습니다."));

            fixedExtension.setIsBlocked(isBlocked);
            FixedExtension saved = fixedExtensionRepository.save(fixedExtension);

            return ApiResponseDto.success("고정 확장자 상태가 업데이트되었습니다.", convertToFixedExtensionDto(saved));
        } catch (Exception e) {
            log.error("고정 확장자 상태 업데이트 실패: {}", e.getMessage());
            return ApiResponseDto.error("고정 확장자 상태 업데이트에 실패했습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomExtensionDto> getAllCustomExtensions() {
        return customExtensionQueryRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToCustomExtensionDto)
                .collect(Collectors.toList());
    }

    @Override
    public ApiResponseDto<CustomExtensionDto> addCustomExtension(ExtensionRequestDto request) {
        try {
            String normalizedExtension = normalizeExtensionName(request.getExtensionName());

            // 유효성 검사
            if (!isValidExtensionName(normalizedExtension)) {
                return ApiResponseDto.error("유효하지 않은 확장자명입니다.");
            }

            if (isDuplicateExtension(normalizedExtension)) {
                return ApiResponseDto.error("이미 존재하는 확장자입니다.");
            }

            if (isFixedExtension(normalizedExtension)) {
                return ApiResponseDto.error("고정 확장자와 중복됩니다.");
            }

            if (getCustomExtensionCount() >= MAX_CUSTOM_EXTENSIONS) {
                return ApiResponseDto.error("커스텀 확장자는 최대 200개까지 추가 가능합니다.");
            }

            CustomExtension customExtension = CustomExtension.builder()
                    .extensionName(normalizedExtension)
                    .build();

            CustomExtension saved = customExtensionRepository.save(customExtension);

            return ApiResponseDto.success("커스텀 확장자가 추가되었습니다.", convertToCustomExtensionDto(saved));
        } catch (Exception e) {
            log.error("커스텀 확장자 추가 실패: {}", e.getMessage());
            return ApiResponseDto.error("커스텀 확장자 추가에 실패했습니다.");
        }
    }

    @Override
    public ApiResponseDto<Void> deleteCustomExtension(Long id) {
        try {
            CustomExtension customExtension = customExtensionRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("커스텀 확장자를 찾을 수 없습니다."));

            customExtensionRepository.delete(customExtension);

            return ApiResponseDto.success("커스텀 확장자가 삭제되었습니다.", null);
        } catch (Exception e) {
            log.error("커스텀 확장자 삭제 실패: {}", e.getMessage());
            return ApiResponseDto.error("커스텀 확장자 삭제에 실패했습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getCustomExtensionCount() {
        return customExtensionQueryRepository.countCustomExtensions();
    }

    @Override
    public boolean isValidExtensionName(String extensionName) {
        if (extensionName == null || extensionName.trim().isEmpty()) {
            return false;
        }

        String normalized = normalizeExtensionName(extensionName);
        
        // 길이 체크
        if (normalized.length() > MAX_EXTENSION_LENGTH) {
            return false;
        }

        // 영문자와 숫자만 허용
        return normalized.matches("^[a-zA-Z0-9]+$");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDuplicateExtension(String extensionName) {
        String normalized = normalizeExtensionName(extensionName);
        return customExtensionQueryRepository.existsByExtensionName(normalized);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFixedExtension(String extensionName) {
        String normalized = normalizeExtensionName(extensionName);
        return fixedExtensionQueryRepository.existsByExtensionName(normalized);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isExtensionBlocked(String extensionName) {
        String normalized = normalizeExtensionName(extensionName);
        
        // 고정 확장자 차단 여부 확인
        boolean isFixedBlocked = fixedExtensionQueryRepository.findIsBlockedByExtensionName(normalized)
                .orElse(false);
        
        // 커스텀 확장자 존재 여부 확인
        boolean isCustomExists = customExtensionQueryRepository.existsByExtensionName(normalized);
        
        return isFixedBlocked || isCustomExists;
    }

    private String normalizeExtensionName(String extensionName) {
        if (extensionName == null) {
            return "";
        }
        // 마침표 제거하고 소문자로 변환
        return extensionName.replace(".", "").toLowerCase().trim();
    }

    private FixedExtensionDto convertToFixedExtensionDto(FixedExtension entity) {
        return FixedExtensionDto.builder()
                .id(entity.getId())
                .extensionName(entity.getExtensionName())
                .displayName(entity.getDisplayName())
                .description(entity.getDescription())
                .isBlocked(entity.getIsBlocked())
                .build();
    }

    private CustomExtensionDto convertToCustomExtensionDto(CustomExtension entity) {
        return CustomExtensionDto.builder()
                .id(entity.getId())
                .extensionName(entity.getExtensionName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
} 