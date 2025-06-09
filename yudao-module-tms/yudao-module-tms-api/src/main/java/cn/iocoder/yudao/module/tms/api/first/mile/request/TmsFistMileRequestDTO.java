package cn.iocoder.yudao.module.tms.api.first.mile.request;

import lombok.Builder;
import lombok.Data;

/**
 * 头程主表数量变化DTO
 */
@Data
@Builder
public class TmsFistMileRequestDTO {
    //id
    private Long id;
    //订购数量
    private Integer qty;
}
