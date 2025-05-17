package cn.iocoder.yudao.module.promotion.api.seckill.dto;

import lombok.Data;

/**
 * 校验参与秒杀 Response DTO
 */
@Data
public class SeckillValidateJoinRespDTO {

    /**
     * 秒杀活动名称
     */
    private String name;
    /**
     * 总限购数量
     *
     * 目的：目前只有 trade 有具体下单的数据，需要交给 trade 价格计算使用
     */
    private Integer totalLimitCount;

    /**
     * 秒杀金额
     */
    private Integer seckillPrice;

}
