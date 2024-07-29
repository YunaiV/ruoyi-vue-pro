package cn.iocoder.yudao.module.pay.enums;

// TODO @芋艿：枚举

/**
 * 通知模板枚举类
 *
 * @author HUIHUI
 */
public interface MessageTemplateConstants {

    //======================= 小程序订阅消息 =======================

    String PAY_WALLET_CHANGE = "充值成功通知";

    /**
     * 充值成功通知模版参数
     *
     * @author HUIHUI
     */
    class PayWalletChangeTemplateParams {

        public static final String NO = "character_string1"; // 流水编号
        public static final String PRICE = "amount2"; // 充值金额
        public static final String PAY_TIME = "time3"; // 充值时间
        public static final String STATUS = "phrase4"; // 充值状态

    }

}
