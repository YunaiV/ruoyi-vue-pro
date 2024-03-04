package cn.iocoder.yudao.module.crm.service.statistics;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.*;
import cn.iocoder.yudao.module.crm.dal.mysql.statistics.CrmStatisticsCustomerMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.dict.dto.DictDataRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

/**
 * CRM 客户分析 Service 实现类
 *
 * @author dhb52
 */
@Service
@Validated
public class CrmStatisticsCustomerServiceImpl implements CrmStatisticsCustomerService {

    private static final String SQL_DATE_FORMAT_BY_MONTH = "%Y%m";
    private static final String SQL_DATE_FORMAT_BY_DAY = "%Y%m%d";

    private static final String TIME_FORMAT_BY_MONTH = "yyyyMM";
    private static final String TIME_FORMAT_BY_DAY = "yyyyMMdd";


    @Resource
    private CrmStatisticsCustomerMapper customerMapper;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;
    @Resource
    private DictDataApi dictDataApi;


    @Override
    public List<CrmStatisticsCustomerSummaryByDateRespVO> getCustomerSummaryByDate(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        final List<Long> userIds = getUserIds(reqVO);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        reqVO.setUserIds(userIds);

        // 2. 获取分项统计数据
        reqVO.setSqlDateFormat(getSqlDateFormat(reqVO.getTimes()[0], reqVO.getTimes()[1]));
        final List<CrmStatisticsCustomerSummaryByDateRespVO> customerCreateCount = customerMapper.selectCustomerCreateCountGroupbyDate(reqVO);
        final List<CrmStatisticsCustomerSummaryByDateRespVO> customerDealCount = customerMapper.selectCustomerDealCountGroupbyDate(reqVO);

        // 3. 获取时间序列
        final List<String> times = generateTimeSeries(reqVO.getTimes()[0], reqVO.getTimes()[1]);

        // 4. 合并统计数据
        List<CrmStatisticsCustomerSummaryByDateRespVO> result = new ArrayList<>(times.size());
        final Map<String, Integer> customerCreateCountMap = convertMap(customerCreateCount, CrmStatisticsCustomerSummaryByDateRespVO::getTime, CrmStatisticsCustomerSummaryByDateRespVO::getCustomerCreateCount);
        final Map<String, Integer> customerDealCountMap = convertMap(customerDealCount, CrmStatisticsCustomerSummaryByDateRespVO::getTime, CrmStatisticsCustomerSummaryByDateRespVO::getCustomerDealCount);
        times.forEach(time -> result.add(
            new CrmStatisticsCustomerSummaryByDateRespVO().setTime(time)
                .setCustomerCreateCount(customerCreateCountMap.getOrDefault(time, 0))
                .setCustomerDealCount(customerDealCountMap.getOrDefault(time, 0))
        ));

        return result;
    }

    @Override
    public List<CrmStatisticsCustomerSummaryByUserRespVO> getCustomerSummaryByUser(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        final List<Long> userIds = getUserIds(reqVO);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        reqVO.setUserIds(userIds);

        // 2. 获取分项统计数据
        final List<CrmStatisticsCustomerSummaryByUserRespVO> customerCreateCount = customerMapper.selectCustomerCreateCountGroupbyUser(reqVO);
        final List<CrmStatisticsCustomerSummaryByUserRespVO> customerDealCount = customerMapper.selectCustomerDealCountGroupbyUser(reqVO);
        final List<CrmStatisticsCustomerSummaryByUserRespVO> contractPrice = customerMapper.selectContractPriceGroupbyUser(reqVO);
        final List<CrmStatisticsCustomerSummaryByUserRespVO> receivablePrice = customerMapper.selectReceivablePriceGroupbyUser(reqVO);

        // 3. 合并统计数据
        final Map<Long, Integer> customerCreateCountMap = convertMap(customerCreateCount, CrmStatisticsCustomerSummaryByUserRespVO::getOwnerUserId, CrmStatisticsCustomerSummaryByUserRespVO::getCustomerCreateCount);
        final Map<Long, Integer> customerDealCountMap = convertMap(customerDealCount, CrmStatisticsCustomerSummaryByUserRespVO::getOwnerUserId, CrmStatisticsCustomerSummaryByUserRespVO::getCustomerDealCount);
        final Map<Long, BigDecimal> contractPriceMap = convertMap(contractPrice, CrmStatisticsCustomerSummaryByUserRespVO::getOwnerUserId, CrmStatisticsCustomerSummaryByUserRespVO::getContractPrice);
        final Map<Long, BigDecimal> receivablePriceMap = convertMap(receivablePrice, CrmStatisticsCustomerSummaryByUserRespVO::getOwnerUserId, CrmStatisticsCustomerSummaryByUserRespVO::getReceivablePrice);
        List<CrmStatisticsCustomerSummaryByUserRespVO> result = new ArrayList<>(userIds.size());
        userIds.forEach(userId -> {
            final CrmStatisticsCustomerSummaryByUserRespVO respVO = new CrmStatisticsCustomerSummaryByUserRespVO();
            respVO.setOwnerUserId(userId);
            respVO.setCustomerCreateCount(customerCreateCountMap.getOrDefault(userId, 0))
                .setCustomerDealCount(customerDealCountMap.getOrDefault(userId, 0))
                .setContractPrice(contractPriceMap.getOrDefault(userId, BigDecimal.valueOf(0)))
                .setReceivablePrice(receivablePriceMap.getOrDefault(userId, BigDecimal.valueOf(0)));
            result.add(respVO);
        });

        // 4. 拼接用户信息
        appendUserInfo(result);

        return result;
    }

