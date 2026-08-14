package cn.iocoder.yudao.module.hrm.dal.mysql.salary.config;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HrmSalaryOptionMapper extends BaseMapperX<HrmSalaryOptionDO> {

    default HrmSalaryOptionDO selectByCode(Integer code) {
        return selectFirstOne(HrmSalaryOptionDO::getCode, code);
    }

    @Select("SELECT * FROM hrm_salary_option WHERE code = #{code} LIMIT 1")
    HrmSalaryOptionDO selectByCodeIncludeDeleted(@Param("code") Integer code);

    default HrmSalaryOptionDO selectByCodeForUpdate(Integer code) {
        return selectFirstOneForUpdate(new LambdaQueryWrapper<HrmSalaryOptionDO>()
                .eq(HrmSalaryOptionDO::getCode, code)
                .orderByDesc(HrmSalaryOptionDO::getId));
    }

    default List<HrmSalaryOptionDO> selectListByEnabledAndVisible(Boolean enabled, Boolean visible) {
        return selectList(new LambdaQueryWrapperX<HrmSalaryOptionDO>()
                .eqIfPresent(HrmSalaryOptionDO::getEnabled, enabled)
                .eqIfPresent(HrmSalaryOptionDO::getVisible, visible)
                .orderByAsc(HrmSalaryOptionDO::getCode));
    }

    @Select("SELECT MAX(code) FROM hrm_salary_option "
            + "WHERE parent_code = #{parentCode} AND code >= #{minCode}")
    Integer selectMaxCodeByParentCodeAndCodeGreaterThanOrEqual(
            @Param("parentCode") Integer parentCode, @Param("minCode") Integer minCode);

}
