package cn.iocoder.yudao.module.hrm.framework.operatelog.core;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.hrm.enums.recruit.post.HrmRecruitPostStatusEnum;
import com.mzt.logapi.service.IParseFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * HRM 招聘职位状态的 {@link IParseFunction} 实现类
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class HrmRecruitPostStatusParseFunction implements IParseFunction {

    public static final String NAME = "getRecruitPostStatusName";

    @Override
    public String functionName() {
        return NAME;
    }

    @Override
    public String apply(Object value) {
        if (StrUtil.isEmptyIfStr(value)) {
            return "";
        }
        HrmRecruitPostStatusEnum status = HrmRecruitPostStatusEnum.valueOf(Integer.valueOf(value.toString()));
        return status == null ? "" : status.getName();
    }

}
