package cn.iocoder.yudao.module.system.enums;

/**
 * System 字典类型的枚举类
 *
 * @author 芋道源码
 */
public interface DictTypeConstants {

    String USER_TYPE = "user_type"; // 用户类型
    String COMMON_STATUS = "sys_common_status"; // 系统状态

    String USER_SEX = "sys_user_sex"; // 用户性别
    String OPERATE_TYPE = "sys_operate_type"; // 操作类型
    String LOGIN_TYPE = "sys_login_type"; // 登录日志的类型
    String LOGIN_RESULT = "sys_login_result"; // 登录结果
    String CONFIG_TYPE = "sys_config_type"; // 参数配置类型
    String BOOLEAN_STRING = "sys_boolean_string"; // Boolean 是否类型

    String SMS_CHANNEL_CODE = "sys_sms_channel_code"; // 短信渠道编码
    String SMS_TEMPLATE_TYPE = "sys_sms_template_type"; // 短信模板类型
    String SMS_SEND_STATUS = "sys_sms_send_status"; // 短信发送状态
    String SMS_RECEIVE_STATUS = "sys_sms_receive_status"; // 短信接收状态

    /**
     * 支付-订单-订单状态
     */
    String PAY_ORDER_STATUS = "pay_order_status";

    /**
     * 支付-订单-订单回调商户状态
     */
    String PAY_ORDER_NOTIFY_STATUS = "pay_order_notify_status";

    /**
     * 支付-订单-订单退款状态
     */
    String PAY_ORDER_REFUND_STATUS = "pay_order_refund_status";

    /**
     * 支付-退款订单-退款状态
     */
    String PAY_REFUND_ORDER_STATUS = "pay_refund_order_status";

    /**
     * 支付-退款订单-退款类别
     */
    String PAY_REFUND_ORDER_TYPE = "pay_refund_order_type";

    String BPM_TASK_ASSIGN_RULE_TYPE = "bpm_task_assign_rule_type"; // 任务分配规则类型
    String BPM_TASK_ASSIGN_SCRIPT = "bpm_task_assign_script"; // 任务分配自定义脚本

}
