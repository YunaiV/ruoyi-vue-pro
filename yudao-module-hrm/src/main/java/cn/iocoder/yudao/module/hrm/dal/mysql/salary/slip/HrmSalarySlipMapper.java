package cn.iocoder.yudao.module.hrm.dal.mysql.salary.slip;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.HrmSalarySlipPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mapper
public interface HrmSalarySlipMapper extends BaseMapperX<HrmSalarySlipDO> {

    default PageResult<HrmSalarySlipDO> selectPage(HrmSalarySlipPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HrmSalarySlipDO>()
                .eqIfPresent(HrmSalarySlipDO::getSendRecordId, reqVO.getSendRecordId())
                .eqIfPresent(HrmSalarySlipDO::getEmployeeId, reqVO.getEmployeeId())
                .inIfPresent(HrmSalarySlipDO::getEmployeeId, reqVO.getEmployeeIds())
                .eqIfPresent(HrmSalarySlipDO::getReadStatus, reqVO.getReadStatus())
                .likeIfPresent(HrmSalarySlipDO::getRemark, reqVO.getRemark())
                .orderByDesc(HrmSalarySlipDO::getId));
    }

    default Map<Long, Long> selectCountMapBySendRecordIdsAndReadStatus(
            Collection<Long> sendRecordIds, Integer readStatus) {
        QueryWrapperX<HrmSalarySlipDO> query = new QueryWrapperX<>();
        query.select("send_record_id AS sendRecordId", "COUNT(id) AS count")
                .in("send_record_id", sendRecordIds)
                .eq("read_status", readStatus)
                .groupBy("send_record_id");
        return CollectionUtils.convertMap(selectMaps(query),
                record -> MapUtil.getLong(record, "sendRecordId"),
                record -> MapUtil.getLong(record, "count"));
    }

    default List<HrmSalarySlipDO> selectListByEmployeeId(Long employeeId) {
        return selectList(new LambdaQueryWrapperX<HrmSalarySlipDO>()
                .eq(HrmSalarySlipDO::getEmployeeId, employeeId));
    }

    default List<HrmSalarySlipDO> selectListByEmployeeId(
            Long employeeId, YearMonth startMonth, YearMonth endMonth,
            Integer orderType, Integer order) {
        QueryWrapperX<HrmSalarySlipDO> query = new QueryWrapperX<>();
        query.eq("employee_id", employeeId)
                .apply(startMonth != null, "(year * 100 + month) >= {0}",
                        startMonth == null ? null : startMonth.getYear() * 100 + startMonth.getMonthValue())
                .apply(endMonth != null, "(year * 100 + month) <= {0}",
                        endMonth == null ? null : endMonth.getYear() * 100 + endMonth.getMonthValue());
        boolean asc = Objects.equals(order, 2);
        if (Objects.equals(orderType, 2)) {
            query.orderBy(true, asc, "real_pay_salary", "id");
        } else {
            // 默认排序表达工资条发放时序；补发旧月份时仍按后发记录优先。
            query.orderBy(true, asc, "id");
        }
        return selectList(query);
    }

    default List<HrmSalarySlipDO> selectListByMonthEmployeeRecordIds(Collection<Long> monthEmployeeRecordIds) {
        return selectList(new LambdaQueryWrapperX<HrmSalarySlipDO>()
                .in(HrmSalarySlipDO::getMonthEmployeeRecordId, monthEmployeeRecordIds));
    }

    default List<HrmSalarySlipDO> selectListByEmployeeIdsAndYearMonth(
            Collection<Long> employeeIds, Integer year, Integer month) {
        return selectList(new LambdaQueryWrapperX<HrmSalarySlipDO>()
                .in(HrmSalarySlipDO::getEmployeeId, employeeIds)
                .eq(HrmSalarySlipDO::getYear, year)
                .eq(HrmSalarySlipDO::getMonth, month));
    }

    default void deleteBySendRecordId(Long sendRecordId) {
        delete(new LambdaQueryWrapperX<HrmSalarySlipDO>()
                .eq(HrmSalarySlipDO::getSendRecordId, sendRecordId));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateByIds(Collection<Long> ids, HrmSalarySlipDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<HrmSalarySlipDO>()
                .in(HrmSalarySlipDO::getId, ids));
    }

}
