package com.fileextension.proj.config.common.utils;

import com.fileextension.proj.service.ExtensionService;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;

public class FileValidationUtils {
    
    // 위험한 확장자들 (실행 가능한 파일) - 하드코딩 제거, ExtensionService에서 동적으로 가져올 예정
    
    // MIME Type 검증 제거 - 조작 가능하므로 신뢰하지 않음
    
    /**
     * 파일명에 위험한 확장자가 포함되어 있는지 확인합니다.
     * 예: test.exe.txt -> test.exe가 파일명, txt가 확장자이므로 exe가 파일명에 포함됨
     */
    public static boolean hasDangerousExtensionInFilename(String filename, ExtensionService extensionService) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        String lowerFilename = filename.toLowerCase();
        int lastDotIndex = lowerFilename.lastIndexOf('.');
        
        if (lastDotIndex == -1) {
            // 확장자가 없는 경우, 전체 파일명 검사
            return containsDangerousExtension(lowerFilename, extensionService);
        }
        
        // 마지막 점 이전의 파일명 부분만 검사
        String filenameWithoutExtension = lowerFilename.substring(0, lastDotIndex);
        
        // 파일명에 차단된 확장자가 포함되어 있는지 확인
        return containsDangerousExtension(filenameWithoutExtension, extensionService);
    }
    

    

    
    /**
     * 매직 바이트로 파일의 실제 형식을 판단하여 확장자를 반환합니다.
     * 확장자 우회 공격만 차단하는 방식으로 변경
     */
    public static String getFileExtensionFromMagicBytes(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return "";
        }
        
        String originalExtension = getLastExtension(file.getOriginalFilename());
        
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = new byte[64]; // 스크립트 파일 감지를 위해 더 많은 바이트 읽기
            int bytesRead = inputStream.read(header);
            
            if (bytesRead < 2) {
                return "";
            }
            
            // 매직 바이트로 실제 파일 형식 감지
            String detectedType = detectFileType(header, bytesRead);
            
            // 확장자 우회 공격 감지 로직
            return detectExtensionBypass(originalExtension, detectedType);
        }
    }
    
    /**
     * 확장자 우회 공격을 감지합니다.
     * 확장자가 안전한데 매직 바이트가 위험한 경우만 차단
     */
    private static String detectExtensionBypass(String originalExtension, String detectedType) {
        // 실행 파일 확장자 목록
        String[] executableExtensions = {"exe", "scr", "cpl", "com", "bat", "cmd", "js"};
        
        // 원본 확장자가 실행 파일인지 확인
        boolean isOriginalExecutable = false;
        for (String ext : executableExtensions) {
            if (originalExtension.equals(ext)) {
                isOriginalExecutable = true;
                break;
            }
        }
        
        // 원본 확장자가 실행 파일이면 확장자 기반으로만 판단 (사용자 설정 존중)
        if (isOriginalExecutable) {
            return originalExtension;
        }
        
        // 원본 확장자가 안전한데 매직 바이트가 실행 파일이면 우회 공격으로 판단
        if (detectedType.equals("exe") || detectedType.equals("bat") || 
            detectedType.equals("cmd") || detectedType.equals("js")) {
            return detectedType; // 실제 위험한 타입 반환
        }
        
        // 그 외의 경우는 원본 확장자 반환
        return originalExtension;
    }
    
    /**
     * 매직 바이트 패턴을 기반으로 파일 형식을 감지합니다.
     */
    private static String detectFileType(byte[] header, int bytesRead) {
        // 실행 파일 검사
        if (isExecutableFile(header)) {
            return "exe";
        }
        
        // 스크립트 파일 검사 (JS, BAT, CMD)
        String scriptType = getScriptType(header, bytesRead);
        if (!scriptType.isEmpty()) {
            return scriptType;
        }
        
        // 문서 파일 검사
        if (isDocumentFile(header)) {
            return "pdf";
        }
        
        // 압축 파일 검사
        if (isArchiveFile(header)) {
            return "zip";
        }
        
        // 이미지 파일 검사
        String imageType = getImageType(header);
        if (!imageType.isEmpty()) {
            return imageType;
        }
        
        // 텍스트 파일 검사
        if (isTextFile(header, bytesRead)) {
            return "txt";
        }
        
        return "";
    }
    
    /**
     * 스크립트 파일 형식을 감지합니다.
     */
    private static String getScriptType(byte[] header, int bytesRead) {
        // 스크립트 파일들은 텍스트 기반이므로 내용을 분석
        String content = new String(header, 0, Math.min(bytesRead, 64));
        String lowerContent = content.toLowerCase();
        
        // JavaScript 파일 감지
        if (lowerContent.contains("function") || lowerContent.contains("var ") || 
            lowerContent.contains("const ") || lowerContent.contains("let ") ||
            lowerContent.contains("console.") || lowerContent.contains("document.") ||
            lowerContent.contains("window.") || lowerContent.contains("require(") ||
            lowerContent.contains("import ") || lowerContent.contains("export ") ||
            lowerContent.contains("module.exports") || lowerContent.contains("class ") ||
            lowerContent.contains("=>") || lowerContent.contains("async ") ||
            lowerContent.contains("await ") || lowerContent.contains("promise")) {
            return "js";
        }
        
        // BAT 파일 감지 (Windows 배치 파일)
        if (lowerContent.contains("@echo") || lowerContent.contains("echo ") ||
            lowerContent.contains("pause") || lowerContent.contains("cls") ||
            lowerContent.contains("dir ") || lowerContent.contains("copy ") ||
            lowerContent.contains("del ") || lowerContent.contains("ren ") ||
            lowerContent.contains("md ") || lowerContent.contains("rd ") ||
            lowerContent.contains("cd ") || lowerContent.contains("set ") ||
            lowerContent.contains("if ") || lowerContent.contains("for ") ||
            lowerContent.contains("goto ") || lowerContent.contains("call ") ||
            lowerContent.contains("start ") || lowerContent.contains("exit") ||
            lowerContent.contains("rem ") || lowerContent.contains("::") ||
            lowerContent.contains("choice ") || lowerContent.contains("find ") ||
            lowerContent.contains("findstr ") || lowerContent.contains("sort ") ||
            lowerContent.contains("type ") || lowerContent.contains("more ")) {
            return "bat";
        }
        
        // CMD 파일 감지 (Windows 명령 파일)
        if (lowerContent.contains("cmd") || lowerContent.contains("command") ||
            lowerContent.contains("powershell") || lowerContent.contains("wscript") ||
            lowerContent.contains("cscript") || lowerContent.contains("reg ") ||
            lowerContent.contains("sc ") || lowerContent.contains("net ") ||
            lowerContent.contains("tasklist") || lowerContent.contains("taskkill") ||
            lowerContent.contains("ipconfig") || lowerContent.contains("ping ") ||
            lowerContent.contains("tracert ") || lowerContent.contains("nslookup") ||
            lowerContent.contains("telnet ") || lowerContent.contains("ftp ") ||
            lowerContent.contains("at ") || lowerContent.contains("schtasks")) {
            return "cmd";
        }
        
        return "";
    }
    
    private static boolean isExecutableFile(byte[] header) {
        // PE 헤더 (Windows 실행 파일)
        if (header[0] == 0x4D && header[1] == 0x5A) { // MZ
            return true;
        }
        
        // ELF 헤더 (Linux 실행 파일)
        if (header[0] == 0x7F && header[1] == 0x45 && header[2] == 0x4C && header[3] == 0x46) { // ELF
            return true;
        }
        
        // Mach-O 헤더 (macOS 실행 파일)
        if ((header[0] == 0xFE && header[1] == 0xED && header[2] == 0xFA && header[3] == 0xCE) ||
            (header[0] == 0xFE && header[1] == 0xED && header[2] == 0xFA && header[3] == 0xCF)) {
            return true;
        }
        
        // 쉘 스크립트
        if (header[0] == 0x23 && header[1] == 0x21) { // #!
            return true;
        }
        
        return false;
    }
    
    private static boolean isDocumentFile(byte[] header) {
        // PDF 파일
        return header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46; // %PDF
    }
    
    private static boolean isArchiveFile(byte[] header) {
        // ZIP 파일 (ZIP, DOCX, XLSX, PPTX 등)
        return header[0] == 0x50 && header[1] == 0x4B; // PK
    }
    
    private static String getImageType(byte[] header) {
        // JPEG
        if (header[0] == (byte)0xFF && header[1] == (byte)0xD8) {
            return "jpg";
        }
        
        // PNG
        if (header[0] == (byte)0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
            return "png";
        }
        
        // GIF
        if ((header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x38) &&
            (header[4] == 0x37 || header[4] == 0x39) && header[5] == 0x61) { // GIF87a or GIF89a
            return "gif";
        }
        
        return "";
    }
    
    private static boolean isTextFile(byte[] header, int bytesRead) {
        // UTF-8 BOM
        if (header[0] == (byte)0xEF && header[1] == (byte)0xBB && header[2] == (byte)0xBF) {
            return true;
        }
        
        // ASCII 텍스트
        for (int i = 0; i < Math.min(bytesRead, 8); i++) {
            if (header[i] < 0x09 || (header[i] > 0x0D && header[i] < 0x20)) {
                return false;
            }
        }
        return true;
    }
    

    
    /**
     * 문자열에 차단된 확장자가 포함되어 있는지 확인합니다.
     */
    private static boolean containsDangerousExtension(String text, ExtensionService extensionService) {
        // 파일명에서 모든 확장자를 추출하여 검사
        String[] parts = text.split("\\.");
        for (String part : parts) {
            if (!part.isEmpty() && extensionService.isExtensionBlocked(part)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 파일명에서 마지막 확장자만 추출합니다.
     * 예: "test.exe.txt" -> "txt"
     */
    public static String getLastExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        // ExtensionServiceImpl.normalizeExtensionName과 일관성 유지
        return filename.substring(lastDotIndex + 1).replace(".", "").toLowerCase().trim();
    }
    

} 