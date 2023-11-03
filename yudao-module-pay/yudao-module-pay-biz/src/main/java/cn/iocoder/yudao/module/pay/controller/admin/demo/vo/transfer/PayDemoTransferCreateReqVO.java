package cn.iocoder.yudao.module.pay.controller.admin.demo.vo.transfer;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.pay.core.enums.transfer.PayTransferTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author jason
 */
@Schema(description = "管理后台 - 示例转账单创建 Request VO")
@Data
public class PayDemoTransferCreateReqVO {

    @Schema(description = "转账类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "转账类型不能为空")
    @InEnum(PayTransferTypeEnum.class)
    private Integer type;

    @NotNull(message = "转账金额不能为空")
    @Min(value = 1, message = "转账金额必须大于零")
    private Integer price;

    // TODO @jason：感觉这个动态字段，晚点改；可能要讨论下怎么搞好；
    @Schema(description = "收款方信息", requiredMode = Schema.RequiredMode.REQUIRED, example = "{'ALIPAY_LOGON_ID':'xxxx'}")
    @NotEmpty(message = "收款方信息不能为空")
    private Map<String, String> payeeInfo;

}
