package com.lookfit.product.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_resource")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResource {

    @Id
    @Column(name = "file_id", length = 30)
    private String fileId;

    @Column(name = "domain_type", length = 20, nullable = false)
    private String domainType; // 도메인구분

    @Column(name = "domain_id", length = 30, nullable = false)
    private String domainId; // 도메인_id

    @Column(name = "original_name", length = 255)
    private String originalName;

    @Column(name = "s3_url", length = 500, nullable = false)
    private String s3Url;

    @Column(name = "content_type", length = 50)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
