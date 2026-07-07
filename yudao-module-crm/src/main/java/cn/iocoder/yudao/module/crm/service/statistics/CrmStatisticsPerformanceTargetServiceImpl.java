package cn.iocoder.yudao.module.crm.service.statistics;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.performance.CrmStatisticsPerformanceTargetActualRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.performance.CrmStatisticsPerformanceTargetReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.performance.CrmStatisticsPerformanceTargetRespVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.performance.CrmPerformanceConfigDO;
import cn.iocoder.yudao.module.crm.dal.mysql.statistics.CrmStatisticsPerformanceTargetMapper;
import cn.iocoder.yudao.module.crm.enums.performance.CrmPerformanceConfigBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.performance.CrmPerformanceConfigObjectTypeEnum;
import cn.iocoder.yudao.module.crm.service.performance.CrmPerformanceConfigService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * CRM 业绩目标统计 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CrmStatisticsPerformanceTargetServiceImpl implements CrmStatisticsPerformanceTargetService {

    @Resource
    private CrmStatisticsPerformanceTargetMapper performanceTargetMapper;
    @Resource
    private CrmPerformanceConfigService performanceConfigService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @Override
    public List<CrmStatisticsPerformanceTargetRespVO> getPerformanceTargetSummary(CrmStatisticsPerformanceTargetReqVO reqVO) {
        // 1. 获得用户编号数组
        reqVO.setUserIds(getUserIds(reqVO));
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }

        // 2.1 获得目标配置
        List<CrmPerformanceConfigDO> performanceConfigs = getPerformanceConfigs(reqVO);
        // 2.2 获得实际完成金额
        List<CrmStatisticsPerformanceTargetActualRespVO> actualList = ObjUtil.equal(reqVO.getBizType(),
                CrmPerformanceConfigBizTypeEnum.CRM_RECEIVABLE.getBizType())
                ? performanceTargetMapper.selectReceivableActualByMonth(reqVO)
                : performanceTargetMapper.selectContractActualByMonth(reqVO);
        Map<Integer, BigDecimal> actualMap = convertMap(actualList,
                CrmStatisticsPerformanceTargetActualRespVO::getMonth,
                CrmStatisticsPerformanceTargetActualRespVO::getCurrentPrice);

        // 3. 组装数据返回
        List<CrmStatisticsPerformanceTargetRespVO> result = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            BigDecimal targetPrice = getMonthTargetPrice(performanceConfigs, month);
            BigDecimal currentPrice = actualMap.getOrDefault(month, BigDecimal.ZERO);
            result.add(new CrmStatisticsPerformanceTargetRespVO().setMonth(month).setTargetPrice(targetPrice)
                    .setCurrentPrice(currentPrice)
                    .setCompletionRate(calculateCompletionRate(currentPrice, targetPrice)));
        }
        return result;
    }

    private List<CrmPerformanceConfigDO> getPerformanceConfigs(CrmStatisticsPerformanceTargetReqVO reqVO) {
        // 情况一：选中某个用户
        if (ObjUtil.isNotNull(reqVO.getUserId())) {
            CrmPerformanceConfigDO performanceConfig = performanceConfigService.getPerformanceConfig(reqVO.getUserId(),
                    CrmPerformanceConfigObjectTypeEnum.USER.getObjectType(), reqVO.getYear(), reqVO.getBizType());
            return performanceConfig == null ? Collections.emptyList() : ListUtil.of(performanceConfig);
        }
        // 情况二：选中某个部门
        return performanceConfigService.getPerformanceConfigList(getDeptIds(reqVO.getDeptId()),
                CrmPerformanceConfigObjectTypeEnum.DEPT.getObjectType(), reqVO.getYear(), reqVO.getBizType());
    }

    private BigDecimal getMonthTargetPrice(List<CrmPerformanceConfigDO> performanceConfigs, int month) {
        return performanceConfigs.stream()
                .map(performanceConfig -> getMonthTargetPrice(performanceConfig, month))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @SuppressWarnings("EnhancedSwitchMigration")
    private BigDecimal getMonthTargetPrice(CrmPerformanceConfigDO performanceConfig, int month) {
        // 1. 无目标配置，返回 0
        if (performanceConfig == null) {
            return BigDecimal.ZERO;
        }
        // 2. 获得月份目标
        BigDecimal targetPrice;
        switch (month) {
            case 1:
                targetPrice = performanceConfig.getJanuaryTargetPrice();
                break;
            case 2:
                targetPrice = performanceConfig.getFebruaryTargetPrice();
                break;
            case 3:
                targetPrice = performanceConfig.getMarchTargetPrice();
                break;
            case 4:
                targetPrice = performanceConfig.getAprilTargetPrice();
                break;
            case 5:
                targetPrice = performanceConfig.getMayTargetPrice();
                break;
            case 6:
                targetPrice = performanceConfig.getJuneTargetPrice();
                break;
            case 7:
                targetPrice = performanceConfig.getJulyTargetPrice();
                break;
            case 8:
                targetPrice = performanceConfig.getAugustTargetPrice();
                break;
            case 9:
                targetPrice = performanceConfig.getSeptemberTargetPrice();
                break;
            case 10:
                targetPrice = performanceConfig.getOctoberTargetPrice();
                break;
            case 11:
                targetPrice = performanceConfig.getNovemberTargetPrice();
                break;
            case 12:
                targetPrice = performanceConfig.getDecemberTargetPrice();
                break;
            default:
                targetPrice = BigDecimal.ZERO;
        }
        return targetPrice != null ? targetPrice : BigDecimal.ZERO;
    }

    private BigDecimal calculateCompletionRate(BigDecimal currentPrice, BigDecimal targetPrice) {
        if (targetPrice == null || targetPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentPrice.multiply(BigDecimal.valueOf(100)).divide(targetPrice, 2, RoundingMode.HALF_UP);
    }

    /**
     * 获取用户编号数组
     *
     * 如果用户编号为空，则获得部门下的用户编号数组，包括子部门的所有用户编号
     *
     * @param reqVO 请求参数
     * @return 用户编号数组
     */
    private List<Long> getUserIds(CrmStatisticsPerformanceTargetReqVO reqVO) {
        // 情况一：选中某个用户
        if (ObjUtil.isNotNull(reqVO.getUserId())) {
            return ListUtil.of(reqVO.getUserId());
        }
        // 情况二：选中某个部门
        // 2.1 获得部门列表
        List<Long> deptIds = getDeptIds(reqVO.getDeptId());
        // 2.2 获得用户编号
        return convertList(adminUserApi.getUserListByDeptIds(deptIds), AdminUserRespDTO::getId);
    }

    /**
     * 获得部门编号数组，包括子部门
     *
     * @param deptId 部门编号
     * @return 部门编号数组
     */
    private List<Long> getDeptIds(Long deptId) {
        List<Long> deptIds = convertList(deptApi.getChildDeptList(deptId), DeptRespDTO::getId);
        deptIds.add(deptId);
        return deptIds;
    }

}
