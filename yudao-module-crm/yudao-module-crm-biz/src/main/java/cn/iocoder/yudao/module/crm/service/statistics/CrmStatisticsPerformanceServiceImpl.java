package cn.iocoder.yudao.module.crm.service.statistics;

import cn.hutool.core.collection.CollUtil;
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
import java.util.Collections;
import java.util.List;
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
        // TODO @scholar：我们可以换个思路实现，减少数据库的计算量；
        // 比如说，2024 年的合同数据，是不是 2022-12 到 2024-12-31，每个月的统计呢？
        // 理解之后，我们可以数据 group by 年-月，20222-12 到 2024-12-31 的，然后内存在聚合出 CrmStatisticsPerformanceRespVO 这样
        // 这样，我们就可以减少数据库的计算量，提升性能；同时 SQL 也会很简单，开发者理解起来也简单哈；
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
     * @param performanceReqVO  参数
     * @param performanceFunction 排行榜方法
     * @return 排行版数据
     */
    private List<CrmStatisticsPerformanceRespVO> getPerformance(CrmStatisticsPerformanceReqVO performanceReqVO, Function<CrmStatisticsPerformanceReqVO,
            List<CrmStatisticsPerformanceRespVO>> performanceFunction) {

        // 1. 获得用户编号数组
        final List<Long> userIds = getUserIds(performanceReqVO);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        performanceReqVO.setUserIds(userIds);
        // 2. 获得排行数据
        List<CrmStatisticsPerformanceRespVO> performance = performanceFunction.apply(performanceReqVO);
        if (CollUtil.isEmpty(performance)) {
            return Collections.emptyList();
        }
        return performance;
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