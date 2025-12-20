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
    (1, 'Tổ chức PTIT', 'ptit@gmail.com.vn', 1, '0123456789', 'PTIT', NULL, NOW(), NOW()),
    (2, 'Tổ chức chứng khoán', 'chungkhoan@gmail.com.vn', 1, '1234245678', 'CHUNGKHOAN', 1, NOW(), NOW()),
    (3, 'Tổ chức bảo hiểm', 'baohiem@gmail.com.vn', 1, '12341567', 'BAOHIEM', 2, NOW(), NOW()),
    (4, 'Tổ chức y tế', 'yte@gmail.com.vn', 1, '1234456', 'YTE', NULL, NOW(), NOW()),
    (5, 'Tổ chức kinh doanh', 'kinhdoanh@gmail.com.vn', 1, '1234456', 'KINHDOANH', 1, NOW(), NOW()),
    (6, 'Tổ chức HDPE', 'hdpe@gmail.com.vn', 1, '12443456', 'HDPE', NULL, NOW(), NOW()),
    (7, 'Tổ chức giáo dục', 'giaoduc@gmail.com.vn', 1, '1293456', 'GIAODUC', 1, NOW(), NOW()),
    (8, 'Tổ chức xây dựng', 'xaydung@gmail.com.vn', 1, '1234536', 'XAYDUNG', NULL, NOW(), NOW()),
    (9, 'Tổ chức tài chính', 'taichinh@gmail.com.vn', 1, '1239456', 'TAICHINH', NULL, NOW(), NOW()),
    (10, 'Tổ chức công nghệ', 'congnghe@gmail.com.vn', 1, '1223456', 'CONGNGHE', NULL, NOW(), NOW()),
    (11, 'Tổ chức buôn bán', 'buonban@gmail.com.vn', 1, '1233456', 'BUONBAN', NULL, NOW(), NOW()),
    (12, 'Tổ chức đào tạo', 'daotao@gmail.com.vn', 1, '12314576', 'DAOTAO', NULL, NOW(), NOW()),
    (13, 'Phòng tổng hợp', 'tonghop@gmail.com.vn', 1, '12379456', 'TONGHOP', NULL, NOW(), NOW()),
    (14, 'Phòng phát triển', 'phattrien@gmail.com.vn', 1, '12345976', 'PHATRIEN', NULL, NOW(), NOW()),
    (15, 'Phòng giáo vụ', 'giaovu@gmail.com.vn', 1, '12346656', 'GIAOVU', NULL, NOW(), NOW()),
    (16, 'Tổ chức công an', 'congan@gmail.com.vn', 1, '124435456', 'CONGAN', NULL, NOW(), NOW()),
    (17, 'Tổ chức ban hành', 'banhanh@gmail.com.vn', 1, '12323456', 'BANHANH', NULL, NOW(), NOW()),
    (18, 'Tổ chức giải trí', 'giaitri@gmail.com.vn', 1, '123234456', 'GIAITRI', NULL, NOW(), NOW()),
    (19, 'Tổ chức giao thương', 'giaothuong@gmail.com.vn', 1, '11123456', 'GIAOTHUONG', NULL, NOW(), NOW()),
    (20, 'Tổ chức hạ tầng', 'hatang@gmail.com.vn', 1, '12345456', 'HATANG', NULL, NOW(), NOW());

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
      (5, 'Đinh Khánh Ngân', 'khanhngan@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0905678901', '1993-09-21', 1, 'male', '333444555', 2, '{}'::jsonb, NOW(), NOW()),
      (6, 'Phạm Anh Tuấn', 'phamanhtuan@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0906789012', '1998-04-10', 1, 'female', '444555666', 3, '{}'::jsonb, NOW(), NOW()),
      (7, 'Phạm Thuý Vân', 'phamthuyvan@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0907890123', '1988-11-30', 1, 'male', '555666777', 1, '{}'::jsonb, NOW(), NOW()),
      (8, 'Đinh Đức Thuận', 'dinhducthuan@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0908901234', '1997-02-14', 1, 'female', '666777888', 2, '{}'::jsonb, NOW(), NOW()),
      (9, 'Trần Quang Huy', 'tranquanghuy@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0909012345', '1985-08-08', 1, 'male', '777888999', 3, '{}'::jsonb, NOW(), NOW()),
      (10, 'Nguyễn Minh Thuỳ', 'nguyenminhthuy@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0910123456', '1994-12-25', 1, 'female', '888999000', 1, '{}'::jsonb, NOW(), NOW()),
      (11, 'Phạm Như May', 'phamnhumay@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0911234567', '1989-03-09', 1, 'male', '999000111', 2, '{}'::jsonb, NOW(), NOW()),
      (12, 'Nguyễn Mạnh Trí', 'nguyenmanhtri@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0912345678', '1991-06-17', 1, 'female', '000111222', 3, '{}'::jsonb, NOW(), NOW()),
      (13, 'Nguyễn Viết Thọ', 'nguyenviettho@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0913456789', '1987-10-10', 1, 'male', '111222333', 1, '{}'::jsonb, NOW(), NOW()),
      (14, 'Nguyễn Hồng Nhung', 'nguyenhongnhung@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0914567890', '1996-01-05', 1, 'female', '222333444', 2, '{}'::jsonb, NOW(), NOW()),
      (15, 'Lê Văn Tuấn Đạt', 'levantuandat@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0915678901', '1990-08-19', 1, 'male', '333444555', 3, '{}'::jsonb, NOW(), NOW()),
      (16, 'Nguyễn Tiến Đạt', 'nguyentiendat@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0916789012', '1993-02-22', 1, 'female', '444555666', 1, '{}'::jsonb, NOW(), NOW()),
      (17, 'Lưu Lâm Thanh Tùng', 'luulamthanhtung@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0917890123', '1999-09-09', 1, 'male', '555666777', 2, '{}'::jsonb, NOW(), NOW()),
      (18, 'Vũ Huy Anh', 'vuhuyanh@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0918901234', '1994-05-27', 1, 'female', '666777888', 3, '{}'::jsonb, NOW(), NOW()),
      (19, 'Nguyễn Hồng Hạnh', 'nguyenhonghanh@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0919012345', '1986-07-07', 1, 'male', '777888999', 1, '{}'::jsonb, NOW(), NOW()),
      (20, 'Giang Thế Vũ', 'giangthevu@gmail.com', '$2a$10$UPF5aWUqKSeuoJR0kpSAKumzjjvqOh7VdpQHVP8ptBoReHO13LXqe', '0920123456', '1992-11-11', 1, 'female', '888999000', 2, '{}'::jsonb, NOW(), NOW());

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




