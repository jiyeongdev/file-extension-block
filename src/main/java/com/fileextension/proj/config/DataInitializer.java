package com.fileextension.proj.config;

import com.fileextension.proj.entity.FixedExtension;
import com.fileextension.proj.repository.FixedExtensionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final FixedExtensionRepository fixedExtensionRepository;
    
    @Value("${file.upload.dir}")
    private String uploadDir;

    @Override
    public void run(String... args) throws Exception {
        // 모든 환경에서 업로드 디렉토리 생성 보장
        ensureUploadDirectoryExists();
        
        initializeFixedExtensions();
    }

    /**
     * 업로드 디렉토리가 존재하는지 확인하고, 없으면 생성
     */
    private void ensureUploadDirectoryExists() {
        try {
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                boolean created = uploadDirectory.mkdirs();
                if (created) {
                    log.info("업로드 디렉토리가 생성되었습니다: {}", uploadDirectory.getAbsolutePath());
                } else {
                    log.error("업로드 디렉토리 생성에 실패했습니다: {}", uploadDirectory.getAbsolutePath());
                }
            } else {
                log.info("업로드 디렉토리가 이미 존재합니다: {}", uploadDirectory.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("업로드 디렉토리 확인/생성 중 오류 발생: {}", e.getMessage());
        }
    }

    private void initializeFixedExtensions() {

        if (fixedExtensionRepository.count() > 0) {
            log.info("고정 확장자 데이터가 이미 존재합니다.");
            return;
        }

          
        List<FixedExtension> fixedExtensions = Arrays.asList(
            FixedExtension.builder()
                .extensionName("bat")
                .displayName("BAT (Batch File)")
                .description("Windows 배치 파일")
                .isBlocked(false)
                .build(),
            FixedExtension.builder()
                .extensionName("cmd")
                .displayName("CMD (Command File)")
                .description("Windows 명령 파일")
                .isBlocked(false)
                .build(),
            FixedExtension.builder()
                .extensionName("com")
                .displayName("COM (Command File)")
                .description("DOS 명령 파일")
                .isBlocked(false)
                .build(),
            FixedExtension.builder()
                .extensionName("cpl")
                .displayName("CPL (Control Panel)")
                .description("Windows 제어판 파일")
                .isBlocked(false)
                .build(),
            FixedExtension.builder()
                .extensionName("exe")
                .displayName("EXE (Executable)")
                .description("Windows 실행 파일")
                .isBlocked(false)
                .build(),
            FixedExtension.builder()
                .extensionName("scr")
                .displayName("SCR (Screen Saver)")
                .description("Windows 화면 보호기")
                .isBlocked(false)
                .build(),
            FixedExtension.builder()
                .extensionName("js")
                .displayName("JS (JavaScript)")
                .description("JavaScript 파일")
                .isBlocked(false)
                .build()
        );

        fixedExtensionRepository.saveAll(fixedExtensions);
        log.info("고정 확장자 데이터 초기화 완료: {}개", fixedExtensions.size());
    }

} 