package cn.iocoder.yudao.module.iot.service.ota;

import cn.hutool.core.convert.Convert;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.record.IotOtaUpgradeRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaUpgradeRecordDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaUpgradeTaskDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ota.IotOtaUpgradeRecordMapper;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeRecordStatusEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
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
public class IotOtaUpgradeRecordServiceImpl implements IotOtaUpgradeRecordService {

    @Resource
    private IotOtaUpgradeRecordMapper upgradeRecordMapper;
    // TODO @li：1）@Resource 写在 @Lazy 之前，先用关键注解；2）有必要的情况下，在写 @Lazy 注解。
    @Lazy
    @Resource
    private IotDeviceService deviceService;
    @Lazy
    @Resource
    private IotOtaFirmwareService firmwareService;
    @Lazy
    @Resource
    private IotOtaUpgradeTaskService upgradeTaskService;

    @Override
    public void createOtaUpgradeRecordBatch(List<Long> deviceIds, Long firmwareId, Long upgradeTaskId) {
        // 1. 校验升级记录信息是否存在，并且已经取消的任务可以重新开始
        // TODO @li：批量查询。。
        deviceIds.forEach(deviceId -> validateUpgradeRecordDuplicate(firmwareId, upgradeTaskId, String.valueOf(deviceId)));

        // 2.初始化OTA升级记录列表信息
        IotOtaUpgradeTaskDO upgradeTask = upgradeTaskService.getUpgradeTask(upgradeTaskId);
        IotOtaFirmwareDO firmware = firmwareService.getOtaFirmware(firmwareId);
        List<IotDeviceDO> deviceList = deviceService.getDeviceListByIdList(deviceIds);
        List<IotOtaUpgradeRecordDO> upgradeRecordList = deviceList.stream().map(device -> {
            IotOtaUpgradeRecordDO upgradeRecord = new IotOtaUpgradeRecordDO();
            upgradeRecord.setFirmwareId(firmware.getId());
            upgradeRecord.setTaskId(upgradeTask.getId());
            upgradeRecord.setProductKey(device.getProductKey());
            upgradeRecord.setDeviceName(device.getDeviceName());
            upgradeRecord.setDeviceId(Convert.toStr(device.getId()));
            upgradeRecord.setFromFirmwareId(Convert.toLong(device.getFirmwareId()));
            upgradeRecord.setStatus(IotOtaUpgradeRecordStatusEnum.PENDING.getStatus());
            upgradeRecord.setProgress(0);
            return upgradeRecord;
        }).collect(Collectors.toList());
        // 3.保存数据
        upgradeRecordMapper.insertBatch(upgradeRecordList);
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
    public Map<Integer, Long> getOtaUpgradeRecordCount(IotOtaUpgradeRecordPageReqVO pageReqVO) {
        // 分别查询不同状态的OTA升级记录数量
        List<Map<String, Object>> upgradeRecordCountList = upgradeRecordMapper.selectOtaUpgradeRecordCount(
                pageReqVO.getTaskId(), pageReqVO.getDeviceName());
        Map<String, Object> upgradeRecordCountMap = ObjectUtils.defaultIfNull(upgradeRecordCountList.get(0));
        Objects.requireNonNull(upgradeRecordCountMap);
        return upgradeRecordCountMap.entrySet().stream().collect(Collectors.toMap(
                entry -> Convert.toInt(entry.getKey()),
                entry -> Convert.toLong(entry.getValue())));
    }

    // TODO @li：1）方法注释，简单写；2）父类写了注释，子类就不用写了。。。
    /**
     * 获取指定固件ID的OTA升级记录统计信息。
     * 该方法通过查询数据库，统计不同状态的OTA升级记录数量，并返回一个包含各状态数量的映射。
     *
     * @param firmwareId 固件ID，用于指定需要统计的固件升级记录。
     * @return 返回一个Map，其中键为升级记录状态（如PENDING、PUSHED等），值为对应状态的记录数量。
     */
    @Override
    @Transactional
    public Map<Integer, Long> getOtaUpgradeRecordStatistics(Long firmwareId) {
        // 查询并统计不同状态的OTA升级记录数量
        List<Map<String, Object>> upgradeRecordStatisticsList = upgradeRecordMapper.selectOtaUpgradeRecordStatistics(firmwareId);
        Map<String, Object> upgradeRecordStatisticsMap = ObjectUtils.defaultIfNull(upgradeRecordStatisticsList.get(0));
        Objects.requireNonNull(upgradeRecordStatisticsMap);
        return upgradeRecordStatisticsMap.entrySet().stream().collect(Collectors.toMap(
                entry -> Convert.toInt(entry.getKey()),
                entry -> Convert.toLong(entry.getValue())));
    }

    @Override
    public void retryUpgradeRecord(Long id) {
        // 1.1.校验升级记录信息是否存在
        IotOtaUpgradeRecordDO upgradeRecord = validateUpgradeRecordExists(id);
        // 1.2.校验升级记录是否可以重新升级
        validateUpgradeRecordCanRetry(upgradeRecord);

        // 2. 将一些数据重置，这样定时任务轮询就可以重启任务
        // TODO @li：更新的时候，wherestatus；
        upgradeRecordMapper.updateById(new IotOtaUpgradeRecordDO()
                .setId(upgradeRecord.getId()).setProgress(0)
                .setStatus(IotOtaUpgradeRecordStatusEnum.PENDING.getStatus()));
    }

    @Override
    public IotOtaUpgradeRecordDO getUpgradeRecord(Long id) {
        return upgradeRecordMapper.selectById(id);
    }

    @Override
    public PageResult<IotOtaUpgradeRecordDO> getUpgradeRecordPage(IotOtaUpgradeRecordPageReqVO pageReqVO) {
        return upgradeRecordMapper.selectUpgradeRecordPage(pageReqVO);
    }

    @Override
    public void cancelUpgradeRecordByTaskId(Long taskId) {
        // 暂定只有待推送的升级记录可以取消 TODO @芋艿：可以看看阿里云，哪些可以取消
        upgradeRecordMapper.updateUpgradeRecordStatusByTaskIdAndStatus(
                IotOtaUpgradeRecordStatusEnum.CANCELED.getStatus(), taskId,
                IotOtaUpgradeRecordStatusEnum.PENDING.getStatus());
    }

    @Override
    public List<IotOtaUpgradeRecordDO> getUpgradeRecordListByState(Integer state) {
        return upgradeRecordMapper.selectUpgradeRecordListByState(state);
    }

    @Override
    public void updateUpgradeRecordStatus(List<Long> ids, Integer status) {
        upgradeRecordMapper.updateUpgradeRecordStatus(ids, status);
    }

    @Override
    public List<IotOtaUpgradeRecordDO> getUpgradeRecordListByTaskId(Long taskId) {
        return upgradeRecordMapper.selectUpgradeRecordListByTaskId(taskId);
    }

    /**
     * 验证指定的升级记录是否存在。
     * <p>
     * 该函数通过给定的ID查询升级记录，如果查询结果为空，则抛出异常，表示升级记录不存在。
     *
     * @param id 升级记录的唯一标识符，类型为Long。
     * @throws cn.iocoder.yudao.framework.common.exception.ServiceException，则抛出异常，异常类型为OTA_UPGRADE_RECORD_NOT_EXISTS。
     */
    private IotOtaUpgradeRecordDO validateUpgradeRecordExists(Long id) {
        // 根据ID查询升级记录
        IotOtaUpgradeRecordDO upgradeRecord = upgradeRecordMapper.selectById(id);
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
        // 根据条件查询升级记录
        IotOtaUpgradeRecordDO upgradeRecord = upgradeRecordMapper.selectByConditions(firmwareId, taskId, deviceId);
        // 如果查询到升级记录且状态不是已取消，则抛出异常
        // TODO @li：if return，减少括号层级；
        // TODO @li：ObjUtil.notEquals，尽量不用 ！取否逻辑；
        if (upgradeRecord != null) {
            if (!IotOtaUpgradeRecordStatusEnum.CANCELED.getStatus().equals(upgradeRecord.getStatus())) {
                // TODO @li：提示的时候，需要把 deviceName 给提示出来，不然用户不知道哪个重复啦。
                throw exception(OTA_UPGRADE_RECORD_DUPLICATE);
            }
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
    private void validateUpgradeRecordCanRetry(IotOtaUpgradeRecordDO upgradeRecord) {
        // 检查升级记录的状态是否为 PENDING、PUSHED 或 UPGRADING
        if (ObjectUtils.equalsAny(upgradeRecord.getStatus(),
                IotOtaUpgradeRecordStatusEnum.PENDING.getStatus(),
                IotOtaUpgradeRecordStatusEnum.PUSHED.getStatus(),
                IotOtaUpgradeRecordStatusEnum.UPGRADING.getStatus())) {
            // 如果升级记录处于上述状态之一，则抛出异常，表示不允许重试
            throw exception(OTA_UPGRADE_RECORD_CANNOT_RETRY);
        }
    }

}
