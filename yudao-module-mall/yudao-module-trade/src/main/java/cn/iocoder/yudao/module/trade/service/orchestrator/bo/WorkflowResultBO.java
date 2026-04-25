package cn.iocoder.yudao.module.trade.service.orchestrator.bo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * B2B 工作流编排结果 BO
 *
 * @author deepay
 */
@Data
public class WorkflowResultBO {

    /** 各步骤执行结果 */
    private List<WorkflowStepBO> steps;

    /** 时间线（步骤名 → 时间）*/
    private List<Map<String, Object>> timeline;

    /** 可分享的资源链接（order_verification / contract_preview / blockchain_proof） */
    private Map<String, Object> verificationLinks;

    /** 智能推荐列表 */
    private Object recommendations;

    /** 整体状态：success / partial / failed */
    private String overallStatus;

    /** 区块链存证任务 ID */
    private String blockchainTaskId;

}
