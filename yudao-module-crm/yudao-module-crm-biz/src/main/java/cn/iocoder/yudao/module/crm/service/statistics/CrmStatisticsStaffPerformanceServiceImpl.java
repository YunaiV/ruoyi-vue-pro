package cn.iocoder.yudao.module.crm.service.statistics;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.CrmStatisticsStaffPerformanceReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.CrmStatisticsStaffPerformanceRespVO;
import cn.iocoder.yudao.module.crm.dal.mysql.statistics.CrmStatisticsStaffPerformanceMapper;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * CRM 员工业绩分析 Service 实现类
 *
 * @author scholar
 */
@Service
@Validated
public class CrmStatisticsStaffPerformanceServiceImpl implements CrmStatisticsStaffPerformanceService {

    @Resource
    private CrmStatisticsStaffPerformanceMapper staffPerformanceMapper;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;



    @Override
    public List<CrmStatisticsStaffPerformanceRespVO> getContractCountStaffPerformance(CrmStatisticsStaffPerformanceReqVO staffPerformanceReqVO) {
        return getStaffPerformance(staffPerformanceReqVO, staffPerformanceMapper::selectContractCountStaffPerformance);
    }
    @Override
    public List<CrmStatisticsStaffPerformanceRespVO> getContractPriceStaffPerformance(CrmStatisticsStaffPerformanceReqVO staffPerformanceReqVO) {
        return getStaffPerformance(staffPerformanceReqVO, staffPerformanceMapper::selectContractPriceStaffPerformance);
    }

    @Override
    public List<CrmStatisticsStaffPerformanceRespVO> getReceivablePriceStaffPerformance(CrmStatisticsStaffPerformanceReqVO staffPerformanceReqVO) {
        return getStaffPerformance(staffPerformanceReqVO, staffPerformanceMapper::selectReceivablePriceStaffPerformance);
    }



    /**
     * 获得员工业绩数据
     *
     * @param staffPerformanceReqVO  参数
     * @param staffPerformanceFunction 排行榜方法
     * @return 排行版数据
     */
    private List<CrmStatisticsStaffPerformanceRespVO> getStaffPerformance(CrmStatisticsStaffPerformanceReqVO staffPerformanceReqVO, Function<CrmStatisticsStaffPerformanceReqVO, List<CrmStatisticsStaffPerformanceRespVO>> staffPerformanceFunction) {

        // 1. 获得员工业绩数据
        List<CrmStatisticsStaffPerformanceRespVO> performance = staffPerformanceFunction.apply(staffPerformanceReqVO);
        if (CollUtil.isEmpty(performance)) {
            return Collections.emptyList();
        }
        performance.sort(Comparator.comparing(CrmStatisticsStaffPerformanceRespVO::getCount).reversed());
        // 3. 拼接用户信息
        appendUserInfo(performance);
        return performance;
    }

    /**
     * 拼接用户信息（昵称、部门）
     *
     * @param performances 员工业绩数据
     */
    private void appendUserInfo(List<CrmStatisticsStaffPerformanceRespVO> performances) {
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(performances, CrmStatisticsStaffPerformanceRespVO::getOwnerUserId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        performances.forEach(performance -> MapUtils.findAndThen(userMap, performance.getOwnerUserId(), user -> {
            performance.setNickname(user.getNickname());
            MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> performance.setDeptName(dept.getName()));
        }));
    }



}