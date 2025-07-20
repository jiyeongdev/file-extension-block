package com.fileextension.proj.controller;

import com.fileextension.proj.dto.CustomExtensionDto;
import com.fileextension.proj.dto.FixedExtensionDto;
import com.fileextension.proj.service.ExtensionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebController {

    private final ExtensionService extensionService;

    @GetMapping("/")
    public String index(Model model) {
        try {
            // 고정 확장자 목록 조회
            List<FixedExtensionDto> fixedExtensions = extensionService.getAllFixedExtensions();
            model.addAttribute("fixedExtensions", fixedExtensions);

            // 커스텀 확장자 목록 조회
            List<CustomExtensionDto> customExtensions = extensionService.getAllCustomExtensions();
            model.addAttribute("customExtensions", customExtensions);

            // 업로드된 파일 목록 추가
            String uploadDir = "./uploads";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            String[] files = dir.list();
            List<String> uploadedFiles = files != null ? Arrays.asList(files) : List.of();
            model.addAttribute("uploadedFiles", uploadedFiles);

            return "index";
        } catch (Exception e) {
            log.error("메인 페이지 로딩 실패: {}", e.getMessage());
            model.addAttribute("error", "페이지 로딩에 실패했습니다.");
            return "error";
        }
    }


} 