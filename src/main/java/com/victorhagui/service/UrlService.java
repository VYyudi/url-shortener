package com.victorhagui.service;

import com.victorhagui.dto.UrlRequestDto;
import com.victorhagui.dto.UrlResponseDto;
import com.victorhagui.models.Url;
import com.victorhagui.repository.UrlRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class UrlService {

    @Autowired
    private UrlRepository urlRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public UrlResponseDto shortenUrl(UrlRequestDto request) {
        Url url = new Url();
        url.setOriginalUrl(request.getOriginalUrl());

        // Se o usuário forneceu código customizado
        if (request.getCustomCode() != null && !request.getCustomCode().isEmpty()) {
            if (urlRepository.existsByShortCode(request.getCustomCode())) {
                throw new RuntimeException("Código customizado já está em uso");
            }
            url.setShortCode(request.getCustomCode());
        } else {
            // Gera código automático
            url.setShortCode(generateShortCode());
        }

        // Define expiração customizada
        if (request.getExpirationDays() != null) {
            url.setExpiresAt(LocalDateTime.now().plusDays(request.getExpirationDays()));
        }

        url = urlRepository.save(url);

        return convertToDto(url);
    }

    @Transactional
    public String getOriginalUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("URL não encontrada"));

        if (url.isExpired()) {
            throw new RuntimeException("URL expirada");
        }

        url.incrementClick();
        urlRepository.save(url);

        return url.getOriginalUrl();
    }

    public UrlResponseDto getStats(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("URL não encontrada"));

        return convertToDto(url);
    }

    private String generateShortCode() {
        String code;
        do {
            code = generateRandomCode(6);
        } while (urlRepository.existsByShortCode(code));

        return code;
    }

    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        return code.toString();
    }

    private UrlResponseDto convertToDto(Url url) {
        String shortUrl = baseUrl + "/" + url.getShortCode();

        return new UrlResponseDto(
                url.getOriginalUrl(),
                shortUrl,
                url.getShortCode(),
                url.getCreatedAt(),
                url.getExpiresAt(),
                url.getClickCount()
        );
    }
}
