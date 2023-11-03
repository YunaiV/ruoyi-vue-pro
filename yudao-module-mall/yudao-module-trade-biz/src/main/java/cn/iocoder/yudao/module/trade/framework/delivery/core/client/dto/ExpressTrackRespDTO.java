package cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 快递查询的轨迹 Resp DTO
 *
 * @author jason
 */
@Data
public class ExpressTrackRespDTO {

    /**
     * 发生时间
     */
    private LocalDateTime time;

    /**
     * 快递状态
     */
    private String content;

}
