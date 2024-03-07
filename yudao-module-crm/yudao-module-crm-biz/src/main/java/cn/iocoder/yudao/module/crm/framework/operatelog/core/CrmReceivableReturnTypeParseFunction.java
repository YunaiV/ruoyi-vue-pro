package cn.iocoder.yudao.module.crm.framework.operatelog.core;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import com.mzt.logapi.service.IParseFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.module.crm.enums.DictTypeConstants.CRM_RECEIVABLE_RETURN_TYPE;

/**
 * CRM 回款方式的 {@link IParseFunction} 实现类
 *
 * @author HUIHUI
 */
@Slf4j
@Component
public class CrmReceivableReturnTypeParseFunction implements IParseFunction {

    public static final String NAME = "getReceivableReturnType";

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
        return DictFrameworkUtils.getDictDataLabel(CRM_RECEIVABLE_RETURN_TYPE, value.toString());
    }

}
