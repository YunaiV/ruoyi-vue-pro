package cn.iocoder.yudao.module.coal.service.productionplan;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import cn.iocoder.yudao.module.coal.controller.admin.productionplan.vo.*;
import cn.iocoder.yudao.module.coal.dal.dataobject.productionplan.ProductionPlanDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.coal.dal.mysql.productionplan.ProductionPlanMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;

/**
 * 生产计划 Service 实现类
 *
 * @author 京京
 */
@Service
@Validated
public class ProductionPlanServiceImpl implements ProductionPlanService {

    @Resource
    private ProductionPlanMapper productionPlanMapper;

    @Override
    public Long createProductionPlan(ProductionPlanSaveReqVO createReqVO) {
        // 校验父计划ID的有效性
        validateParentProductionPlan(null, createReqVO.getParentId());
        // 校验计划名称的唯一性
        validateProductionPlanNameUnique(null, createReqVO.getParentId(), createReqVO.getName());

        // 插入
        ProductionPlanDO productionPlan = BeanUtils.toBean(createReqVO, ProductionPlanDO.class);
        productionPlanMapper.insert(productionPlan);

        // 返回
        return productionPlan.getId();
    }

    @Override
    public void updateProductionPlan(ProductionPlanSaveReqVO updateReqVO) {
        // 校验存在
        validateProductionPlanExists(updateReqVO.getId());
        // 校验父计划ID的有效性
        validateParentProductionPlan(updateReqVO.getId(), updateReqVO.getParentId());
        // 校验计划名称的唯一性
        validateProductionPlanNameUnique(updateReqVO.getId(), updateReqVO.getParentId(), updateReqVO.getName());

        // 更新
        ProductionPlanDO updateObj = BeanUtils.toBean(updateReqVO, ProductionPlanDO.class);
        productionPlanMapper.updateById(updateObj);
    }

    @Override
    public void deleteProductionPlan(Long id) {
        // 校验存在
        validateProductionPlanExists(id);
        // 校验是否有子生产计划
        if (productionPlanMapper.selectCountByParentId(id) > 0) {
            throw exception(PRODUCTION_PLAN_EXITS_CHILDREN);
        }
        // 删除
        productionPlanMapper.deleteById(id);
    }


    private void validateProductionPlanExists(Long id) {
        if (productionPlanMapper.selectById(id) == null) {
            throw exception(PRODUCTION_PLAN_NOT_EXISTS);
        }
    }

