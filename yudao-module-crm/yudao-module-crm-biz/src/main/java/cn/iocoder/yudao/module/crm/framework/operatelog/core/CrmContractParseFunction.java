package cn.iocoder.yudao.module.crm.framework.operatelog.core;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import com.mzt.logapi.service.IParseFunction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.module.crm.enums.operatelog.CrmParseFunctionNameConstants.GET_CONTRACT_BY_ID;

/**
 * CRM 合同的 {@link IParseFunction} 实现类
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class CrmContractParseFunction implements IParseFunction {

    @Resource
    private CrmContractService contractService;

    @Override
    public boolean executeBefore() {
        return true; // 先转换值后对比
    }

    @Override
    public String functionName() {
        return GET_CONTRACT_BY_ID;
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
