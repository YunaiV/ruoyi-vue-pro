package cn.iocoder.yudao.module.iot.service.ota;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.IotOtaTaskCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.IotOtaTaskPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaTaskDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaTaskRecordDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ota.IotOtaTaskMapper;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaTaskDeviceScopeEnum;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaTaskRecordStatusEnum;
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
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
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
    private IotOtaTaskRecordService otaTaskRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOtaTask(IotOtaTaskCreateReqVO createReqVO) {
        // 1.1 校验固件信息是否存在
        IotOtaFirmwareDO firmware = otaFirmwareService.validateFirmwareExists(createReqVO.getFirmwareId());
        // 1.2 校验同一固件的升级任务名称不重复
        if (otaTaskMapper.selectByFirmwareIdAndName(firmware.getId(), createReqVO.getName()) != null) {
            throw exception(OTA_TASK_CREATE_FAIL_NAME_DUPLICATE);
        }
        // 1.3 校验设备范围信息
        List<IotDeviceDO> devices = validateOtaTaskDeviceScope(createReqVO, firmware.getProductId());

        // 2. 保存升级任务，直接转换
        IotOtaTaskDO task = BeanUtils.toBean(createReqVO, IotOtaTaskDO.class)
                .setStatus(IotOtaTaskStatusEnum.IN_PROGRESS.getStatus())
                .setDeviceTotalCount(devices.size()).setDeviceSuccessCount(0);
        otaTaskMapper.insert(task);

        // 3. 生成设备升级记录
        otaTaskRecordService.createOtaTaskRecordList(devices, firmware.getId(), task.getId());
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOtaTask(Long id) {
        // 1.1 校验升级任务是否存在
        IotOtaTaskDO upgradeTask = validateUpgradeTaskExists(id);
        // 1.2 校验升级任务是否可以取消
        if (ObjUtil.notEqual(upgradeTask.getStatus(), IotOtaTaskStatusEnum.IN_PROGRESS.getStatus())) {
            throw exception(OTA_TASK_CANCEL_FAIL_STATUS_END);
        }

        // 2. 更新升级任务状态为已取消
        otaTaskMapper.updateById(IotOtaTaskDO.builder()
                .id(id).status(IotOtaTaskStatusEnum.CANCELED.getStatus())
                .build());

        // 3. 更新升级记录状态为已取消
        otaTaskRecordService.cancelTaskRecordListByTaskId(id);
    }

    @Override
    public IotOtaTaskDO getOtaTask(Long id) {
        return otaTaskMapper.selectById(id);
    }

    @Override
    public PageResult<IotOtaTaskDO> getOtaTaskPage(IotOtaTaskPageReqVO pageReqVO) {
        return otaTaskMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateOtaTaskStatusEnd(Long taskId) {
        int updateCount = otaTaskMapper.updateByIdAndStatus(taskId, IotOtaTaskStatusEnum.IN_PROGRESS.getStatus(),
                new IotOtaTaskDO().setStatus(IotOtaTaskStatusEnum.END.getStatus()));
        if (updateCount == 0) {
            log.warn("[updateOtaTaskStatusEnd][任务({})不存在或状态不是进行中，无法更新]", taskId);
        }
    }

    private List<IotDeviceDO> validateOtaTaskDeviceScope(IotOtaTaskCreateReqVO createReqVO, Long productId) {
        // 情况一：选择设备
        if (Objects.equals(createReqVO.getDeviceScope(), IotOtaTaskDeviceScopeEnum.SELECT.getScope())) {
            // 1.1 校验设备存在
            List<IotDeviceDO> devices = deviceService.validateDeviceListExists(createReqVO.getDeviceIds());
            for (IotDeviceDO device : devices) {
                if (ObjUtil.notEqual(device.getProductId(), productId)) {
                    throw exception(DEVICE_NOT_EXISTS);
                }
            }
            // 1.2 校验设备是否已经是该固件版本
            devices.forEach(device -> {
                if (Objects.equals(device.getFirmwareId(), createReqVO.getFirmwareId())) {
                    throw exception(OTA_TASK_CREATE_FAIL_DEVICE_FIRMWARE_EXISTS, device.getDeviceName());
                }
            });
            // 1.3 校验设备是否已经在升级中
            List<IotOtaTaskRecordDO> records = otaTaskRecordService.getOtaTaskRecordListByDeviceIdAndStatus(
                    convertSet(devices, IotDeviceDO::getId), IotOtaTaskRecordStatusEnum.IN_PROCESS_STATUSES);
            devices.forEach(device -> {
                if (CollUtil.contains(records, item -> item.getDeviceId().equals(device.getId()))) {
                    throw exception(OTA_TASK_CREATE_FAIL_DEVICE_OTA_IN_PROCESS, device.getDeviceName());
                }
            });
            return devices;
        }
        // 情况二：全部设备
        if (Objects.equals(createReqVO.getDeviceScope(), IotOtaTaskDeviceScopeEnum.ALL.getScope())) {
            List<IotDeviceDO> devices = deviceService.getDeviceListByProductId(productId);
            // 2.1.1 移除已经是该固件版本的设备
            devices.removeIf(device -> Objects.equals(device.getFirmwareId(), createReqVO.getFirmwareId()));
            // 2.1.2 移除已经在升级中的设备
            List<IotOtaTaskRecordDO> records = otaTaskRecordService.getOtaTaskRecordListByDeviceIdAndStatus(
                    convertSet(devices, IotDeviceDO::getId), IotOtaTaskRecordStatusEnum.IN_PROCESS_STATUSES);
            devices.removeIf(device -> CollUtil.contains(records,
                    item -> item.getDeviceId().equals(device.getId())));
            // 2.2 校验是否有可升级的设备
            if (CollUtil.isEmpty(devices)) {
                throw exception(OTA_TASK_CREATE_FAIL_DEVICE_EMPTY);
            }
            return devices;
        }
        throw new IllegalArgumentException("不支持的设备范围：" + createReqVO.getDeviceScope());
    }

    private IotOtaTaskDO validateUpgradeTaskExists(Long id) {
        IotOtaTaskDO upgradeTask = otaTaskMapper.selectById(id);
        if (Objects.isNull(upgradeTask)) {
            throw exception(OTA_TASK_NOT_EXISTS);
        }
        return upgradeTask;
    }

}
