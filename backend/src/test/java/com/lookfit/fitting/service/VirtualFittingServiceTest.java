package com.lookfit.fitting.service;

import com.lookfit.fitting.domain.FittingStatus;
import com.lookfit.fitting.domain.VirtualFitting;
import com.lookfit.fitting.dto.FittingDto;
import com.lookfit.fitting.repository.VirtualFittingRepository;
import com.lookfit.global.exception.BusinessException;
import com.lookfit.global.exception.ErrorCode;
import com.lookfit.product.domain.Product;
import com.lookfit.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * VirtualFittingService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class VirtualFittingServiceTest {

    @Mock
    private VirtualFittingRepository fittingRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private VirtualFittingService fittingService;

    private Product testProduct;
    private MultipartFile testImageFile;

    @BeforeEach
    void setUp() {
        // 업로드 디렉토리 설정
        ReflectionTestUtils.setField(fittingService, "uploadDir", "/tmp/fitting_test");

        // 테스트용 상품
        testProduct = Product.builder()
                .productId("P001")
                .productName("테스트 상품")
                .productPrice(BigDecimal.valueOf(50000))
                .productStock(10)
                .productCategory("상의")
                .build();

        // 테스트용 이미지 파일
        testImageFile = new MockMultipartFile(
                "image",
                "test_user.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    @Test
    @DisplayName("사용자 이미지 업로드 성공")
    void uploadUserImage_Success() {
        // given
        String memberId = "test_member";
        String productId = "P001";
        String category = "upper_body";

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(fittingRepository.save(any(VirtualFitting.class))).thenAnswer(invocation -> {
            VirtualFitting fitting = invocation.getArgument(0);
            return fitting;
        });

        // when
        FittingDto.UploadResponse response = fittingService.uploadUserImage(
                memberId,
                productId,
                category,
                testImageFile
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.getFittingId()).isNotNull();
        assertThat(response.getUserImageUrl()).contains("/images/fitting/user/");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getMessage()).contains("업로드 완료");

        verify(productRepository, times(1)).findById(productId);
        verify(fittingRepository, times(1)).save(any(VirtualFitting.class));
    }

    @Test
    @DisplayName("존재하지 않는 상품으로 업로드 시 예외 발생")
    void uploadUserImage_ProductNotFound() {
        // given
        String memberId = "test_member";
        String productId = "INVALID_ID";
        String category = "upper_body";

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fittingService.uploadUserImage(
                memberId,
                productId,
                category,
                testImageFile
        ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);

        verify(productRepository, times(1)).findById(productId);
        verify(fittingRepository, never()).save(any());
    }

    @Test
    @DisplayName("잘못된 카테고리로 업로드 시 예외 발생")
    void uploadUserImage_InvalidCategory() {
        // given
        String memberId = "test_member";
        String productId = "P001";
        String category = "invalid_category";

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // when & then
        assertThatThrownBy(() -> fittingService.uploadUserImage(
                memberId,
                productId,
                category,
                testImageFile
        ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE)
                .hasMessageContaining("유효하지 않은 카테고리");

        verify(productRepository, times(1)).findById(productId);
        verify(fittingRepository, never()).save(any());
    }

    @Test
    @DisplayName("빈 이미지 파일로 업로드 시 예외 발생")
    void uploadUserImage_EmptyFile() {
        // given
        String memberId = "test_member";
        String productId = "P001";
        String category = "upper_body";
        MultipartFile emptyFile = new MockMultipartFile(
                "image",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // when & then
        assertThatThrownBy(() -> fittingService.uploadUserImage(
                memberId,
                productId,
                category,
                emptyFile
        ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE)
                .hasMessageContaining("이미지 파일이 비어있습니다");

        verify(fittingRepository, never()).save(any());
    }

    @Test
    @DisplayName("파일 크기 초과 시 예외 발생")
    void uploadUserImage_FileTooLarge() {
        // given
        String memberId = "test_member";
        String productId = "P001";
        String category = "upper_body";

        // 11MB 파일 생성
        byte[] largeContent = new byte[11 * 1024 * 1024];
        MultipartFile largeFile = new MockMultipartFile(
                "image",
                "large.jpg",
                "image/jpeg",
                largeContent
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // when & then
        assertThatThrownBy(() -> fittingService.uploadUserImage(
                memberId,
                productId,
                category,
                largeFile
        ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE)
                .hasMessageContaining("10MB를 초과할 수 없습니다");

        verify(fittingRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미지가 아닌 파일 업로드 시 예외 발생")
    void uploadUserImage_NotImageFile() {
        // given
        String memberId = "test_member";
        String productId = "P001";
        String category = "upper_body";
        MultipartFile textFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "not an image".getBytes()
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // when & then
        assertThatThrownBy(() -> fittingService.uploadUserImage(
                memberId,
                productId,
                category,
                textFile
        ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE)
                .hasMessageContaining("이미지 파일만 업로드 가능합니다");

        verify(fittingRepository, never()).save(any());
    }

    @Test
    @DisplayName("유효한 카테고리 검증 - upper_body")
    void validateCategory_UpperBody() {
        // given
        when(productRepository.findById("P001")).thenReturn(Optional.of(testProduct));
        when(fittingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when & then
        assertThatCode(() -> fittingService.uploadUserImage(
                "test_member",
                "P001",
                "upper_body",
                testImageFile
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("유효한 카테고리 검증 - lower_body")
    void validateCategory_LowerBody() {
        // given
        when(productRepository.findById("P001")).thenReturn(Optional.of(testProduct));
        when(fittingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when & then
        assertThatCode(() -> fittingService.uploadUserImage(
                "test_member",
                "P001",
                "lower_body",
                testImageFile
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("유효한 카테고리 검증 - dresses")
    void validateCategory_Dresses() {
        // given
        when(productRepository.findById("P001")).thenReturn(Optional.of(testProduct));
        when(fittingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when & then
        assertThatCode(() -> fittingService.uploadUserImage(
                "test_member",
                "P001",
                "dresses",
                testImageFile
        )).doesNotThrowAnyException();
    }
}
