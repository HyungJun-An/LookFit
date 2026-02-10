package com.lookfit.fitting.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Hugging Face Spaces API 연동 서비스
 * Kolors Virtual Try-On 사용 (무료!)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HuggingFaceApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // IDM-VTON Space (Gradio 새 API 형식)
    private static final String HF_SPACE_BASE = "https://yisol-idm-vton.hf.space";
    private static final String API_NAME = "tryon";

    /**
     * Hugging Face Spaces API로 가상 피팅 이미지 생성 요청 (Gradio 새 형식)
     *
     * @param userImageUrl 사용자 이미지 URL
     * @param garmentImageUrl 의류 이미지 URL
     * @param category 카테고리 (upper_body, lower_body, dresses)
     * @return 생성된 이미지 URL
     */
    public String generateVirtualTryOn(String userImageUrl, String garmentImageUrl, String category) {
        try {
            log.info("🤗 Hugging Face API 호출 - userImage: {}, garmentImage: {}", userImageUrl, garmentImageUrl);

            // 1단계: POST /call/{api_name} - Event ID 받기
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("data", Arrays.asList(
                    userImageUrl,
                    garmentImageUrl,
                    category
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            String callUrl = HF_SPACE_BASE + "/call/" + API_NAME;
            ResponseEntity<String> callResponse = restTemplate.exchange(
                    callUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            // 응답 로그 출력
            String responseBody = callResponse.getBody();
            log.info("🔍 POST 응답: {}", responseBody);

            // Event ID 추출
            JsonNode callJson = objectMapper.readTree(responseBody);
            String eventId = callJson.get("event_id").asText();
            log.info("Event ID 받음: {}", eventId);

            // 2단계: GET /call/{api_name}/{event_id} - 결과 폴링 (SSE 형식)
            String resultUrl = HF_SPACE_BASE + "/call/" + API_NAME + "/" + eventId;
            int maxRetries = 60; // 최대 60초 대기

            for (int i = 0; i < maxRetries; i++) {
                Thread.sleep(1000); // 1초 대기

                ResponseEntity<String> resultResponse = restTemplate.getForEntity(resultUrl, String.class);
                String resultBody = resultResponse.getBody();

                if (resultBody != null && !resultBody.isEmpty()) {
                    log.debug("🔍 폴링 응답 ({}초): {}", i + 1, resultBody.substring(0, Math.min(200, resultBody.length())));

                    // SSE 형식 파싱: "data: {...}" 라인에서 JSON 추출
                    String[] lines = resultBody.split("\n");
                    for (String line : lines) {
                        if (line.startsWith("data: ")) {
                            String jsonData = line.substring(6); // "data: " 제거

                            try {
                                JsonNode dataJson = objectMapper.readTree(jsonData);

                                // 완료 확인: data 필드에 배열이 있으면 완료
                                if (dataJson.isArray() && dataJson.size() > 0) {
                                    String imageUrl = dataJson.get(0).asText();
                                    log.info("✅ Hugging Face AI 완료 - resultUrl: {}", imageUrl);
                                    return imageUrl;
                                }
                            } catch (Exception parseError) {
                                log.debug("JSON 파싱 실패 (진행 중일 수 있음): {}", jsonData);
                            }
                        }
                    }
                }
            }

            throw new RuntimeException("타임아웃: 60초 내에 결과를 받지 못했습니다.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("작업이 중단되었습니다.", e);
        } catch (Exception e) {
            log.error("❌ Hugging Face API 실패", e);
            throw new RuntimeException("AI 이미지 생성 요청 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 생성 상태 확인 (Hugging Face Spaces는 동기 방식이므로 즉시 완료)
     */
    public Map<String, Object> getGenerationStatus(String resultImageUrl) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "succeeded");
        result.put("output", resultImageUrl);
        return result;
    }
}
