package cn.iocoder.yudao.module.trade.framework.order.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单日志的创建 Request DTO
 *
 * @author 陈賝
 * @since 2023/7/6 15:27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeOrderLogCreateReqDTO {

    /**
     * 编号
     */
    private Long id;
    /**
     * 用户编号
     * <p>
     * 关联 1：AdminUserDO 的 id 字段
     * 关联 2：MemberUserDO 的 id 字段
     */
    private Long userId;
    /**
     * 用户类型
     */
    private Integer userType;
    /**
     * 订单编号
     */
    private Long orderId;
    /**
     * 操作类型
     */
    private Integer operateType;
    /**
     * 操作明细
     */
    private String content;

}