    private void validateParentProductionPlan(Long id, Long parentId) {
        if (parentId == null || ProductionPlanDO.PARENT_ID_ROOT.equals(parentId)) {
            return;
        }
        // 1. 不能设置自己为父生产计划
        if (Objects.equals(id, parentId)) {
            throw exception(PRODUCTION_PLAN_PARENT_ERROR);
        }
        // 2. 父生产计划不存在
        ProductionPlanDO parentProductionPlan = productionPlanMapper.selectById(parentId);
        if (parentProductionPlan == null) {
            throw exception(PRODUCTION_PLAN_PARENT_NOT_EXITS);
        }
        // 3. 递归校验父生产计划，如果父生产计划是自己的子生产计划，则报错，避免形成环路
        if (id == null) { // id 为空，说明新增，不需要考虑环路
            return;
        }
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            // 3.1 校验环路
            parentId = parentProductionPlan.getParentId();
            if (Objects.equals(id, parentId)) {
                throw exception(PRODUCTION_PLAN_PARENT_IS_CHILD);
            }
            // 3.2 继续递归下一级父生产计划
            if (parentId == null || ProductionPlanDO.PARENT_ID_ROOT.equals(parentId)) {
                break;
            }
            parentProductionPlan = productionPlanMapper.selectById(parentId);
            if (parentProductionPlan == null) {
                break;
            }
        }
    }

    private void validateProductionPlanNameUnique(Long id, Long parentId, String name) {
        ProductionPlanDO productionPlan = productionPlanMapper.selectByParentIdAndName(parentId, name);
        if (productionPlan == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的生产计划
        if (id == null) {
            throw exception(PRODUCTION_PLAN_NAME_DUPLICATE);
        }
        if (!Objects.equals(productionPlan.getId(), id)) {
            throw exception(PRODUCTION_PLAN_NAME_DUPLICATE);
        }
    }

    @Override
    public ProductionPlanDO getProductionPlan(Long id) {
        return productionPlanMapper.selectById(id);
    }

    @Override
    public List<ProductionPlanDO> getProductionPlanList(ProductionPlanListReqVO listReqVO) {
        return productionPlanMapper.selectList(listReqVO);
    }

    @Override
    @Transactional
    public void generateYearlyPlan(ProductionPlanSaveReqVO createReqVO) {
        // 1. 插入年度计划
        ProductionPlanDO yearPlan = BeanUtils.toBean(createReqVO, ProductionPlanDO.class);
        yearPlan.setPlanYear(createReqVO.getPlanYear()); // 显式设置年份，确保保存
        productionPlanMapper.insert(yearPlan); // 插入数据库，为了获得 id

        // 2. 批量插入后续计划
        List<ProductionPlanDO> batchList = new ArrayList<>();

        // 2.1 生成月度计划
        for (int i = 1; i <= 12; i++) {
            ProductionPlanDO monthPlan = generateSubPlan(yearPlan, yearPlan.getId(), i + "月", 12, 2); // planType = 2
            monthPlan.setPlanMonth(i);
            productionPlanMapper.insert(monthPlan); // 插入数据库，为了获得 id

            // 2.2 生成日计划
            int daysInMonth = java.time.YearMonth.of(yearPlan.getPlanYear(), i).lengthOfMonth();
            for (int j = 1; j <= daysInMonth; j++) {
                // 注意：除数使用该月的实际天数，保证日计划合计约等于月计划
                ProductionPlanDO dayPlan = generateSubPlan(monthPlan, monthPlan.getId(), j + "日", daysInMonth, 3); // planType = 3
                dayPlan.setPlanDate(LocalDate.of(yearPlan.getPlanYear(), monthPlan.getPlanMonth(), j));
                productionPlanMapper.insert(dayPlan); // 插入数据库，为了获得 id

                // 2.3 生成班次计划
                batchList.add(generateSubPlan(dayPlan, dayPlan.getId(), "早班", 3, 4)); // planType = 4
                batchList.add(generateSubPlan(dayPlan, dayPlan.getId(), "中班", 3, 4)); // planType = 4
                batchList.add(generateSubPlan(dayPlan, dayPlan.getId(), "晚班", 3, 4)); // planType = 4
            }
        }

        // 批量插入所有班次计划
        for (ProductionPlanDO plan : batchList) {
            productionPlanMapper.insert(plan);
        }
    }

    @Override
    public void deleteProductionPlanByYear(Integer year) {
        productionPlanMapper.deleteByYear(year);
    }


    @Override
    public void physicalDeleteProductionPlanByYear(Integer year) {
        productionPlanMapper.physicalDeleteByYear(year);
    }

    /**
     * 生成子计划
     *
     * @param parentPlan 父计划
     * @param parentId 父计划编号
     * @param name 计划名称
     * @param subMultiple 子计划的拆分倍数
     * @param planType 计划类型
     * @return 子计划
     */
    private ProductionPlanDO generateSubPlan(ProductionPlanDO parentPlan, Long parentId, String name, int subMultiple, Integer planType) {
        ProductionPlanDO subPlan = new ProductionPlanDO();
        subPlan.setName(parentPlan.getName() + "-" + name);
        subPlan.setParentId(parentId);
        subPlan.setPlanType(planType);
        subPlan.setStatus(parentPlan.getStatus());
        subPlan.setPlanYear(parentPlan.getPlanYear()); // 继承父计划的年份

        // 将“量”相关的字段，进行除法
        subPlan.setRawCoalPlan(parentPlan.getRawCoalPlan().divide(BigDecimal.valueOf(subMultiple), 2, RoundingMode.HALF_UP));
        subPlan.setFineCoalPlan(parentPlan.getFineCoalPlan().divide(BigDecimal.valueOf(subMultiple), 2, RoundingMode.HALF_UP));
        subPlan.setGranularCoalPlan(parentPlan.getGranularCoalPlan().divide(BigDecimal.valueOf(subMultiple), 2, RoundingMode.HALF_UP));
        subPlan.setLargeBlockCoalPlan(parentPlan.getLargeBlockCoalPlan().divide(BigDecimal.valueOf(subMultiple), 2, RoundingMode.HALF_UP));
        subPlan.setMediumBlockCoalPlan(parentPlan.getMediumBlockCoalPlan().divide(BigDecimal.valueOf(subMultiple), 2, RoundingMode.HALF_UP));
        subPlan.setSmallBlockCoalPlan(parentPlan.getSmallBlockCoalPlan().divide(BigDecimal.valueOf(subMultiple), 2, RoundingMode.HALF_UP));
        subPlan.setMiddlingCoalPlan(parentPlan.getMiddlingCoalPlan().divide(BigDecimal.valueOf(subMultiple), 2, RoundingMode.HALF_UP));
        subPlan.setSlimePlan(parentPlan.getSlimePlan().divide(BigDecimal.valueOf(subMultiple), 2, RoundingMode.HALF_UP));
        subPlan.setGanguePlan(parentPlan.getGanguePlan().divide(BigDecimal.valueOf(subMultiple), 2, RoundingMode.HALF_UP));

        // 将“灰分”相关的字段，直接复制
        subPlan.setFineCoalAsh(parentPlan.getFineCoalAsh());
        subPlan.setGranularCoalAsh(parentPlan.getGranularCoalAsh());
        subPlan.setLargeBlockCoalAsh(parentPlan.getLargeBlockCoalAsh());
        subPlan.setMediumBlockCoalAsh(parentPlan.getMediumBlockCoalAsh());
        subPlan.setSmallBlockCoalAsh(parentPlan.getSmallBlockCoalAsh());
        subPlan.setMiddlingCoalAsh(parentPlan.getMiddlingCoalAsh());
        return subPlan;
    }

}