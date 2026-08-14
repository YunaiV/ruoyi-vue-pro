package cn.iocoder.yudao.module.hrm.dal.mysql.salary.monthrecord;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.HrmSalaryMonthRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HrmSalaryMonthRecordMapper extends BaseMapperX<HrmSalaryMonthRecordDO> {

    default HrmSalaryMonthRecordDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(HrmSalaryMonthRecordDO::getId, id);
    }

    default HrmSalaryMonthRecordDO selectByYearMonth(Integer year, Integer month) {
        return selectLastOne(new LambdaQueryWrapperX<HrmSalaryMonthRecordDO>()
                .eq(HrmSalaryMonthRecordDO::getYear, year)
                .eq(HrmSalaryMonthRecordDO::getMonth, month)
                .orderByAsc(HrmSalaryMonthRecordDO::getId));
    }

    default HrmSalaryMonthRecordDO selectLast() {
        return selectOne(new LambdaQueryWrapperX<HrmSalaryMonthRecordDO>()
                .orderByDesc(HrmSalaryMonthRecordDO::getYear)
                .orderByDesc(HrmSalaryMonthRecordDO::getMonth)
                .last("LIMIT 1"));
    }

    default PageResult<HrmSalaryMonthRecordDO> selectPage(HrmSalaryMonthRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HrmSalaryMonthRecordDO>()
                .eqIfPresent(HrmSalaryMonthRecordDO::getYear, reqVO.getYear())
                .eqIfPresent(HrmSalaryMonthRecordDO::getMonth, reqVO.getMonth())
                .eqIfPresent(HrmSalaryMonthRecordDO::getStatus, reqVO.getStatus())
                .orderByDesc(HrmSalaryMonthRecordDO::getYear)
                .orderByDesc(HrmSalaryMonthRecordDO::getMonth));
    }

    default List<HrmSalaryMonthRecordDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<HrmSalaryMonthRecordDO>()
                .eqIfPresent(HrmSalaryMonthRecordDO::getStatus, status)
                .orderByDesc(HrmSalaryMonthRecordDO::getYear)
                .orderByDesc(HrmSalaryMonthRecordDO::getMonth));
    }

}
