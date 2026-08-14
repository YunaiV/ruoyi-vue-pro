package cn.iocoder.yudao.module.hrm.dal.mysql.employee.info;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeFileDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HrmEmployeeFileMapper extends BaseMapperX<HrmEmployeeFileDO> {

    default List<HrmEmployeeFileDO> selectListByEmployeeId(Long employeeId) {
        return selectList(new LambdaQueryWrapperX<HrmEmployeeFileDO>()
                .eq(HrmEmployeeFileDO::getEmployeeId, employeeId)
                .orderByAsc(HrmEmployeeFileDO::getType)
                .orderByAsc(HrmEmployeeFileDO::getId));
    }

    default List<HrmEmployeeFileDO> selectListByEmployeeIdAndType(Long employeeId, Integer type) {
        return selectList(new LambdaQueryWrapperX<HrmEmployeeFileDO>()
                .eq(HrmEmployeeFileDO::getEmployeeId, employeeId)
                .eq(HrmEmployeeFileDO::getType, type)
                .orderByAsc(HrmEmployeeFileDO::getId));
    }

}
