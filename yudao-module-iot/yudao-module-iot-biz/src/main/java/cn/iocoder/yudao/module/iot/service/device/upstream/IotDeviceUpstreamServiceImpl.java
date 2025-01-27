package cn.iocoder.yudao.module.iot.service.device.upstream;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceEventReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDevicePropertyReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceStatusUpdateReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceUpstreamAbstractReqDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageIdentifierEnum;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.mq.producer.device.IotDeviceProducer;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

/**
 * 设备上行 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotDeviceUpstreamServiceImpl implements IotDeviceUpstreamService {

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotDeviceProducer deviceProducer;

    @Override
    public void updateDeviceStatus(IotDeviceStatusUpdateReqDTO updateReqDTO) {
        log.info("[updateDeviceStatus][更新设备状态: {}]", updateReqDTO);
        // TODO 芋艿：插件状态
    }

    @Override
    public void reportDevicePropertyData(IotDevicePropertyReportReqDTO reportReqDTO) {
        // 1.1 获得设备
        log.info("[reportDevicePropertyData][上报设备属性数据: {}]", reportReqDTO);
        IotDeviceDO device = getDevice(reportReqDTO);
        if (device == null) {
            log.error("[reportDevicePropertyData][设备({}/{})不存在]",
                    reportReqDTO.getProductKey(), reportReqDTO.getDeviceName());
            return;
        }
        // 1.2 记录设备的最后时间
        updateDeviceLastTime(device, reportReqDTO);

        // 2. 发送设备消息
        IotDeviceMessage message = BeanUtils.toBean(reportReqDTO, IotDeviceMessage.class)
                .setType(IotDeviceMessageTypeEnum.PROPERTY.getType())
                .setIdentifier(IotDeviceMessageIdentifierEnum.PROPERTY_REPORT.getIdentifier())
                .setData(reportReqDTO.getProperties());
        sendDeviceMessage(message, device);
    }

    @Override
    public void reportDeviceEventData(IotDeviceEventReportReqDTO reportReqDTO) {
        log.info("[reportDeviceEventData][上报设备事件数据: {}]", reportReqDTO);

        // TODO 芋艿：待实现
    }

    private IotDeviceDO getDevice(IotDeviceUpstreamAbstractReqDTO reqDTO) {
        return TenantUtils.executeIgnore(() -> // 需要忽略租户，因为请求时，未带租户编号
                deviceService.getDeviceByProductKeyAndDeviceName(reqDTO.getProductKey(), reqDTO.getDeviceName()));
    }

    private void updateDeviceLastTime(IotDeviceDO deviceDO, IotDeviceUpstreamAbstractReqDTO reqDTO) {
        // TODO 芋艿：插件状态
        // TODO 芋艿：操作时间
    }

    private void sendDeviceMessage(IotDeviceMessage message, IotDeviceDO device) {
        // 1. 完善消息
        message.setDeviceKey(device.getDeviceKey());
        if (StrUtil.isEmpty(message.getRequestId())) {
            message.setRequestId(IdUtil.fastSimpleUUID());
        }
        if (message.getReportTime() == null) {
            message.setReportTime(LocalDateTime.now());
        }

        // 2. 发送消息
        try {
            deviceProducer.sendDeviceMessage(message);
            log.info("[sendDeviceMessage][message({}) 发送消息成功]", message);
        } catch (Exception e) {
            log.error("[sendDeviceMessage][message({}) 发送消息失败]", message, e);
        }
    }

}
