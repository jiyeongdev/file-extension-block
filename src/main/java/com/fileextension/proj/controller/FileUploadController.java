package com.fileextension.proj.controller;

import com.fileextension.proj.config.common.utils.FileUtils;
import com.fileextension.proj.config.common.utils.FileValidationUtils;
import com.fileextension.proj.dto.ApiResponseDto;
import com.fileextension.proj.service.ExtensionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final ExtensionService extensionService;

    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;



    // 파일 목록 조회 API
    @GetMapping("/api/files")
    @ResponseBody
    public ResponseEntity<ApiResponseDto> getFileList() {
        try {
            File dir = new File(uploadDir);
            if (!dir.exists() || !dir.isDirectory()) {
                return ResponseEntity.ok(ApiResponseDto.success("파일 목록 조회 성공", new String[0]));
            }

            File[] files = dir.listFiles(File::isFile);
            if (files == null) {
                return ResponseEntity.ok(ApiResponseDto.success("파일 목록 조회 성공", new String[0]));
            }

            List<String> fileNames = Arrays.stream(files)
                .sorted((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified())) // 최신순 정렬
                .map(File::getName)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponseDto.success("파일 목록 조회 성공", fileNames));
            
        } catch (Exception e) {
            log.error("파일 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponseDto.error("파일 목록 조회 실패: " + e.getMessage()));
        }
    }

    // 새로운 AJAX 방식 (REST API)
    @PostMapping("/api/files/upload")
    @ResponseBody
    public ResponseEntity<ApiResponseDto> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 파일 검증
            if (file == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("파일을 선택해 주세요."));
            }
            
            if (file.getOriginalFilename() == null || file.getOriginalFilename().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("유효한 파일명이 필요합니다."));
            }

            
            String originalFilename = file.getOriginalFilename();
            
            // 통합 파일 검증
            ApiResponseDto validationResult = validateFile(file, originalFilename);
            if (!validationResult.isSuccess()) {
                return ResponseEntity.badRequest().body(validationResult);
            }
            
       
            // 파일 저장 (중복 파일명 처리)
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String finalFilename = FileUtils.generateUniqueFilename(originalFilename, dir);
            File dest = new File(dir, finalFilename);
            if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
            file.transferTo(dest);

            return ResponseEntity.ok(ApiResponseDto.success("파일 업로드 성공", finalFilename));
            
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponseDto.error("파일 저장 실패: " + e.getMessage()));
        }
    }



    // 새로운 AJAX 방식 (REST API)
    @DeleteMapping("/api/files/{filename}")
    @ResponseBody
    public ResponseEntity<ApiResponseDto> deleteFile(@PathVariable String filename) {
        try {
            File file = new File(uploadDir, filename);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            if (!file.isFile()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("파일이 아닙니다."));
            }
            
            if (file.delete()) {
                return ResponseEntity.ok(ApiResponseDto.success("파일 삭제 성공", filename));
            } else {
                return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("파일 삭제 실패"));
            }
        } catch (Exception e) {
            log.error("파일 삭제 중 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponseDto.error("파일 삭제 중 오류가 발생했습니다."));
        }
    }

    /**
     * 파일 업로드 전 통합 검증을 수행합니다.
     */
    private ApiResponseDto validateFile(MultipartFile file, String originalFilename) {
        try {
            // 1. 확장자 추출 및 정규화
            String extension = FileValidationUtils.getLastExtension(originalFilename);
            String normalized = extension != null ? extension.replace(".", "").toLowerCase().trim() : "";

            // 2. 확장자 유효성 검증
            if (!extensionService.isValidExtensionName(normalized)) {
                return ApiResponseDto.error("유효하지 않은 확장자입니다.");
            }
            
            // 3. 확장자 차단 여부 확인
            if (extensionService.isExtensionBlocked(normalized)) {
                return ApiResponseDto.error("차단된 확장자입니다.");
            }
    
            // 4. 확장자 우회 공격 방지 검사
            if (FileValidationUtils.hasDangerousExtensionInFilename(originalFilename, extensionService)) {
                return ApiResponseDto.error("파일명에 차단된 확장자가 포함되어 있습니다. (예: test.exe.txt)");
            }
            
            // 5. 매직 바이트 검증 (한 번만 호출)
            String detectedExtension = FileValidationUtils.getFileExtensionFromMagicBytes(file);
            if (!detectedExtension.isEmpty() && extensionService.isExtensionBlocked(detectedExtension)) {
                String magicByteInfo = getMagicByteInfo(detectedExtension);
                return ApiResponseDto.error("확장자 우회 공격이 감지되었습니다. (매직 바이트: " + magicByteInfo + ")");
            }
            
            return ApiResponseDto.success("파일 검증 성공");
            
        } catch (IOException e) {
            log.error("파일 검증 중 오류: {}", e.getMessage());
            return ApiResponseDto.error("파일 검증 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 매직 바이트 정보를 사용자 친화적으로 반환합니다.
     */
    private String getMagicByteInfo(String detectedExtension) {
        switch (detectedExtension.toLowerCase()) {
            case "exe":
                return "PE 헤더 (4D 5A) - Windows 실행 파일";
            case "bat":
                return "배치 스크립트 패턴 - Windows 배치 파일";
            case "cmd":
                return "명령 스크립트 패턴 - Windows 명령 파일";
            case "js":
                return "JavaScript 패턴 - JavaScript 파일";
            case "pdf":
                return "PDF 시그니처 (25 50 44 46) - PDF 문서";
            case "zip":
                return "ZIP 시그니처 (50 4B) - 압축 파일";
            case "jpg":
                return "JPEG 시그니처 (FF D8) - JPEG 이미지";
            case "png":
                return "PNG 시그니처 (89 50 4E 47) - PNG 이미지";
            case "gif":
                return "GIF 시그니처 (47 49 46 38) - GIF 이미지";
            default:
                return detectedExtension + " 형식";
        }
    }

} 