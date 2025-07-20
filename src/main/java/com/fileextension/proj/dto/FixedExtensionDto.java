package com.fileextension.proj.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedExtensionDto {
    private Long id;
    private String extensionName;
    private String displayName;
    private String description;
    private Boolean isBlocked;
} 