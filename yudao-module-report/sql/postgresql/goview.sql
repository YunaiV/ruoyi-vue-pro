-- ----------------------------
-- GoView 数据可视化大屏项目表
-- ----------------------------
DROP SEQUENCE IF EXISTS report_go_view_project_seq;
CREATE SEQUENCE report_go_view_project_seq START WITH 1 INCREMENT BY 1;

DROP TABLE IF EXISTS report_go_view_project;
CREATE TABLE report_go_view_project (
  id INT8 NOT NULL DEFAULT nextval('report_go_view_project_seq'),
  name VARCHAR(200) NOT NULL,
  pic_url VARCHAR(500),
  content TEXT,
  status INT2 NOT NULL DEFAULT 0,
  remark VARCHAR(500),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id INT8 NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

COMMENT ON TABLE report_go_view_project IS 'GoView 数据可视化项目表';
COMMENT ON COLUMN report_go_view_project.id IS '编号';
COMMENT ON COLUMN report_go_view_project.name IS '项目名称';
COMMENT ON COLUMN report_go_view_project.pic_url IS '预览图片URL';
COMMENT ON COLUMN report_go_view_project.content IS '报表内容(JSON配置)';
COMMENT ON COLUMN report_go_view_project.status IS '发布状态(0-已发布 1-未发布)';
COMMENT ON COLUMN report_go_view_project.remark IS '项目备注';
COMMENT ON COLUMN report_go_view_project.creator IS '创建者';
COMMENT ON COLUMN report_go_view_project.create_time IS '创建时间';
COMMENT ON COLUMN report_go_view_project.updater IS '更新者';
COMMENT ON COLUMN report_go_view_project.update_time IS '更新时间';
COMMENT ON COLUMN report_go_view_project.deleted IS '是否删除';
COMMENT ON COLUMN report_go_view_project.tenant_id IS '租户编号';
