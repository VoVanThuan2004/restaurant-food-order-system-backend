INSERT INTO role (role_id, role_name, description)
SELECT * FROM (
                  SELECT UUID() as role_id, 'ADMIN' as role_name, 'Quản trị viên hệ thống - Toàn quyền' as description
                  UNION ALL SELECT UUID(), 'STAFF', 'Nhân viên phục vụ - Tạo bàn, gọi món ăn cho khách và thanh toán'
                  UNION ALL SELECT UUID(), 'CHEF', 'Đầu bếp - Quản lý đơn hàng gọi món và quản lý trạng thái món ăn'
              ) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM role LIMIT 1);