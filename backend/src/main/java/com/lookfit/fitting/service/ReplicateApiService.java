package com.lookfit.fitting.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Replicate API 연동 서비스
 * IDM-VTON 모델을 사용한 가상 피팅 이미지 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReplicateApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${fitting.replicate.api-key:}")
    private String apiKey;

    @Value("${fitting.replicate.model-version:}")
    private String modelVersion;

    private static final String REPLICATE_API_URL = "https://api.replicate.com/v1/predictions";

    /**
     * Replicate API로 가상 피팅 이미지 생성 요청
     *
     * @param userImageUrl 사용자 이미지 URL
     * @param garmentImageUrl 의류 이미지 URL
     * @param category 카테고리 (upper_body, lower_body, dresses)
     * @return Prediction ID
     */
    public String createPrediction(String userImageUrl, String garmentImageUrl, String category) {
        try {
            // API 키 확인
            if (apiKey == null || apiKey.isEmpty()) {
                log.warn("⚠️ Replicate API 키가 설정되지 않음 - Mock 응답 반환");
                return "mock_prediction_" + System.currentTimeMillis();
            }

            // 요청 바디 생성
            Map<String, Object> input = new HashMap<>();
            input.put("garm_img", garmentImageUrl);
            input.put("human_img", userImageUrl);
            input.put("category", category);
            input.put("garment_des", "a clothing item");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("version", modelVersion);
            requestBody.put("input", input);

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Token " + apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // API 호출
            log.info("Replicate API 호출 - userImage: {}, garmentImage: {}, category: {}",
                    userImageUrl, garmentImageUrl, category);

            ResponseEntity<String> response = restTemplate.exchange(
                    REPLICATE_API_URL,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            // 응답 파싱
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            String predictionId = jsonResponse.get("id").asText();

            log.info("Replicate Prediction 생성 완료 - predictionId: {}", predictionId);
            return predictionId;

        } catch (HttpClientErrorException e) {
            // 402 Payment Required - 크레딧 부족, Mock 모드로 폴백
            if (e.getStatusCode() == HttpStatus.PAYMENT_REQUIRED) {
                log.warn("⚠️ Replicate 크레딧 부족 (402 Payment Required) - Mock 응답 반환");
                log.warn("💳 크레딧 충전: https://replicate.com/account/billing#billing");
                return "mock_prediction_" + System.currentTimeMillis();
            }
            // 기타 4xx 에러
            log.error("Replicate API 클라이언트 에러 ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 이미지 생성 요청 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Replicate API 호출 실패", e);
            throw new RuntimeException("AI 이미지 생성 요청 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Prediction 상태 조회
     *
     * @param predictionId Prediction ID
     * @return 상태 정보 (status, output 등)
     */
    public Map<String, Object> getPredictionStatus(String predictionId) {
        try {
            // Mock 응답 처리
            if (predictionId.startsWith("mock_prediction_")) {
                return createMockPredictionStatus(predictionId);
            }

            // API 키 확인
            if (apiKey == null || apiKey.isEmpty()) {
                log.warn("⚠️ Replicate API 키가 설정되지 않음");
                return createMockPredictionStatus(predictionId);
            }

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Token " + apiKey);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            // API 호출
            String url = REPLICATE_API_URL + "/" + predictionId;
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            // 응답 파싱
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());

            Map<String, Object> result = new HashMap<>();
            result.put("status", jsonResponse.get("status").asText());

            if (jsonResponse.has("output") && !jsonResponse.get("output").isNull()) {
                result.put("output", jsonResponse.get("output").asText());
            }

            if (jsonResponse.has("error") && !jsonResponse.get("error").isNull()) {
                result.put("error", jsonResponse.get("error").asText());
            }

            log.debug("Prediction 상태 조회 - predictionId: {}, status: {}", predictionId, result.get("status"));
            return result;

        } catch (Exception e) {
            log.error("Prediction 상태 조회 실패 - predictionId: {}", predictionId, e);
            throw new RuntimeException("AI 이미지 생성 상태 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Mock Prediction 상태 생성 (테스트용)
     */
    private Map<String, Object> createMockPredictionStatus(String predictionId) {
        Map<String, Object> result = new HashMap<>();

        // Mock: 10초 후 완료된 것으로 시뮬레이션
        long elapsed = System.currentTimeMillis() - Long.parseLong(predictionId.replace("mock_prediction_", ""));
        if (elapsed > 10000) { // 10초 경과
            result.put("status", "succeeded");
            // Mock 테스트용 의류 착용 이미지 (Unsplash - fashion model)
            result.put("output", "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=512&h=768&fit=crop");
        } else {
            result.put("status", "processing");
        }

        return result;
    }
}
