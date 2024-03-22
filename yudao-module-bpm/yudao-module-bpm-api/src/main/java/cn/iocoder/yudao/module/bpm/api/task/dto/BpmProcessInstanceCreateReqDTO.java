package cn.iocoder.yudao.module.bpm.api.task.dto;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * 流程实例的创建 Request DTO
 *
 * @author 芋道源码
 */
@Data
public class BpmProcessInstanceCreateReqDTO {

    /**
     * 流程定义的标识
     */
    @NotEmpty(message = "流程定义的标识不能为空")
    private String processDefinitionKey;
    /**
     * 变量实例（动态表单）
     */
    private Map<String, Object> variables;

    /**
     * 业务的唯一标识
     *
     * 例如说，请假申请的编号。通过它，可以查询到对应的实例
     */
    @NotEmpty(message = "业务的唯一标识")
    private String businessKey;

    /**
     * 发起人自选审批人 Map
     *
     * key：taskKey 任务编码
     * value：审批人的数组
     * 例如：{ taskKey1 :[1, 2] }，则表示 taskKey1 这个任务，提前设定了，由 userId 为 1,2 的用户进行审批
     */
    private Map<String, List<Long>> startUserSelectAssignees;

}
