package cn.iocoder.yudao.module.hrm.dal.mysql.salary.monthrecord;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryEmployeeMonthRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface HrmSalaryMonthEmployeeRecordMapper extends BaseMapperX<HrmSalaryMonthEmployeeRecordDO> {

    default PageResult<HrmSalaryMonthEmployeeRecordDO> selectPage(HrmSalaryMonthEmployeeRecordPageReqVO reqVO) {
        MPJLambdaWrapperX<HrmSalaryMonthEmployeeRecordDO> query =
                new MPJLambdaWrapperX<HrmSalaryMonthEmployeeRecordDO>()
                .selectAll(HrmSalaryMonthEmployeeRecordDO.class)
                .eq(HrmSalaryMonthEmployeeRecordDO::getMonthRecordId, reqVO.getMonthRecordId())
                .eqIfPresent(HrmSalaryMonthEmployeeRecordDO::getEmployeeId, reqVO.getEmployeeId())
                .inIfPresent(HrmSalaryMonthEmployeeRecordDO::getEmployeeId, reqVO.getEmployeeIds());
        query.orderByAsc(HrmSalaryMonthEmployeeRecordDO::getEmployeeId);
        appendSalarySlipSentCondition(query, reqVO.getSalarySlipSent());
        return selectJoinPage(reqVO, HrmSalaryMonthEmployeeRecordDO.class, query);
    }

    default PageResult<HrmSalaryMonthEmployeeRecordDO> selectPageByEmployeeId(
            HrmSalaryEmployeeMonthRecordPageReqVO reqVO, Collection<Long> monthRecordIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HrmSalaryMonthEmployeeRecordDO>()
                .eq(HrmSalaryMonthEmployeeRecordDO::getEmployeeId, reqVO.getEmployeeId())
                .in(HrmSalaryMonthEmployeeRecordDO::getMonthRecordId, monthRecordIds)
                .orderByDesc(HrmSalaryMonthEmployeeRecordDO::getYear)
                .orderByDesc(HrmSalaryMonthEmployeeRecordDO::getMonth)
                .orderByDesc(HrmSalaryMonthEmployeeRecordDO::getId));
    }

    default List<HrmSalaryMonthEmployeeRecordDO> selectListByMonthRecordId(Long monthRecordId) {
        return selectList(new LambdaQueryWrapperX<HrmSalaryMonthEmployeeRecordDO>()
                .eq(HrmSalaryMonthEmployeeRecordDO::getMonthRecordId, monthRecordId)
                .orderByAsc(HrmSalaryMonthEmployeeRecordDO::getId));
    }

    default List<HrmSalaryMonthEmployeeRecordDO> selectList(Long monthRecordId, Collection<Long> employeeIds) {
        return selectList(new LambdaQueryWrapperX<HrmSalaryMonthEmployeeRecordDO>()
                .eq(HrmSalaryMonthEmployeeRecordDO::getMonthRecordId, monthRecordId)
                .inIfPresent(HrmSalaryMonthEmployeeRecordDO::getEmployeeId, employeeIds)
                .orderByAsc(HrmSalaryMonthEmployeeRecordDO::getEmployeeId));
    }

    default List<HrmSalaryMonthEmployeeRecordDO> selectList(HrmSalaryMonthEmployeeRecordListReqVO reqVO) {
        MPJLambdaWrapperX<HrmSalaryMonthEmployeeRecordDO> query =
                new MPJLambdaWrapperX<HrmSalaryMonthEmployeeRecordDO>()
                .selectAll(HrmSalaryMonthEmployeeRecordDO.class)
                .eq(HrmSalaryMonthEmployeeRecordDO::getMonthRecordId, reqVO.getMonthRecordId())
                .eqIfPresent(HrmSalaryMonthEmployeeRecordDO::getEmployeeId, reqVO.getEmployeeId())
                .inIfPresent(HrmSalaryMonthEmployeeRecordDO::getEmployeeId, reqVO.getEmployeeIds());
        query.orderByAsc(HrmSalaryMonthEmployeeRecordDO::getEmployeeId);
        appendSalarySlipSentCondition(query, reqVO.getSalarySlipSent());
        return selectJoinList(HrmSalaryMonthEmployeeRecordDO.class, query);
    }

    default HrmSalaryMonthEmployeeRecordDO selectByEmployeeIdAndYearMonth(Long employeeId, Integer year, Integer month) {
        return selectLastOne(new LambdaQueryWrapperX<HrmSalaryMonthEmployeeRecordDO>()
                .eq(HrmSalaryMonthEmployeeRecordDO::getEmployeeId, employeeId)
                .eq(HrmSalaryMonthEmployeeRecordDO::getYear, year)
                .eq(HrmSalaryMonthEmployeeRecordDO::getMonth, month)
                .orderByAsc(HrmSalaryMonthEmployeeRecordDO::getId));
    }

    default void deleteByMonthRecordId(Long monthRecordId) {
        delete(new LambdaQueryWrapperX<HrmSalaryMonthEmployeeRecordDO>()
                .eq(HrmSalaryMonthEmployeeRecordDO::getMonthRecordId, monthRecordId));
    }

    /**
     * 拼接工资条发送状态查询条件
     *
     * @param query 查询条件
     * @param salarySlipSent 是否已发送工资条
     */
    default void appendSalarySlipSentCondition(
            MPJLambdaWrapperX<HrmSalaryMonthEmployeeRecordDO> query, Boolean salarySlipSent) {
        if (Boolean.TRUE.equals(salarySlipSent)) {
            query.innerJoin(HrmSalarySlipDO.class, HrmSalarySlipDO::getMonthEmployeeRecordId,
                            HrmSalaryMonthEmployeeRecordDO::getId)
                    .distinct();
        } else if (Boolean.FALSE.equals(salarySlipSent)) {
            query.leftJoin(HrmSalarySlipDO.class, HrmSalarySlipDO::getMonthEmployeeRecordId,
                            HrmSalaryMonthEmployeeRecordDO::getId)
                    .isNull(HrmSalarySlipDO::getId);
        }
    }

}
