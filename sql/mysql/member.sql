-- 查询上级菜单、排序
SELECT parent_id, sort
INTO @parentId, @sort
FROM system_menu
WHERE name = '用户等级修改'
LIMIT 1;
-- 新增 按钮权限
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('用户积分修改', 'member:user:update-point', 3, @sort + 1, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('用户余额修改', 'member:user:update-balance', 3, @sort + 2, @parentId, '', '', '', 0);
