package cn.iocoder.yudao.module.promotion.api.seckill.dto;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀活动商品 Response DTO
 *
 * @author HUIHUI
 */
@Data
public class SeckillActivityProductRespDTO {

    /**
     * 秒杀参与商品编号
     */
    private Long id;
    /**
     * 秒杀活动 id
     *
     * 关联 SeckillActivityDO#getId()
     */
    private Long activityId;
    /**
     * 秒杀时段 id
     *
     * 关联 SeckillConfigDO#getId()
     */
    private List<Long> configIds;
    /**
     * 商品 SPU 编号
     */
    private Long spuId;
    /**
     * 商品 SKU 编号
     */
    private Long skuId;
    /**
     * 秒杀金额，单位：分
     */
    private Integer seckillPrice;
    /**
     * 秒杀库存
     */
    private Integer stock;

    /**
     * 秒杀商品状态
     *
     * 枚举 {@link CommonStatusEnum 对应的类}
     */
    private Integer activityStatus;
    /**
     * 活动开始时间点
     */
    private LocalDateTime activityStartTime;
    /**
     * 活动结束时间点
     */
    private LocalDateTime activityEndTime;

}
