package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.config;

import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneActionTypeEnum;
import lombok.Data;

// TODO @puhui999：这个要不内嵌到 IoTRuleSceneDO 里面？
/**
 * 执行器配置
 *
 * @author 芋道源码
 */
@Data
public class IotRuleSceneActionConfig {

    /**
     * 执行类型
     *
     * 枚举 {@link IotRuleSceneActionTypeEnum}
     */
    private Integer type;

    /**
     * 设备控制
     *
     * 必填：当 {@link #type} 为 {@link IotRuleSceneActionTypeEnum#DEVICE_CONTROL} 时
     */
    private IotRuleSceneActionDeviceControl deviceControl;

    /**
     * 数据桥接编号
     *
     * 必填：当 {@link #type} 为 {@link IotRuleSceneActionTypeEnum#DATA_BRIDGE} 时
     * 关联：{@link IotDataBridgeDO#getId()}
     */
    private Long dataBridgeId;

}