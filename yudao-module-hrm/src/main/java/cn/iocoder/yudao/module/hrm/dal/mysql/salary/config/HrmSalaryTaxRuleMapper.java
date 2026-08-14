package cn.iocoder.yudao.module.hrm.dal.mysql.salary.config;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryTaxRuleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HrmSalaryTaxRuleMapper extends BaseMapperX<HrmSalaryTaxRuleDO> {

    default HrmSalaryTaxRuleDO selectByName(String name) {
        return selectFirstOne(HrmSalaryTaxRuleDO::getName, name);
    }

    default List<HrmSalaryTaxRuleDO> selectList() {
        return selectList(new LambdaQueryWrapperX<HrmSalaryTaxRuleDO>().orderByAsc(HrmSalaryTaxRuleDO::getId));
    }

}
