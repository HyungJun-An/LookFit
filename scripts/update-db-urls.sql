-- 로컬 이미지 URL로 업데이트 (개발 환경)
UPDATE file_resource SET s3_url = '/images/products/P001/main.jpg' WHERE domain_id = 'P001' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P002/main.jpg' WHERE domain_id = 'P002' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P003/main.jpg' WHERE domain_id = 'P003' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P004/main.jpg' WHERE domain_id = 'P004' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P005/main.jpg' WHERE domain_id = 'P005' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P006/main.jpg' WHERE domain_id = 'P006' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P007/main.jpg' WHERE domain_id = 'P007' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P008/main.jpg' WHERE domain_id = 'P008' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P009/main.jpg' WHERE domain_id = 'P009' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P010/main.jpg' WHERE domain_id = 'P010' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P011/main.jpg' WHERE domain_id = 'P011' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P012/main.jpg' WHERE domain_id = 'P012' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P013/main.jpg' WHERE domain_id = 'P013' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P014/main.jpg' WHERE domain_id = 'P014' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P015/main.jpg' WHERE domain_id = 'P015' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P016/main.jpg' WHERE domain_id = 'P016' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P017/main.jpg' WHERE domain_id = 'P017' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P018/main.jpg' WHERE domain_id = 'P018' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P019/main.jpg' WHERE domain_id = 'P019' AND domain_type = 'product';
UPDATE file_resource SET s3_url = '/images/products/P020/main.jpg' WHERE domain_id = 'P020' AND domain_type = 'product';

-- S3 URL로 업데이트 (프로덕션 환경) - 주석 처리
-- S3_BUCKET과 REGION을 실제 값으로 변경 후 사용
/*
UPDATE file_resource SET s3_url = 'https://lookfit-products.s3.ap-northeast-2.amazonaws.com/images/products/P001/main.jpg' WHERE domain_id = 'P001';
UPDATE file_resource SET s3_url = 'https://lookfit-products.s3.ap-northeast-2.amazonaws.com/images/products/P002/main.jpg' WHERE domain_id = 'P002';
-- ... (P003-P020도 동일하게)
*/
