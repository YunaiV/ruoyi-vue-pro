package cn.iocoder.yudao.module.hrm.dal.mysql.salary.employee;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.employee.HrmSalaryEmployeeInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Mapper
public interface HrmSalaryEmployeeInfoMapper extends BaseMapperX<HrmSalaryEmployeeInfoDO> {

    default HrmSalaryEmployeeInfoDO selectByEmployeeId(Long employeeId) {
        return selectLastOne(new LambdaQueryWrapperX<HrmSalaryEmployeeInfoDO>()
                .eq(HrmSalaryEmployeeInfoDO::getEmployeeId, employeeId)
                .orderByAsc(HrmSalaryEmployeeInfoDO::getId));
    }

    default PageResult<HrmSalaryEmployeeInfoDO> selectPage(HrmSalaryEmployeeInfoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HrmSalaryEmployeeInfoDO>()
                .eqIfPresent(HrmSalaryEmployeeInfoDO::getEmployeeId, reqVO.getEmployeeId())
                .inIfPresent(HrmSalaryEmployeeInfoDO::getEmployeeId, reqVO.getEmployeeIds())
                .eqIfPresent(HrmSalaryEmployeeInfoDO::getChangeType, reqVO.getChangeType())
                .orderByDesc(HrmSalaryEmployeeInfoDO::getId));
    }

    default List<HrmSalaryEmployeeInfoDO> selectListByEmployeeIds(List<Long> employeeIds) {
        return selectList(new LambdaQueryWrapperX<HrmSalaryEmployeeInfoDO>()
                .in(HrmSalaryEmployeeInfoDO::getEmployeeId, employeeIds)
                .orderByDesc(HrmSalaryEmployeeInfoDO::getId));
    }

    default List<Long> selectEmployeeIdListByChangeType(Integer changeType) {
        LambdaQueryWrapperX<HrmSalaryEmployeeInfoDO> query = new LambdaQueryWrapperX<>();
        query.select(HrmSalaryEmployeeInfoDO::getEmployeeId);
        query.eqIfPresent(HrmSalaryEmployeeInfoDO::getChangeType, changeType);
        return convertList(selectList(query),
                HrmSalaryEmployeeInfoDO::getEmployeeId);
    }

}
