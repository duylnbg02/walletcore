-- 1. Xóa bảng trung gian
DROP TABLE user_roles;

-- 2. Thêm role_code vào users
ALTER TABLE users
    ADD COLUMN role_code VARCHAR(50) NOT NULL DEFAULT 'USER';

-- 3. Tạo khóa ngoại đến roles.code
ALTER TABLE users
    ADD CONSTRAINT fk_users_role_code
        FOREIGN KEY (role_code) REFERENCES roles(code);

-- 4. Tạo index
CREATE INDEX idx_users_role_code
    ON users(role_code);