package cn.iocoder.yudao.module.iot.service.ota;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.task.IotOtaUpgradeTaskPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.task.IotOtaUpgradeTaskSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.ota.IotOtaUpgradeRecordConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaUpgradeTaskDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ota.IotOtaUpgradeTaskMapper;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeTaskScopeEnum;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeTaskStatusEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.ota.bo.IotOtaUpgradeRecordCreateReqBO;
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

@Slf4j
@Service
@Validated
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
        // 1.3 校验升级范围=2(指定设备时),deviceIds deviceNames不为空并且长度相等
        // TODO @li：deviceNames 应该后端查询
        validateScopeAndDevice(createReqVO.getScope(), createReqVO.getDeviceIds(), createReqVO.getDeviceNames());
        // TODO @li：如果全部范围，但是没设备可以升级，需要报错

        // 2. 保存 OTA 升级任务信息到数据库
        IotOtaUpgradeTaskDO upgradeTask = initUpgradeTask(createReqVO, firmware.getProductId());
        upgradeTaskMapper.insert(upgradeTask);

        // 3. 生成设备升级记录信息并存储，等待定时任务轮询
        List<IotOtaUpgradeRecordCreateReqBO> upgradeRecordList = initUpgradeRecordList(
                upgradeTask, firmware, createReqVO.getDeviceIds());
        // TODO @li：只需要传递 deviceIds、firewareId、剩余的 upgradeRecordService 里面自己处理；这样，后续 record 加字段，都不需要透传太多；解耦
        upgradeRecordService.createUpgradeRecordBatch(upgradeRecordList);
        // TODO @芋艿: 创建任务触发的其他Action
        return upgradeTask.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelUpgradeTask(Long id) {
        // 1.1 校验升级任务是否存在
        IotOtaUpgradeTaskDO upgradeTask = validateUpgradeTaskExists(id);
        // 1.2 校验升级任务是否可以取消
        // TODO @li：这种一次性的，可以不考虑拆分方法
        validateUpgradeTaskCanCancel(upgradeTask);

        // 2. 更新 OTA 升级任务状态为已取消
        upgradeTaskMapper.updateById(IotOtaUpgradeTaskDO.builder()
                .id(id).status(IotOtaUpgradeTaskStatusEnum.CANCELED.getStatus())
                .build());
        // 3. 更新 OTA 升级记录状态为已取消
        upgradeRecordService.cancelUpgradeRecordByTaskId(id);
        // TODO @芋艿: 取消任务触发的其他Action
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
        upgradeTaskMapper.updateById(IotOtaUpgradeTaskDO.builder()
                .id(id).status(status)
                .build());
    }

    /**
     * 校验固件升级任务是否重复
     * <p>
     * 该方法用于检查给定固件ID和任务名称组合是否已存在于数据库中，如果存在则抛出异常，
     * 表示任务名称对于该固件而言是重复的此检查确保用户不能创建具有相同名称的任务，
     * 从而避免数据重复和混淆
     *
     * @param firmwareId 固件的唯一标识符，用于区分不同的固件
     * @param taskName   升级任务的名称，用于与固件ID一起检查重复性
     * @throws cn.iocoder.yudao.framework.common.exception.ServerException 则抛出预定义的异常
     */
    private void validateFirmwareTaskDuplicate(Long firmwareId, String taskName) {
        // 查询数据库中是否有相同固件ID和任务名称的升级任务存在
        List<IotOtaUpgradeTaskDO> upgradeTaskList = upgradeTaskMapper.selectByFirmwareIdAndName(firmwareId, taskName);
        // 如果查询结果不为空，说明存在重复的任务名称，抛出异常
        if (CollUtil.isNotEmpty(upgradeTaskList)) {
            throw exception(OTA_UPGRADE_TASK_NAME_DUPLICATE);
        }
    }

    /**
     * 验证升级任务的范围和设备参数是否有效
     * 当选择特定设备进行升级时，确保提供的设备ID和设备名称列表有效且对应
     *
     * @param scope       升级任务的范围，表示是选择特定设备还是其他范围
     * @param deviceIds   设备ID列表，用于标识参与升级的设备
     * @param deviceNames 设备名称列表，与设备ID列表对应
     */
    private void validateScopeAndDevice(Integer scope, List<Long> deviceIds, List<String> deviceNames) {
        // 当升级任务范围为选择特定设备时
        if (Objects.equals(scope, IotOtaUpgradeTaskScopeEnum.SELECT.getScope())) {
            // 检查设备ID列表和设备名称列表是否为空或长度不一致，若不符合要求，则抛出异常
            if (CollUtil.isEmpty(deviceIds) || CollUtil.isEmpty(deviceNames) || deviceIds.size() != deviceNames.size()) {
                throw exception(OTA_UPGRADE_TASK_PARAMS_INVALID);
            }
        }
    }

    /**
     * 验证升级任务是否存在
     * <p>
     * 通过查询数据库来验证给定ID的升级任务是否存在此方法主要用于确保后续操作所针对的升级任务是有效的
     *
     * @param id 升级任务的唯一标识符如果为null或数据库中不存在对应的记录，则认为任务不存在
     * @throws cn.iocoder.yudao.framework.common.exception.ServiceException 如果升级任务不存在，则抛出异常提示任务不存在
     */
    private IotOtaUpgradeTaskDO validateUpgradeTaskExists(Long id) {
        // 查询数据库中是否有相同固件ID和任务名称的升级任务存在
        IotOtaUpgradeTaskDO upgradeTask = upgradeTaskMapper.selectById(id);
        // 如果查询结果不为空，说明存在重复的任务名称，抛出异常
        if (Objects.isNull(upgradeTask)) {
            throw exception(OTA_UPGRADE_TASK_NOT_EXISTS);
        }
        return upgradeTask;
    }

    /**
     * 验证升级任务是否可以被取消
     * <p>
     * 此方法旨在确保只有当升级任务处于进行中状态时，才可以执行取消操作
     * 它通过比较任务的当前状态与预定义的进行中状态来判断是否允许取消操作
     * 如果任务状态不符合条件，则抛出异常，表明该任务无法取消
     *
     * @param upgradeTask 待验证的升级任务对象，包含任务的详细信息，如状态等
     * @throws cn.iocoder.yudao.framework.common.exception.ServiceException 如果任务状态不是进行中，则抛出此异常，表明任务无法取消
     */
    private void validateUpgradeTaskCanCancel(IotOtaUpgradeTaskDO upgradeTask) {
        // 检查升级任务的状态是否为进行中，只有此状态下的任务才允许取消
        if (!Objects.equals(upgradeTask.getStatus(), IotOtaUpgradeTaskStatusEnum.IN_PROGRESS.getStatus())) {
            // 只有进行中的任务才可以取消
            throw exception(OTA_UPGRADE_TASK_CANNOT_CANCEL);
        }
    }

    // TODO @li：一次性，不复用的，可以直接写在对应的逻辑里；
    /**
     * 初始化升级任务
     * <p>
     * 根据请求参数创建升级任务对象，并根据选择的范围初始化设备数量
     * 如果选择特定设备进行升级，则设备数量为所选设备的总数
     * 如果选择全部设备进行升级，则设备数量为该固件对应产品下的所有设备总数
     *
     * @param createReqVO 升级任务保存请求对象，包含创建升级任务所需的信息
     * @return 返回初始化后的升级任务对象
     */
    private IotOtaUpgradeTaskDO initUpgradeTask(IotOtaUpgradeTaskSaveReqVO createReqVO, String productId) {
        // 配置各项参数
        IotOtaUpgradeTaskDO upgradeTask = IotOtaUpgradeTaskDO.builder()
                // TODO @li：不用每个占一行。最好相同类型的，放在一行里；
                .name(createReqVO.getName())
                .description(createReqVO.getDescription())
                .firmwareId(createReqVO.getFirmwareId())
                .scope(createReqVO.getScope())
                .deviceIds(createReqVO.getDeviceIds())
                .deviceNames(createReqVO.getDeviceNames())
                .deviceCount(Convert.toLong(CollUtil.size(createReqVO.getDeviceIds())))
                .status(IotOtaUpgradeTaskStatusEnum.IN_PROGRESS.getStatus())
                .build();
        // 如果选择全选，则需要查询设备数量
        if (Objects.equals(createReqVO.getScope(), IotOtaUpgradeTaskScopeEnum.ALL.getScope())) {
            // 根据产品ID查询设备数量
            Long deviceCount = deviceService.getDeviceCountByProductId(Convert.toLong(productId));
            // 设置升级任务的设备数量
            upgradeTask.setDeviceCount(deviceCount);
        }
        // 返回初始化后的升级任务对象
        return upgradeTask;
    }

    /**
     * 初始化升级记录列表
     * <p>
     * 根据升级任务的范围（选择设备或按产品ID）获取设备列表，并将其转换为升级记录请求对象列表。
     *
     * @param upgradeTask 升级任务对象，包含升级任务的相关信息
     * @param firmware    固件对象，包含固件的相关信息
     * @param deviceIds   设备ID列表，仅在升级任务范围为选择设备时使用
     * @return 升级记录请求对象列表，包含每个设备的升级记录信息
     */
    private List<IotOtaUpgradeRecordCreateReqBO> initUpgradeRecordList(
            IotOtaUpgradeTaskDO upgradeTask, IotOtaFirmwareDO firmware, List<Long> deviceIds) {
        // TODO @li：需要考虑，如果创建多个任务，相互之间不能重复；
        // 1）指定设备的时候，进行校验；2）如果是全部，则过滤其它已经发起的；；；；；另外，需要排除掉 cancel 的哈。因为 cancal 之后，还可以发起
        // 根据升级任务的范围确定设备列表
        List<IotDeviceDO> deviceList;
        if (Objects.equals(upgradeTask.getScope(), IotOtaUpgradeTaskScopeEnum.SELECT.getScope())) {
            // 如果升级任务范围为选择设备，则根据设备ID列表获取设备信息
            deviceList = deviceService.getDeviceListByIdList(deviceIds);
        } else {
            // 如果升级任务范围为按产品ID，则根据固件的产品ID获取设备信息
            deviceList = deviceService.getDeviceListByProductId(Convert.toLong(firmware.getProductId()));
        }
        // 将升级任务、固件和设备列表转换为升级记录请求对象列表
        return IotOtaUpgradeRecordConvert.INSTANCE.convertBOList(upgradeTask, firmware, deviceList);
    }

}
