package cn.iocoder.yudao.module.crm.framework.operatelog.core;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import com.mzt.logapi.service.IParseFunction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * CRM 合同的 {@link IParseFunction} 实现类
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class CrmContractParseFunction implements IParseFunction {

    public static final String NAME = "getContractById";

    @Resource
    private CrmContractService contractService;

    @Override
    public boolean executeBefore() {
        // 必须在方法执行后（后置相）取值：模板引用的是 LogRecordContext 变量
        // （如 #receivablePlan.contractId），这些变量在方法内部末尾才绑定，
        // 创建前（前置相）取值为 null，会抛 SpelEvaluationException EL1007E
        return false;
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
        CrmContractDO contract = contractService.getContract(Long.parseLong(value.toString()));
        return contract == null ? "" : contract.getName();
    }

}
