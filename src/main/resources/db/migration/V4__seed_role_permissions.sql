-- ============================================
-- Insert role_permissions - CHỈ CHẠY KHI CHƯA CÓ DỮ LIỆU
-- ============================================

-- Kiểm tra xem đã có dữ liệu trong bảng role_permission chưa
-- Nếu chưa có thì mới insert

-- ADMIN: toàn bộ permissions (dùng CROSS JOIN)
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r
         CROSS JOIN permission p
WHERE r.role_name = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM role_permission LIMIT 1);

-- STAFF permissions
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r
         CROSS JOIN permission p
WHERE r.role_name = 'STAFF'
  AND p.name IN (
                 'ORDER_VIEW',
                 'ORDER_CREATE',
                 'ORDER_UPDATE',
                 'ORDER_CANCEL',
                 'TABLE_VIEW',
                 'DISH_VIEW',
                 'CATEGORY_VIEW'
    )
  AND NOT EXISTS (SELECT 1 FROM role_permission WHERE role_id = r.role_id LIMIT 1);

-- CHEF permissions
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r
         CROSS JOIN permission p
WHERE r.role_name = 'CHEF'
  AND p.name IN (
                 'ORDER_VIEW',
                 'ORDER_UPDATE',
                 'DISH_VIEW',
                 'DISH_EDIT',
                 'CATEGORY_VIEW'
    )
  AND NOT EXISTS (SELECT 1 FROM role_permission WHERE role_id = r.role_id LIMIT 1);