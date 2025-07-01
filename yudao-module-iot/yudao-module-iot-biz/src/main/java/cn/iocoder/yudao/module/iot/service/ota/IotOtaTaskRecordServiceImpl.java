package cn.iocoder.yudao.module.iot.service.ota;

import cn.hutool.core.convert.Convert;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.record.IotOtaTaskRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaTaskRecordDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaTaskDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ota.IotOtaUpgradeRecordMapper;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaTaskRecordStatusEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

// TODO @li：@Service、@Validated、@Slf4j，先用关键注解；2）类注释，简单写
@Slf4j
@Service
@Validated
public class IotOtaTaskRecordServiceImpl implements IotOtaTaskRecordService {

    @Resource
    private IotOtaUpgradeRecordMapper iotOtaUpgradeRecordMapper;

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotOtaFirmwareService firmwareService;
    @Resource
    private IotOtaTaskService upgradeTaskService;

    @Override
    public void createOtaTaskRecordBatch(List<Long> deviceIds, Long firmwareId, Long upgradeTaskId) {
        // 1. 校验升级记录信息是否存在，并且已经取消的任务可以重新开始
        deviceIds.forEach(deviceId -> validateUpgradeRecordDuplicate(firmwareId, upgradeTaskId, String.valueOf(deviceId)));

        // 2. 初始化OTA升级记录列表信息
        IotOtaTaskDO upgradeTask = upgradeTaskService.getOtaTask(upgradeTaskId);
        IotOtaFirmwareDO firmware = firmwareService.getOtaFirmware(firmwareId);
        List<IotDeviceDO> deviceList = deviceService.getDeviceListByIdList(deviceIds);
        List<IotOtaTaskRecordDO> upgradeRecordList = deviceList.stream().map(device -> {
            IotOtaTaskRecordDO upgradeRecord = new IotOtaTaskRecordDO();
            upgradeRecord.setFirmwareId(firmware.getId());
            upgradeRecord.setTaskId(upgradeTask.getId());
            upgradeRecord.setDeviceId(device.getId());
            upgradeRecord.setFromFirmwareId(Convert.toLong(device.getFirmwareId()));
            upgradeRecord.setStatus(IotOtaTaskRecordStatusEnum.PENDING.getStatus());
            upgradeRecord.setProgress(0);
            return upgradeRecord;
        }).toList();
        // 3. 保存数据
        iotOtaUpgradeRecordMapper.insertBatch(upgradeRecordList);
        // TODO @芋艿：在这里需要处理推送升级任务的逻辑
    }

    // TODO @li：1）方法注释，简单写；2）父类写了注释，子类就不用写了。。。
    /**
     * 获取OTA升级记录的数量统计。
     * 该方法根据传入的查询条件，统计不同状态的OTA升级记录数量，并返回一个包含各状态数量的映射。
     *
     * @param pageReqVO 包含查询条件的请求对象，主要包括任务ID和设备名称等信息。
     * @return 返回一个Map，其中键为状态常量，值为对应状态的记录数量。
     */
    @Override
    @Transactional
    public Map<Integer, Long> getOtaTaskRecordCount(IotOtaTaskRecordPageReqVO pageReqVO) {
        // 分别查询不同状态的OTA升级记录数量
        List<Map<String, Object>> upgradeRecordCountList = iotOtaUpgradeRecordMapper.selectOtaUpgradeRecordCount(
                pageReqVO.getTaskId(), pageReqVO.getDeviceName());
        Map<String, Object> upgradeRecordCountMap = ObjectUtils.defaultIfNull(upgradeRecordCountList.get(0));
        Objects.requireNonNull(upgradeRecordCountMap);
        return upgradeRecordCountMap.entrySet().stream().collect(Collectors.toMap(
                entry -> Convert.toInt(entry.getKey()),
                entry -> Convert.toLong(entry.getValue())));
    }

    @Override
    @Transactional
    public Map<Integer, Long> getOtaTaskRecordStatistics(Long firmwareId) {
        // 查询并统计不同状态的OTA升级记录数量
        List<Map<String, Object>> upgradeRecordStatisticsList = iotOtaUpgradeRecordMapper.selectOtaUpgradeRecordStatistics(firmwareId);
        Map<String, Object> upgradeRecordStatisticsMap = ObjectUtils.defaultIfNull(upgradeRecordStatisticsList.get(0));
        Objects.requireNonNull(upgradeRecordStatisticsMap);
        return upgradeRecordStatisticsMap.entrySet().stream().collect(Collectors.toMap(
                entry -> Convert.toInt(entry.getKey()),
                entry -> Convert.toLong(entry.getValue())));
    }

