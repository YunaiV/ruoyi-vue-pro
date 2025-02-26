package cn.iocoder.yudao.module.iot.service.ota;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.record.IotOtaUpgradeRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaUpgradeRecordDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ota.IotOtaUpgradeRecordMapper;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeRecordStatusEnum;
import cn.iocoder.yudao.module.iot.service.ota.bo.IotOtaUpgradeRecordCreateReqBO;
import cn.iocoder.yudao.module.iot.service.ota.bo.IotOtaUpgradeRecordUpdateReqBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

@Slf4j
@Service
@Validated
public class IotOtaUpgradeRecordServiceImpl implements IotOtaUpgradeRecordService {

    @Resource
    private IotOtaUpgradeRecordMapper upgradeRecordMapper;

    @Override
    public void createUpgradeRecordBatch(List<IotOtaUpgradeRecordCreateReqBO> createList) {
        // 1. 批量校验参数信息
        createList.forEach(saveBO -> validateUpgradeRecordDuplicate(saveBO.getFirmwareId(), saveBO.getTaskId(), saveBO.getDeviceId()));

        // 2. 将数据批量存储到数据库里
        List<IotOtaUpgradeRecordDO> upgradeRecords = BeanUtils.toBean(createList, IotOtaUpgradeRecordDO.class);
        upgradeRecordMapper.insertBatch(upgradeRecords);
    }

