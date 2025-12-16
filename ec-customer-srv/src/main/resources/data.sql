-- DO
-- $$
-- DECLARE
-- r RECORD;
-- BEGIN
-- FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
--         EXECUTE 'TRUNCATE TABLE public.' || quote_ident(r.tablename) || ' RESTART IDENTITY CASCADE';
-- END LOOP;
-- END;
-- $$;



INSERT INTO organizations (id, name, email, status, tax_code, code, parent_id, created_at, updated_at)
VALUES
    (1, 'Tổ chức PTIT', 'ptit@phuhoa.vn', 1, '0123456789', 'PTIT', NULL, NOW(), NOW()),
    (2, 'Tổ chức chứng khoán', 'chungkhoan@phuhoa.vn', 1, '12345678', 'CHUNGKHOAN', 1, NOW(), NOW()),
    (3, 'Tổ chức bảo hiểm', 'baohiem@phuhoa.vn', 1, '1234567', 'BAOHIEM', 2, NOW(), NOW()),
    (4, 'Tổ chức y tế', 'yte@gmail.com.vn', 1, '123456', 'YTE', 1, NOW(), NOW()),
    (5, 'Tổ chức kinh doanh', 'kinhdoanh@gmail.com.vn', 1, '123456', 'KINHDOANH', 1, NOW(), NOW()),
    (6, 'Tổ chức HDPE', 'hdpe@gmail.com.vn', 1, '123456', 'HDPE', 1, NOW(), NOW()),
    (7, 'Tổ chức giáo dục', 'giaoduc@gmail.com.vn', 1, '123456', 'GIAODUC', 1, NOW(), NOW()),
    (8, 'Tổ chức xây dựng', 'xaydung@gmail.com.vn', 1, '123456', 'XAYDUNG', 1, NOW(), NOW()),
    (9, 'Tổ chức tài chính', 'taichinh@gmail.com.vn', 1, '123456', 'TAICHINH', 1, NOW(), NOW());


INSERT INTO permissions (id, name, created_at, updated_at)
VALUES
    (1, 'CREATE_CUSTOMER', NOW(), NOW()),
    (2, 'VIEW_CUSTOMER', NOW(), NOW()),
    (3, 'UPDATE_CUSTOMER', NOW(), NOW()),
    (4, 'DELETE_CUSTOMER', NOW(), NOW()),
    (5, 'VIEW_REPORT', NOW(), NOW()),
    (6, 'CONFIG_CERTIFICATE', NOW(), NOW()),
    (7, 'CONFIG_TYPE_DOCUMENT', NOW(), NOW()),
    (8, 'MANAGER_CUSTOMER', NOW(), NOW()),
    (9, 'MANAGER_ORGANIZATION', NOW(), NOW()),
    (10, 'MANAGER_ROLE', NOW(), NOW());


INSERT INTO roles (id, name, status, created_at, updated_at)
VALUES
    (1, 'ADMIN', 1, NOW(), NOW()),
    (2, 'USER', 1, NOW(), NOW());


