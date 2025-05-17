package cn.iocoder.yudao.module.trade.service.brokerage.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 佣金 增加 Request BO
 *
 * @author owen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrokerageAddReqBO {

    /**
     * 业务编号
     */
    @NotBlank(message = "业务编号不能为空")
    private String bizId;
    /**
     * 佣金基数
     */
    @NotNull(message = "佣金基数不能为空")
    private Integer basePrice;
    /**
     * 一级佣金（固定）
     */
    @NotNull(message = "一级佣金（固定）不能为空")
    private Integer firstFixedPrice;
    /**
     * 二级佣金（固定）
     */
    private Integer secondFixedPrice;

    /**
     * 来源用户编号
     */
    @NotNull(message = "来源用户编号不能为空")
    private Long sourceUserId;

    /**
     * 佣金记录标题
     */
    @NotEmpty(message = "佣金记录标题不能为空")
    private String title;

}
