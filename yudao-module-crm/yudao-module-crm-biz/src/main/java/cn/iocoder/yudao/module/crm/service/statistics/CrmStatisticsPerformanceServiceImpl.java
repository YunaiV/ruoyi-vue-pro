package cn.iocoder.yudao.module.crm.service.statistics;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.performance.CrmStatisticsPerformanceReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.performance.CrmStatisticsPerformanceRespVO;
import cn.iocoder.yudao.module.crm.dal.mysql.statistics.CrmStatisticsPerformanceMapper;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

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
     * 获得员工业绩数据，并通过如下方法拿到对应的lastYearCount，lastMonthCount
     * 比如说，构造2024 年的CrmStatisticsPerformanceRespVO，获得 2023-01 到 2024-12的月统计数据即可
     * 可以数据 group by 年-月，2023-01 到 2024-12的，然后聚合出 CrmStatisticsPerformanceRespVO
     * @param performanceReqVO  参数
     * @param performanceFunction 员工业绩统计方法
     * @return 员工业绩数据
     */
    private List<CrmStatisticsPerformanceRespVO> getPerformance(CrmStatisticsPerformanceReqVO performanceReqVO,
               Function<CrmStatisticsPerformanceReqVO, List<CrmStatisticsPerformanceRespVO>> performanceFunction) {

        // 1. 获得用户编号数组
        final List<Long> userIds = getUserIds(performanceReqVO);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        performanceReqVO.setUserIds(userIds);

        // 2. 获得业绩数据
        List<CrmStatisticsPerformanceRespVO> performanceList = performanceFunction.apply(performanceReqVO);

        // 获取查询的年份
        String currentYear = LocalDateTimeUtil.format(performanceReqVO.getTimes()[0],"yyyy");
        Map<Integer, CrmStatisticsPerformanceRespVO> currentYearMap = new TreeMap<>();//查询当年的map数据
        Map<Integer, CrmStatisticsPerformanceRespVO> lastYearMap = new TreeMap<>();//前一年的map数据

        for (int month = 1; month <= 12; month++) {
            //根据数据库的月销售数据查询结果，构造查询当年的map数据
            String currentYearKey = String.format("%d%02d", Integer.parseInt(currentYear), month);
            buildYearMapData(performanceList, currentYearMap, currentYearKey);

            //根据数据库的月销售数据查询结果，构造查询前一年的map数据
            String lastYearKey = String.format("%d%02d", Integer.parseInt(currentYear)-1, month);
            buildYearMapData(performanceList, lastYearMap, lastYearKey);
        }
        //根据构造好的map数据，计算查询当年的环比和同比数据，并构造好返回的respVOList
        List<CrmStatisticsPerformanceRespVO> respVOList = new ArrayList<>();
        for (int key : currentYearMap.keySet()) {
            BigDecimal lastYearCount = lastYearMap.get(key-100).getCurrentMonthCount();
            BigDecimal lastMonthCount;
            if (key % 100 > 1) {//2-12月份的前一个月数据
                lastMonthCount = currentYearMap.get(key-1).getCurrentMonthCount();
            } else {//1月份的前一个月数据
                lastMonthCount = lastYearMap.get(key-89).getCurrentMonthCount();
            }

            currentYearMap.get(key).setLastYearCount(lastYearCount);
            currentYearMap.get(key).setLastMonthCount(lastMonthCount);

            respVOList.add(currentYearMap.get(key));
        }

        return respVOList;
    }

    /**
     * 根据mapKey，添加当年和前一年的月销售记录到对应的map结构中
     * @param performanceList   数据库中查询到的月销售记录
     * @param YearDataMap   将查询到的月销售记录put到对应的map中，如果月销售记录为null，置为0
     * @param mapKey        对应的mapKey
     */
    private void buildYearMapData(List<CrmStatisticsPerformanceRespVO> performanceList,
                                  Map<Integer, CrmStatisticsPerformanceRespVO> YearDataMap,
                                  String mapKey)
    {
        CrmStatisticsPerformanceRespVO currentYearData = performanceList.stream()
                .filter(data -> data.getTime().equals(mapKey))
                .findFirst()
                .orElse(null);

        if(currentYearData != null) {
            YearDataMap.put(Integer.parseInt(mapKey), currentYearData);
        } else {
            CrmStatisticsPerformanceRespVO defaultVO = new CrmStatisticsPerformanceRespVO();
            defaultVO.setTime(mapKey);
            defaultVO.setCurrentMonthCount(BigDecimal.ZERO);
            defaultVO.setLastMonthCount(BigDecimal.ZERO);
            defaultVO.setLastYearCount(BigDecimal.ZERO);
            YearDataMap.put(Integer.parseInt(mapKey), defaultVO);
        }
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
            return List.of(reqVO.getUserId());
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