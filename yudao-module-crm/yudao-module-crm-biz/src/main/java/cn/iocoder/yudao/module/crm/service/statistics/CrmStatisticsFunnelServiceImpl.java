package cn.iocoder.yudao.module.crm.service.statistics;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel.CrmStatisticBusinessEndStatusRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel.CrmStatisticFunnelRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel.CrmStatisticsBusinessSummaryByDateRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel.CrmStatisticsFunnelReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.mysql.statistics.CrmStatisticsFunnelMapper;
import cn.iocoder.yudao.module.crm.enums.business.CrmBusinessEndStatusEnum;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

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
    private CrmCustomerService customerService;
    @Resource
    private CrmBusinessService businessService;
    @Resource
    private DeptApi deptApi;

    // TODO @puhui999：貌似想了下，可能还是得按照；；；
    @Override
    public CrmStatisticFunnelRespVO getFunnelSummary(CrmStatisticsFunnelReqVO reqVO) {
        // 1. 获得用户编号数组
        List<Long> userIds = getUserIds(reqVO);
        if (CollUtil.isEmpty(userIds)) {
            return null;
        }
        reqVO.setUserIds(userIds);

        // 2. 获得漏斗数据
        return new CrmStatisticFunnelRespVO(
                customerService.getCustomerCountByOwnerUserIds(userIds, reqVO.getTimes()),
                businessService.getBusinessCountByOwnerUserIdsAndEndStatus(userIds, reqVO.getTimes(), null),
                businessService.getBusinessCountByOwnerUserIdsAndEndStatus(userIds, reqVO.getTimes(), CrmBusinessEndStatusEnum.WIN.getStatus())
        );
    }

    @Override
    public List<CrmStatisticBusinessEndStatusRespVO> getBusinessEndStatusSummary(CrmStatisticsFunnelReqVO reqVO) {
        // 1. 获得用户编号数组
        reqVO.setUserIds(getUserIds(reqVO));
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }

        // TODO @puhui999：这个可以优化下，通过统计 sql，不通过内存计算；
        // 2.1 获得用户负责的商机
        List<CrmBusinessDO> businessList = businessService.getBusinessListByOwnerUserIdsAndEndStatusNotNull(reqVO.getUserIds(), reqVO.getTimes());
        // 2.2 统计各阶段数据
        Map<Integer, List<CrmBusinessDO>> businessMap = convertMultiMap(businessList, CrmBusinessDO::getEndStatus);
        return convertList(CrmBusinessEndStatusEnum.values(), endStatusEnum -> {
            List<CrmBusinessDO> list = businessMap.get(endStatusEnum.getStatus());
            if (CollUtil.isEmpty(list)) {
                return new CrmStatisticBusinessEndStatusRespVO(endStatusEnum.getStatus(), 0L, BigDecimal.ZERO);
            }
            return new CrmStatisticBusinessEndStatusRespVO(endStatusEnum.getStatus(), (long) list.size(),
                    getSumValue(list, CrmBusinessDO::getTotalPrice, BigDecimal::add));
        });
    }

    @Override
    public List<CrmStatisticsBusinessSummaryByDateRespVO> getBusinessSummaryByDate(CrmStatisticsFunnelReqVO reqVO) {
        // 1. 获得用户编号数组
        reqVO.setUserIds(getUserIds(reqVO));
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }

        // 2. 按天统计，获取分项统计数据
        // TODO @puhui999：可以这个统计，返回的时候，就把数量、金额一起统计好；
        List<CrmStatisticsBusinessSummaryByDateRespVO> businessCreateCountList = funnelMapper.selectBusinessCreateCountGroupByDate(reqVO);
        List<CrmBusinessDO> businessList = businessService.getBusinessListByOwnerUserIdsAndDate(reqVO.getUserIds(), reqVO.getTimes());
        Map<String, BigDecimal> businessDealCountMap = businessList.stream().collect(Collectors.groupingBy(business ->
                        business.getCreateTime().format(DateTimeFormatter.ofPattern(FORMAT_YEAR_MONTH_DAY)),
                Collectors.reducing(BigDecimal.ZERO, CrmBusinessDO::getTotalPrice, BigDecimal::add)));

        // 3. 按照日期间隔，合并数据
        List<LocalDateTime[]> timeRanges = LocalDateTimeUtils.getDateRangeList(reqVO.getTimes()[0], reqVO.getTimes()[1], reqVO.getInterval());
        return convertList(timeRanges, times -> {
            Integer businessCreateCount = businessCreateCountList.stream()
                    .filter(vo -> LocalDateTimeUtils.isBetween(times[0], times[1], vo.getTime()))
                    .mapToInt(CrmStatisticsBusinessSummaryByDateRespVO::getBusinessCreateCount).sum();
            BigDecimal businessDealCount = businessDealCountMap.entrySet().stream()
                    .filter(vo -> LocalDateTimeUtils.isBetween(times[0], times[1], vo.getKey()))
                    .map(Map.Entry::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new CrmStatisticsBusinessSummaryByDateRespVO()
                    .setTime(LocalDateTimeUtils.formatDateRange(times[0], times[1], reqVO.getInterval()))
                    .setBusinessCreateCount(businessCreateCount).setBusinessDealCount(businessDealCount);
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
        return businessService.getBusinessPageByDate(pageVO.getUserIds(), pageVO.getTimes(), pageVO.getPageNo(), pageVO.getPageSize());
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
            return List.of(reqVO.getUserId());
        }
        // 情况二：选中某个部门
        // 2.1 获得部门列表
        List<Long> deptIds = convertList(deptApi.getChildDeptList(reqVO.getDeptId()), DeptRespDTO::getId);
        deptIds.add(reqVO.getDeptId());
        // 2.2 获得用户编号
        return convertList(adminUserApi.getUserListByDeptIds(deptIds), AdminUserRespDTO::getId);
    }

}
