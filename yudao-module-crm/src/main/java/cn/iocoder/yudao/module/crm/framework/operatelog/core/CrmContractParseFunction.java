package cn.iocoder.yudao.module.crm.framework.operatelog.core;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import com.mzt.logapi.service.IParseFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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
        // 可见 https://github.com/YunaiV/ruoyi-vue-pro/pull/1247 描述
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
