package cn.iocoder.yudao.module.iot.service.alert;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.alert.vo.recrod.IotAlertRecordPageReqVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertRecordDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.alert.IotAlertRecordMapper;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/**
 * IoT 告警记录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotAlertRecordServiceImpl implements IotAlertRecordService {

    @Resource
    private IotAlertRecordMapper alertRecordMapper;

    @Resource
    private IotDeviceService deviceService;

    @Override
    public IotAlertRecordDO getAlertRecord(Long id) {
        return alertRecordMapper.selectById(id);
    }

    @Override
    public PageResult<IotAlertRecordDO> getAlertRecordPage(IotAlertRecordPageReqVO pageReqVO) {
        return alertRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotAlertRecordDO> getAlertRecordListBySceneRuleId(Long sceneRuleId, Long deviceId, Boolean processStatus) {
        return alertRecordMapper.selectListBySceneRuleId(sceneRuleId, deviceId, processStatus);
    }

    @Override
    public void processAlertRecordList(Collection<Long> ids, String processRemark) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 批量更新告警记录的处理状态
        alertRecordMapper.updateList(ids, IotAlertRecordDO.builder()
                .processStatus(true).processRemark(processRemark).build());
    }

    @Override
    public Long createAlertRecord(IotAlertConfigDO config, Long sceneRuleId, IotDeviceMessage message) {
        // 构建告警记录
        IotAlertRecordDO.IotAlertRecordDOBuilder builder = IotAlertRecordDO.builder()
                .configId(config.getId()).configName(config.getName()).configLevel(config.getLevel())
                .sceneRuleId(sceneRuleId).processStatus(false);
        if (message != null) {
            builder.deviceMessage(message);
            // 填充设备信息
            IotDeviceDO device = deviceService.getDeviceFromCache(message.getDeviceId());
            if (device != null) {
                builder.productId(device.getProductId()).deviceId(device.getId());
            }
        }

        // 插入记录
        IotAlertRecordDO record = builder.build();
        alertRecordMapper.insert(record);
        return record.getId();
    }

}