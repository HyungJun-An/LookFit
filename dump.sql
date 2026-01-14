Enter password: 
-- MySQL dump 10.13  Distrib 9.5.0, for Linux (aarch64)
--
-- Host: localhost    Database: lookfit_db
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
Warning: A partial dump from a server that has GTIDs will by default include the GTIDs of all transactions, even those that changed suppressed parts of the database. If you don't want to restore GTIDs, pass --set-gtid-purged=OFF. To make a complete dump, pass --all-databases --triggers --routines --events. 
Warning: A dump from a server that has GTIDs enabled will by default include the GTIDs of all transactions, even those that were executed during its extraction and might not be represented in the dumped data. This might result in an inconsistent data dump. 
In order to ensure a consistent backup of the database, pass --single-transaction or --lock-all-tables or --source-data. 
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '01db9ca2-eb8c-11f0-9309-be4456abbe4d:1-40';

--
-- Table structure for table `b_qna`
--

DROP TABLE IF EXISTS `b_qna`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `b_qna`
--

LOCK TABLES `b_qna` WRITE;
/*!40000 ALTER TABLE `b_qna` DISABLE KEYS */;
/*!40000 ALTER TABLE `b_qna` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `b_review`
--

DROP TABLE IF EXISTS `b_review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
  PRIMARY KEY (`b_no`,`pID`),
  KEY `FK_product_TO_b_review` (`pID`),
  CONSTRAINT `FK_product_TO_b_review` FOREIGN KEY (`pID`) REFERENCES `product` (`pID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `b_review`
--

LOCK TABLES `b_review` WRITE;
/*!40000 ALTER TABLE `b_review` DISABLE KEYS */;
/*!40000 ALTER TABLE `b_review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `buy`
--

DROP TABLE IF EXISTS `buy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `buy`
--

LOCK TABLES `buy` WRITE;
/*!40000 ALTER TABLE `buy` DISABLE KEYS */;
/*!40000 ALTER TABLE `buy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `c_qna`
--

DROP TABLE IF EXISTS `c_qna`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `c_qna` (
  `c_no` int NOT NULL AUTO_INCREMENT COMMENT '문의게시판댓글번호',
  `c_ref` int NOT NULL COMMENT '문의게시판번호',
  `c_writer` varchar(15) DEFAULT NULL COMMENT '댓글작성자',
  `c_comment_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '댓글작성일자',
  `c_content` text COMMENT '댓글내용',
  `deleted_at` datetime DEFAULT NULL COMMENT '삭제날짜',
  PRIMARY KEY (`c_no`,`c_ref`),
  KEY `FK_b_qna_TO_c_qna` (`c_ref`),
  CONSTRAINT `FK_b_qna_TO_c_qna` FOREIGN KEY (`c_ref`) REFERENCES `b_qna` (`b_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `c_qna`
--

LOCK TABLES `c_qna` WRITE;
/*!40000 ALTER TABLE `c_qna` DISABLE KEYS */;
/*!40000 ALTER TABLE `c_qna` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart` (
  `pID` varchar(30) NOT NULL COMMENT '상품아이디',
  `memberid` varchar(15) NOT NULL COMMENT '회원아이디',
  `pname` varchar(30) DEFAULT NULL COMMENT '상품명',
  `amount` int DEFAULT NULL COMMENT '수량',
  `pprice` decimal(10,0) DEFAULT NULL COMMENT '가격',
  PRIMARY KEY (`pID`,`memberid`),
  KEY `FK_member_TO_cart` (`memberid`),
  CONSTRAINT `FK_member_TO_cart` FOREIGN KEY (`memberid`) REFERENCES `member` (`memberid`),
  CONSTRAINT `FK_product_TO_cart` FOREIGN KEY (`pID`) REFERENCES `product` (`pID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart`
--

LOCK TABLES `cart` WRITE;
/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file_resource`
--

DROP TABLE IF EXISTS `file_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_resource`
--

LOCK TABLES `file_resource` WRITE;
/*!40000 ALTER TABLE `file_resource` DISABLE KEYS */;
/*!40000 ALTER TABLE `file_resource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member` (
  `memberid` varchar(50) NOT NULL,
  `membername` varchar(255) NOT NULL,
  `gender` varchar(1) DEFAULT NULL,
  `age` int DEFAULT NULL COMMENT '나이',
  `email` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `favorite` varchar(50) DEFAULT NULL COMMENT '선호도',
  `grade` varchar(20) DEFAULT NULL,
  `enrolldate` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '최초가입일',
  `delflag` varchar(255) DEFAULT NULL,
  `deletedate` datetime DEFAULT NULL COMMENT '최종탈퇴일',
  `regflag` varchar(1) DEFAULT NULL,
  `password` varchar(300) DEFAULT NULL COMMENT '비밀번호',
  PRIMARY KEY (`memberid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member`
--

LOCK TABLES `member` WRITE;
/*!40000 ALTER TABLE `member` DISABLE KEYS */;
INSERT INTO `member` VALUES ('google_11510','안형준',NULL,NULL,'wns1265@gmail.com',NULL,NULL,'USER','2026-01-07 18:05:14','N',NULL,'S',NULL);
/*!40000 ALTER TABLE `member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_io`
--

DROP TABLE IF EXISTS `product_io`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_io` (
  `iono` int NOT NULL AUTO_INCREMENT COMMENT '입출고번호',
  `pID` varchar(30) NOT NULL COMMENT '상품아이디',
  `orderno` int NOT NULL COMMENT '주문번호',
  `amount` int DEFAULT NULL COMMENT '수량',
  `status` varchar(10) DEFAULT NULL COMMENT '입출고상태 (I, O)',
  `pDate` datetime DEFAULT NULL COMMENT '입출고날짜',
  `memberid` varchar(15) NOT NULL COMMENT '회원아이디',
  PRIMARY KEY (`iono`,`pID`,`orderno`),
  KEY `FK_product_TO_product_io` (`pID`),
  KEY `FK_buy_TO_product_io` (`orderno`),
  CONSTRAINT `FK_buy_TO_product_io` FOREIGN KEY (`orderno`) REFERENCES `buy` (`orderno`),
  CONSTRAINT `FK_product_TO_product_io` FOREIGN KEY (`pID`) REFERENCES `product` (`pID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_io`
--

LOCK TABLES `product_io` WRITE;
/*!40000 ALTER TABLE `product_io` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_io` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `search_log`
--

DROP TABLE IF EXISTS `search_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `search_log` (
  `search_id` int NOT NULL AUTO_INCREMENT COMMENT '검색번호',
  `keyword` varchar(100) NOT NULL COMMENT '키워드',
  `searched_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '검색일자',
  `memberid` varchar(15) DEFAULT NULL COMMENT '회원아이디 (비회원은 NULL)',
  PRIMARY KEY (`search_id`),
  KEY `idx_search_log_memberid` (`memberid`),
  KEY `idx_search_keyword` (`keyword`,`searched_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `search_log`
--

LOCK TABLES `search_log` WRITE;
/*!40000 ALTER TABLE `search_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `search_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `social_account`
--

DROP TABLE IF EXISTS `social_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `social_account` (
  `social_id` int NOT NULL AUTO_INCREMENT COMMENT '소셜아이디',
  `memberid` varchar(50) NOT NULL,
  `provider` varchar(20) NOT NULL COMMENT '제공사',
  `provider_user_id` varchar(100) NOT NULL COMMENT '제공사회원아이디',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '회원가입일',
  PRIMARY KEY (`social_id`,`memberid`),
  KEY `FK_member_TO_social_account` (`memberid`),
  CONSTRAINT `FK_member_TO_social_account` FOREIGN KEY (`memberid`) REFERENCES `member` (`memberid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `social_account`
--

LOCK TABLES `social_account` WRITE;
/*!40000 ALTER TABLE `social_account` DISABLE KEYS */;
INSERT INTO `social_account` VALUES (1,'google_11510','google','115105742901086543031','2026-01-07 18:05:14');
/*!40000 ALTER TABLE `social_account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_address`
--

DROP TABLE IF EXISTS `user_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_address` (
  `address_id` int NOT NULL AUTO_INCREMENT,
  `memberid` varchar(15) NOT NULL COMMENT '회원아이디',
  `address` varchar(100) DEFAULT NULL COMMENT '배송지',
  PRIMARY KEY (`address_id`),
  KEY `FK_member_TO_user_address` (`memberid`),
  CONSTRAINT `FK_member_TO_user_address` FOREIGN KEY (`memberid`) REFERENCES `member` (`memberid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_address`
--

LOCK TABLES `user_address` WRITE;
/*!40000 ALTER TABLE `user_address` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_address` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-14  4:13:15
