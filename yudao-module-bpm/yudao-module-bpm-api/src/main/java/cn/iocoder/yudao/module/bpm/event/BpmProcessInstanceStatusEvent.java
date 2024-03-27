package cn.iocoder.yudao.module.bpm.event;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

import javax.validation.constraints.NotNull;

/**
 * 流程实例的状态（结果）发生变化的 Event
 *
 * @author 芋道源码
 */
@SuppressWarnings("ALL")
@Data
public class BpmProcessInstanceStatusEvent extends ApplicationEvent {

    /**
     * 流程实例的编号
     */
    @NotNull(message = "流程实例的编号不能为空")
    private String id;
    /**
     * 流程实例的 key
     */
    @NotNull(message = "流程实例的 key 不能为空")
    private String processDefinitionKey;
    /**
     * 流程实例的结果
     */
    @NotNull(message = "流程实例的状态不能为空")
    private Integer status;
    /**
     * 流程实例对应的业务标识
     * 例如说，请假
     */
    private String businessKey;

    public BpmProcessInstanceStatusEvent(Object source) {
        super(source);
    }

}
