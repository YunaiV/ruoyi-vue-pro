package cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto;

import lombok.Data;

/**
 * 快递查询 Resp DTO
 *
 * @author jason
 */
@Data
public class ExpressTrackRespDTO {

    // TODO @jason：LocalDateTime
    /**
     * 发生时间
     */
    private String time;
    // TODO @jason：其它字段可能要补充下
    /**
     * 快递状态
     */
    private String state;

}
