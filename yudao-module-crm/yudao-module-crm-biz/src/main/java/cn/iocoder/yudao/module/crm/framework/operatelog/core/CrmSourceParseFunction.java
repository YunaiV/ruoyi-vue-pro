package cn.iocoder.yudao.module.crm.framework.operatelog.core;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.dict.core.util.DictFrameworkUtils;
import com.mzt.logapi.service.IParseFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.module.crm.enums.DictTypeConstants.CRM_CUSTOMER_SOURCE;

/**
 * 自定义函数-通过客户来源编号获取客户来源信息
 *
 * @author HUIHUI
 */
@Slf4j
@Component
public class CrmSourceParseFunction implements IParseFunction {

    @Override
    public boolean executeBefore() {
        return true; // 先转换值后对比
    }

    @Override
    public String functionName() {
        return "getSource";
    }

    @Override
    public String apply(Object value) {
        if (ObjUtil.isEmpty(value)) {
            return "";
        }
        if (StrUtil.isEmpty(value.toString())) {
            return "";
        }

        // 获取客户来源信息
        return DictFrameworkUtils.getDictDataLabel(CRM_CUSTOMER_SOURCE, value.toString());
    }

}
