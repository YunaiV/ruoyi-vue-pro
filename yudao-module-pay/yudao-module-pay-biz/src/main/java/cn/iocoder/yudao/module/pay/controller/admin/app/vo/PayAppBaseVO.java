package cn.iocoder.yudao.module.pay.controller.admin.app.vo;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
* 支付应用信息 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class PayAppBaseVO {

    @Schema(description = "应用名", requiredMode = Schema.RequiredMode.REQUIRED, example = "小豆")
    @NotNull(message = "应用名不能为空")
    private String name;

    @Schema(description = "开启状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "开启状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "备注", example = "我是一个测试应用")
    private String remark;

    @Schema(description = "支付结果的回调地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "http://127.0.0.1:48080/pay-callback")
    @NotNull(message = "支付结果的回调地址不能为空")
    @URL(message = "支付结果的回调地址必须为 URL 格式")
    private String orderNotifyUrl;

    @Schema(description = "退款结果的回调地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "http://127.0.0.1:48080/refund-callback")
    @NotNull(message = "退款结果的回调地址不能为空")
    @URL(message = "退款结果的回调地址必须为 URL 格式")
    private String refundNotifyUrl;

}
