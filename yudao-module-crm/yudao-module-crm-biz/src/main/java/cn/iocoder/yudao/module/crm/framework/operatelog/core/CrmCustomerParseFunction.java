package cn.iocoder.yudao.module.crm.framework.operatelog.core;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import com.mzt.logapi.service.IParseFunction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.module.crm.enums.operatelog.CrmParseFunctionNameConstants.GET_CUSTOMER_BY_ID;

/**
 * CRM 客户的 {@link IParseFunction} 实现类
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class CrmCustomerParseFunction implements IParseFunction {

    @Resource
    private CrmCustomerService customerService;

    @Override
    public boolean executeBefore() {
        return true; // 先转换值后对比
    }

    @Override
    public String functionName() {
        return GET_CUSTOMER_BY_ID;
    }

    @Override
    public String apply(Object value) {
        if (StrUtil.isEmptyIfStr(value)) {
            return "";
        }
        CrmCustomerDO crmCustomerDO = customerService.getCustomer(Long.parseLong(value.toString()));
        return crmCustomerDO == null ? "" : crmCustomerDO.getName();
    }

}
