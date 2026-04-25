package cn.iocoder.yudao.module.trade.service.wholesale.bo;

import lombok.Data;

/**
 * 智能下一步建议 BO
 *
 * @author deepay
 */
@Data
public class WholesaleNextStepBO {

    /** 操作动作标识 */
    private String action;

    /** 操作标题 */
    private String title;

    /** 详细说明 */
    private String description;

    /** 优先级（数字越小越优先） */
    private Integer priority;

}
