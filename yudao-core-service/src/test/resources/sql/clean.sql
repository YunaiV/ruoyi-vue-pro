-- inf 开头的 DB
DELETE FROM "inf_api_access_log";
DELETE FROM "inf_file";
DELETE FROM "inf_api_error_log";

-- sys 开头的 DB
DELETE FROM "sys_user_session";
DELETE FROM "sys_dict_data";
DELETE FROM "sys_sms_template";
DELETE FROM "sys_sms_log";
DELETE FROM "sys_social_user";
