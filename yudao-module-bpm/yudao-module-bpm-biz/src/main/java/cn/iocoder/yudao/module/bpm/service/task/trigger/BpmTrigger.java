package cn.iocoder.yudao.module.bpm.service.task.trigger;

import cn.iocoder.yudao.module.bpm.enums.definition.BpmTriggerTypeEnum;

// TODO @芋艿：可能会想换个包地址
/**
 * BPM 触发器接口
 * <p>
 * 处理不同的动作
 *
 * @author jason
 */
public interface BpmTrigger {

    /**
     * 对应触发器类型
     *
     * @return 触发器类型
     */
    BpmTriggerTypeEnum getType();

    /**
     * 触发器执行
     *
     * @param processInstanceId 流程实例编号
     * @param param 触发器参数
     */
    void execute(String processInstanceId, String param);

}
