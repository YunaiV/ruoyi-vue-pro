package cn.iocoder.yudao.module.crm.framework.operatelog.function;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.dict.core.util.DictFrameworkUtils;
import com.mzt.logapi.service.IParseFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.module.crm.enums.DictTypeConstants.CRM_CUSTOMER_LEVEL;

/**
 * 自定义函数-通过客户等级编号获取客户等级信息
 *
 * @author HUIHUI
 */
@Slf4j
@Component
public class CrmLevelParseFunction implements IParseFunction {

    @Override
    public boolean executeBefore() {
        return true; // 先转换值后对比
    }

    @Override
    public String functionName() {
        return "getLevel";
    }

    @Override
    public String apply(Object value) {
        if (value == null) {
            return "";
        }
        if (StrUtil.isEmpty(value.toString())) {
            return "";
        }

        // 获取客户等级信息
        try {
            return DictFrameworkUtils.getDictDataLabel(CRM_CUSTOMER_LEVEL, value.toString());
        } catch (Exception ignored) {
        }
        return "";
    }
}
