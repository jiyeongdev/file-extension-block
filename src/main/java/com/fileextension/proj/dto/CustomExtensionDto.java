package com.fileextension.proj.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomExtensionDto {
    private Long id;
    private String extensionName;
    private LocalDateTime createdAt;
} 