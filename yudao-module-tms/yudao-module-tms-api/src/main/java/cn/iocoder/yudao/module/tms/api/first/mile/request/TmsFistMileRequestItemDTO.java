package cn.iocoder.yudao.module.tms.api.first.mile.request;

import lombok.Builder;
import lombok.Data;

/**
 * 头程申请数量变化DTO
 */
@Data
@Builder
public class TmsFistMileRequestItemDTO {
    //id
    private Long itemId;
    //订购数量变动值
    private Integer qty;
}
