-- Chỉ insert dữ liệu nếu bảng permission chưa có dữ liệu
INSERT INTO permission (permission_id, name, resource)
SELECT * FROM (
                  SELECT UUID() as permission_id, 'ORDER_VIEW' as name, 'Order' as resource
                  UNION ALL SELECT UUID(), 'ORDER_CREATE', 'Order'
                  UNION ALL SELECT UUID(), 'ORDER_UPDATE', 'Order'
                  UNION ALL SELECT UUID(), 'ORDER_DELETE', 'Order'
                  UNION ALL SELECT UUID(), 'ORDER_CANCEL', 'Order'
                  UNION ALL SELECT UUID(), 'DISH_VIEW', 'Dish'
                  UNION ALL SELECT UUID(), 'DISH_CREATE', 'Dish'
                  UNION ALL SELECT UUID(), 'DISH_EDIT', 'Dish'
                  UNION ALL SELECT UUID(), 'DISH_DELETE', 'Dish'
                  UNION ALL SELECT UUID(), 'CATEGORY_CREATE', 'Category'
                  UNION ALL SELECT UUID(), 'CATEGORY_UPDATE', 'Category'
                  UNION ALL SELECT UUID(), 'CATEGORY_DELETE', 'Category'
                  UNION ALL SELECT UUID(), 'CATEGORY_VIEW', 'Category'
                  UNION ALL SELECT UUID(), 'TABLE_VIEW', 'DiningTable'
                  UNION ALL SELECT UUID(), 'TABLE_CREATE', 'DiningTable'
                  UNION ALL SELECT UUID(), 'TABLE_EDIT', 'DiningTable'
                  UNION ALL SELECT UUID(), 'TABLE_DELETE', 'DiningTable'
                  UNION ALL SELECT UUID(), 'REPORT_VIEW', 'Report'
                  UNION ALL SELECT UUID(), 'REPORT_EXPORT', 'Report'
                  UNION ALL SELECT UUID(), 'USER_VIEW', 'User'
                  UNION ALL SELECT UUID(), 'USER_CREATE', 'User'
                  UNION ALL SELECT UUID(), 'USER_EDIT', 'User'
                  UNION ALL SELECT UUID(), 'USER_DELETE', 'User'
                  UNION ALL SELECT UUID(), 'ROLE_VIEW', 'Role'
                  UNION ALL SELECT UUID(), 'ROLE_CREATE', 'Role'
                  UNION ALL SELECT UUID(), 'ROLE_EDIT', 'Role'
                  UNION ALL SELECT UUID(), 'ROLE_DELETE', 'Role'
              ) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM permission LIMIT 1);