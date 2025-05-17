package cn.iocoder.yudao.module.crm.service.statistics;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.enums.AreaTypeEnum;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.*;
import cn.iocoder.yudao.module.crm.dal.mysql.statistics.CrmStatisticsCustomerMapper;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

/**
 * CRM 客户分析 Service 实现类
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
    public List<CrmStatisticsCustomerSummaryByDateRespVO> getCustomerSummaryByDate(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        reqVO.setUserIds(getUserIds(reqVO));
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }

        // 2. 按天统计，获取分项统计数据
        List<CrmStatisticsCustomerSummaryByDateRespVO> customerCreateCountList = customerMapper.selectCustomerCreateCountGroupByDate(reqVO);
        List<CrmStatisticsCustomerSummaryByDateRespVO> customerDealCountList = customerMapper.selectCustomerDealCountGroupByDate(reqVO);

        // 3. 按照日期间隔，合并数据
        List<LocalDateTime[]> timeRanges = LocalDateTimeUtils.getDateRangeList(reqVO.getTimes()[0], reqVO.getTimes()[1], reqVO.getInterval());
        return convertList(timeRanges, times -> {
            Integer customerCreateCount = customerCreateCountList.stream()
                    .filter(vo -> LocalDateTimeUtils.isBetween(times[0], times[1], vo.getTime()))
                    .mapToInt(CrmStatisticsCustomerSummaryByDateRespVO::getCustomerCreateCount).sum();
            Integer customerDealCount = customerDealCountList.stream()
                    .filter(vo -> LocalDateTimeUtils.isBetween(times[0], times[1], vo.getTime()))
                    .mapToInt(CrmStatisticsCustomerSummaryByDateRespVO::getCustomerDealCount).sum();
            return new CrmStatisticsCustomerSummaryByDateRespVO()
                    .setTime(LocalDateTimeUtils.formatDateRange(times[0], times[1], reqVO.getInterval()))
                    .setCustomerCreateCount(customerCreateCount).setCustomerDealCount(customerDealCount);
        });
    }

    @Override
    public List<CrmStatisticsCustomerSummaryByUserRespVO> getCustomerSummaryByUser(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        reqVO.setUserIds(getUserIds(reqVO));
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }

        // 2. 按用户统计，获取分项统计数据
        List<CrmStatisticsCustomerSummaryByUserRespVO> customerCreateCountList = customerMapper.selectCustomerCreateCountGroupByUser(reqVO);
        List<CrmStatisticsCustomerSummaryByUserRespVO> customerDealCountList = customerMapper.selectCustomerDealCountGroupByUser(reqVO);
        List<CrmStatisticsCustomerSummaryByUserRespVO> contractPriceList = customerMapper.selectContractPriceGroupByUser(reqVO);
        List<CrmStatisticsCustomerSummaryByUserRespVO> receivablePriceList = customerMapper.selectReceivablePriceGroupByUser(reqVO);

        // 3.1 按照用户，合并统计数据
        List<CrmStatisticsCustomerSummaryByUserRespVO> summaryList = convertList(reqVO.getUserIds(), userId -> {
            Integer customerCreateCount = customerCreateCountList.stream().filter(vo -> userId.equals(vo.getOwnerUserId()))
                    .mapToInt(CrmStatisticsCustomerSummaryByUserRespVO::getCustomerCreateCount).sum();
            Integer customerDealCount = customerDealCountList.stream().filter(vo -> userId.equals(vo.getOwnerUserId()))
                    .mapToInt(CrmStatisticsCustomerSummaryByUserRespVO::getCustomerDealCount).sum();
            BigDecimal contractPrice = contractPriceList.stream().filter(vo -> userId.equals(vo.getOwnerUserId()))
                    .reduce(BigDecimal.ZERO, (sum, vo) -> sum.add(vo.getContractPrice()), BigDecimal::add);
            BigDecimal receivablePrice = receivablePriceList.stream().filter(vo -> userId.equals(vo.getOwnerUserId()))
                    .reduce(BigDecimal.ZERO, (sum, vo) -> sum.add(vo.getReceivablePrice()), BigDecimal::add);
            return (CrmStatisticsCustomerSummaryByUserRespVO) new CrmStatisticsCustomerSummaryByUserRespVO()
                    .setCustomerCreateCount(customerCreateCount).setCustomerDealCount(customerDealCount)
                    .setContractPrice(contractPrice).setReceivablePrice(receivablePrice).setOwnerUserId(userId);
        });
        // 3.2 拼接用户信息
        appendUserInfo(summaryList);
        return summaryList;
    }

    @Override
    public List<CrmStatisticsFollowUpSummaryByDateRespVO> getFollowUpSummaryByDate(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        reqVO.setUserIds(getUserIds(reqVO));
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }

        // 2. 按天统计，获取分项统计数据
        List<CrmStatisticsFollowUpSummaryByDateRespVO> followUpRecordCountList = customerMapper.selectFollowUpRecordCountGroupByDate(reqVO);
        List<CrmStatisticsFollowUpSummaryByDateRespVO> followUpCustomerCountList = customerMapper.selectFollowUpCustomerCountGroupByDate(reqVO);

        // 3. 按照时间间隔，合并统计数据
        List<LocalDateTime[]> timeRanges = LocalDateTimeUtils.getDateRangeList(reqVO.getTimes()[0], reqVO.getTimes()[1], reqVO.getInterval());
        return convertList(timeRanges, times -> {
            Integer followUpRecordCount = followUpRecordCountList.stream()
                    .filter(vo -> LocalDateTimeUtils.isBetween(times[0], times[1], vo.getTime()))
                    .mapToInt(CrmStatisticsFollowUpSummaryByDateRespVO::getFollowUpRecordCount).sum();
            Integer followUpCustomerCount = followUpCustomerCountList.stream()
                    .filter(vo -> LocalDateTimeUtils.isBetween(times[0], times[1], vo.getTime()))
                    .mapToInt(CrmStatisticsFollowUpSummaryByDateRespVO::getFollowUpCustomerCount).sum();
            return new CrmStatisticsFollowUpSummaryByDateRespVO()
                    .setTime(LocalDateTimeUtils.formatDateRange(times[0], times[1], reqVO.getInterval()))
                    .setFollowUpCustomerCount(followUpRecordCount).setFollowUpRecordCount(followUpCustomerCount);
        });
    }

    @Override
    public List<CrmStatisticsFollowUpSummaryByUserRespVO> getFollowUpSummaryByUser(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        reqVO.setUserIds(getUserIds(reqVO));
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }

        // 2. 按用户统计，获取分项统计数据
        List<CrmStatisticsFollowUpSummaryByUserRespVO> followUpRecordCountList = customerMapper.selectFollowUpRecordCountGroupByUser(reqVO);
        List<CrmStatisticsFollowUpSummaryByUserRespVO> followUpCustomerCountList = customerMapper.selectFollowUpCustomerCountGroupByUser(reqVO);

        // 3.1 按照用户，合并统计数据
        List<CrmStatisticsFollowUpSummaryByUserRespVO> summaryList = convertList(reqVO.getUserIds(), userId -> {
            Integer followUpRecordCount = followUpRecordCountList.stream().filter(vo -> userId.equals(vo.getOwnerUserId()))
                    .mapToInt(CrmStatisticsFollowUpSummaryByUserRespVO::getFollowUpRecordCount).sum();
            Integer followUpCustomerCount = followUpCustomerCountList.stream().filter(vo -> userId.equals(vo.getOwnerUserId()))
                    .mapToInt(CrmStatisticsFollowUpSummaryByUserRespVO::getFollowUpCustomerCount).sum();
            return (CrmStatisticsFollowUpSummaryByUserRespVO) new CrmStatisticsFollowUpSummaryByUserRespVO()
                    .setFollowUpCustomerCount(followUpRecordCount).setFollowUpRecordCount(followUpCustomerCount).setOwnerUserId(userId);
        });
        // 3.2 拼接用户信息
        appendUserInfo(summaryList);
        return summaryList;
    }

    @Override
    public List<CrmStatisticsFollowUpSummaryByTypeRespVO> getFollowUpSummaryByType(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        reqVO.setUserIds(getUserIds(reqVO));
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }

        // 2. 获得跟进数据
        return customerMapper.selectFollowUpRecordCountGroupByType(reqVO);
    }

    @Override
    public List<CrmStatisticsCustomerContractSummaryRespVO> getContractSummary(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        reqVO.setUserIds(getUserIds(reqVO));
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }

        // 2. 按用户统计，获取统计数据
        List<CrmStatisticsCustomerContractSummaryRespVO> summaryList = customerMapper.selectContractSummary(reqVO);

        // 3. 拼接信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSetByFlatMap(summaryList, vo -> Stream.of(NumberUtils.parseLong(vo.getCreator()), vo.getOwnerUserId())));
        summaryList.forEach(vo -> {
            findAndThen(userMap, NumberUtils.parseLong(vo.getCreator()), user -> vo.setCreatorUserName(user.getNickname()));
            findAndThen(userMap, vo.getOwnerUserId(), user -> vo.setOwnerUserName(user.getNickname()));
        });
        return summaryList;
    }

    @Override
    public List<CrmStatisticsPoolSummaryByDateRespVO> getPoolSummaryByDate(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        reqVO.setUserIds(getUserIds(reqVO));
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }

        // 2. 按天统计，获取分项统计数据
        List<CrmStatisticsPoolSummaryByDateRespVO> customerPutCountList = customerMapper.selectPoolCustomerPutCountByDate(reqVO);
        List<CrmStatisticsPoolSummaryByDateRespVO> customerTakeCountList = customerMapper.selectPoolCustomerTakeCountByDate(reqVO);

        // 3. 按照日期间隔，合并数据
        List<LocalDateTime[]> timeRanges = LocalDateTimeUtils.getDateRangeList(reqVO.getTimes()[0], reqVO.getTimes()[1], reqVO.getInterval());
        return convertList(timeRanges, times -> {
            Integer customerPutCount = customerPutCountList.stream()
                .filter(vo -> LocalDateTimeUtils.isBetween(times[0], times[1], vo.getTime()))
                .mapToInt(CrmStatisticsPoolSummaryByDateRespVO::getCustomerPutCount).sum();
            Integer customerTakeCount = customerTakeCountList.stream()
                .filter(vo -> LocalDateTimeUtils.isBetween(times[0], times[1], vo.getTime()))
                .mapToInt(CrmStatisticsPoolSummaryByDateRespVO::getCustomerTakeCount).sum();
            return new CrmStatisticsPoolSummaryByDateRespVO()
                .setTime(LocalDateTimeUtils.formatDateRange(times[0], times[1], reqVO.getInterval()))
                .setCustomerPutCount(customerPutCount).setCustomerTakeCount(customerTakeCount);
        });
    }

    @Override
    public List<CrmStatisticsPoolSummaryByUserRespVO> getPoolSummaryByUser(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        reqVO.setUserIds(getUserIds(reqVO));
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }

        // 2. 按用户统计，获取分项统计数据
        List<CrmStatisticsPoolSummaryByUserRespVO> customerPutCountList = customerMapper.selectPoolCustomerPutCountByUser(reqVO);
        List<CrmStatisticsPoolSummaryByUserRespVO> customerTakeCountList = customerMapper.selectPoolCustomerTakeCountByUser(reqVO);

        // 3.1 按照用户，合并统计数据
        List<CrmStatisticsPoolSummaryByUserRespVO> summaryList = convertList(reqVO.getUserIds(), userId -> {
            Integer customerPutCount = customerPutCountList.stream().filter(vo -> userId.equals(vo.getOwnerUserId()))
                .mapToInt(CrmStatisticsPoolSummaryByUserRespVO::getCustomerPutCount).sum();
            Integer customerTakeCount = customerTakeCountList.stream().filter(vo -> userId.equals(vo.getOwnerUserId()))
                .mapToInt(CrmStatisticsPoolSummaryByUserRespVO::getCustomerTakeCount).sum();
            return (CrmStatisticsPoolSummaryByUserRespVO) new CrmStatisticsPoolSummaryByUserRespVO()
                .setCustomerPutCount(customerPutCount).setCustomerTakeCount(customerTakeCount)
                .setOwnerUserId(userId);
        });
        // 3.2 拼接用户信息
        appendUserInfo(summaryList);
        return summaryList;
    }

    @Override
    public List<CrmStatisticsCustomerDealCycleByDateRespVO> getCustomerDealCycleByDate(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        reqVO.setUserIds(getUserIds(reqVO));
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }

        // 2. 按天统计，获取分项统计数据
        List<CrmStatisticsCustomerDealCycleByDateRespVO> customerDealCycleList = customerMapper.selectCustomerDealCycleGroupByDate(reqVO);

        // 3. 按照日期间隔，合并统计数据
        List<LocalDateTime[]> timeRanges = LocalDateTimeUtils.getDateRangeList(reqVO.getTimes()[0], reqVO.getTimes()[1], reqVO.getInterval());
        return convertList(timeRanges, times -> {
            Double customerDealCycle = customerDealCycleList.stream()
                    .filter(vo -> LocalDateTimeUtils.isBetween(times[0], times[1], vo.getTime()))
                    .mapToDouble(CrmStatisticsCustomerDealCycleByDateRespVO::getCustomerDealCycle).sum();
            return new CrmStatisticsCustomerDealCycleByDateRespVO()
                    .setTime(LocalDateTimeUtils.formatDateRange(times[0], times[1], reqVO.getInterval()))
                    .setCustomerDealCycle(customerDealCycle);
        });
    }

    @Override
    public List<CrmStatisticsCustomerDealCycleByUserRespVO> getCustomerDealCycleByUser(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        reqVO.setUserIds(getUserIds(reqVO));
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }

        // 2. 按用户统计，获取分项统计数据
        List<CrmStatisticsCustomerDealCycleByUserRespVO> customerDealCycleList = customerMapper.selectCustomerDealCycleGroupByUser(reqVO);
        List<CrmStatisticsCustomerSummaryByUserRespVO> customerDealCountList = customerMapper.selectCustomerDealCountGroupByUser(reqVO);

        // 3.1 按照用户，合并统计数据
        List<CrmStatisticsCustomerDealCycleByUserRespVO> summaryList = convertList(reqVO.getUserIds(), userId -> {
            Double customerDealCycle = customerDealCycleList.stream().filter(vo -> userId.equals(vo.getOwnerUserId()))
                    .mapToDouble(CrmStatisticsCustomerDealCycleByUserRespVO::getCustomerDealCycle).sum();
            Integer customerDealCount = customerDealCountList.stream().filter(vo -> userId.equals(vo.getOwnerUserId()))
                    .mapToInt(CrmStatisticsCustomerSummaryByUserRespVO::getCustomerDealCount).sum();
            return (CrmStatisticsCustomerDealCycleByUserRespVO) new CrmStatisticsCustomerDealCycleByUserRespVO()
                    .setCustomerDealCycle(customerDealCycle).setCustomerDealCount(customerDealCount).setOwnerUserId(userId);
        });
        // 3.2 拼接用户信息
        appendUserInfo(summaryList);
        return summaryList;
    }

    @Override
    public List<CrmStatisticsCustomerDealCycleByAreaRespVO> getCustomerDealCycleByArea(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        List<Long> userIds = getUserIds(reqVO);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        reqVO.setUserIds(userIds);

        // 2. 获取客户地区统计数据
        List<CrmStatisticsCustomerDealCycleByAreaRespVO> dealCycleByAreaList = customerMapper.selectCustomerDealCycleGroupByAreaId(reqVO);
        if (CollUtil.isEmpty(dealCycleByAreaList)) {
            return Collections.emptyList();
        }

        // 3. 拼接数据
        Map<Integer, Area> areaMap = convertMap(AreaUtils.getByType(AreaTypeEnum.PROVINCE, Function.identity()), Area::getId);
        return convertList(dealCycleByAreaList, vo -> {
            if (vo.getAreaId() != null) {
                Integer parentId = AreaUtils.getParentIdByType(vo.getAreaId(), AreaTypeEnum.PROVINCE);
                findAndThen(areaMap, parentId, area -> vo.setAreaId(parentId).setAreaName(area.getName()));
            }
            return vo;
        });
    }

    @Override
    public List<CrmStatisticsCustomerDealCycleByProductRespVO> getCustomerDealCycleByProduct(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        List<Long> userIds = getUserIds(reqVO);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        reqVO.setUserIds(userIds);

        // 2. 获取客户产品统计数据
        // TODO @dhb52：未读取产品名
        return customerMapper.selectCustomerDealCycleGroupByProductId(reqVO);
    }

    /**
     * 拼接用户信息（昵称）
     *
     * @param voList 统计数据
     */
    private <T extends CrmStatisticsCustomerByUserBaseRespVO> void appendUserInfo(List<T> voList) {
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(voList, CrmStatisticsCustomerByUserBaseRespVO::getOwnerUserId));
        voList.forEach(vo -> findAndThen(userMap, vo.getOwnerUserId(), user -> vo.setOwnerUserName(user.getNickname())));
    }

    /**
     * 获取用户编号数组。如果用户编号为空, 则获得部门下的用户编号数组，包括子部门的所有用户编号
     *
     * @param reqVO 请求参数
     * @return 用户编号数组
     */
    private List<Long> getUserIds(CrmStatisticsCustomerReqVO reqVO) {
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
