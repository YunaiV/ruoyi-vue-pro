package cn.iocoder.yudao.module.iot.service.device;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceDataSimulatorSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceLogDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.ThingModelMessage;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotDeviceLogDataMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * IoT 设备日志数据 Service 实现了
 *
 * @author alwayssuper
 */
@Service
@Slf4j
@Validated
public class IotDeviceLogDataServiceImpl implements IotDeviceLogDataService{

    @Resource
    private IotDeviceLogDataMapper deviceLogDataMapper;

    @Override
    public void defineDeviceLog() {
        if (deviceLogDataMapper.checkDeviceLogSTableExists() != null) {
            log.info("[defineDeviceLog][设备日志超级表已存在，跳过创建]");
            return;
        }

        log.info("[defineDeviceLog][设备日志超级表不存在，开始创建]");
        deviceLogDataMapper.createDeviceLogSTable();
        log.info("[defineDeviceLog][设备日志超级表不存在，创建完成]");
    }

    @Override
    public void createDeviceLog(IotDeviceDataSimulatorSaveReqVO simulatorReqVO) {
        // 1. 转换请求对象为 DO
        IotDeviceLogDO iotDeviceLogDO = BeanUtils.toBean(simulatorReqVO, IotDeviceLogDO.class);

        // 2. 处理时间字段
//        iotDeviceLogDO.setTs(currentTime); // TODO @super：TS在SQL中直接NOW   咱们的TS数据获取是走哪一种；走 now()

        // 3. 插入数据
        deviceLogDataMapper.insert(iotDeviceLogDO);
    }

    @Override
    public PageResult<IotDeviceLogDO> getDeviceLogPage(IotDeviceLogPageReqVO pageReqVO) {
        // TODO @芋艿：增加一个表不存在的 try catch
        List<IotDeviceLogDO> list = deviceLogDataMapper.selectPage(pageReqVO);
        Long total = deviceLogDataMapper.selectCount(pageReqVO);
        return new PageResult<>(list, total);
    }

    @Override
    public void saveDeviceLog(ThingModelMessage message) {
        IotDeviceLogDO log = IotDeviceLogDO.builder()
                .id(message.getId())
                .deviceKey(message.getDeviceKey())
                .productKey(message.getProductKey())
                .type(message.getMethod())               // 消息类型，使用method作为类型 TODO 芋艿：在看看
                .subType("property")                 // TODO 芋艿:这块先写死，后续优化
                .content(JSONUtil.toJsonStr(message))   // TODO 芋艿:后续优化
                .reportTime(message.getTime()) // 上报时间 TODO 芋艿：在想想时间
                .build();
        deviceLogDataMapper.insert(log);
    }

}
