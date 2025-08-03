package cn.iocoder.yudao.module.trade.service.aftersale.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 售后日志的创建 Request BO
 *
 * @author 陈賝
 * @since 2023/6/19 09:54
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AfterSaleLogCreateReqBO {

    /**
     * 用户编号
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;
    /**
     * 用户类型
     */
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    /**
     * 售后编号
     */
    @NotNull(message = "售后编号不能为空")
    private Long afterSaleId;
    /**
     * 操作前状态
     */
    private Integer beforeStatus;
    /**
     * 操作后状态
     */
    @NotNull(message = "操作后的状态不能为空")
    private Integer afterStatus;

    /**
     * 操作类型
     */
    @NotNull(message = "操作类型不能为空")
    private Integer operateType;
    /**
     * 操作明细
     */
    @NotEmpty(message = "操作明细不能为空")
    private String content;
}
