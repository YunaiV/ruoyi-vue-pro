package cn.iocoder.yudao.module.crm.framework.operatelog.core;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.crm.enums.CrmDictTypeConstants;
import com.mzt.logapi.service.IParseFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 产品状态的 {@link IParseFunction} 实现类
 *
 * @author anhaohao
 */
@Component
@Slf4j
public class CrmProductStatusParseFunction implements IParseFunction {

    public static final String NAME = "getProductStatusName";

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
        return DictFrameworkUtils.getDictDataLabel(CrmDictTypeConstants.CRM_PRODUCT_STATUS, value.toString());
    }

}
