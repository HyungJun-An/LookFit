-- ============================================
-- LookFit Database Schema
-- ============================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. 회원 관련 테이블
-- ============================================

-- 회원 테이블
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
  `memberid` varchar(50) NOT NULL COMMENT '회원아이디',
  `membername` varchar(255) NOT NULL COMMENT '회원이름',
  `gender` varchar(1) DEFAULT NULL COMMENT '성별 (M, F)',
  `age` int DEFAULT NULL COMMENT '나이',
  `email` varchar(255) DEFAULT NULL COMMENT '이메일',
  `phone` varchar(20) DEFAULT NULL COMMENT '전화번호',
  `favorite` varchar(50) DEFAULT NULL COMMENT '선호도',
  `grade` varchar(20) DEFAULT NULL COMMENT '등급 (USER, ADMIN)',
  `enrolldate` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '최초가입일',
  `delflag` varchar(255) DEFAULT NULL COMMENT '삭제플래그',
  `deletedate` datetime DEFAULT NULL COMMENT '최종탈퇴일',
  `regflag` varchar(1) DEFAULT NULL COMMENT '가입구분 (S: Social, G: General)',
  `password` varchar(300) DEFAULT NULL COMMENT '비밀번호',
  PRIMARY KEY (`memberid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 소셜 계정 테이블
DROP TABLE IF EXISTS `social_account`;
CREATE TABLE `social_account` (
  `social_id` int NOT NULL AUTO_INCREMENT COMMENT '소셜아이디',
  `memberid` varchar(50) NOT NULL COMMENT '회원아이디',
  `provider` varchar(20) NOT NULL COMMENT '제공사 (google, kakao 등)',
  `provider_user_id` varchar(100) NOT NULL COMMENT '제공사회원아이디',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '회원가입일',
  PRIMARY KEY (`social_id`, `memberid`),
  KEY `FK_member_TO_social_account` (`memberid`),
  CONSTRAINT `FK_member_TO_social_account` FOREIGN KEY (`memberid`) REFERENCES `member` (`memberid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 사용자 주소 테이블
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
  `address_id` int NOT NULL AUTO_INCREMENT COMMENT '주소번호',
  `memberid` varchar(15) NOT NULL COMMENT '회원아이디',
  `address` varchar(100) DEFAULT NULL COMMENT '배송지',
  PRIMARY KEY (`address_id`),
  KEY `FK_member_TO_user_address` (`memberid`),
  CONSTRAINT `FK_member_TO_user_address` FOREIGN KEY (`memberid`) REFERENCES `member` (`memberid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 2. 상품 관련 테이블
-- ============================================

-- 상품 테이블
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `pID` varchar(30) NOT NULL COMMENT '상품아이디',
  `pname` varchar(30) DEFAULT NULL COMMENT '상품명',
  `pprice` decimal(10,0) DEFAULT NULL COMMENT '가격',
  `pcategory` varchar(30) DEFAULT NULL COMMENT '상품카테고리',
  `description` varchar(50) DEFAULT NULL COMMENT '상품설명',
  `pcompany` varchar(30) DEFAULT NULL COMMENT '제조회사',
  `pstock` int DEFAULT NULL COMMENT '상품수량',
  PRIMARY KEY (`pID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 상품 입출고 테이블
DROP TABLE IF EXISTS `product_io`;
CREATE TABLE `product_io` (
  `iono` int NOT NULL AUTO_INCREMENT COMMENT '입출고번호',
  `pID` varchar(30) NOT NULL COMMENT '상품아이디',
  `orderno` int NOT NULL COMMENT '주문번호',
  `amount` int DEFAULT NULL COMMENT '수량',
  `status` varchar(10) DEFAULT NULL COMMENT '입출고상태 (I: 입고, O: 출고)',
  `pDate` datetime DEFAULT NULL COMMENT '입출고날짜',
  `memberid` varchar(15) NOT NULL COMMENT '회원아이디',
  PRIMARY KEY (`iono`, `pID`, `orderno`),
  KEY `FK_product_TO_product_io` (`pID`),
  KEY `FK_buy_TO_product_io` (`orderno`),
  CONSTRAINT `FK_buy_TO_product_io` FOREIGN KEY (`orderno`) REFERENCES `buy` (`orderno`),
  CONSTRAINT `FK_product_TO_product_io` FOREIGN KEY (`pID`) REFERENCES `product` (`pID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 3. 장바구니 관련 테이블
-- ============================================

-- 장바구니 테이블
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `pID` varchar(30) NOT NULL COMMENT '상품아이디',
  `memberid` varchar(15) NOT NULL COMMENT '회원아이디',
  `pname` varchar(30) DEFAULT NULL COMMENT '상품명',
  `amount` int DEFAULT NULL COMMENT '수량',
  `pprice` decimal(10,0) DEFAULT NULL COMMENT '가격',
  PRIMARY KEY (`pID`, `memberid`),
  KEY `FK_member_TO_cart` (`memberid`),
  KEY `FK_product_TO_cart` (`pID`),
  CONSTRAINT `FK_member_TO_cart` FOREIGN KEY (`memberid`) REFERENCES `member` (`memberid`),
  CONSTRAINT `FK_product_TO_cart` FOREIGN KEY (`pID`) REFERENCES `product` (`pID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 4. 주문/결제 관련 테이블
-- ============================================

-- 주문 테이블
DROP TABLE IF EXISTS `buy`;
CREATE TABLE `buy` (
  `orderno` int NOT NULL AUTO_INCREMENT COMMENT '주문번호',
  `res_name` varchar(20) DEFAULT NULL COMMENT '받는사람이름',
  `res_address` varchar(100) DEFAULT NULL COMMENT '받는사람주소',
  `res_phone` char(11) DEFAULT NULL COMMENT '받는사람전화번호',
  `memberid` varchar(15) NOT NULL COMMENT '회원아이디',
  `res_requirement` varchar(100) DEFAULT NULL COMMENT '요청사항',
  `totalprice` decimal(10,0) DEFAULT NULL COMMENT '총가격',
  `orderdate` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '주문날짜',
  PRIMARY KEY (`orderno`),
  KEY `FK_member_TO_buy` (`memberid`),
  CONSTRAINT `FK_member_TO_buy` FOREIGN KEY (`memberid`) REFERENCES `member` (`memberid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 5. 게시판 관련 테이블
-- ============================================

-- Q&A 게시판 테이블
DROP TABLE IF EXISTS `b_qna`;
CREATE TABLE `b_qna` (
  `b_no` int NOT NULL AUTO_INCREMENT COMMENT '문의게시판번호',
  `b_title` varchar(50) DEFAULT NULL COMMENT '문의게시판제목',
  `b_pwd` int DEFAULT NULL COMMENT '비밀글비밀번호',
  `b_writer` varchar(15) DEFAULT NULL COMMENT '문의게시판작성자',
  `b_content` text COMMENT '게시글내용',
  `b_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '작성일자',
  `b_readcount` int DEFAULT '0' COMMENT '조회수',
  `b_category` varchar(50) DEFAULT NULL COMMENT '카테고리',
  `pID` varchar(30) DEFAULT NULL COMMENT '상품아이디',
  `lock_flag` char(1) DEFAULT 'N' COMMENT '게시글잠금여부',
  `deleted_at` datetime DEFAULT NULL COMMENT '삭제날짜',
  PRIMARY KEY (`b_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Q&A 댓글 테이블
DROP TABLE IF EXISTS `c_qna`;
CREATE TABLE `c_qna` (
  `c_no` int NOT NULL AUTO_INCREMENT COMMENT '문의게시판댓글번호',
  `c_ref` int NOT NULL COMMENT '문의게시판번호',
  `c_writer` varchar(15) DEFAULT NULL COMMENT '댓글작성자',
  `c_comment_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '댓글작성일자',
  `c_content` text COMMENT '댓글내용',
  `deleted_at` datetime DEFAULT NULL COMMENT '삭제날짜',
  PRIMARY KEY (`c_no`, `c_ref`),
  KEY `FK_b_qna_TO_c_qna` (`c_ref`),
  CONSTRAINT `FK_b_qna_TO_c_qna` FOREIGN KEY (`c_ref`) REFERENCES `b_qna` (`b_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 리뷰 게시판 테이블
DROP TABLE IF EXISTS `b_review`;
CREATE TABLE `b_review` (
  `b_no` int NOT NULL AUTO_INCREMENT COMMENT '후기게시판번호',
  `pID` varchar(30) NOT NULL COMMENT '상품아이디',
  `b_writer` varchar(15) DEFAULT NULL COMMENT '후기게시판작성자',
  `b_content` text COMMENT '후기내용',
  `rating` tinyint DEFAULT '5' COMMENT '별점 (1~5)',
  `b_original_filename` varchar(100) DEFAULT NULL COMMENT '사용자가올린첨부파일이름',
  `b_renamed_filename` varchar(100) DEFAULT NULL COMMENT '서버에저장된첨부파일이름',
  `b_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '등록날짜',
  `deleted_at` datetime DEFAULT NULL COMMENT '삭제날짜',
  PRIMARY KEY (`b_no`, `pID`),
  KEY `FK_product_TO_b_review` (`pID`),
  CONSTRAINT `FK_product_TO_b_review` FOREIGN KEY (`pID`) REFERENCES `product` (`pID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 6. 파일 리소스 테이블
-- ============================================

-- 파일 리소스 테이블
DROP TABLE IF EXISTS `file_resource`;
CREATE TABLE `file_resource` (
  `file_id` varchar(30) NOT NULL COMMENT '파일번호',
  `domain_type` varchar(20) NOT NULL COMMENT '도메인구분',
  `domain_id` varchar(30) NOT NULL COMMENT '도메인_id',
  `original_name` varchar(255) DEFAULT NULL COMMENT '원본파일이름',
  `s3_url` varchar(500) NOT NULL COMMENT 'S3링크',
  `content_type` varchar(50) DEFAULT NULL COMMENT '파일구분',
  `file_size` bigint DEFAULT NULL COMMENT '파일사이즈',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '등록날짜',
  PRIMARY KEY (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 7. 검색 로그 테이블
-- ============================================

-- 검색 로그 테이블
DROP TABLE IF EXISTS `search_log`;
CREATE TABLE `search_log` (
  `search_id` int NOT NULL AUTO_INCREMENT COMMENT '검색번호',
  `keyword` varchar(100) NOT NULL COMMENT '키워드',
  `searched_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '검색일자',
  `memberid` varchar(15) DEFAULT NULL COMMENT '회원아이디 (비회원은 NULL)',
  PRIMARY KEY (`search_id`),
  KEY `idx_search_log_memberid` (`memberid`),
  KEY `idx_search_keyword` (`keyword`, `searched_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
