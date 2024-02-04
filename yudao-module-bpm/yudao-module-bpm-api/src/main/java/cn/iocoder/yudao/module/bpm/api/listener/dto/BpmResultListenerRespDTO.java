package cn.iocoder.yudao.module.bpm.api.listener.dto;

import lombok.Data;

// TODO @芋艿：后续改成支持 RPC
/**
 * 业务流程实例的结果 Response DTO
 *
 * @author HUIHUI
 */
@Data
public class BpmResultListenerRespDTO {

    /**
     * 流程实例的编号
     */
    private String id;
    /**
     * 流程实例的 key
     */
    private String processDefinitionKey;
    /**
     * 流程实例的结果
     */
    private Integer result;
    /**
     * 流程实例对应的业务标识
     * 例如说，请假
     */
    private String businessKey;

}
