package cn.iocoder.yudao.module.promotion.api.bargain.dto;

import lombok.Data;

/**
 * 校验参与砍价 Response DTO
 */
@Data
public class BargainValidateJoinRespDTO {

    /**
     * 砍价活动编号
     */
    private Long activityId;
    /**
     * 砍价活动名称
     */
    private String name;

    /**
     * 砍价金额
     */
    private Integer bargainPrice;

}
