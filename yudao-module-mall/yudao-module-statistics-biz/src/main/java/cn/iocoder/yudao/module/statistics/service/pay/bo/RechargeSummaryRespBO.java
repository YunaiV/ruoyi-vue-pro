package cn.iocoder.yudao.module.statistics.service.pay.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 充值统计 Response BO
 */
@Data
public class RechargeSummaryRespBO {

    /**
     * 充值会员数量
     */
    private Integer rechargeUserCount;

    /**
     * 充值金额
     */
    private Integer rechargePrice;

}
