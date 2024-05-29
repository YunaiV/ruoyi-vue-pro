package cn.iocoder.yudao.module.crm.service.statistics;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.mysql.statistics.CrmStatisticsFunnelMapper;
import cn.iocoder.yudao.module.crm.enums.business.CrmBusinessEndStatusEnum;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * CRM 销售漏斗分析 Service 实现类
 *
 * @author HUIHUI
 */
@Service
public class CrmStatisticsFunnelServiceImpl implements CrmStatisticsFunnelService {

    @Resource
    private CrmStatisticsFunnelMapper funnelMapper;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private CrmBusinessService businessService;
    @Resource
    private DeptApi deptApi;

    @Override
    public CrmStatisticFunnelSummaryRespVO getFunnelSummary(CrmStatisticsFunnelReqVO reqVO) {
        // 1. 获得用户编号数组
        List<Long> userIds = getUserIds(reqVO);
        if (CollUtil.isEmpty(userIds)) {
            return null;
        }
        reqVO.setUserIds(userIds);

        // 2. 获得漏斗数据
        Long customerCount = funnelMapper.selectCustomerCountByDate(reqVO);
        Long businessCount = funnelMapper.selectBusinessCountByDateAndEndStatus(reqVO, null);
        Long businessWinCount = funnelMapper.selectBusinessCountByDateAndEndStatus(reqVO, CrmBusinessEndStatusEnum.WIN.getStatus());
        return new CrmStatisticFunnelSummaryRespVO(customerCount, businessCount, businessWinCount);
    }

    @Override
    public List<CrmStatisticsBusinessSummaryByEndStatusRespVO> getBusinessSummaryByEndStatus(CrmStatisticsFunnelReqVO reqVO) {
        // 1. 获得用户编号数组
        reqVO.setUserIds(getUserIds(reqVO));
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }

        // 2. 获得统计数据
        return funnelMapper.selectBusinessSummaryListGroupByEndStatus(reqVO);
    }

    @Override
    public List<CrmStatisticsBusinessSummaryByDateRespVO> getBusinessSummaryByDate(CrmStatisticsFunnelReqVO reqVO) {
        // 1. 获得用户编号数组
        reqVO.setUserIds(getUserIds(reqVO));
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }

        // 2. 按天统计，获取分项统计数据
        List<CrmStatisticsBusinessSummaryByDateRespVO> businessSummaryList = funnelMapper.selectBusinessSummaryGroupByDate(reqVO);
        // 3. 按照日期间隔，合并数据
        List<LocalDateTime[]> timeRanges = LocalDateTimeUtils.getDateRangeList(reqVO.getTimes()[0], reqVO.getTimes()[1], reqVO.getInterval());
        return convertList(timeRanges, times -> {
            Long businessCreateCount = businessSummaryList.stream()
                    .filter(vo -> LocalDateTimeUtils.isBetween(times[0], times[1], vo.getTime()))
                    .mapToLong(CrmStatisticsBusinessSummaryByDateRespVO::getBusinessCreateCount).sum();
            BigDecimal businessDealCount = businessSummaryList.stream()
                    .filter(vo -> LocalDateTimeUtils.isBetween(times[0], times[1], vo.getTime()))
                    .map(CrmStatisticsBusinessSummaryByDateRespVO::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new CrmStatisticsBusinessSummaryByDateRespVO()
                    .setTime(LocalDateTimeUtils.formatDateRange(times[0], times[1], reqVO.getInterval()))
                    .setBusinessCreateCount(businessCreateCount).setTotalPrice(businessDealCount);
        });
    }

    @Override
    public List<CrmStatisticsBusinessInversionRateSummaryByDateRespVO> getBusinessInversionRateSummaryByDate(CrmStatisticsFunnelReqVO reqVO) {
        // 1. 获得用户编号数组
        reqVO.setUserIds(getUserIds(reqVO));
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }

        // 2. 按天统计，获取分项统计数据
        List<CrmStatisticsBusinessInversionRateSummaryByDateRespVO> businessSummaryList = funnelMapper.selectBusinessInversionRateSummaryByDate(reqVO);
        // 3. 按照日期间隔，合并数据
        List<LocalDateTime[]> timeRanges = LocalDateTimeUtils.getDateRangeList(reqVO.getTimes()[0], reqVO.getTimes()[1], reqVO.getInterval());
        return convertList(timeRanges, times -> {
            Long businessCount = businessSummaryList.stream()
                    .filter(vo -> LocalDateTimeUtils.isBetween(times[0], times[1], vo.getTime()))
                    .mapToLong(CrmStatisticsBusinessInversionRateSummaryByDateRespVO::getBusinessCount).sum();
            Long businessWinCount = businessSummaryList.stream()
                    .filter(vo -> LocalDateTimeUtils.isBetween(times[0], times[1], vo.getTime()))
                    .mapToLong(CrmStatisticsBusinessInversionRateSummaryByDateRespVO::getBusinessWinCount).sum();
            return new CrmStatisticsBusinessInversionRateSummaryByDateRespVO()
                    .setTime(LocalDateTimeUtils.formatDateRange(times[0], times[1], reqVO.getInterval()))
                    .setBusinessCount(businessCount).setBusinessWinCount(businessWinCount);
        });
    }

    @Override
    public PageResult<CrmBusinessDO> getBusinessPageByDate(CrmStatisticsFunnelReqVO pageVO) {
        // 1. 获得用户编号数组
        pageVO.setUserIds(getUserIds(pageVO));
        if (CollUtil.isEmpty(pageVO.getUserIds())) {
            return PageResult.empty();
        }
        // 2. 执行查询
        return businessService.getBusinessPageByDate(pageVO);
    }

    /**
     * 获取用户编号数组。如果用户编号为空, 则获得部门下的用户编号数组，包括子部门的所有用户编号
     *
     * @param reqVO 请求参数
     * @return 用户编号数组
     */
    private List<Long> getUserIds(CrmStatisticsFunnelReqVO reqVO) {
        // 情况一：选中某个用户
        if (ObjUtil.isNotNull(reqVO.getUserId())) {
            return ListUtil.of(reqVO.getUserId());
        }
        // 情况二：选中某个部门
        // 2.1 获得部门列表
        List<Long> deptIds = convertList(deptApi.getChildDeptList(reqVO.getDeptId()), DeptRespDTO::getId);
        deptIds.add(reqVO.getDeptId());
        // 2.2 获得用户编号
        return convertList(adminUserApi.getUserListByDeptIds(deptIds), AdminUserRespDTO::getId);
    }

}