    @Override
    public List<CrmStatisticsFollowupSummaryByDateRespVO> getFollowupSummaryByDate(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        final List<Long> userIds = getUserIds(reqVO);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        reqVO.setUserIds(userIds);

        // 2. 获取分项统计数据
        reqVO.setSqlDateFormat(getSqlDateFormat(reqVO.getTimes()[0], reqVO.getTimes()[1]));
        reqVO.setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType());
        final List<CrmStatisticsFollowupSummaryByDateRespVO> followupRecordCount = customerMapper.selectFollowupRecordCountGroupbyDate(reqVO);
        final List<CrmStatisticsFollowupSummaryByDateRespVO> followupCustomerCount = customerMapper.selectFollowupCustomerCountGroupbyDate(reqVO);

        // 3. 获取时间序列
        final List<String> times = generateTimeSeries(reqVO.getTimes()[0], reqVO.getTimes()[1]);

        // 4. 合并统计数据
        List<CrmStatisticsFollowupSummaryByDateRespVO> result = new ArrayList<>(times.size());
        final Map<String, Integer> followupRecordCountMap = convertMap(followupRecordCount, CrmStatisticsFollowupSummaryByDateRespVO::getTime, CrmStatisticsFollowupSummaryByDateRespVO::getFollowupRecordCount);
        final Map<String, Integer> followupCustomerCountMap = convertMap(followupCustomerCount, CrmStatisticsFollowupSummaryByDateRespVO::getTime, CrmStatisticsFollowupSummaryByDateRespVO::getFollowupCustomerCount);
        times.forEach(time -> result.add(
            new CrmStatisticsFollowupSummaryByDateRespVO().setTime(time)
                .setFollowupRecordCount(followupRecordCountMap.getOrDefault(time, 0))
                .setFollowupCustomerCount(followupCustomerCountMap.getOrDefault(time, 0))
        ));

