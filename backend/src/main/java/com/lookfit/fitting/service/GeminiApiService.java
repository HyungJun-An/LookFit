package com.lookfit.fitting.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

/**
 * Google Gemini API 연동 서비스 (Nano Banana)
 * Virtual Try-On 이미지 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiApiService {

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash-exp}")
    private String modelName;

    @Value("${fitting.image.result-dir:backend/src/main/resources/static/images/fitting/result}")
    private String resultDir;

    /**
     * Google Gemini API로 가상 피팅 이미지 생성
     *
     * @param userImagePath 사용자 이미지 절대 경로
     * @param garmentImagePath 의류 이미지 절대 경로
     * @param category 카테고리 (upper_body, lower_body, dresses)
     * @return 생성된 이미지 URL (상대 경로)
     */
    public String generateVirtualTryOn(String userImagePath, String garmentImagePath, String category) {
        try {
            log.info("🤖 Gemini API 호출 시작 - userImage: {}, garmentImage: {}, category: {}",
                    userImagePath, garmentImagePath, category);

            // 1. 이미지 파일 읽기
            byte[] userImageBytes = downloadImageAsBytes(userImagePath);
            byte[] garmentImageBytes = downloadImageAsBytes(garmentImagePath);

            // 2. Gemini 클라이언트 생성
            try (Client client = new Client.Builder()
                    .apiKey(apiKey)
                    .build()) {

                // 3. 프롬프트 생성 (카테고리별 차별화)
                String prompt = createPrompt(category);

                // 4. Gemini API 호출 (여러 이미지 + 텍스트 프롬프트)
                var response = client.models.generateContent(
                        modelName,
                        Content.fromParts(
                                Part.fromBytes(userImageBytes, "image/jpeg"),
                                Part.fromBytes(garmentImageBytes, "image/jpeg"),
                                Part.fromText(prompt)
                        ),
                        GenerateContentConfig.builder()
                                .responseModalities("TEXT", "IMAGE")
                                .build()
                );

                // 5. 응답에서 이미지 추출 및 저장
                String resultImageUrl = extractAndSaveImage(response);

                log.info("✅ Gemini AI 완료 - resultUrl: {}", resultImageUrl);
                return resultImageUrl;
            }

        } catch (Exception e) {
            log.error("❌ Gemini API 실패 - userImage: {}, garmentImage: {}",
                    userImagePath, garmentImagePath, e);
            throw new RuntimeException("AI 이미지 생성 요청 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 이미지 다운로드 (URL이면 다운로드, 로컬 경로면 읽기)
     */
    private byte[] downloadImageAsBytes(String imagePath) throws IOException {
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            // URL에서 다운로드
            log.debug("이미지 다운로드: {}", imagePath);
            return restTemplate.getForObject(imagePath, byte[].class);
        } else {
            // 로컬 파일 읽기
            Path path = Paths.get(imagePath);
            if (!Files.exists(path)) {
                throw new IOException("파일이 존재하지 않습니다: " + imagePath);
            }
            log.debug("로컬 이미지 읽기: {}", imagePath);
            return Files.readAllBytes(path);
        }
    }

    /**
     * 카테고리별 프롬프트 생성
     */
    private String createPrompt(String category) {
        return switch (category) {
            case "upper_body" -> """
                    Create a realistic virtual try-on image where the person in the first image is wearing the garment from the second image.
                    The garment should be a top or upper body clothing item.
                    Ensure the fit looks natural and the clothing matches the person's body posture and shape.
                    Preserve the person's face, hair, and background. Only replace the upper body clothing.
                    Make it look like a professional product photo.
                    """;
            case "lower_body" -> """
                    Create a realistic virtual try-on image where the person in the first image is wearing the garment from the second image.
                    The garment should be pants, skirt, or lower body clothing.
                    Ensure the fit looks natural and the clothing matches the person's body posture and shape.
                    Preserve the person's face, hair, upper body, and background. Only replace the lower body clothing.
                    Make it look like a professional product photo.
                    """;
            case "dresses" -> """
                    Create a realistic virtual try-on image where the person in the first image is wearing the dress from the second image.
                    Ensure the dress fits naturally to the person's body shape and posture.
                    Preserve the person's face, hair, and background. Replace the entire outfit with the dress.
                    Make it look like a professional product photo.
                    """;
            default -> """
                    Create a realistic virtual try-on image by combining the person from the first image with the clothing from the second image.
                    Make it look professional and natural.
                    """;
        };
    }

    /**
     * Gemini 응답에서 이미지 추출 및 로컬 저장
     */
    private String extractAndSaveImage(GenerateContentResponse response) throws IOException {
        for (Part part : Objects.requireNonNull(response.parts())) {
            if (part.inlineData().isPresent()) {
                var blob = part.inlineData().get();
                if (blob.data().isPresent()) {
                    byte[] imageData = blob.data().get();

                    // 파일명 생성 (UUID)
                    String filename = UUID.randomUUID() + ".png";

                    // 결과 디렉토리 생성
                    Path resultPath = Paths.get(resultDir).toAbsolutePath();
                    Files.createDirectories(resultPath);

                    // 이미지 저장
                    Path filePath = resultPath.resolve(filename);
                    Files.write(filePath, imageData);

                    log.info("이미지 저장 완료: {}", filePath);

                    // 상대 경로 반환
                    return "/images/fitting/result/" + filename;
                }
            }
        }

        throw new RuntimeException("Gemini 응답에서 이미지를 찾을 수 없습니다.");
    }
}
