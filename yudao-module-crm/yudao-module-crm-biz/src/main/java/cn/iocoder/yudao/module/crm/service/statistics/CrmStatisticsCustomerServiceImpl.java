package cn.iocoder.yudao.module.crm.service.statistics;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.CrmStatisticsCustomerReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.CrmStatisticsCustomerCountVO;
import cn.iocoder.yudao.module.crm.dal.mysql.statistics.CrmStatisticsCustomerMapper;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

/**
 * CRM 数据统计 员工客户分析 Service 实现类
 *
 * @author dhb52
 */
@Service
@Validated
public class CrmStatisticsCustomerServiceImpl implements CrmStatisticsCustomerService {

    @Resource
    private CrmStatisticsCustomerMapper customerMapper;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @Override
    public List<CrmStatisticsCustomerCountVO> getTotalCustomerCount(CrmStatisticsCustomerReqVO reqVO) {
        return getStat(reqVO, customerMapper::selectCustomerCountGroupbyDate);
    }

    @Override
    public List<CrmStatisticsCustomerCountVO> getDealTotalCustomerCount(CrmStatisticsCustomerReqVO reqVO) {
        return getStat(reqVO, customerMapper::selectDealCustomerCountGroupbyDate);
    }

    /**
     * 获得统计数据
     *
     * @param reqVO        参数
     * @param statFunction 统计方法
     * @return 统计数据
     */
    private List<CrmStatisticsCustomerCountVO> getStat(CrmStatisticsCustomerReqVO reqVO, Function<CrmStatisticsCustomerReqVO, List<CrmStatisticsCustomerCountVO>> statFunction) {
        // 1. 获得用户编号数组: 如果用户编号为空, 则获得部门下的用户编号数组
        if (ObjUtil.isNotNull(reqVO.getUserId())) {
            reqVO.setUserIds(List.of(reqVO.getUserId()));
        } else {
            reqVO.setUserIds(getUserIds(reqVO.getDeptId()));
        }
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }

        // 2. 生成日期格式
        LocalDateTime startTime = reqVO.getTimes()[0];
        final LocalDateTime endTime = reqVO.getTimes()[1];
        final long days = LocalDateTimeUtil.between(startTime, endTime).toDays();
        boolean byMonth = days > 31;
        if (byMonth) {
            // 按月
            reqVO.setSqlDateFormat("%Y%m");
        } else {
            // 按日
            reqVO.setSqlDateFormat("%Y%m%d");
        }

        // 3. 获得排行数据
        List<CrmStatisticsCustomerCountVO> stats = statFunction.apply(reqVO);

        // 4. 生成时间序列
        List<CrmStatisticsCustomerCountVO> result = CollUtil.newArrayList();
        while (startTime.compareTo(endTime) <= 0) {
            final String category = LocalDateTimeUtil.format(startTime, byMonth ? "yyyyMM" : "yyyyMMdd");
            result.add(new CrmStatisticsCustomerCountVO().setCategory(category).setCount(0));
            if (byMonth)
                startTime = startTime.plusMonths(1);
            else
                startTime = startTime.plusDays(1);
        }

        // 5. 使用时间序列填充结果
        final Map<String, Integer> statMap = convertMap(stats,
                CrmStatisticsCustomerCountVO::getCategory,
                CrmStatisticsCustomerCountVO::getCount);
        result.forEach(r -> {
            if (statMap.containsKey(r.getCategory())) {
                r.setCount(statMap.get(r.getCategory()));
            }
        });

        return result;
    }


    /**
     * 获得部门下的用户编号数组，包括子部门的
     *
     * @param deptId 部门编号
     * @return 用户编号数组
     */
    public List<Long> getUserIds(Long deptId) {
        // 1. 获得部门列表
        List<Long> deptIds = convertList(deptApi.getChildDeptList(deptId), DeptRespDTO::getId);
        deptIds.add(deptId);
        // 2. 获得用户编号
        return convertList(adminUserApi.getUserListByDeptIds(deptIds), AdminUserRespDTO::getId);
    }
}
