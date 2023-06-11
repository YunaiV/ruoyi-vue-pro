package cn.iocoder.yudao.module.trade.framework.delivery.core.dto;

import lombok.Data;

/**
 * 快递查询 Resp DTO
 *
 * @author jason
 */
@Data
public class ExpressQueryRespDTO {

    /**
     * 发生时间
     */
    private String time;

    /**
     * 快递状态
     */
    private String state;
}
