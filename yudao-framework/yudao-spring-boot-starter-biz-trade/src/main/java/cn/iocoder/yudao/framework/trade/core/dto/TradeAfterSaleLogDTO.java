package cn.iocoder.yudao.framework.trade.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TradeAfterSaleLogDTO {

    /**
     * 编号
     */
    private Long id;
    /**
     * 用户编号
     *
     * 关联 1：AdminUserDO 的 id 字段
     * 关联 2：MemberUserDO 的 id 字段
     */
    private Long userId;
    /**
     * 用户类型
     */
    private Integer userType;
    /**
     * 售后编号
     */
    private Long afterSaleId;
    /**
     * 操作类型
     */
    private String operateType;
    /**
     * 操作明细
     */
    private String content;

}
