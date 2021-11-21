-- inf 开头的 DB
DELETE FROM "inf_config";
DELETE FROM "inf_file";
DELETE FROM "inf_job";
DELETE FROM "inf_job_log";
DELETE FROM "inf_api_access_log";
DELETE FROM "inf_api_error_log";

-- sys 开头的 DB
DELETE FROM "sys_dept";
DELETE FROM "sys_dict_data";
DELETE FROM "sys_role";
DELETE FROM "sys_role_menu";
DELETE FROM "sys_menu";
DELETE FROM "sys_user_role";
DELETE FROM "sys_dict_type";
DELETE FROM "sys_user_session";
DELETE FROM "sys_post";
DELETE FROM "sys_login_log";
DELETE FROM "sys_operate_log";
DELETE FROM "sys_user";
DELETE FROM "sys_sms_channel";
DELETE FROM "sys_sms_template";
DELETE FROM "sys_sms_log";
DELETE FROM "sys_error_code";
DELETE FROM "sys_social_user";

-- pay 开头的 DB
DELETE FROM pay_merchant;
DELETE FROM pay_app;
DELETE FROM pay_channel
