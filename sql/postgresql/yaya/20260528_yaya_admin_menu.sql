-- Yaya admin menu and permissions for RuoYi dynamic routes.

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
) VALUES
  (9000, 'Yaya', '', 1, 80, 0, '/yaya', 'ep:reading', NULL, NULL, 0, true, true, true, 'admin', now(), 'admin', now(), 0),
  (9001, 'Content Topics', 'yaya:topic:query', 2, 1, 9000, 'content-topics', 'ep:collection', 'yaya/topic/index', 'YayaContentTopic', 0, true, true, true, 'admin', now(), 'admin', now(), 0),
  (9002, 'Import Batches', 'yaya:import:preview', 2, 2, 9000, 'import-batches', 'ep:upload-filled', 'yaya/import/index', 'YayaImportBatch', 0, true, true, true, 'admin', now(), 'admin', now(), 0),
  (9003, 'Member Plans', 'yaya:member-plan:query', 2, 3, 9000, 'member-plans', 'ep:medal', 'yaya/memberPlan/index', 'YayaMemberPlan', 0, true, true, true, 'admin', now(), 'admin', now(), 0),
  (9010, 'Topic Query', 'yaya:topic:query', 3, 1, 9001, '', '', '', NULL, 0, true, true, true, 'admin', now(), 'admin', now(), 0),
  (9011, 'Topic Create', 'yaya:topic:create', 3, 2, 9001, '', '', '', NULL, 0, true, true, true, 'admin', now(), 'admin', now(), 0),
  (9012, 'Topic Update', 'yaya:topic:update', 3, 3, 9001, '', '', '', NULL, 0, true, true, true, 'admin', now(), 'admin', now(), 0),
  (9013, 'Topic Publish', 'yaya:topic:publish', 3, 4, 9001, '', '', '', NULL, 0, true, true, true, 'admin', now(), 'admin', now(), 0),
  (9014, 'Import Preview', 'yaya:import:preview', 3, 1, 9002, '', '', '', NULL, 0, true, true, true, 'admin', now(), 'admin', now(), 0),
  (9015, 'Import Run', 'yaya:import:run', 3, 2, 9002, '', '', '', NULL, 0, true, true, true, 'admin', now(), 'admin', now(), 0),
  (9020, 'Member Plan Query', 'yaya:member-plan:query', 3, 1, 9003, '', '', '', NULL, 0, true, true, true, 'admin', now(), 'admin', now(), 0),
  (9021, 'Member Plan Create', 'yaya:member-plan:create', 3, 2, 9003, '', '', '', NULL, 0, true, true, true, 'admin', now(), 'admin', now(), 0),
  (9022, 'Member Plan Update', 'yaya:member-plan:update', 3, 3, 9003, '', '', '', NULL, 0, true, true, true, 'admin', now(), 'admin', now(), 0)
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  permission = EXCLUDED.permission,
  type = EXCLUDED.type,
  sort = EXCLUDED.sort,
  parent_id = EXCLUDED.parent_id,
  path = EXCLUDED.path,
  icon = EXCLUDED.icon,
  component = EXCLUDED.component,
  component_name = EXCLUDED.component_name,
  status = EXCLUDED.status,
  visible = EXCLUDED.visible,
  keep_alive = EXCLUDED.keep_alive,
  always_show = EXCLUDED.always_show,
  updater = 'admin',
  update_time = now(),
  deleted = 0;

SELECT setval(
  'system_menu_seq',
  GREATEST((SELECT last_value FROM system_menu_seq), (SELECT max(id) FROM system_menu))
);

INSERT INTO system_role_menu (
  id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id
)
SELECT nextval('system_role_menu_seq'), 1, menu_id, 'admin', now(), 'admin', now(), 0, 1
FROM (
  VALUES (9000), (9001), (9002), (9003),
         (9010), (9011), (9012), (9013), (9014), (9015),
         (9020), (9021), (9022)
) AS menus(menu_id)
WHERE NOT EXISTS (
  SELECT 1 FROM system_role_menu
  WHERE role_id = 1 AND tenant_id = 1 AND menu_id = menus.menu_id AND deleted = 0
);
