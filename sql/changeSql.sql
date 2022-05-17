-- 增加title 2022-05-17
alter table tb_article add column title varchar(100) NULL DEFAULT '默认值' COMMENT '注释' after sender_name;