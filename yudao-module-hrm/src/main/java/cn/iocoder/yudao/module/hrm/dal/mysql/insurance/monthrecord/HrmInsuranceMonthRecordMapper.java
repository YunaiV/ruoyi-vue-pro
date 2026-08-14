package cn.iocoder.yudao.module.hrm.dal.mysql.insurance.monthrecord;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord.HrmInsuranceMonthRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HrmInsuranceMonthRecordMapper extends BaseMapperX<HrmInsuranceMonthRecordDO> {

    default HrmInsuranceMonthRecordDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(HrmInsuranceMonthRecordDO::getId, id);
    }

    default HrmInsuranceMonthRecordDO selectByYearMonth(Integer year, Integer month) {
        return selectLastOne(new LambdaQueryWrapperX<HrmInsuranceMonthRecordDO>()
                .eq(HrmInsuranceMonthRecordDO::getYear, year)
                .eq(HrmInsuranceMonthRecordDO::getMonth, month)
                .orderByAsc(HrmInsuranceMonthRecordDO::getId));
    }

    default HrmInsuranceMonthRecordDO selectLast() {
        return selectLastOne(new LambdaQueryWrapperX<HrmInsuranceMonthRecordDO>()
                .orderByAsc(HrmInsuranceMonthRecordDO::getYear)
                .orderByAsc(HrmInsuranceMonthRecordDO::getMonth)
                .orderByAsc(HrmInsuranceMonthRecordDO::getId));
    }

    default HrmInsuranceMonthRecordDO selectLastForUpdate() {
        return selectFirstOneForUpdate(new LambdaQueryWrapperX<HrmInsuranceMonthRecordDO>()
                .orderByDesc(HrmInsuranceMonthRecordDO::getYear)
                .orderByDesc(HrmInsuranceMonthRecordDO::getMonth)
                .orderByDesc(HrmInsuranceMonthRecordDO::getId));
    }

    default List<HrmInsuranceMonthRecordDO> selectListByYear(Integer year) {
        return selectList(new LambdaQueryWrapperX<HrmInsuranceMonthRecordDO>()
                .eqIfPresent(HrmInsuranceMonthRecordDO::getYear, year)
                .orderByDesc(HrmInsuranceMonthRecordDO::getYear)
                .orderByDesc(HrmInsuranceMonthRecordDO::getMonth));
    }

}
