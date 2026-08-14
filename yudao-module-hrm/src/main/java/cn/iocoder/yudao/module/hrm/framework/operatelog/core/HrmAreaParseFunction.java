package cn.iocoder.yudao.module.hrm.framework.operatelog.core;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import com.mzt.logapi.service.IParseFunction;
import org.springframework.stereotype.Component;

/**
 * 地名的 {@link IParseFunction} 实现类
 *
 * @author 芋道源码
 */
@Component("hrmAreaParseFunction")
public class HrmAreaParseFunction implements IParseFunction {

    public static final String NAME = "getArea";

    @Override
    public boolean executeBefore() {
        return true;
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
        return AreaUtils.format(Integer.parseInt(value.toString()));
    }

}
