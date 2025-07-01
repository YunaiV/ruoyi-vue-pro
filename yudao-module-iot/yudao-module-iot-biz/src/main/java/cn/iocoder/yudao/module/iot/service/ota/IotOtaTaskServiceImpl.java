package cn.iocoder.yudao.module.iot.service.ota;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.IotOtaTaskCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.IotOtaTaskPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaTaskDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ota.IotOtaTaskMapper;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaTaskDeviceScopeEnum;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaTaskStatusEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT OTA 升级任务 Service 实现类
 *
 * @author Shelly Chan
 */
@Service
@Validated
@Slf4j
public class IotOtaTaskServiceImpl implements IotOtaTaskService {

    @Resource
    private IotOtaTaskMapper otaTaskMapper;

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotOtaFirmwareService otaFirmwareService;
    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private IotOtaTaskRecordService otaUpgradeRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOtaTask(IotOtaTaskCreateReqVO createReqVO) {
        // 1.1 校验同一固件的升级任务名称不重复
        validateFirmwareTaskDuplicate(createReqVO.getFirmwareId(), createReqVO.getName());
        // 1.2 校验固件信息是否存在
        IotOtaFirmwareDO firmware = otaFirmwareService.validateFirmwareExists(createReqVO.getFirmwareId());
        // 1.3 补全设备范围信息，并且校验是否又设备可以升级，如果没有设备可以升级，则报错
        validateScopeAndDevice(createReqVO.getDeviceScope(), createReqVO.getDeviceIds(), firmware.getProductId());

        // 2. 保存 OTA 升级任务信息到数据库
        IotOtaTaskDO upgradeTask = initOtaUpgradeTask(createReqVO, firmware.getProductId());
        otaTaskMapper.insert(upgradeTask);

        // 3. 生成设备升级记录信息并存储，等待定时任务轮询
        // TODO @芋艿：在处理；
//        otaUpgradeRecordService.createOtaUpgradeRecordBatch(upgradeTask.getDeviceIds(), firmware.getId(), upgradeTask.getId());
        return upgradeTask.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOtaTask(Long id) {
        // 1.1 校验升级任务是否存在
        IotOtaTaskDO upgradeTask = validateUpgradeTaskExists(id);
        // 1.2 校验升级任务是否可以取消
        // TODO @li：ObjUtil notequals
        if (!Objects.equals(upgradeTask.getStatus(), IotOtaTaskStatusEnum.IN_PROGRESS.getStatus())) {
            throw exception(OTA_UPGRADE_TASK_CANNOT_CANCEL);
        }

        // 2. 更新 OTA 升级任务状态为已取消
        otaTaskMapper.updateById(IotOtaTaskDO.builder()
                .id(id).status(IotOtaTaskStatusEnum.CANCELED.getStatus())
                .build());

        // 3. 更新 OTA 升级记录状态为已取消
        otaUpgradeRecordService.cancelUpgradeRecordByTaskId(id);
    }

    @Override
    public IotOtaTaskDO getOtaTask(Long id) {
        return otaTaskMapper.selectById(id);
    }

    @Override
    public PageResult<IotOtaTaskDO> getOtaTaskPage(IotOtaTaskPageReqVO pageReqVO) {
        return otaTaskMapper.selectUpgradeTaskPage(pageReqVO);
    }

    /**
     * 校验固件升级任务是否重复
     */
    private void validateFirmwareTaskDuplicate(Long firmwareId, String taskName) {
        List<IotOtaTaskDO> upgradeTaskList = otaTaskMapper.selectByFirmwareIdAndName(firmwareId, taskName);
        if (CollUtil.isNotEmpty(upgradeTaskList)) {
            throw exception(OTA_UPGRADE_TASK_NAME_DUPLICATE);
        }
    }

    private void validateScopeAndDevice(Integer scope, List<Long> deviceIds, Long productId) {
        if (Objects.equals(scope, IotOtaTaskDeviceScopeEnum.SELECT.getScope())) {
            if (CollUtil.isEmpty(deviceIds)) {
                throw exception(OTA_UPGRADE_TASK_DEVICE_IDS_EMPTY);
            }
            return;
        }

        if (Objects.equals(scope, IotOtaTaskDeviceScopeEnum.ALL.getScope())) {
            List<IotDeviceDO> deviceList = deviceService.getDeviceListByProductId(productId);
            if (CollUtil.isEmpty(deviceList)) {
                throw exception(OTA_UPGRADE_TASK_DEVICE_LIST_EMPTY);
            }
        }
    }

    private IotOtaTaskDO validateUpgradeTaskExists(Long id) {
        IotOtaTaskDO upgradeTask = otaTaskMapper.selectById(id);
        if (Objects.isNull(upgradeTask)) {
            throw exception(OTA_UPGRADE_TASK_NOT_EXISTS);
        }
        return upgradeTask;
    }

    private IotOtaTaskDO initOtaUpgradeTask(IotOtaTaskCreateReqVO createReqVO, Long productId) {
        IotOtaTaskDO upgradeTask = BeanUtils.toBean(createReqVO, IotOtaTaskDO.class);
        upgradeTask.setDeviceTotalCount(Convert.toLong(CollUtil.size(createReqVO.getDeviceIds())))
                .setStatus(IotOtaTaskStatusEnum.IN_PROGRESS.getStatus());

        if (Objects.equals(createReqVO.getDeviceScope(), IotOtaTaskDeviceScopeEnum.ALL.getScope())) {
            List<IotDeviceDO> deviceList = deviceService.getDeviceListByProductId(productId);
            upgradeTask.setDeviceTotalCount((long) deviceList.size());
//            upgradeTask.setDeviceIds(
//                    deviceList.stream().map(IotDeviceDO::getId).collect(Collectors.toList()));
        }
        return upgradeTask;
    }

}
