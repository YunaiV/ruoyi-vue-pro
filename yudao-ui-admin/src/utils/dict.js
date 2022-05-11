/**
 * Created by 芋道源码
 *
 * 数据字典工具类
 */
import store from '@/store'

export const DICT_TYPE = {
  USER_TYPE: 'user_type',
  COMMON_STATUS: 'common_status',

  // ========== SYSTEM 模块 ==========
  SYSTEM_USER_SEX: 'system_user_sex',
  SYSTEM_MENU_TYPE: 'system_menu_type',
  SYSTEM_ROLE_TYPE: 'system_role_type',
  SYSTEM_DATA_SCOPE: 'system_data_scope',
  SYSTEM_NOTICE_TYPE: 'system_notice_type',
  SYSTEM_OPERATE_TYPE: 'system_operate_type',
  SYSTEM_LOGIN_TYPE: 'system_login_type',
  SYSTEM_LOGIN_RESULT: 'system_login_result',
  SYSTEM_SMS_CHANNEL_CODE: 'system_sms_channel_code',
  SYSTEM_SMS_TEMPLATE_TYPE: 'system_sms_template_type',
  SYSTEM_SMS_SEND_STATUS: 'system_sms_send_status',
  SYSTEM_SMS_RECEIVE_STATUS: 'system_sms_receive_status',
  SYSTEM_ERROR_CODE_TYPE: 'system_error_code_type',
  SYSTEM_OAUTH2_GRANT_TYPE: 'system_oauth2_grant_type',

  // ========== INFRA 模块 ==========
  INFRA_BOOLEAN_STRING: 'infra_boolean_string',
  INFRA_REDIS_TIMEOUT_TYPE: 'infra_redis_timeout_type',
  INFRA_JOB_STATUS: 'infra_job_status',
  INFRA_JOB_LOG_STATUS: 'infra_job_log_status',
  INFRA_API_ERROR_LOG_PROCESS_STATUS: 'infra_api_error_log_process_status',
  INFRA_CONFIG_TYPE: 'infra_config_type',
  INFRA_CODEGEN_TEMPLATE_TYPE: 'infra_codegen_template_type',
  INFRA_CODEGEN_SCENE: 'infra_codegen_scene',
  INFRA_FILE_STORAGE: 'infra_file_storage',

  // ========== BPM 模块 ==========
  BPM_MODEL_CATEGORY: 'bpm_model_category',
  BPM_MODEL_FORM_TYPE: 'bpm_model_form_type',
  BPM_TASK_ASSIGN_RULE_TYPE: 'bpm_task_assign_rule_type',
  BPM_PROCESS_INSTANCE_STATUS: 'bpm_process_instance_status',
  BPM_PROCESS_INSTANCE_RESULT: 'bpm_process_instance_result',
  BPM_TASK_ASSIGN_SCRIPT: 'bpm_task_assign_script',
  BPM_OA_LEAVE_TYPE: 'bpm_oa_leave_type',

  // ========== PAY 模块 ==========
  PAY_CHANNEL_WECHAT_VERSION: 'pay_channel_wechat_version', // 微信渠道版本
  PAY_CHANNEL_ALIPAY_SIGN_TYPE: 'pay_channel_alipay_sign_type', // 支付渠道支付宝算法类型
  PAY_CHANNEL_ALIPAY_MODE: 'pay_channel_alipay_mode', // 支付宝公钥类型
  PAY_CHANNEL_ALIPAY_SERVER_TYPE: 'pay_channel_alipay_server_type', // 支付宝网关地址
  PAY_CHANNEL_CODE_TYPE: 'pay_channel_code_type', // 支付渠道编码类型
  PAY_ORDER_NOTIFY_STATUS: 'pay_order_notify_status', // 商户支付订单回调状态
  PAY_ORDER_STATUS: 'pay_order_status', // 商户支付订单状态
  PAY_ORDER_REFUND_STATUS: 'pay_order_refund_status', // 商户支付订单退款状态
  PAY_REFUND_ORDER_STATUS: 'pay_refund_order_status', // 退款订单状态
  PAY_REFUND_ORDER_TYPE: 'pay_refund_order_type', // 退款订单类别
}

/**
 * 获取 dictType 对应的数据字典数组
 *
 * @param dictType 数据类型
 * @returns {*|Array} 数据字典数组
 */
export function getDictDatas(dictType) {
  return store.getters.dict_datas[dictType] || []
}

/**
 * 获取 dictType 对应的数据字典数组
 *
 * @param dictType 数据类型
 * @param values 数组、单个元素
 * @returns {*|Array} 数据字典数组
 */
export function getDictDatas2(dictType, values) {
  if (values === undefined) {
    return [];
  }
  // 如果是单个元素，则转换成数组
  if (!Array.isArray(values)) {
    values = [this.value];
  }
  // 获得字典数据
  const results = [];
  for (const value of values) {
    const dict = getDictData(dictType, value);
    if (dict) {
      results.push(dict);
    }
  }
  return results;
}

export function getDictData(dictType, value) {
  // 获取 dictType 对应的数据字典数组
  const dictDatas = getDictDatas(dictType)
  if (!dictDatas || dictDatas.length === 0) {
    return ''
  }
  // 获取 value 对应的展示名
  value = value + '' // 强制转换成字符串，因为 DictData 小类数值，是字符串
  for (const dictData of dictDatas) {
    if (dictData.value === value) {
      return dictData;
    }
  }
  return undefined
}

export function getDictDataLabel(dictType, value) {
  const dict = getDictData(dictType, value);
  return dict ? dict.label : '';
}

export class getDictDataL {
}
