package cn.iocoder.yudao.module.iot.service.rule.action;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotRuleSceneDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneActionTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.service.rule.IotDataBridgeService;
import cn.iocoder.yudao.module.iot.service.rule.action.databridge.IotDataBridgeExecute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * IoT 数据桥梁的 {@link IotRuleSceneAction} 实现类
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class IotRuleSceneDataBridgeAction implements IotRuleSceneAction {

    @Resource
    private IotDataBridgeService dataBridgeService;
    @Resource
    private List<IotDataBridgeExecute<?>> dataBridgeExecutes;

    @Override
    public void execute(IotDeviceMessage message, IotRuleSceneDO.ActionConfig config) throws Exception {
        // 1.1 如果消息为空，直接返回
        if (message == null) {
            return;
        }
        // 1.2 获得数据桥梁
        Assert.notNull(config.getDataBridgeId(), "数据桥梁编号不能为空");
        IotDataBridgeDO dataBridge = dataBridgeService.getDataBridge(config.getDataBridgeId());
        if (dataBridge == null || dataBridge.getConfig() == null) {
            log.error("[execute][message({}) config({}) 对应的数据桥梁不存在]", message, config);
            return;
        }
        if (CommonStatusEnum.isDisable(dataBridge.getStatus())) {
            log.info("[execute][message({}) config({}) 对应的数据桥梁({}) 状态为禁用]", message, config, dataBridge);
            return;
        }

        // 2. 执行数据桥接操作
        for (IotDataBridgeExecute<?> execute : dataBridgeExecutes) {
            execute.execute(message, dataBridge);
        }
    }

    @Override
    public IotRuleSceneActionTypeEnum getType() {
        return IotRuleSceneActionTypeEnum.DATA_BRIDGE;
    }

}
