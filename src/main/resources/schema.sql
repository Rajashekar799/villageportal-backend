CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    details VARCHAR(1000) NOT NULL,
    date_label VARCHAR(100)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(255),
    phone VARCHAR(30) NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS shops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_name VARCHAR(255) NOT NULL,
    owner_name VARCHAR(255) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    category VARCHAR(120) NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    problem_description VARCHAR(1200) NOT NULL,
    complaint_status VARCHAR(40) DEFAULT 'NEW'
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @complaint_status_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'complaints'
      AND column_name = 'complaint_status'
);

SET @complaint_status_sql := IF(
    @complaint_status_exists = 0,
    'ALTER TABLE complaints ADD COLUMN complaint_status VARCHAR(40) DEFAULT ''NEW''',
    'SELECT 1'
);

PREPARE complaint_status_stmt FROM @complaint_status_sql;
EXECUTE complaint_status_stmt;
DEALLOCATE PREPARE complaint_status_stmt;

CREATE TABLE IF NOT EXISTS gallery_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(120) NOT NULL,
    image_url VARCHAR(1000) NOT NULL,
    thumbnail_url VARCHAR(1000)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
