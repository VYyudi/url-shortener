package com.victorhagui.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UrlRequestDto {
    @NotBlank(message = "URL não pode estar vazia")
    @Pattern(
            regexp = "^https?://.*",
            message = "URL deve começar com http:// ou https://"
    )
    private String originalUrl;

    private String customCode; // Opcional: código personalizado

    private Integer expirationDays; // Opcional: dias até expirar
}
