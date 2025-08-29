package cn.iocoder.yudao.module.iot.service.ota;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.record.IotOtaTaskRecordPageReqVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaTaskRecordDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ota.IotOtaTaskRecordMapper;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaTaskRecordStatusEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * OTA 升级任务记录 Service 实现类
 */
@Service
@Validated
@Slf4j
public class IotOtaTaskRecordServiceImpl implements IotOtaTaskRecordService {

    @Resource
    private IotOtaTaskRecordMapper otaTaskRecordMapper;

    @Resource
    private IotOtaFirmwareService otaFirmwareService;
    @Resource
    private IotOtaTaskService otaTaskService;
    @Resource
    private IotDeviceMessageService deviceMessageService;
    @Resource
    private IotDeviceService deviceService;

    @Override
    public void createOtaTaskRecordList(List<IotDeviceDO> devices, Long firmwareId, Long taskId) {
        List<IotOtaTaskRecordDO> records = convertList(devices, device ->
                IotOtaTaskRecordDO.builder().firmwareId(firmwareId).taskId(taskId)
                        .deviceId(device.getId()).fromFirmwareId(Convert.toLong(device.getFirmwareId()))
                        .status(IotOtaTaskRecordStatusEnum.PENDING.getStatus()).progress(0).build());
        otaTaskRecordMapper.insertBatch(records);
    }

    @Override
    public Map<Integer, Long> getOtaTaskRecordStatusStatistics(Long firmwareId, Long taskId) {
        // 按照 status 枚举，初始化 countMap 为 0
        Map<Integer, Long> countMap = convertMap(Arrays.asList(IotOtaTaskRecordStatusEnum.values()),
                IotOtaTaskRecordStatusEnum::getStatus, iotOtaTaskRecordStatusEnum -> 0L);

        // 查询记录，只返回 id、status 字段
        List<IotOtaTaskRecordDO> records = otaTaskRecordMapper.selectListByFirmwareIdAndTaskId(firmwareId, taskId);
        Map<Long, List<Integer>> deviceStatusesMap = convertMultiMap(records,
                IotOtaTaskRecordDO::getDeviceId, IotOtaTaskRecordDO::getStatus);
        // 找到第一个匹配的优先级状态，避免重复计算
        deviceStatusesMap.forEach((deviceId, statuses) -> {
            for (Integer priorityStatus : IotOtaTaskRecordStatusEnum.PRIORITY_STATUSES) {
                if (statuses.contains(priorityStatus)) {
                    countMap.put(priorityStatus, countMap.get(priorityStatus) + 1);
                    return;
                }
            }
        });
        return countMap;
    }

    @Override
    public IotOtaTaskRecordDO getOtaTaskRecord(Long id) {
        return otaTaskRecordMapper.selectById(id);
    }

