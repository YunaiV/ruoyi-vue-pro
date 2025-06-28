package cn.iocoder.yudao.module.iot.service.alert;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.alert.vo.recrod.IotAlertRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alert.vo.recrod.IotAlertRecordProcessReqVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertRecordDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.alert.IotAlertRecordMapper;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.ALERT_RECORD_NOT_EXISTS;

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
    public void processAlertRecord(IotAlertRecordProcessReqVO processReqVO) {
        // 校验告警记录是否存在
        IotAlertRecordDO alertRecord = alertRecordMapper.selectById(processReqVO.getId());
        if (alertRecord == null) {
            throw exception(ALERT_RECORD_NOT_EXISTS);
        }

        // 更新处理状态和备注
        alertRecordMapper.updateById(IotAlertRecordDO.builder()
                .id(processReqVO.getId())
                .processStatus(true)
                .processRemark(processReqVO.getProcessRemark())
                .build());
    }

    @Override
    public Long createAlertRecord(IotAlertConfigDO config, IotDeviceMessage message) {
        // 构建告警记录
        IotAlertRecordDO.IotAlertRecordDOBuilder builder = IotAlertRecordDO.builder()
                .configId(config.getId()).configName(config.getName()).configLevel(config.getLevel())
                .processStatus(false);
        if (message != null) {
            builder.deviceMessage(message);
            // 填充设备信息
            IotDeviceDO device = deviceService.getDeviceFromCache(message.getDeviceId());
            if (device!= null) {
                builder.productId(device.getProductId()).deviceId(device.getId());
            }
        }
        // 插入记录
        IotAlertRecordDO record = builder.build();
        alertRecordMapper.insert(record);
        return record.getId();
    }

}