package cn.iocoder.yudao.module.trade.api.brokerage.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分销用户 DTO
 *
 * @author owen
 */
@Data
public class BrokerageUserDTO {

    /**
     * 用户编号
     * <p>
     * 对应 MemberUserDO 的 id 字段
     */
    private Long id;

    /**
     * 推广员编号
     * <p>
     * 关联 MemberUserDO 的 id 字段
     */
    private Long bindUserId;
    /**
     * 推广员绑定时间
     */
    private LocalDateTime bindUserTime;

    /**
     * 推广资格
     */
    private Boolean brokerageEnabled;
    /**
     * 成为分销员时间
     */
    private LocalDateTime brokerageTime;

    /**
     * 可用佣金
     */
    private Integer price;
    /**
     * 冻结佣金
     */
    private Integer frozenPrice;

}
