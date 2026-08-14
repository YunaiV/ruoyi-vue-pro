package cn.iocoder.yudao.module.hrm.dal.mysql.employee.experience;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience.HrmEmployeeWorkExperienceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HrmEmployeeWorkExperienceMapper extends BaseMapperX<HrmEmployeeWorkExperienceDO> {

    default List<HrmEmployeeWorkExperienceDO> selectListByEmployeeId(Long employeeId) {
        return selectList(new LambdaQueryWrapperX<HrmEmployeeWorkExperienceDO>()
                .eq(HrmEmployeeWorkExperienceDO::getEmployeeId, employeeId)
                .orderByAsc(HrmEmployeeWorkExperienceDO::getSort)
                .orderByDesc(HrmEmployeeWorkExperienceDO::getId));
    }

}
