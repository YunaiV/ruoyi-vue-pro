package cn.iocoder.yudao.module.trade.service.brokerage.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户佣金合计 BO
 *
 * @author owen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBrokerageSummaryRespBO {

    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 推广数量
     */
    private Integer count;
    /**
     * 佣金总额
     */
    private Integer price;

}
