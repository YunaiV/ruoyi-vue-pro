package cn.iocoder.yudao.module.iot.service.ota;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.task.IotOtaUpgradeTaskPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.task.IotOtaUpgradeTaskSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaUpgradeTaskDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ota.IotOtaUpgradeTaskMapper;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeTaskScopeEnum;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeTaskStatusEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT OTA升级任务 Service 实现类
 *
 * @author Shelly Chan
 */
@Service
@Validated
@Slf4j
public class IotOtaUpgradeTaskServiceImpl implements IotOtaUpgradeTaskService {

    @Resource
    private IotOtaUpgradeTaskMapper upgradeTaskMapper;

    @Resource
    @Lazy
    private IotDeviceService deviceService;
    @Resource
    @Lazy
    private IotOtaFirmwareService firmwareService;
    @Resource
    @Lazy
    private IotOtaUpgradeRecordService upgradeRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUpgradeTask(IotOtaUpgradeTaskSaveReqVO createReqVO) {
        // 1.1 校验同一固件的升级任务名称不重复
        validateFirmwareTaskDuplicate(createReqVO.getFirmwareId(), createReqVO.getName());
        // 1.2 校验固件信息是否存在
        IotOtaFirmwareDO firmware = firmwareService.validateFirmwareExists(createReqVO.getFirmwareId());
        // 1.3 补全设备范围信息，并且校验是否又设备可以升级，如果没有设备可以升级，则报错
        validateScopeAndDevice(createReqVO.getScope(), createReqVO.getDeviceIds(), firmware.getProductId());

        // 2. 保存 OTA 升级任务信息到数据库
        IotOtaUpgradeTaskDO upgradeTask = initOtaUpgradeTask(createReqVO, firmware.getProductId());
        upgradeTaskMapper.insert(upgradeTask);

        // 3. 生成设备升级记录信息并存储，等待定时任务轮询
        upgradeRecordService.createOtaUpgradeRecordBatch(upgradeTask.getDeviceIds(), firmware.getId(), upgradeTask.getId());
        return upgradeTask.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelUpgradeTask(Long id) {
        // 1.1 校验升级任务是否存在
        IotOtaUpgradeTaskDO upgradeTask = validateUpgradeTaskExists(id);
        // 1.2 校验升级任务是否可以取消
        // TODO @li：ObjUtil notequals
        if (!Objects.equals(upgradeTask.getStatus(), IotOtaUpgradeTaskStatusEnum.IN_PROGRESS.getStatus())) {
            throw exception(OTA_UPGRADE_TASK_CANNOT_CANCEL);
        }

        // 2. 更新 OTA 升级任务状态为已取消
        upgradeTaskMapper.updateById(IotOtaUpgradeTaskDO.builder()
                .id(id).status(IotOtaUpgradeTaskStatusEnum.CANCELED.getStatus())
                .build());

        // 3. 更新 OTA 升级记录状态为已取消
        upgradeRecordService.cancelUpgradeRecordByTaskId(id);
    }

    @Override
    public IotOtaUpgradeTaskDO getUpgradeTask(Long id) {
        return upgradeTaskMapper.selectById(id);
    }

    @Override
    public PageResult<IotOtaUpgradeTaskDO> getUpgradeTaskPage(IotOtaUpgradeTaskPageReqVO pageReqVO) {
        return upgradeTaskMapper.selectUpgradeTaskPage(pageReqVO);
    }

    @Override
    public List<IotOtaUpgradeTaskDO> getUpgradeTaskByState(Integer state) {
        return upgradeTaskMapper.selectUpgradeTaskByState(state);
    }

    @Override
    public void updateUpgradeTaskStatus(Long id, Integer status) {
        upgradeTaskMapper.updateById(IotOtaUpgradeTaskDO.builder().id(id).status(status).build());
    }

    /**
     * 校验固件升级任务是否重复
     */
    private void validateFirmwareTaskDuplicate(Long firmwareId, String taskName) {
        List<IotOtaUpgradeTaskDO> upgradeTaskList = upgradeTaskMapper.selectByFirmwareIdAndName(firmwareId, taskName);
        if (CollUtil.isNotEmpty(upgradeTaskList)) {
            throw exception(OTA_UPGRADE_TASK_NAME_DUPLICATE);
        }
    }

    /**
     * 验证升级任务的范围和设备列表的有效性。
     *
     * @param scope     升级任务的范围，参考 IotOtaUpgradeTaskScopeEnum 枚举值
     * @param deviceIds 设备ID列表，当范围为"选择设备"时，该列表不能为空
     * @param productId 产品ID，当范围为"所有设备"时，用于获取设备列表
     * @throws cn.iocoder.yudao.framework.common.exception.ServiceException，抛出相应的异常
     */
    private void validateScopeAndDevice(Integer scope, List<Long> deviceIds, Long productId) {
        if (Objects.equals(scope, IotOtaUpgradeTaskScopeEnum.SELECT.getScope())) {
            if (CollUtil.isEmpty(deviceIds)) {
                throw exception(OTA_UPGRADE_TASK_DEVICE_IDS_EMPTY);
            }
            return;
        }
        
        if (Objects.equals(scope, IotOtaUpgradeTaskScopeEnum.ALL.getScope())) {
            List<IotDeviceDO> deviceList = deviceService.getDeviceListByProductId(productId);
            if (CollUtil.isEmpty(deviceList)) {
                throw exception(OTA_UPGRADE_TASK_DEVICE_LIST_EMPTY);
            }
        }
    }

    /**
     * 验证升级任务是否存在
     */
    private IotOtaUpgradeTaskDO validateUpgradeTaskExists(Long id) {
        IotOtaUpgradeTaskDO upgradeTask = upgradeTaskMapper.selectById(id);
        if (Objects.isNull(upgradeTask)) {
            throw exception(OTA_UPGRADE_TASK_NOT_EXISTS);
        }
        return upgradeTask;
    }

    /**
     * 初始化升级任务
     */
    private IotOtaUpgradeTaskDO initOtaUpgradeTask(IotOtaUpgradeTaskSaveReqVO createReqVO, Long productId) {
        IotOtaUpgradeTaskDO upgradeTask = BeanUtils.toBean(createReqVO, IotOtaUpgradeTaskDO.class);
        upgradeTask.setDeviceCount(Convert.toLong(CollUtil.size(createReqVO.getDeviceIds())))
                .setStatus(IotOtaUpgradeTaskStatusEnum.IN_PROGRESS.getStatus());
        
        if (Objects.equals(createReqVO.getScope(), IotOtaUpgradeTaskScopeEnum.ALL.getScope())) {
            List<IotDeviceDO> deviceList = deviceService.getDeviceListByProductId(productId);
            upgradeTask.setDeviceCount((long) deviceList.size());
            upgradeTask.setDeviceIds(
                    deviceList.stream().map(IotDeviceDO::getId).collect(Collectors.toList()));
        }
        return upgradeTask;
    }

}
