package com.fileextension.proj.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtensionRequestDto {
    
    @NotBlank(message = "확장자명은 필수입니다.")
    @Size(max = 20, message = "확장자명은 최대 20자까지 입력 가능합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "확장자명은 영문자와 숫자만 입력 가능합니다.")
    private String extensionName;
} 