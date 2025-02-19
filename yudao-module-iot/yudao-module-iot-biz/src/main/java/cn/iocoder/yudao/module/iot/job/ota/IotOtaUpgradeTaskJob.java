package cn.iocoder.yudao.module.iot.job.ota;

import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaUpgradeRecordDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaUpgradeTaskDO;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeRecordStatusEnum;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeTaskStatusEnum;
import cn.iocoder.yudao.module.iot.service.ota.IotOtaUpgradeRecordService;
import cn.iocoder.yudao.module.iot.service.ota.IotOtaUpgradeTaskService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class IotOtaUpgradeTaskJob implements JobHandler {

    @Resource
    private IotOtaUpgradeTaskService upgradeTaskService;
    @Resource
    private IotOtaUpgradeRecordService upgradeRecordService;

    @Override
    @TenantJob
    public String execute(String param) throws Exception {
        // 1.这个任务主要是为了检查并更新升级任务的状态
        List<IotOtaUpgradeTaskDO> upgradeTasks = upgradeTaskService
                .getUpgradeTaskByState(IotOtaUpgradeTaskStatusEnum.IN_PROGRESS.getStatus());
        // 2.遍历并且确定升级任务的状态
        upgradeTasks.forEach(this::checkUpgradeTaskState);
        // TODO @芋艿: 其他的一些业务逻辑
        return "";
    }

    private void checkUpgradeTaskState(IotOtaUpgradeTaskDO upgradeTask) {
        // 1.查询任务所有的升级记录
        List<IotOtaUpgradeRecordDO> upgradeRecords =
                upgradeRecordService.getUpgradeRecordListByTaskId(upgradeTask.getId());
        if (upgradeRecords.stream().anyMatch(upgradeRecord ->
                ObjectUtils.equalsAny(upgradeRecord.getStatus(),
                        IotOtaUpgradeRecordStatusEnum.PENDING.getStatus(),
                        IotOtaUpgradeRecordStatusEnum.PUSHED.getStatus(),
                        IotOtaUpgradeRecordStatusEnum.UPGRADING.getStatus()))) {
            // 如果存在正在升级的升级记录，则升级任务的状态为进行中
            log.debug("升级任务 {} 状态为进行中", upgradeTask.getId());
        } else if (upgradeRecords.stream().allMatch(upgradeRecord ->
                ObjectUtils.equalsAny(upgradeRecord.getStatus(),
                        IotOtaUpgradeRecordStatusEnum.SUCCESS.getStatus()))) {
            // 如果全部升级成功，则升级任务的状态为已完成
            upgradeTaskService.updateUpgradeTaskStatus(upgradeTask.getId(),
                    IotOtaUpgradeTaskStatusEnum.COMPLETED.getStatus());
        } else if (upgradeRecords.stream().noneMatch(upgradeRecord ->
                ObjectUtils.equalsAny(upgradeRecord.getStatus(),
                        IotOtaUpgradeRecordStatusEnum.PENDING.getStatus(),
                        IotOtaUpgradeRecordStatusEnum.PUSHED.getStatus(),
                        IotOtaUpgradeRecordStatusEnum.UPGRADING.getStatus())) &&
                upgradeRecords.stream().anyMatch(upgradeRecord ->
                        ObjectUtils.equalsAny(upgradeRecord.getStatus(),
                                IotOtaUpgradeRecordStatusEnum.FAILURE.getStatus(),
                                IotOtaUpgradeRecordStatusEnum.CANCELED.getStatus()))) {
            // 如果全部升级完毕，但是存在升级失败或者取消的升级记录，则升级任务的状态为失败
            upgradeTaskService.updateUpgradeTaskStatus(upgradeTask.getId(),
                    IotOtaUpgradeTaskStatusEnum.INCOMPLETE.getStatus());
        }
    }

}
