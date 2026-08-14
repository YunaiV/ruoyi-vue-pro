package cn.iocoder.yudao.module.hrm.framework.operatelog.core;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import com.mzt.logapi.service.IParseFunction;
import org.springframework.stereotype.Component;

/**
 * HRM 银行卡号脱敏的 {@link IParseFunction} 实现类
 *
 * @author 芋道源码
 */
@Component
public class HrmBankCardNumberParseFunction implements IParseFunction {

    public static final String NAME = "getHrmBankCardNumber";

    @Override
    public String functionName() {
        return NAME;
    }

    @Override
    public String apply(Object value) {
        if (StrUtil.isEmptyIfStr(value)) {
            return "";
        }
        return DesensitizedUtil.bankCard(value.toString());
    }

}
