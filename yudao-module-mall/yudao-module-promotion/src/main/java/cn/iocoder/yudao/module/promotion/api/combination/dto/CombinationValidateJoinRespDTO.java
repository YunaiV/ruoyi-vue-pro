package cn.iocoder.yudao.module.promotion.api.combination.dto;

import lombok.Data;

/**
 * 校验参与拼团 Response DTO
 *
 * @author HUIHUI
 */
@Data
public class CombinationValidateJoinRespDTO {

    /**
     * 砍价活动编号
     */
    private Long activityId;
    /**
     * 砍价活动名称
     */
    private String name;

    /**
     * 拼团金额
     */
    private Integer combinationPrice;

}
