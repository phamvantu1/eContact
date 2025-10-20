INSERT INTO organizations (id, name, email, status, tax_code, code, parent_id, created_at, updated_at)
VALUES
    (1, 'Công ty Phú Hòa', 'info@phuhoa.vn', 1, '123456789', 'ORG001', NULL, NOW(), NOW()),
    (2, 'Chi nhánh Hà Nội', 'hanoi@phuhoa.vn', 1, '987654321', 'ORG002', 1, NOW(), NOW()),
    (3, 'Chi nhánh TP.HCM', 'hcm@phuhoa.vn', 1, '567890123', 'ORG003', 1, NOW(), NOW());


INSERT INTO permissions (id, name, created_at, updated_at)
VALUES
    (1, 'CREATE_CUSTOMER', NOW(), NOW()),
    (2, 'VIEW_CUSTOMER', NOW(), NOW()),
    (3, 'UPDATE_CUSTOMER', NOW(), NOW()),
    (4, 'DELETE_CUSTOMER', NOW(), NOW()),
    (5, 'MANAGE_ROLE', NOW(), NOW());



INSERT INTO roles (id, name, status, created_at, updated_at)
VALUES
    (1, 'ADMIN', 1, NOW(), NOW()),
    (2, 'MANAGER', 1, NOW(), NOW()),
    (3, 'USER', 1, NOW(), NOW());


-- Bảng customers (20 bản ghi mẫu)
INSERT INTO customers (
    id, name, email, password, phone, birthday, status, gender, tax_code, organization_id, sign_image, created_at, updated_at
) VALUES
      (1, 'Nguyễn Văn A', 'a@gmail.com', '123456', '0901234567', '1990-01-01', 1, 'male', '123456789', 1, '{}'::jsonb, NOW(), NOW()),
      (2, 'Trần Thị B', 'b@gmail.com', '123456', '0902345678', '1995-05-05', 1, 'female', '987654321', 2, '{}'::jsonb, NOW(), NOW()),
      (3, 'Lê Văn C', 'c@gmail.com', '123456', '0903456789', '2000-03-03', 1, 'male', '111222333', 3, '{}'::jsonb, NOW(), NOW()),
      (4, 'Phạm Thị D', 'd@gmail.com', '123456', '0904567890', '1992-07-12', 1, 'female', '222333444', 1, '{}'::jsonb, NOW(), NOW()),
      (5, 'Hoàng Văn E', 'e@gmail.com', '123456', '0905678901', '1993-09-21', 1, 'male', '333444555', 2, '{}'::jsonb, NOW(), NOW()),
      (6, 'Đỗ Thị F', 'f@gmail.com', '123456', '0906789012', '1998-04-10', 1, 'female', '444555666', 3, '{}'::jsonb, NOW(), NOW()),
      (7, 'Phan Văn G', 'g@gmail.com', '123456', '0907890123', '1988-11-30', 1, 'male', '555666777', 1, '{}'::jsonb, NOW(), NOW()),
      (8, 'Bùi Thị H', 'h@gmail.com', '123456', '0908901234', '1997-02-14', 1, 'female', '666777888', 2, '{}'::jsonb, NOW(), NOW()),
      (9, 'Vũ Văn I', 'i@gmail.com', '123456', '0909012345', '1985-08-08', 1, 'male', '777888999', 3, '{}'::jsonb, NOW(), NOW()),
      (10, 'Ngô Thị J', 'j@gmail.com', '123456', '0910123456', '1994-12-25', 1, 'female', '888999000', 1, '{}'::jsonb, NOW(), NOW()),
      (11, 'Đinh Văn K', 'k@gmail.com', '123456', '0911234567', '1989-03-09', 1, 'male', '999000111', 2, '{}'::jsonb, NOW(), NOW()),
      (12, 'Trương Thị L', 'l@gmail.com', '123456', '0912345678', '1991-06-17', 1, 'female', '000111222', 3, '{}'::jsonb, NOW(), NOW()),
      (13, 'Nguyễn Văn M', 'm@gmail.com', '123456', '0913456789', '1987-10-10', 1, 'male', '111222333', 1, '{}'::jsonb, NOW(), NOW()),
      (14, 'Trần Thị N', 'n@gmail.com', '123456', '0914567890', '1996-01-05', 1, 'female', '222333444', 2, '{}'::jsonb, NOW(), NOW()),
      (15, 'Lê Văn O', 'o@gmail.com', '123456', '0915678901', '1990-08-19', 1, 'male', '333444555', 3, '{}'::jsonb, NOW(), NOW()),
      (16, 'Phạm Thị P', 'p@gmail.com', '123456', '0916789012', '1993-02-22', 1, 'female', '444555666', 1, '{}'::jsonb, NOW(), NOW()),
      (17, 'Hoàng Văn Q', 'q@gmail.com', '123456', '0917890123', '1999-09-09', 1, 'male', '555666777', 2, '{}'::jsonb, NOW(), NOW()),
      (18, 'Đỗ Thị R', 'r@gmail.com', '123456', '0918901234', '1994-05-27', 1, 'female', '666777888', 3, '{}'::jsonb, NOW(), NOW()),
      (19, 'Phan Văn S', 's@gmail.com', '123456', '0919012345', '1986-07-07', 1, 'male', '777888999', 1, '{}'::jsonb, NOW(), NOW()),
      (20, 'Bùi Thị T', 't@gmail.com', '123456', '0920123456', '1992-11-11', 1, 'female', '888999000', 2, '{}'::jsonb, NOW(), NOW());

-- Gán role cho từng customer (ADMIN=1, MANAGER=2, USER=3)
INSERT INTO customer_roles (customer_id, role_id)
VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 1),
    (5, 2),
    (6, 3),
    (7, 1),
    (8, 2),
    (9, 3),
    (10, 1),
    (11, 2),
    (12, 3),
    (13, 1),
    (14, 2),
    (15, 3),
    (16, 1),
    (17, 2),
    (18, 3),
    (19, 1),
    (20, 2);




