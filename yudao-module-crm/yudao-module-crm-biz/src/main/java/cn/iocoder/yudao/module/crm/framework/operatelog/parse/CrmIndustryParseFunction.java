package cn.iocoder.yudao.module.crm.framework.operatelog.parse;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.dict.core.util.DictFrameworkUtils;
import com.mzt.logapi.service.IParseFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.module.crm.enums.DictTypeConstants.CRM_CUSTOMER_INDUSTRY;

/**
 * 自定义函数-通过行业编号获取行业信息
 *
 * @author HUIHUI
 */
@Slf4j
@Component
public class CrmIndustryParseFunction implements IParseFunction {

    @Override
    public boolean executeBefore() {
        return true; // 先转换值后对比
    }

    @Override
    public String functionName() {
        return "getIndustryById";
    }

    @Override
    public String apply(Object value) {
        if (value == null) {
            return "";
        }
        if (StrUtil.isEmpty(value.toString())) {
            return "";
        }

        // 获取行业信息
        try {
            return DictFrameworkUtils.getDictDataLabel(CRM_CUSTOMER_INDUSTRY, value.toString());
        } catch (Exception ignored) {
        }
        return "";
    }
}
