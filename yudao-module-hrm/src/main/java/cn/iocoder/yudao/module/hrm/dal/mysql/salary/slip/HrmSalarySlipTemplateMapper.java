package cn.iocoder.yudao.module.hrm.dal.mysql.salary.slip;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HrmSalarySlipTemplateMapper extends BaseMapperX<HrmSalarySlipTemplateDO> {

    default List<HrmSalarySlipTemplateDO> selectListByDefaultStatusDesc() {
        return selectList(new LambdaQueryWrapperX<HrmSalarySlipTemplateDO>()
                .orderByDesc(HrmSalarySlipTemplateDO::getDefaultStatus)
                .orderByDesc(HrmSalarySlipTemplateDO::getId));
    }

    /**
     * 获得平台级默认工资条模板
     *
     * <p>默认模板以 tenant_id = 0 保存，由所有租户只读共享；租户自己的自定义模板仍由租户拦截器隔离。</p>
     *
     * @return 平台级默认工资条模板
     */
    default HrmSalarySlipTemplateDO selectGlobalDefaultTemplate() {
        return selectOne(new QueryWrapperX<HrmSalarySlipTemplateDO>()
                .eq("tenant_id", 0L)
                .eq("default_status", true)
                .orderByDesc("id")
                .limitN(1));
    }

}
