package cn.iocoder.yudao.module.trade.service.orchestrator.bo;

import lombok.Data;

/**
 * 工作流步骤 BO
 *
 * @author deepay
 */
@Data
public class WorkflowStepBO {

    private Integer step;
    private String name;
    /** completed / failed / skipped */
    private String status;
    private Object details;
    private String completedAt;

}
