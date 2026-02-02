plugins {
    java
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
}
group = "com.lookfit"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

// 라이브러리 버전 관리
val springCloudAwsVersion = "3.1.0"
val jjwtVersion = "0.12.5"
val queryDslVersion = "5.0.0"

dependencies {
    // 1. Web & Validation
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // 2. Database & JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j")

    // 3. Querydsl (동적 쿼리 및 성능 최적화)
    implementation("com.querydsl:querydsl-jpa:$queryDslVersion:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:$queryDslVersion:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // 4. Redis (AI 결과 캐싱 및 세션 대용)
//    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // 5. AWS (S3 이미지 관리)
//    implementation("io.awspring.cloud:spring-cloud-aws-starter-s3:$springCloudAwsVersion")

    // 6. Security & Social Login (OAuth2)
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    // 7. JWT (인증 유지)
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    // 8. Search (ElasticSearch - 인기 검색어 및 로그 분석용)
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")

    // 9. Monitoring (부하 테스트 지표 측정용)
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // 10. Utils (Lombok & Mapper)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    implementation("org.modelmapper:modelmapper:3.2.0") // DTO 변환용 추천

    // 11. Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("com.h2database:h2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Querydsl QClass 생성 경로 설정
val querydslDir = "src/main/generated"

sourceSets {
    main {
        java {
            srcDirs("src/main/java", querydslDir)
        }
    }
}

tasks.withType<JavaCompile> {
    options.generatedSourceOutputDirectory.set(file(querydslDir))
}

tasks.named("clean") {
    doLast {
        file(querydslDir).deleteRecursively()
    }
}
group = "com"
version = "0.0.1-SNAPSHOT"
description = "LookFit"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}