-- Bảng customers (20 bản ghi mẫu)
INSERT INTO customers (
    id, name, email, password, phone, birthday, status, gender, tax_code, organization_id, sign_image, created_at, updated_at
) VALUES
      (1, 'Phạm Văn Tú', 'phamvantu.work@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0354808080', '2003-01-01', 1, 'male', '123456789', 1, '{}'::jsonb, NOW(), NOW()),
      (2, 'Nguyễn Thái Minh', 'thaiminhnguyen2003@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0902345678', '2003-05-05', 1, 'male', '12345678', 1, '{}'::jsonb, NOW(), NOW()),
      (3, 'Trần Tuấn Phúc', 'tranphuc120203@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0903456789', '2003-03-03', 1, 'male', '1234567', 1, '{}'::jsonb, NOW(), NOW()),
      (4, 'Phạm Hoàng Lâm', 'tutupham5@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0904567890', '1992-07-12', 1, 'female', '222333444', 1, '{}'::jsonb, NOW(), NOW()),
      (5, 'Hoàng Văn An', 'anhoang@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0905678901', '1993-09-21', 1, 'male', '333444555', 2, '{}'::jsonb, NOW(), NOW()),
      (6, 'Đỗ Thị Lụa', 'dothilua@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0906789012', '1998-04-10', 1, 'female', '444555666', 3, '{}'::jsonb, NOW(), NOW()),
      (7, 'Phan Văn Giang', 'giangphan@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0907890123', '1988-11-30', 1, 'male', '555666777', 1, '{}'::jsonb, NOW(), NOW()),
      (8, 'Bùi Thị Hoa', 'hoabui@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0908901234', '1997-02-14', 1, 'female', '666777888', 2, '{}'::jsonb, NOW(), NOW()),
      (9, 'Vũ Văn Tiến', 'tienvu@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0909012345', '1985-08-08', 1, 'male', '777888999', 3, '{}'::jsonb, NOW(), NOW()),
      (10, 'Ngô Thị Thương', 'thuongngo@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0910123456', '1994-12-25', 1, 'female', '888999000', 1, '{}'::jsonb, NOW(), NOW()),
      (11, 'Đinh Văn Kiên', 'kiendinh@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0911234567', '1989-03-09', 1, 'male', '999000111', 2, '{}'::jsonb, NOW(), NOW()),
      (12, 'Trương Thị Lan', 'lan@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0912345678', '1991-06-17', 1, 'female', '000111222', 3, '{}'::jsonb, NOW(), NOW()),
      (13, 'Nguyễn Văn Mùi', 'muinguyen@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0913456789', '1987-10-10', 1, 'male', '111222333', 1, '{}'::jsonb, NOW(), NOW()),
      (14, 'Trần Thị Nhung', 'nhungtran@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0914567890', '1996-01-05', 1, 'female', '222333444', 2, '{}'::jsonb, NOW(), NOW()),
      (15, 'Lê Văn Thọ', 'thole@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0915678901', '1990-08-19', 1, 'male', '333444555', 3, '{}'::jsonb, NOW(), NOW()),
      (16, 'Phạm Thị Thùy', 'thuypham@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0916789012', '1993-02-22', 1, 'female', '444555666', 1, '{}'::jsonb, NOW(), NOW()),
      (17, 'Hoàng Văn Quân', 'quanhoang@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0917890123', '1999-09-09', 1, 'male', '555666777', 2, '{}'::jsonb, NOW(), NOW()),
      (18, 'Đỗ Thị Hạnh', 'hanhdo@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0918901234', '1994-05-27', 1, 'female', '666777888', 3, '{}'::jsonb, NOW(), NOW()),
      (19, 'Phan Văn Sĩ', 'siphan@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0919012345', '1986-07-07', 1, 'male', '777888999', 1, '{}'::jsonb, NOW(), NOW()),
      (20, 'Bùi Thị Linh', 'linhbui@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0920123456', '1992-11-11', 1, 'female', '888999000', 2, '{}'::jsonb, NOW(), NOW());

-- Gán role cho từng customer (ADMIN=1, MANAGER=2, USER=3)
INSERT INTO customer_roles (customer_id, role_id)
VALUES
    (1, 1),
    (2, 1),
    (3, 1),
    (4, 1),
    (5, 2),
    (6, 1),
    (7, 1),
    (8, 2),
    (9, 1),
    (10, 1),
    (11, 2),
    (12, 1),
    (13, 1),
    (14, 1),
    (15, 1),
    (16, 1),
    (17, 2),
    (18, 1),
    (19, 1),
    (20, 2);

-- Đồng bộ lại sequence cho bảng organizations
SELECT setval(pg_get_serial_sequence('organizations', 'id'), COALESCE(MAX(id), 1)) FROM organizations;

-- Đồng bộ lại sequence cho bảng permissions
SELECT setval(pg_get_serial_sequence('permissions', 'id'), COALESCE(MAX(id), 1)) FROM permissions;

-- Đồng bộ lại sequence cho bảng roles
SELECT setval(pg_get_serial_sequence('roles', 'id'), COALESCE(MAX(id), 1)) FROM roles;

-- Đồng bộ lại sequence cho bảng customers
SELECT setval(pg_get_serial_sequence('customers', 'id'), COALESCE(MAX(id), 1)) FROM customers;

-- Nếu bảng customer_roles có id tự tăng thì thêm, còn nếu chỉ gồm (customer_id, role_id) thì KHÔNG cần
-- SELECT setval(pg_get_serial_sequence('customer_roles', 'id'), COALESCE(MAX(id), 1)) FROM customer_roles;