    @Override
    public IotOtaTaskRecordDO getOtaTaskRecord(Long id) {
        return iotOtaUpgradeRecordMapper.selectById(id);
    }

    @Override
    public PageResult<IotOtaTaskRecordDO> getOtaTaskRecordPage(IotOtaTaskRecordPageReqVO pageReqVO) {
        return iotOtaUpgradeRecordMapper.selectUpgradeRecordPage(pageReqVO);
    }

    @Override
    public void cancelUpgradeRecordByTaskId(Long taskId) {
        // 暂定只有待推送的升级记录可以取消 TODO @芋艿：可以看看阿里云，哪些可以取消
        iotOtaUpgradeRecordMapper.updateUpgradeRecordStatusByTaskIdAndStatus(
                IotOtaTaskRecordStatusEnum.CANCELED.getStatus(), taskId,
                IotOtaTaskRecordStatusEnum.PENDING.getStatus());
    }

    /**
     * 验证指定的升级记录是否存在。
     * <p>
     * 该函数通过给定的ID查询升级记录，如果查询结果为空，则抛出异常，表示升级记录不存在。
     *
     * @param id 升级记录的唯一标识符，类型为Long。
     * @throws cn.iocoder.yudao.framework.common.exception.ServiceException，则抛出异常，异常类型为OTA_UPGRADE_RECORD_NOT_EXISTS。
     */
    private IotOtaTaskRecordDO validateUpgradeRecordExists(Long id) {
        // 根据ID查询升级记录
        IotOtaTaskRecordDO upgradeRecord = iotOtaUpgradeRecordMapper.selectById(id);
        // 如果查询结果为空，抛出异常
        if (upgradeRecord == null) {
            throw exception(OTA_UPGRADE_RECORD_NOT_EXISTS);
        }
        return upgradeRecord;
    }

    // TODO @li：注释有点冗余
    /**
     * 校验固件升级记录是否重复。
     * <p>
     * 该函数用于检查给定的固件ID、任务ID和设备ID是否已经存在未取消的升级记录。
     * 如果存在未取消的记录，则抛出异常，提示升级记录重复。
     *
     * @param firmwareId 固件ID，用于标识特定的固件版本
     * @param taskId     任务ID，用于标识特定的升级任务
     * @param deviceId   设备ID，用于标识特定的设备
     */
    private void validateUpgradeRecordDuplicate(Long firmwareId, Long taskId, String deviceId) {
        IotOtaTaskRecordDO upgradeRecord = iotOtaUpgradeRecordMapper.selectByConditions(firmwareId, taskId, deviceId);
        if (upgradeRecord == null) {
            return;
        }
        if (!Objects.equals(upgradeRecord.getStatus(), IotOtaTaskRecordStatusEnum.CANCELED.getStatus())) {
            throw exception(OTA_UPGRADE_RECORD_DUPLICATE);
        }
    }

    // TODO @li：注释有点冗余
    /**
     * 验证升级记录是否可以重试。
     * <p>
     * 该方法用于检查给定的升级记录是否处于允许重试的状态。如果升级记录的状态为
     * PENDING、PUSHED 或 UPGRADING，则抛出异常，表示不允许重试。
     *
     * @param upgradeRecord 需要验证的升级记录对象，类型为 IotOtaUpgradeRecordDO
     * @throws cn.iocoder.yudao.framework.common.exception.ServiceException，则抛出 OTA_UPGRADE_RECORD_CANNOT_RETRY 异常
     */
    // TODO @li：这种一次性的方法（不复用的），其实一步一定要抽成小方法；
    private void validateUpgradeRecordCanRetry(IotOtaTaskRecordDO upgradeRecord) {
        if (ObjectUtils.equalsAny(upgradeRecord.getStatus(),
                IotOtaTaskRecordStatusEnum.PENDING.getStatus(),
                IotOtaTaskRecordStatusEnum.PUSHED.getStatus(),
                IotOtaTaskRecordStatusEnum.UPGRADING.getStatus())) {
            throw exception(OTA_UPGRADE_RECORD_CANNOT_RETRY);
        }
    }

}
