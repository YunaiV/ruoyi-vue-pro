package cn.iocoder.yudao.module.trade.controller.app.aftersale.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.AfterSaleWayEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "用户 App - 交易售后创建 Request VO")
@Data
public class AppAfterSaleCreateReqVO {

    @Schema(description = "订单项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "订单项编号不能为空")
    private Long orderItemId;

    @Schema(description = "售后方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "售后方式不能为空")
    @InEnum(value = AfterSaleWayEnum.class, message = "售后方式必须是 {value}")
    private Integer way;

    @Schema(description = "退款金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "退款金额不能为空")
    @Min(value = 1, message = "退款金额必须大于 0")
    private Integer refundPrice;

    @Schema(description = "申请原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "申请原因不能为空")
    private String applyReason;

    @Schema(description = "补充描述", example = "商品质量不好")
    private String applyDescription;

    @Schema(description = "补充凭证图片", example = "https://www.iocoder.cn/1.png, https://www.iocoder.cn/2.png")
    private List<String> applyPicUrls;

}
