package cn.iocoder.yudao.module.hrm.framework.operatelog.core;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import com.mzt.logapi.service.IParseFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * HRM 通用状态的 {@link IParseFunction} 实现类
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class HrmCommonStatusParseFunction implements IParseFunction {

    public static final String NAME = "getHrmCommonStatusName";

    @Override
    public String functionName() {
        return NAME;
    }

    @Override
    public String apply(Object value) {
        if (StrUtil.isEmptyIfStr(value)) {
            return "";
        }
        Integer status = Integer.valueOf(value.toString());
        if (CommonStatusEnum.isEnable(status)) {
            return CommonStatusEnum.ENABLE.getName();
        }
        if (CommonStatusEnum.isDisable(status)) {
            return CommonStatusEnum.DISABLE.getName();
        }
        return "";
    }

}
