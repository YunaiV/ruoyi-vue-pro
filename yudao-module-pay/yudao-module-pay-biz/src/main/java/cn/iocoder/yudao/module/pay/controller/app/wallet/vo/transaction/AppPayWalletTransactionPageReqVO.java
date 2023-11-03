package cn.iocoder.yudao.module.pay.controller.app.wallet.vo.transaction;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 钱包流水分页 Request VO")
@Data
public class AppPayWalletTransactionPageReqVO extends PageParam {

    /**
     * 类型 - 收入
     */
    public static final Integer TYPE_INCOME = 1;
    /**
     * 类型 - 支出
     */
    public static final Integer TYPE_EXPENSE = 2;

    @Schema(description = "类型",  example = "1")
    private Integer type;

}
