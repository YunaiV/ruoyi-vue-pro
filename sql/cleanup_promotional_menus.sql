-- 清理推广菜单脚本
-- 适用于 MySQL 和 PostgreSQL
-- 执行此脚本可删除系统中的推广菜单项

-- 删除推广菜单
DELETE FROM system_menu WHERE id IN (1254, 2159, 2160);

-- 验证删除结果
SELECT id, name, path FROM system_menu WHERE id IN (1254, 2159, 2160);

-- 说明:
-- ID 1254: 作者动态 (https://www.iocoder.cn)
-- ID 2159: Boot 开发文档 (https://doc.iocoder.cn/)
-- ID 2160: Cloud 开发文档 (https://cloud.iocoder.cn)