    @Override
    public PageResult<IotOtaTaskRecordDO> getOtaTaskRecordPage(IotOtaTaskRecordPageReqVO pageReqVO) {
        return otaTaskRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public void cancelTaskRecordListByTaskId(Long taskId) {
        List<IotOtaTaskRecordDO> records = otaTaskRecordMapper.selectListByTaskIdAndStatus(
                taskId, IotOtaTaskRecordStatusEnum.IN_PROCESS_STATUSES);
        if (CollUtil.isEmpty(records)) {
            return;
        }
        // 批量更新
        Collection<Long> ids = convertSet(records, IotOtaTaskRecordDO::getId);
        otaTaskRecordMapper.updateListByIdAndStatus(ids, IotOtaTaskRecordStatusEnum.IN_PROCESS_STATUSES,
                IotOtaTaskRecordDO.builder().status(IotOtaTaskRecordStatusEnum.CANCELED.getStatus())
                        .description(IotOtaTaskRecordDO.DESCRIPTION_CANCEL_BY_TASK).build());
    }

    @Override
    public List<IotOtaTaskRecordDO> getOtaTaskRecordListByDeviceIdAndStatus(Set<Long> deviceIds, Set<Integer> statuses) {
        return otaTaskRecordMapper.selectListByDeviceIdAndStatus(deviceIds, statuses);
    }

    @Override
    public List<IotOtaTaskRecordDO> getOtaRecordListByStatus(Integer status) {
        return otaTaskRecordMapper.selectListByStatus(status);
    }

    @Override
    public void cancelOtaTaskRecord(Long id) {
        // 1. 校验记录是否存在
        IotOtaTaskRecordDO record = validateUpgradeRecordExists(id);

        // 2. 更新记录状态为取消
        int updateCount = otaTaskRecordMapper.updateByIdAndStatus(record.getId(), IotOtaTaskRecordStatusEnum.IN_PROCESS_STATUSES,
                IotOtaTaskRecordDO.builder().id(id).status(IotOtaTaskRecordStatusEnum.CANCELED.getStatus())
                .description(IotOtaTaskRecordDO.DESCRIPTION_CANCEL_BY_RECORD).build());
        if (updateCount == 0) {
            throw exception(OTA_TASK_RECORD_CANCEL_FAIL_STATUS_ERROR);
        }

        // 3. 检查并更新任务状态
        checkAndUpdateOtaTaskStatus(record.getTaskId());
    }

    @Override
    public boolean pushOtaTaskRecord(IotOtaTaskRecordDO record, IotOtaFirmwareDO fireware, IotDeviceDO device) {
        try {
            // 1. 推送 OTA 任务记录
            IotDeviceMessage message = IotDeviceMessage.buildOtaUpgrade(
                    fireware.getVersion(), fireware.getFileUrl(), fireware.getFileSize(),
                    fireware.getFileDigestAlgorithm(), fireware.getFileDigestValue());
            deviceMessageService.sendDeviceMessage(message, device);

            // 2. 更新 OTA 升级记录状态为进行中
            int updateCount = otaTaskRecordMapper.updateByIdAndStatus(
                    record.getId(), IotOtaTaskRecordStatusEnum.PENDING.getStatus(),
                    IotOtaTaskRecordDO.builder().status(IotOtaTaskRecordStatusEnum.PUSHED.getStatus())
                            .description(StrUtil.format("已推送，设备消息编号({})", message.getId())).build());
            Assert.isTrue(updateCount == 1, "更新设备记录({})状态失败", record.getId());
            return true;
        } catch (Exception ex) {
            log.error("[pushOtaTaskRecord][推送 OTA 任务记录({}) 失败]", record.getId(), ex);
            otaTaskRecordMapper.updateById(IotOtaTaskRecordDO.builder().id(record.getId())
                    .description(StrUtil.format("推送失败，错误信息({})", ex.getMessage())).build());
            return false;
        }
    }

    private IotOtaTaskRecordDO validateUpgradeRecordExists(Long id) {
        IotOtaTaskRecordDO upgradeRecord = otaTaskRecordMapper.selectById(id);
        if (upgradeRecord == null) {
            throw exception(OTA_TASK_RECORD_NOT_EXISTS);
        }
        return upgradeRecord;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public void updateOtaRecordProgress(IotDeviceDO device, IotDeviceMessage message) {
        // 1.1 参数解析
        Map<String, Object> params = (Map<String, Object>) message.getParams();
        String version = MapUtil.getStr(params, "version");
        Assert.notBlank(version, "version 不能为空");
        Integer status = MapUtil.getInt(params, "status");
        Assert.notNull(status, "status 不能为空");
        Assert.notNull(IotOtaTaskRecordStatusEnum.of(status), "status 状态不正确");
        String description = MapUtil.getStr(params, "description");
        Integer progress = MapUtil.getInt(params, "progress");
        Assert.notNull(progress, "progress 不能为空");
        Assert.isTrue(progress >= 0 && progress <= 100, "progress 必须在 0-100 之间");
        // 1.2 查询 OTA 升级记录
        List<IotOtaTaskRecordDO> records = otaTaskRecordMapper.selectListByDeviceIdAndStatus(
                device.getId(), IotOtaTaskRecordStatusEnum.IN_PROCESS_STATUSES);
        if (CollUtil.isEmpty(records)) {
            throw exception(OTA_TASK_RECORD_UPDATE_PROGRESS_FAIL_NO_EXISTS);
        }
        if (records.size() > 1) {
            log.warn("[updateOtaRecordProgress][message({}) 对应升级记录过多({})]", message, records);
        }
        IotOtaTaskRecordDO record = CollUtil.getFirst(records);
        // 1.3 查询 OTA 固件
        IotOtaFirmwareDO firmware = otaFirmwareService.getOtaFirmwareByProductIdAndVersion(
                device.getProductId(), version);
        if (firmware == null) {
            throw exception(OTA_FIRMWARE_NOT_EXISTS);
        }

        // 2. 更新 OTA 升级记录状态
        int updateCount = otaTaskRecordMapper.updateByIdAndStatus(
                record.getId(), IotOtaTaskRecordStatusEnum.IN_PROCESS_STATUSES,
                IotOtaTaskRecordDO.builder().status(status).description(description).progress(progress).build());
        if (updateCount == 0) {
            throw exception(OTA_TASK_RECORD_UPDATE_PROGRESS_FAIL_NO_EXISTS);
        }

        // 3. 如果升级成功，则更新设备固件版本
        if (IotOtaTaskRecordStatusEnum.SUCCESS.getStatus().equals(status)) {
            deviceService.updateDeviceFirmware(device.getId(), firmware.getId());
        }

        // 4. 如果状态是“已结束”（非进行中），则更新任务状态
        if (!IotOtaTaskRecordStatusEnum.IN_PROCESS_STATUSES.contains(status)) {
            checkAndUpdateOtaTaskStatus(record.getTaskId());
        }
    }

    /**
     * 检查并更新任务状态
     * 如果任务下没有进行中的记录，则将任务状态更新为已结束
     */
    private void checkAndUpdateOtaTaskStatus(Long taskId) {
        // 如果还有进行中的记录，直接返回
        Long inProcessCount = otaTaskRecordMapper.selectCountByTaskIdAndStatus(
                taskId, IotOtaTaskRecordStatusEnum.IN_PROCESS_STATUSES);
        if (inProcessCount > 0) {
            return;
        }

        // 没有进行中的记录，将任务状态更新为已结束
        otaTaskService.updateOtaTaskStatusEnd(taskId);
    }

}
