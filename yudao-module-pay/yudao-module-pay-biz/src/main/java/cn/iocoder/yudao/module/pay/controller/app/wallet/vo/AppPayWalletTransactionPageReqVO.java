package cn.iocoder.yudao.module.pay.controller.app.wallet.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.pay.enums.member.WalletTransactionQueryTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 钱包余额明细分页 Request VO")
@Data
public class AppPayWalletTransactionPageReqVO extends PageParam {

    @Schema(description = "余额明细查询分类",  example = "1")
    @InEnum(value = WalletTransactionQueryTypeEnum.class, message = "查询类型必须是 {value}")
    private Integer type;
}
