package cn.iocoder.yudao.module.iot.job.ota;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaTaskRecordDO;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaTaskRecordStatusEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.ota.IotOtaFirmwareService;
import cn.iocoder.yudao.module.iot.service.ota.IotOtaTaskRecordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IoT OTA 升级推送 Job：查询待推送的 OTA 升级记录，并推送给设备
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class IotOtaUpgradeJob implements JobHandler {

    @Resource
    private IotOtaTaskRecordService otaTaskRecordService;
    @Resource
    private IotOtaFirmwareService otaFirmwareService;
    @Resource
    private IotDeviceService deviceService;

    @Override
    @TenantJob
    public String execute(String param) throws Exception {
        // 1. 查询待推送的 OTA 升级记录
        List<IotOtaTaskRecordDO> records = otaTaskRecordService.getOtaRecordListByStatus(
                IotOtaTaskRecordStatusEnum.PENDING.getStatus());
        if (CollUtil.isEmpty(records)) {
            return null;
        }

        // TODO 芋艿：可以优化成批量获取 原因是：1. N+1 问题；2. offline 的设备无需查询
        // 2. 遍历推送记录
        int successCount = 0;
        int failureCount = 0;
        Map<Long, IotOtaFirmwareDO> otaFirmwares = new HashMap<>();
        for (IotOtaTaskRecordDO record : records) {
            try {
                // 2.1 设备如果不在线，直接跳过
                IotDeviceDO device = deviceService.getDeviceFromCache(record.getDeviceId());
                // TODO 芋艿：【优化】当前逻辑跳过了离线的设备，但未充分利用 MQTT 的离线消息能力。
                // 1. MQTT 协议本身支持持久化会话（Clean Session=false）和 QoS > 0 的消息，允许 broker 为离线设备缓存消息。
                // 2. 对于 OTA 升级这类非实时性强的任务，即使设备当前离线，也应该可以推送升级指令。设备在下次上线时即可收到。
                // 3. 后续可以考虑：增加一个“允许离线推送”的选项。如果开启，即使设备状态为 OFFLINE，也应尝试推送消息，依赖 MQTT Broker 的能力进行离线缓存。
                if (device == null || IotDeviceStateEnum.isNotOnline(device.getState())) {
                    continue;
                }
                // 2.2 获取 OTA 固件信息
                IotOtaFirmwareDO fireware = otaFirmwares.get(record.getFirmwareId());
                if (fireware == null) {
                    fireware = otaFirmwareService.getOtaFirmware(record.getFirmwareId());
                    otaFirmwares.put(record.getFirmwareId(), fireware);
                }
                // 2.3 推送 OTA 升级任务
                boolean result = otaTaskRecordService.pushOtaTaskRecord(record, fireware, device);
                if (result) {
                    successCount++;
                } else {
                    failureCount++;
                }
            } catch (Exception e) {
                failureCount++;
                log.error("[execute][推送 OTA 升级任务({})发生异常]", record.getId(), e);
            }
        }
        return StrUtil.format("升级任务推送成功：{} 条，送失败：{} 条", successCount, failureCount);
    }

}
