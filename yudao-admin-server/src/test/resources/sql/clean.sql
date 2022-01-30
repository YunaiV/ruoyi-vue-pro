-- inf 开头的 DB
DELETE FROM "inf_config";
DELETE FROM "inf_file";
DELETE FROM "inf_job";
DELETE FROM "inf_job_log";
DELETE FROM "inf_api_access_log";
DELETE FROM "inf_api_error_log";

-- pay 开头的 DB
DELETE FROM pay_merchant;
DELETE FROM pay_app;
DELETE FROM pay_channel;
DELETE FROM pay_order;
DELETE FROM pay_refund;

-- bpm 开头的 DB
DELETE FROM "bpm_form";
DELETE FROM "bpm_user_group";
