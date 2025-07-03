package cn.iocoder.yudao.module.iot.service.ota;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.record.IotOtaTaskRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaTaskRecordDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ota.IotOtaTaskRecordMapper;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaTaskRecordStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.OTA_TASK_RECORD_NOT_EXISTS;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.OTA_TASK_RECORD_CANCEL_FAIL_STATUS_ERROR;

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
    private IotOtaTaskService otaTaskService;

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
                        .description(IotOtaTaskRecordDO.DESCRIPTION_CANCEL_BY_RECORD).build());
    }

    @Override
    public List<IotOtaTaskRecordDO> getOtaTaskRecordListByDeviceIdAndStatus(Set<Long> deviceIds, Set<Integer> statuses) {
        return otaTaskRecordMapper.selectListByDeviceIdAndStatus(deviceIds, statuses);
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

    private IotOtaTaskRecordDO validateUpgradeRecordExists(Long id) {
        IotOtaTaskRecordDO upgradeRecord = otaTaskRecordMapper.selectById(id);
        if (upgradeRecord == null) {
            throw exception(OTA_TASK_RECORD_NOT_EXISTS);
        }
        return upgradeRecord;
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
