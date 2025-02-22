package cn.iocoder.yudao.module.iot.job.ota;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaUpgradeRecordDO;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeRecordStatusEnum;
import cn.iocoder.yudao.module.iot.service.ota.IotOtaUpgradeRecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IotOtaUpgradeRecordJob implements JobHandler {

    @Resource
    private IotOtaUpgradeRecordService upgradeRecordService;

    @Override
    @TenantJob
    public String execute(String param) throws Exception {
        // 1. 查询待处理的升级记录
        List<IotOtaUpgradeRecordDO> upgradeRecords = upgradeRecordService
                .getUpgradeRecordListByState(IotOtaUpgradeRecordStatusEnum.PENDING.getStatus());

        // TODO @芋艿 2.执行升级动作
        // TODO @li：应该是逐条 push，逐条更新。不用批量哈

        // 3. 最终，更新升级记录状态
        List<Long> ids = upgradeRecords.stream().map(IotOtaUpgradeRecordDO::getId).toList();
        upgradeRecordService.updateUpgradeRecordStatus(ids, IotOtaUpgradeRecordStatusEnum.PUSHED.getStatus());
        return "";
    }

}
