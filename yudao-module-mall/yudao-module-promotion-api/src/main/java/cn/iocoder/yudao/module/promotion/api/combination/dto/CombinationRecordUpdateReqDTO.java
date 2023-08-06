package cn.iocoder.yudao.module.promotion.api.combination.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 拼团记录的更新 Request DTO
 *
 * @author HUIHUI
 */
@Data
public class CombinationRecordUpdateReqDTO {

    /**
     * 用户编号
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    /**
     * 订单编号
     */
    @NotNull(message = "订单编号不能为空")
    private Long orderId;

    /**
     * 开团状态：正在开团 拼团成功 拼团失败
     */
    @NotNull(message = "开团状态不能为空")
    private Integer status;

    /**
     * 团开始时间
     */
    private LocalDateTime startTime;

}
