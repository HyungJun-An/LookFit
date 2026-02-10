package com.lookfit.fitting.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Hugging Face Gradio Client (Python) 연동 서비스
 * IDM-VTON Space 사용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HuggingFaceGradioService {

    private final ObjectMapper objectMapper;

    @Value("${fitting.image.result-dir:src/main/resources/static/images/fitting/result}")
    private String resultDir;

    @Value("${fitting.image.upload-dir:src/main/resources/static/images/fitting/user}")
    private String uploadDir;

    private static final String PYTHON_SCRIPT = "scripts/virtual_tryon.py";
    private static final String STATIC_IMAGE_BASE = "src/main/resources/static/images";

    /**
     * Python Gradio Client를 사용한 가상 피팅 이미지 생성
     *
     * @param userImageUrl 사용자 이미지 URL (상대 경로)
     * @param garmentImageUrl 의류 이미지 URL (상대 경로)
     * @param category 카테고리
     * @return 생성된 이미지 URL (상대 경로)
     */
    public String generateVirtualTryOn(String userImageUrl, String garmentImageUrl, String category) {
        try {
            log.info("🐍 Python Gradio Client 호출 시작 - userImage: {}, garmentImage: {}, category: {}",
                    userImageUrl, garmentImageUrl, category);

            // URL을 로컬 파일 경로로 변환
            String userImagePath = convertUrlToLocalPath(userImageUrl);
            String garmentImagePath = convertUrlToLocalPath(garmentImageUrl);

            log.info("변환된 경로 - user: {}, garment: {}", userImagePath, garmentImagePath);

            // 1. Python 스크립트 실행
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python3",
                    PYTHON_SCRIPT,
                    userImagePath,
                    garmentImagePath,
                    category
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 2. 출력 읽기
            StringBuilder output = new StringBuilder();
            StringBuilder jsonOutput = new StringBuilder();
            boolean jsonStarted = false;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");

                    // JSON 블록 추출 ('{' 시작부터)
                    if (line.trim().startsWith("{")) {
                        jsonStarted = true;
                        jsonOutput = new StringBuilder();
                    }
                    if (jsonStarted) {
                        jsonOutput.append(line).append("\n");
                    }

                    log.debug("Python 출력: {}", line);
                }
            }

            // 3. 프로세스 완료 대기 (최대 5분)
            boolean finished = process.waitFor(5, TimeUnit.MINUTES);
            if (!finished) {
                process.destroy();
                throw new RuntimeException("Python 스크립트 타임아웃 (5분 초과)");
            }

            int exitCode = process.exitValue();
            String extractedJson = jsonOutput.toString().trim();

            log.info("Python 스크립트 종료 - exitCode: {}", exitCode);
            log.debug("추출된 JSON: {}", extractedJson);

            // 4. JSON 응답 파싱
            if (extractedJson.isEmpty()) {
                throw new RuntimeException("Python 스크립트에서 JSON 출력을 찾을 수 없습니다. 전체 출력: " + output.toString());
            }

            JsonNode result = objectMapper.readTree(extractedJson);

            if (!result.get("success").asBoolean()) {
                String error = result.has("error") ? result.get("error").asText() : "Unknown error";
                String errorType = result.has("error_type") ? result.get("error_type").asText() : "UNKNOWN";

                // GPU 할당량 초과 에러 처리
                if ("QUOTA_EXCEEDED".equals(errorType)) {
                    log.warn("GPU 할당량 초과: {}", error);
                    throw new com.lookfit.global.exception.BusinessException(
                            com.lookfit.global.exception.ErrorCode.GPU_QUOTA_EXCEEDED,
                            error
                    );
                }

                throw new RuntimeException("Python 스크립트 실패: " + error);
            }

            // 5. 생성된 이미지 파일 경로 (Gradio가 생성한 임시 파일)
            String resultImagePath = result.get("result_image").asText();
            log.info("Gradio 생성 이미지: {}", resultImagePath);

            // 6. 이미지를 우리 서버의 result 디렉토리로 복사
            String savedImageUrl = copyImageToResultDir(resultImagePath);

            log.info("✅ Hugging Face Gradio 완료 - resultUrl: {}", savedImageUrl);
            return savedImageUrl;

        } catch (Exception e) {
            log.error("❌ Python Gradio Client 실패 - userImage: {}, garmentImage: {}",
                    userImageUrl, garmentImageUrl, e);
            throw new RuntimeException("AI 이미지 생성 요청 실패: " + e.getMessage(), e);
        }
    }

    /**
     * URL을 로컬 파일 경로로 변환
     * 예: /images/fitting/user/test/abc.jpg → backend/src/main/resources/static/images/fitting/user/test/abc.jpg
     * 예: /images/products/P001 → backend/src/main/resources/static/images/products/P001/main.jpg
     */
    private String convertUrlToLocalPath(String relativeUrl) {
        // 상대 경로에서 /images/ 제거
        String path = relativeUrl.replaceFirst("^/images/", "");

        // 절대 경로 생성
        Path absolutePath = Paths.get(STATIC_IMAGE_BASE, path).toAbsolutePath();

        // products 디렉토리인 경우 main.jpg 추가
        if (path.startsWith("products/") && !path.endsWith(".jpg") && !path.endsWith(".png")) {
            absolutePath = Paths.get(absolutePath.toString(), "main.jpg");
        }

        log.debug("URL 변환: {} → {}", relativeUrl, absolutePath);
        return absolutePath.toString();
    }

    /**
     * Gradio 생성 이미지를 result 디렉토리로 복사
     */
    private String copyImageToResultDir(String gradioImagePath) throws IOException {
        // 파일명 생성
        String filename = UUID.randomUUID() + ".png";

        // 결과 디렉토리 생성
        Path resultPath = Paths.get(resultDir).toAbsolutePath();
        Files.createDirectories(resultPath);

        // 이미지 복사
        Path sourcePath = Paths.get(gradioImagePath);
        Path targetPath = resultPath.resolve(filename);
        Files.copy(sourcePath, targetPath);

        log.info("이미지 복사 완료: {} → {}", gradioImagePath, targetPath);

        // 상대 경로 반환
        return "/images/fitting/result/" + filename;
    }
}
