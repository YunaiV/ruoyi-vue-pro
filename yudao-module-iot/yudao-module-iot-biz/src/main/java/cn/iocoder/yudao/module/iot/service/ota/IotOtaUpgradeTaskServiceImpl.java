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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

// TODO @li：完善注释、注解顺序
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

    // TODO @li：注释有点冗余
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

    // TODO @li：注释有点冗余
    /**
     * 验证升级任务的范围和设备列表的有效性。
     * <p>
     * 根据升级任务的范围（scope），验证设备列表（deviceIds）或产品ID（productId）是否有效。
     * 如果范围是“选择设备”（SELECT），则必须提供设备列表；如果范围是“所有设备”（ALL），则必须根据产品ID获取设备列表，并确保列表不为空。
     *
     * @param scope     升级任务的范围，参考 IotOtaUpgradeTaskScopeEnum 枚举值
     * @param deviceIds 设备ID列表，当范围为“选择设备”时，该列表不能为空
     * @param productId 产品ID，当范围为“所有设备”时，用于获取设备列表
     * @throws cn.iocoder.yudao.framework.common.exception.ServiceException，抛出相应的异常
     */
    private void validateScopeAndDevice(Integer scope, List<Long> deviceIds, String productId) {
        // TODO @li：if return
        // 验证范围为“选择设备”时，设备列表不能为空
        if (Objects.equals(scope, IotOtaUpgradeTaskScopeEnum.SELECT.getScope())) {
            if (CollUtil.isEmpty(deviceIds)) {
                throw exception(OTA_UPGRADE_TASK_DEVICE_IDS_EMPTY);
            }
        } else if (Objects.equals(scope, IotOtaUpgradeTaskScopeEnum.ALL.getScope())) {
            // 验证范围为“所有设备”时，根据产品ID获取的设备列表不能为空
            List<IotDeviceDO> deviceList = deviceService.getDeviceListByProductId(Convert.toLong(productId));
            if (CollUtil.isEmpty(deviceList)) {
                throw exception(OTA_UPGRADE_TASK_DEVICE_LIST_EMPTY);
            }
        }
    }

    // TODO @li：注释有点冗余
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

    // TODO @li：注释有点冗余
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
    // TODO @li：一次性的方法，不用特别抽小方法
    private IotOtaUpgradeTaskDO initOtaUpgradeTask(IotOtaUpgradeTaskSaveReqVO createReqVO, String productId) {
        // 将请求参数转换为升级任务对象
        IotOtaUpgradeTaskDO upgradeTask = BeanUtils.toBean(createReqVO, IotOtaUpgradeTaskDO.class);
        // 初始化的时候，设置设备数量和状态
        upgradeTask.setDeviceCount(Convert.toLong(CollUtil.size(createReqVO.getDeviceIds())))
                .setStatus(IotOtaUpgradeTaskStatusEnum.IN_PROGRESS.getStatus());
        // 如果选择全选，则需要查询设备数量
        if (Objects.equals(createReqVO.getScope(), IotOtaUpgradeTaskScopeEnum.ALL.getScope())) {
            // 根据产品ID查询设备数量
            List<IotDeviceDO> deviceList = deviceService.getDeviceListByProductId(Convert.toLong(productId));
            // 设置升级任务的设备数量
            upgradeTask.setDeviceCount((long) deviceList.size());
            upgradeTask.setDeviceIds(
                    deviceList.stream().map(IotDeviceDO::getId).collect(Collectors.toList()));
        }
        // 返回初始化后的升级任务对象
        return upgradeTask;
    }

}
