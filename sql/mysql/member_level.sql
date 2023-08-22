-- 增加字典
insert system_dict_type(name, type) values ('会员经验业务类型', 'member_experience_biz_type');
insert system_dict_data(dict_type, label, value, sort) values ('member_experience_biz_type', '管理员调整', '0', 0);
insert system_dict_data(dict_type, label, value, sort) values ('member_experience_biz_type', '邀新奖励', '1', 1);
insert system_dict_data(dict_type, label, value, sort) values ('member_experience_biz_type', '下单奖励', '2', 2);
insert system_dict_data(dict_type, label, value, sort) values ('member_experience_biz_type', '退单扣除', '3', 3);
insert system_dict_data(dict_type, label, value, sort) values ('member_experience_biz_type', '签到奖励', '4', 4);
insert system_dict_data(dict_type, label, value, sort) values ('member_experience_biz_type', '抽奖奖励', '5', 5);
