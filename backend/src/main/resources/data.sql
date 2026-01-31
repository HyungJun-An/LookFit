-- Test Product Data for LookFit
-- Korean Fashion E-commerce Test Data

-- Products (상품 데이터)
INSERT INTO product (pid, pname, pprice, pcategory, description, pcompany, pstock) VALUES
-- 아우터 (Outer)
('P001', '오버핏 울 코트', 189000, '아우터', '고급 울 소재 코트', 'AMORE', 50),
('P002', '숏 패딩 점퍼', 129000, '아우터', '가벼운 숏 패딩', 'NORTHFACE', 80),
('P003', '데님 트러커 자켓', 89000, '아우터', '클래식 자켓', 'LEVIS', 60),
('P004', '레더 라이더 자켓', 259000, '아우터', '정통 라이더', 'SCHOTT', 30),

-- 상의 (Top)
('P005', '베이직 티셔츠', 29000, '상의', '데일리 티셔츠', 'UNIQLO', 200),
('P006', '스트라이프 셔츠', 59000, '상의', '루즈핏 셔츠', 'MUJI', 100),
('P007', '캐시미어 니트', 129000, '상의', '캐시미어 혼방', 'COS', 70),
('P008', '후드 집업', 79000, '상의', '클래식 후드', 'CHAMPION', 150),

-- 하의 (Bottom)
('P009', '슬림핏 청바지', 89000, '하의', '슬림핏 데님', 'LEVIS', 120),
('P010', '와이드 슬랙스', 69000, '하의', '편안한 슬랙스', 'UNIQLO', 90),
('P011', '카고 조거', 79000, '하의', '실용적 카고', 'CARHARTT', 110),
('P012', '코듀로이 팬츠', 89000, '하의', '빈티지 코듀로이', 'DICKIES', 70),

-- 신발 (Shoes)
('P013', '화이트 스니커즈', 129000, '신발', '미니멀 스니커즈', 'COMMONPJ', 100),
('P014', '청크 러닝화', 159000, '신발', '볼륨감 러닝화', 'NEWBALANCE', 80),
('P015', '첼시 부츠', 189000, '신발', '클래식 부츠', 'DRMARTENS', 60),

-- 액세서리 (Accessory)
('P016', '크로스백', 89000, '가방', '심플 크로스백', 'NORTHFACE', 50),
('P017', '볼캡', 39000, '모자', '빈티지 볼캡', 'CARHARTT', 100),
('P018', '레더 벨트', 59000, '벨트', '가죽 벨트', 'ANDERSON', 80),

-- 여성 의류 (Women)
('P019', '플리츠 스커트', 69000, '치마', '우아한 스커트', 'ZARA', 70),
('P020', '오버핏 블레이저', 159000, '아우터', '모던 블레이저', 'MANGO', 60);

-- File Resources (상품 이미지)
INSERT INTO file_resource (file_id, domain_type, domain_id, s3_url, content_type, file_size) VALUES
-- P001 Images
('F001', 'product', 'P001', 'https://images.unsplash.com/photo-1539533018447-63fcce2678e3?w=800', 'image/jpeg', 102400),
('F002', 'product', 'P001', 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=800', 'image/jpeg', 98304),

-- P002 Images
('F003', 'product', 'P002', 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=800', 'image/jpeg', 115200),

-- P003 Images
('F004', 'product', 'P003', 'https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=800', 'image/jpeg', 109600),

-- P004 Images
('F005', 'product', 'P004', 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=800', 'image/jpeg', 112640),

-- P005 Images
('F006', 'product', 'P005', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800', 'image/jpeg', 95232),

-- P006 Images
('F007', 'product', 'P006', 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800', 'image/jpeg', 103424),

-- P007 Images
('F008', 'product', 'P007', 'https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=800', 'image/jpeg', 99328),

-- P008 Images
('F009', 'product', 'P008', 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=800', 'image/jpeg', 107520),

-- P009 Images
('F010', 'product', 'P009', 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=800', 'image/jpeg', 101376),

-- P010 Images
('F011', 'product', 'P010', 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=800', 'image/jpeg', 106496),

-- P011 Images
('F012', 'product', 'P011', 'https://images.unsplash.com/photo-1603252109303-2751441dd157?w=800', 'image/jpeg', 108544),

-- P012 Images
('F013', 'product', 'P012', 'https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=800', 'image/jpeg', 104448),

-- P013 Images
('F014', 'product', 'P013', 'https://images.unsplash.com/photo-1549298916-b41d501d3772?w=800', 'image/jpeg', 97280),

-- P014 Images
('F015', 'product', 'P014', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800', 'image/jpeg', 111616),

-- P015 Images
('F016', 'product', 'P015', 'https://images.unsplash.com/photo-1608256246200-53e635b5b65f?w=800', 'image/jpeg', 105472),

-- P016 Images
('F017', 'product', 'P016', 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800', 'image/jpeg', 100352),

-- P017 Images
('F018', 'product', 'P017', 'https://images.unsplash.com/photo-1588850561407-ed78c282e89b?w=800', 'image/jpeg', 94208),

-- P018 Images
('F019', 'product', 'P018', 'https://images.unsplash.com/photo-1624222247344-550fb60583c2?w=800', 'image/jpeg', 96256),

-- P019 Images
('F020', 'product', 'P019', 'https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=800', 'image/jpeg', 102400),

-- P020 Images
('F021', 'product', 'P020', 'https://images.unsplash.com/photo-1591369822096-ffd140ec948f?w=800', 'image/jpeg', 108544);
