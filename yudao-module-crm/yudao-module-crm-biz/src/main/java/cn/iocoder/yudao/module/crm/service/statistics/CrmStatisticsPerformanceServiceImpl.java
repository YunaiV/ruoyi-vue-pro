package cn.iocoder.yudao.module.crm.service.statistics;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.performance.CrmStatisticsPerformanceReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.performance.CrmStatisticsPerformanceRespVO;
import cn.iocoder.yudao.module.crm.dal.mysql.statistics.CrmStatisticsPerformanceMapper;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * CRM 员工业绩分析 Service 实现类
 *
 * @author scholar
 */
@Service
@Validated
public class CrmStatisticsPerformanceServiceImpl implements CrmStatisticsPerformanceService {

    @Resource
    private CrmStatisticsPerformanceMapper performanceMapper;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @Override
    public List<CrmStatisticsPerformanceRespVO> getContractCountPerformance(CrmStatisticsPerformanceReqVO performanceReqVO) {
        return getPerformance(performanceReqVO, performanceMapper::selectContractCountPerformance);
    }

    @Override
    public List<CrmStatisticsPerformanceRespVO> getContractPricePerformance(CrmStatisticsPerformanceReqVO performanceReqVO) {
        return getPerformance(performanceReqVO, performanceMapper::selectContractPricePerformance);
    }

    @Override
    public List<CrmStatisticsPerformanceRespVO> getReceivablePricePerformance(CrmStatisticsPerformanceReqVO performanceReqVO) {
        return getPerformance(performanceReqVO, performanceMapper::selectReceivablePricePerformance);
    }

    /**
     * 获得员工业绩数据
     *
     * 1. 获得今年 + 去年的数据
     * 2. 遍历今年的月份，逐个拼接去年的月份数据
     *
     * @param performanceReqVO  参数
     * @param performanceFunction 员工业绩统计方法
     * @return 员工业绩数据
     */
    private List<CrmStatisticsPerformanceRespVO> getPerformance(CrmStatisticsPerformanceReqVO performanceReqVO,
                                                                Function<CrmStatisticsPerformanceReqVO, List<CrmStatisticsPerformanceRespVO>> performanceFunction) {

        // 1. 获得用户编号数组
        List<Long> userIds = getUserIds(performanceReqVO);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        performanceReqVO.setUserIds(userIds);

        // 2. 获得业绩数据
        int year = performanceReqVO.getTimes()[0].getYear(); // 获取查询的年份
        performanceReqVO.getTimes()[0] = performanceReqVO.getTimes()[0].minusYears(1);
        List<CrmStatisticsPerformanceRespVO> performanceList = performanceFunction.apply(performanceReqVO);
        Map<String, BigDecimal> performanceMap = convertMap(performanceList, CrmStatisticsPerformanceRespVO::getTime,
                CrmStatisticsPerformanceRespVO::getCurrentMonthCount);

        // 3. 组装数据返回
        List<CrmStatisticsPerformanceRespVO> result = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            String currentMonth = String.format("%d%02d", year, month);
            String lastMonth = month == 1 ? String.format("%d%02d", year - 1, 12) : String.format("%d%02d", year, month - 1);
            String lastYear = String.format("%d%02d", year - 1, month);
            result.add(new CrmStatisticsPerformanceRespVO().setTime(currentMonth)
                    .setCurrentMonthCount(performanceMap.getOrDefault(currentMonth, BigDecimal.ZERO))
                    .setLastMonthCount(performanceMap.getOrDefault(lastMonth, BigDecimal.ZERO))
                    .setLastYearCount(performanceMap.getOrDefault(lastYear, BigDecimal.ZERO)));
        }
        return result;
    }

    /**
     * 获取用户编号数组。如果用户编号为空, 则获得部门下的用户编号数组，包括子部门的所有用户编号
     *
     * @param reqVO 请求参数
     * @return 用户编号数组
     */
    private List<Long> getUserIds(CrmStatisticsPerformanceReqVO reqVO) {
        // 情况一：选中某个用户
        if (ObjUtil.isNotNull(reqVO.getUserId())) {
            return ListUtil.of(reqVO.getUserId());
        }
        // 情况二：选中某个部门
        // 2.1 获得部门列表
        final Long deptId = reqVO.getDeptId();
        List<Long> deptIds = convertList(deptApi.getChildDeptList(deptId), DeptRespDTO::getId);
        deptIds.add(deptId);
        // 2.2 获得用户编号
        return convertList(adminUserApi.getUserListByDeptIds(deptIds), AdminUserRespDTO::getId);
    }

}