    @Override
    @Transactional
    public void updateUpgradeRecord(IotOtaUpgradeRecordUpdateReqBO updateReqBO) {
        // 1. 校验升级记录信息是否存在
        validateUpgradeRecordExists(updateReqBO.getId());

        // 2. 将数据转化成数据库存储的格式
        IotOtaUpgradeRecordDO updateRecord = BeanUtils.toBean(updateReqBO, IotOtaUpgradeRecordDO.class);
        upgradeRecordMapper.updateById(updateRecord);
        // TODO @芋艿: 更新升级记录触发的其他Action
    }

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
        // TODO @li: 通过 groupby 统计下；
        Long pending = upgradeRecordMapper.getOtaUpgradeRecordCount(pageReqVO.getTaskId(), pageReqVO.getDeviceName(), IotOtaUpgradeRecordStatusEnum.PENDING.getStatus());
        Long pushed = upgradeRecordMapper.getOtaUpgradeRecordCount(pageReqVO.getTaskId(), pageReqVO.getDeviceName(), IotOtaUpgradeRecordStatusEnum.PUSHED.getStatus());
        Long upgrading = upgradeRecordMapper.getOtaUpgradeRecordCount(pageReqVO.getTaskId(), pageReqVO.getDeviceName(), IotOtaUpgradeRecordStatusEnum.UPGRADING.getStatus());
        Long success = upgradeRecordMapper.getOtaUpgradeRecordCount(pageReqVO.getTaskId(), pageReqVO.getDeviceName(), IotOtaUpgradeRecordStatusEnum.SUCCESS.getStatus());
        Long failure = upgradeRecordMapper.getOtaUpgradeRecordCount(pageReqVO.getTaskId(), pageReqVO.getDeviceName(), IotOtaUpgradeRecordStatusEnum.FAILURE.getStatus());
        Long canceled = upgradeRecordMapper.getOtaUpgradeRecordCount(pageReqVO.getTaskId(), pageReqVO.getDeviceName(), IotOtaUpgradeRecordStatusEnum.CANCELED.getStatus());
        // 将各状态的数量封装到Map中返回
        // TODO @li：使用 MapUtil，因为 Map.of 是 jdk9 才有，后续不好同步到 master 的 jdk8；
        return Map.of(IotOtaUpgradeRecordPageReqVO.PENDING, pending,
                IotOtaUpgradeRecordPageReqVO.PUSHED, pushed,
                IotOtaUpgradeRecordPageReqVO.UPGRADING, upgrading,
                IotOtaUpgradeRecordPageReqVO.SUCCESS, success,
                IotOtaUpgradeRecordPageReqVO.FAILURE, failure,
                IotOtaUpgradeRecordPageReqVO.CANCELED, canceled);
    }

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
        // TODO @li: 通过 groupby 统计下；
        Long pending = upgradeRecordMapper.getOtaUpgradeRecordStatistics(firmwareId, IotOtaUpgradeRecordStatusEnum.PENDING.getStatus());
        Long pushed = upgradeRecordMapper.getOtaUpgradeRecordStatistics(firmwareId, IotOtaUpgradeRecordStatusEnum.PUSHED.getStatus());
        Long upgrading = upgradeRecordMapper.getOtaUpgradeRecordStatistics(firmwareId, IotOtaUpgradeRecordStatusEnum.UPGRADING.getStatus());
        Long success = upgradeRecordMapper.getOtaUpgradeRecordStatistics(firmwareId, IotOtaUpgradeRecordStatusEnum.SUCCESS.getStatus());
        Long failure = upgradeRecordMapper.getOtaUpgradeRecordStatistics(firmwareId, IotOtaUpgradeRecordStatusEnum.FAILURE.getStatus());
        Long canceled = upgradeRecordMapper.getOtaUpgradeRecordStatistics(firmwareId, IotOtaUpgradeRecordStatusEnum.CANCELED.getStatus());
        // 将统计结果封装为Map并返回
        return Map.of(IotOtaUpgradeRecordPageReqVO.PENDING, pending,
                IotOtaUpgradeRecordPageReqVO.PUSHED, pushed,
                IotOtaUpgradeRecordPageReqVO.UPGRADING, upgrading,
                IotOtaUpgradeRecordPageReqVO.SUCCESS, success,
                IotOtaUpgradeRecordPageReqVO.FAILURE, failure,
                IotOtaUpgradeRecordPageReqVO.CANCELED, canceled);
    }

    @Override
    public void retryUpgradeRecord(Long id) {
        // 1.1.校验升级记录信息是否存在
        IotOtaUpgradeRecordDO upgradeRecord = validateUpgradeRecordExists(id);
        // 1.2.校验升级记录是否可以重新升级
        validateUpgradeRecordCanRetry(upgradeRecord);

        // 2.将一些数据重置，这样定时任务轮询就可以重启任务
        upgradeRecordMapper.updateById(new IotOtaUpgradeRecordDO()
                .setId(upgradeRecord.getId()).setProgress(0)
                .setStatus(IotOtaUpgradeRecordStatusEnum.PENDING.getStatus()));
        // TODO @芋艿: 重试升级记录触发的其他Action
        // TODO 如果一个升级记录被取消或者已经执行失败，重试成功，是否会对升级任务的状态有影响？
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
        // 暂定只有待推送的升级记录可以取消
        upgradeRecordMapper.cancelUpgradeRecordByTaskId(taskId);
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

    /**
     * 验证固件升级记录是否存在。
     * <p>
     * 该函数通过给定的固件ID、任务ID和设备ID查询升级记录，如果查询结果为空，则抛出异常。
     *
     * @param firmwareId 固件ID，用于标识特定的固件版本
     * @param taskId     任务ID，用于标识特定的升级任务
     * @param deviceId   设备ID，用于标识特定的设备
     * @throws cn.iocoder.yudao.framework.common.exception.ServiceException，则抛出OTA_UPGRADE_RECORD_NOT_EXISTS异常
     */
    private void validateUpgradeRecordDuplicate(Long firmwareId, Long taskId, String deviceId) {
        // 根据条件查询升级记录
        IotOtaUpgradeRecordDO upgradeRecord = upgradeRecordMapper.selectByConditions(firmwareId, taskId, deviceId);
        // 如果查询结果为空，抛出异常
        if (upgradeRecord != null) {
            throw exception(OTA_UPGRADE_RECORD_DUPLICATE);
        }
    }

    /**
     * 验证升级记录是否可以重试。
     * <p>
     * 该方法用于检查给定的升级记录是否处于允许重试的状态。如果升级记录的状态为
     * PENDING、PUSHED 或 UPGRADING，则抛出异常，表示不允许重试。
     *
     * @param upgradeRecord 需要验证的升级记录对象，类型为 IotOtaUpgradeRecordDO
     * @throws cn.iocoder.yudao.framework.common.exception.ServiceException，则抛出 OTA_UPGRADE_RECORD_CANNOT_RETRY 异常
     */
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
