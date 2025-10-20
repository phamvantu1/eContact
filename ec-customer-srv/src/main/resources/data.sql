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


INSERT INTO customers (
    id, name, email, password, phone, birthday, status, gender, tax_code, organization_id, sign_image, created_at, updated_at
) VALUES
      (1, 'Nguyễn Văn A', 'a@gmail.com', '123456', '0901234567', '1990-01-01', 1, 'male', '123456789', 1, '{}'::jsonb, NOW(), NOW()),
      (2, 'Trần Thị B', 'b@gmail.com', '123456', '0902345678', '1995-05-05', 1, 'female', '987654321', 2, '{}'::jsonb, NOW(), NOW()),
      (3, 'Lê Văn C', 'c@gmail.com', '123456', '0903456789', '2000-03-03', 1, 'male', '111222333', 3, '{}'::jsonb, NOW(), NOW());


INSERT INTO customer_roles (customer_id, role_id)
VALUES
    (1, 1),  -- Nguyễn Văn A là ADMIN
    (2, 2),  -- Trần Thị B là MANAGER
    (3, 3);  -- Lê Văn C là USER



