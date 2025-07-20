package com.fileextension.proj.config.common.utils;

import java.io.File;

public class FileUtils {
    
    /**
     * 중복 파일명을 처리하여 고유한 파일명을 생성합니다.
     * 방법: 넘버링 방식 (-1, -2, -3...)
     */
    public static String generateUniqueFilename(String originalFilename, File directory) {
        if (originalFilename == null) return "";
        
        String nameWithoutExt = originalFilename;
        String extension = "";
        
        // 확장자 분리
        int dotIdx = originalFilename.lastIndexOf('.');
        if (dotIdx != -1) {
            nameWithoutExt = originalFilename.substring(0, dotIdx);
            extension = originalFilename.substring(dotIdx);
        }
        
        String filename = originalFilename;
        int counter = 1;
        int maxTries = 100; // 최대 100번 시도
        // 파일이 존재하면 넘버링 추가
        while (new File(directory, filename).exists()) {
            if (counter > maxTries) {
                throw new RuntimeException("중복 파일명 처리 실패: 최대 시도 횟수 초과");
            }
            filename = String.format("%s-%d%s", nameWithoutExt, counter++, extension);
        }
        
        return filename;
    }
}
