package cn.iocoder.yudao.framework.pay.core.enums.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付 UI 展示模式
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PayOrderDisplayModeEnum {

    URL("url"), // Redirect 跳转链接的方式
    IFRAME("iframe"), // IFrame 内嵌链接的方式【目前暂时用不到】
    FORM("form"), // HTML 表单提交
    QR_CODE("qr_code"), // 二维码的文字内容
    QR_CODE_URL("qr_code_url"), // 二维码的图片链接【目前暂时用不到】
    BAR_CODE("bar_code"), // 条形码
    APP("app"), // 应用【目前暂时用不到】
    CUSTOM("custom"), // 自定义：每种支付方式，做个性化处理；例如说，微信公众号支付时，调用 JSAPI 接口
    ;

    /**
     * 展示模式
     */
    private final String mode;

}
