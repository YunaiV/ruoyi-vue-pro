package cn.iocoder.yudao.module.iot.service.device.data;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceLogDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotDeviceLogDataMapper;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * IoT 设备日志数据 Service 实现类
 *
 * @author alwayssuper
 */
@Service
@Slf4j
@Validated
public class IotDeviceLogServiceImpl implements IotDeviceLogService {

    @Resource
    private IotDeviceLogDataMapper deviceLogDataMapper;

    @Override
    public void defineDeviceLog() {
        if (StrUtil.isNotEmpty(deviceLogDataMapper.showDeviceLogSTable())) {
            log.info("[defineDeviceLog][设备日志超级表已存在，创建跳过]");
            return;
        }

        log.info("[defineDeviceLog][设备日志超级表不存在，创建开始...]");
        deviceLogDataMapper.createDeviceLogSTable();
        log.info("[defineDeviceLog][设备日志超级表不存在，创建成功]");
    }

    @Override
    public void createDeviceLog(IotDeviceMessage message) {
        IotDeviceLogDO log = BeanUtils.toBean(message, IotDeviceLogDO.class)
                .setId(IdUtil.fastSimpleUUID())
                .setContent(JsonUtils.toJsonString(message.getData()));
        deviceLogDataMapper.insert(log);
    }

    @Override
    public PageResult<IotDeviceLogDO> getDeviceLogPage(IotDeviceLogPageReqVO pageReqVO) {
        // TODO @芋艿：增加一个表不存在的 try catch
        List<IotDeviceLogDO> list = deviceLogDataMapper.selectPage(pageReqVO);
        Long total = deviceLogDataMapper.selectCount(pageReqVO);
        return new PageResult<>(list, total);
    }

}
