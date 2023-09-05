package cn.iocoder.yudao.module.trade.job.brokerage;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.trade.service.brokerage.record.MemberBrokerageRecordService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 佣金解冻 Job
 *
 * @author owen
 */
@Component
@TenantJob
public class MemberBrokerageRecordUnfreezeJob implements JobHandler {

    @Resource
    private MemberBrokerageRecordService memberBrokerageRecordService;

    @Override
    public String execute(String param) {
        int count = memberBrokerageRecordService.unfreezeRecord();
        return StrUtil.format("解冻佣金 {} 个", count);
    }

}
