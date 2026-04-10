package cn.iocoder.yudao.module.mes.service.cal.plan;

import cn.hutool.core.lang.Pair;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.shift.MesCalPlanShiftPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.shift.MesCalPlanShiftSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanShiftDO;
import cn.iocoder.yudao.module.mes.dal.mysql.cal.plan.MesCalPlanShiftMapper;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalShiftTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import cn.hutool.core.collection.CollUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 计划班次 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesCalPlanShiftServiceImpl implements MesCalPlanShiftService {

    @Resource
    private MesCalPlanShiftMapper planShiftMapper;
    @Resource
    @Lazy
    private MesCalPlanService planService;

    @Override
    public Long createPlanShift(MesCalPlanShiftSaveReqVO createReqVO) {
        // 校验计划未确认
        planService.validatePlanPrepare(createReqVO.getPlanId());
        // 校验班次数量限制
        validatePlanShiftCount(createReqVO.getPlanId());

        // 插入
        MesCalPlanShiftDO planShift = BeanUtils.toBean(createReqVO, MesCalPlanShiftDO.class);
        planShiftMapper.insert(planShift);
        return planShift.getId();
    }

    @Override
    public void updatePlanShift(MesCalPlanShiftSaveReqVO updateReqVO) {
        // 校验存在
        MesCalPlanShiftDO existShift = validatePlanShiftExists(updateReqVO.getId());
        // 校验计划未确认
        planService.validatePlanPrepare(existShift.getPlanId());
        // 更新
        MesCalPlanShiftDO updateObj = BeanUtils.toBean(updateReqVO, MesCalPlanShiftDO.class);
        planShiftMapper.updateById(updateObj);
    }

    @Override
    public void deletePlanShift(Long id) {
        // 校验存在
        MesCalPlanShiftDO existShift = validatePlanShiftExists(id);
        // 校验计划未确认
        planService.validatePlanPrepare(existShift.getPlanId());
        // 删除
        planShiftMapper.deleteById(id);
    }

    private MesCalPlanShiftDO validatePlanShiftExists(Long id) {
        MesCalPlanShiftDO shift = planShiftMapper.selectById(id);
        if (shift == null) {
            throw exception(CAL_PLAN_SHIFT_NOT_EXISTS);
        }
        return shift;
    }

    /**
     * 校验班次数量是否超限
     * <p>
     * 通过 planId 查询已有班次数量，根据排班计划的轮班方式校验
     */
    private void validatePlanShiftCount(Long planId) {
        MesCalPlanDO plan = planService.getPlan(planId);
        if (plan == null) {
            return;
        }
        Long count = planShiftMapper.selectCountByPlanId(planId);
        MesCalShiftTypeEnum shiftTypeEnum = MesCalShiftTypeEnum.valueOf(plan.getShiftType());
        if (count >= shiftTypeEnum.getShiftCount()) {
            throw exception(CAL_PLAN_SHIFT_COUNT_EXCEED);
        }
    }

    @Override
    public MesCalPlanShiftDO getPlanShift(Long id) {
        return planShiftMapper.selectById(id);
    }

    @Override
    public PageResult<MesCalPlanShiftDO> getPlanShiftPage(MesCalPlanShiftPageReqVO pageReqVO) {
        return planShiftMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesCalPlanShiftDO> getPlanShiftListByPlanId(Long planId) {
        return planShiftMapper.selectListByPlanId(planId);
    }

    @Override
    public Long getPlanShiftCountByPlanId(Long planId) {
        return planShiftMapper.selectCountByPlanId(planId);
    }

    @Override
    public void addDefaultPlanShift(Long planId, Integer shiftType) {
        // 根据轮班方式，通过枚举配置生成默认班次
        MesCalShiftTypeEnum shiftTypeEnum = MesCalShiftTypeEnum.valueOf(shiftType);
        List<Pair<String, String[]>> shifts = shiftTypeEnum.getShifts();
        for (int i = 0; i < shifts.size(); i++) {
            Pair<String, String[]> shift = shifts.get(i);
            MesCalPlanShiftDO planShiftDO = MesCalPlanShiftDO.builder()
                    .planId(planId).sort(i + 1).name(shift.getKey())
                    .startTime(shift.getValue()[0]).endTime(shift.getValue()[1])
                    .build();
            planShiftMapper.insert(planShiftDO);
        }
    }

    @Override
    public List<MesCalPlanShiftDO> getPlanShiftList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return planShiftMapper.selectByIds(ids);
    }

    @Override
    public void deletePlanShiftByPlanId(Long planId) {
        planShiftMapper.deleteByPlanId(planId);
    }

}
