package cn.iocoder.yudao.module.crm.framework.operatelog.core;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivablePlanDO;
import cn.iocoder.yudao.module.crm.service.receivable.CrmReceivablePlanService;
import com.mzt.logapi.service.IParseFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * CRM 回款计划的 {@link IParseFunction} 实现类
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class CrmReceivablePlanParseFunction implements IParseFunction {

    public static final String NAME = "getReceivablePlanServiceById";

    @Resource
    private CrmReceivablePlanService receivablePlanService;

    @Override
    public boolean executeBefore() {
        return true; // 先转换值后对比
    }

    @Override
    public String functionName() {
        return NAME;
    }

    @Override
    public String apply(Object value) {
        if (StrUtil.isEmptyIfStr(value)) {
            return "";
        }
        CrmReceivablePlanDO receivablePlan = receivablePlanService.getReceivablePlan(Long.parseLong(value.toString()));
        return receivablePlan == null ? "" : receivablePlan.getPeriod().toString();
    }

}
