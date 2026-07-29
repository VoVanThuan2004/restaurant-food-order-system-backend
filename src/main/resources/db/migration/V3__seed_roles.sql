INSERT INTO role (role_id, role_name, description)
SELECT * FROM (
                  SELECT gen_random_uuid()::VARCHAR as role_id, 'ADMIN' as role_name, 'Quản trị viên hệ thống - Toàn quyền' as description
    UNION ALL SELECT gen_random_uuid()::VARCHAR, 'STAFF', 'Nhân viên phục vụ - Tạo bàn, gọi món ăn cho khách và thanh toán'
    UNION ALL SELECT gen_random_uuid()::VARCHAR, 'CHEF', 'Đầu bếp - Quản lý đơn hàng gọi món và quản lý trạng thái món ăn'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM role);