        return result;
    }

    @Override
    public List<CrmStatisticsFollowupSummaryByUserRespVO> getFollowupSummaryByUser(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        final List<Long> userIds = getUserIds(reqVO);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        reqVO.setUserIds(userIds);

        // 2. 获取分项统计数据
        reqVO.setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType());
        final List<CrmStatisticsFollowupSummaryByUserRespVO> followupRecordCount = customerMapper.selectFollowupRecordCountGroupbyUser(reqVO);
        final List<CrmStatisticsFollowupSummaryByUserRespVO> followupCustomerCount = customerMapper.selectFollowupCustomerCountGroupbyUser(reqVO);

        // 3. 合并统计数据
        final Map<Long, Integer> followupRecordCountMap = convertMap(followupRecordCount, CrmStatisticsFollowupSummaryByUserRespVO::getOwnerUserId, CrmStatisticsFollowupSummaryByUserRespVO::getFollowupRecordCount);
        final Map<Long, Integer> followupCustomerCountMap = convertMap(followupCustomerCount, CrmStatisticsFollowupSummaryByUserRespVO::getOwnerUserId, CrmStatisticsFollowupSummaryByUserRespVO::getFollowupCustomerCount);
        List<CrmStatisticsFollowupSummaryByUserRespVO> result = new ArrayList<>(userIds.size());
        userIds.forEach(userId -> {
            final CrmStatisticsFollowupSummaryByUserRespVO stat = new CrmStatisticsFollowupSummaryByUserRespVO()
                .setFollowupRecordCount(followupRecordCountMap.getOrDefault(userId, 0))
                .setFollowupCustomerCount(followupCustomerCountMap.getOrDefault(userId, 0));
            stat.setOwnerUserId(userId);
            result.add(stat);
        });

        // 4. 拼接用户信息
        appendUserInfo(result);

        return result;
    }

    @Override
    public List<CrmStatisticsFollowupSummaryByTypeRespVO> getFollowupSummaryByType(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        final List<Long> userIds = getUserIds(reqVO);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        reqVO.setUserIds(userIds);

        // 2. 获得排行数据
        reqVO.setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType());
        List<CrmStatisticsFollowupSummaryByTypeRespVO> stats = customerMapper.selectFollowupRecordCountGroupbyType(reqVO);

        // 3. 获取字典数据
        List<DictDataRespDTO> followUpTypes = dictDataApi.getDictDataList("crm_follow_up_type");
        final Map<String, String> followUpTypeMap = convertMap(followUpTypes, DictDataRespDTO::getValue, DictDataRespDTO::getLabel);
        stats.forEach(stat -> {
            stat.setFollowupType(followUpTypeMap.get(stat.getFollowupType()));
        });

        return stats;
    }

    @Override
    public List<CrmStatisticsCustomerContractSummaryRespVO> getContractSummary(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        final List<Long> userIds = getUserIds(reqVO);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        reqVO.setUserIds(userIds);

        List<CrmStatisticsCustomerContractSummaryRespVO> contractSummary = customerMapper.selectContractSummary(reqVO);

        // 2. 拼接用户信息
        final Set<Long> userIdSet = new HashSet<>();
        userIdSet.addAll(userIds);
        userIdSet.addAll(convertSet(contractSummary, CrmStatisticsCustomerContractSummaryRespVO::getCreatorUserId));
        final Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIdSet);
        contractSummary.forEach(contract -> contract.setCreatorUserName(userMap.get(contract.getCreatorUserId()).getNickname())
            .setOwnerUserName(userMap.get(contract.getOwnerUserId()).getNickname()));

        return contractSummary;
    }

    @Override
    public List<CrmStatisticsCustomerDealCycleByDateRespVO> getCustomerDealCycleByDate(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        final List<Long> userIds = getUserIds(reqVO);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        reqVO.setUserIds(userIds);

        // 2. 获取分项统计数据
        reqVO.setSqlDateFormat(getSqlDateFormat(reqVO.getTimes()[0], reqVO.getTimes()[1]));
        reqVO.setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType());
        final List<CrmStatisticsCustomerDealCycleByDateRespVO> customerDealCycle = customerMapper.selectCustomerDealCycleGroupbyDate(reqVO);

        // 3. 获取时间序列
        final List<String> times = generateTimeSeries(reqVO.getTimes()[0], reqVO.getTimes()[1]);

        // 4. 合并统计数据
        List<CrmStatisticsCustomerDealCycleByDateRespVO> result = new ArrayList<>(times.size());
        final Map<String, Double> customerDealCycleMap = convertMap(customerDealCycle, CrmStatisticsCustomerDealCycleByDateRespVO::getTime, CrmStatisticsCustomerDealCycleByDateRespVO::getCustomerDealCycle);
        times.forEach(time -> result.add(
            new CrmStatisticsCustomerDealCycleByDateRespVO().setTime(time)
                .setCustomerDealCycle(customerDealCycleMap.getOrDefault(time, Double.valueOf(0)))
        ));

        return result;
    }

    @Override
    public List<CrmStatisticsCustomerDealCycleByUserRespVO> getCustomerDealCycleByUser(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        final List<Long> userIds = getUserIds(reqVO);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        reqVO.setUserIds(userIds);

        // 2. 获取分项统计数据
        reqVO.setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType());
        final List<CrmStatisticsCustomerDealCycleByUserRespVO> customerDealCycle = customerMapper.selectCustomerDealCycleGroupbyUser(reqVO);
        final List<CrmStatisticsCustomerSummaryByUserRespVO> customerDealCount = customerMapper.selectCustomerDealCountGroupbyUser(reqVO);

        // 3. 合并统计数据
        final Map<Long, Double> customerDealCycleMap = convertMap(customerDealCycle, CrmStatisticsCustomerDealCycleByUserRespVO::getOwnerUserId, CrmStatisticsCustomerDealCycleByUserRespVO::getCustomerDealCycle);
        final Map<Long, Integer> customerDealCountMap = convertMap(customerDealCount, CrmStatisticsCustomerSummaryByUserRespVO::getOwnerUserId, CrmStatisticsCustomerSummaryByUserRespVO::getCustomerDealCount);
        List<CrmStatisticsCustomerDealCycleByUserRespVO> result = new ArrayList<>(userIds.size());
        userIds.forEach(userId -> {
            final CrmStatisticsCustomerDealCycleByUserRespVO stat = new CrmStatisticsCustomerDealCycleByUserRespVO()
                .setCustomerDealCycle(customerDealCycleMap.getOrDefault(userId, 0.0))
                .setCustomerDealCount(customerDealCountMap.getOrDefault(userId, 0));
            stat.setOwnerUserId(userId);
            result.add(stat);
        });

        // 4. 拼接用户信息
        appendUserInfo(result);

        return result;
    }

    /**
     * 拼接用户信息（昵称）
     *
     * @param stats 统计数据
     */
    private <T extends CrmStatisticsCustomerByUserBaseRespVO> void appendUserInfo(List<T> stats) {
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(stats, CrmStatisticsCustomerByUserBaseRespVO::getOwnerUserId));
        stats.forEach(stat -> MapUtils.findAndThen(userMap, stat.getOwnerUserId(), user -> {
            stat.setOwnerUserName(user.getNickname());
        }));
    }

    /**
     * 获取用户编号数组。如果用户编号为空, 则获得部门下的用户编号数组，包括子部门的所有用户编号
     *
     * @param reqVO 请求参数
     * @return 用户编号数组
     */
    private List<Long> getUserIds(CrmStatisticsCustomerReqVO reqVO) {
        if (ObjUtil.isNotNull(reqVO.getUserId())) {
            return List.of(reqVO.getUserId());
        } else {
            // 1. 获得部门列表
            final Long deptId = reqVO.getDeptId();
            List<Long> deptIds = convertList(deptApi.getChildDeptList(deptId), DeptRespDTO::getId);
            deptIds.add(deptId);
            // 2. 获得用户编号
            return convertList(adminUserApi.getUserListByDeptIds(deptIds), AdminUserRespDTO::getId);
        }
    }


    /**
     * 判断是否按照 月粒度 统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 是, 按月粒度, 否则按天粒度统计。
     */
    private boolean queryByMonth(LocalDateTime startTime, LocalDateTime endTime) {
        return LocalDateTimeUtil.between(startTime, endTime).toDays() > 31;
    }

    /**
     * 生成时间序列
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 时间序列
     */
    private List<String> generateTimeSeries(LocalDateTime startTime, LocalDateTime endTime) {
        boolean byMonth = queryByMonth(startTime, endTime);
        List<String> times = CollUtil.newArrayList();
        while (!startTime.isAfter(endTime)) {
            times.add(LocalDateTimeUtil.format(startTime, byMonth ? TIME_FORMAT_BY_MONTH : TIME_FORMAT_BY_DAY));
            if (byMonth)
                startTime = startTime.plusMonths(1);
            else
                startTime = startTime.plusDays(1);
        }

        return times;
    }

    /**
     * 获取 SQL 查询 GROUP BY 的时间格式
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return SQL 查询 GROUP BY 的时间格式
     */
    private String getSqlDateFormat(LocalDateTime startTime, LocalDateTime endTime) {
        return queryByMonth(startTime, endTime) ? SQL_DATE_FORMAT_BY_MONTH : SQL_DATE_FORMAT_BY_DAY;
    }

}
