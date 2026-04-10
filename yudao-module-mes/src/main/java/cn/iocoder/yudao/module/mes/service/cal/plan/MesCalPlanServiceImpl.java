package cn.iocoder.yudao.module.mes.service.cal.plan;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.MesCalPlanPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.MesCalPlanSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanDO;
import cn.iocoder.yudao.module.mes.dal.mysql.cal.plan.MesCalPlanMapper;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalPlanStatusEnum;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalShiftTypeEnum;
import cn.iocoder.yudao.module.mes.service.cal.team.MesCalTeamShiftService;
import cn.hutool.core.util.ObjUtil;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 排班计划 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesCalPlanServiceImpl implements MesCalPlanService {

    @Resource
    private MesCalPlanMapper planMapper;
    @Resource
    @Lazy
    private MesCalPlanShiftService planShiftService;
    @Resource
    @Lazy
    private MesCalPlanTeamService planTeamService;
    @Resource
    @Lazy
    private MesCalTeamShiftService teamShiftService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPlan(MesCalPlanSaveReqVO createReqVO) {
        // 1. 校验编码唯一
        validatePlanCodeUnique(null, createReqVO.getCode());

        // 2. 插入计划
        MesCalPlanDO plan = BeanUtils.toBean(createReqVO, MesCalPlanDO.class);
        plan.setStatus(MesCalPlanStatusEnum.PREPARE.getStatus()); // 默认草稿
        planMapper.insert(plan);

        // 3. 自动生成默认班次
        if (plan.getShiftType() != null) {
            planShiftService.addDefaultPlanShift(plan.getId(), plan.getShiftType());
        }
        return plan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePlan(MesCalPlanSaveReqVO updateReqVO) {
        // 1.1 校验存在
        MesCalPlanDO existPlan = validatePlanExists(updateReqVO.getId());
        // 1.2 校验计划未确认（已确认不允许编辑）
        if (MesCalPlanStatusEnum.CONFIRMED.getStatus().equals(existPlan.getStatus())) {
            throw exception(CAL_PLAN_NOT_PREPARE);
        }
        // 1.3 校验编码唯一
        validatePlanCodeUnique(updateReqVO.getId(), updateReqVO.getCode());

        // 2. 更新
        updateReqVO.setStatus(null); // 不允许通过 update 修改状态，状态变更走专用接口
        MesCalPlanDO updateObj = BeanUtils.toBean(updateReqVO, MesCalPlanDO.class);
        planMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPlan(Long id) {
        // 1.1 校验存在 + 校验状态为草稿
        MesCalPlanDO plan = validatePlanPrepare(id);
        // 1.2 校验班组数量与轮班方式匹配
        validateTeamCountForConfirm(id, plan.getShiftType());

        // 2. 更新状态为已确认
        MesCalPlanDO updateObj = new MesCalPlanDO();
        updateObj.setId(id);
        updateObj.setStatus(MesCalPlanStatusEnum.CONFIRMED.getStatus());
        planMapper.updateById(updateObj);

        // 3. 生成班组排班记录
        teamShiftService.generateTeamShiftRecords(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Long id) {
        // 1.1 校验存在 + 校验状态为草稿
        validatePlanPrepare(id);

        // 2.1 级联删除班次 + 班组关联
        planShiftService.deletePlanShiftByPlanId(id);
        planTeamService.deleteByPlanId(id);
        // 2.2 删除计划
        planMapper.deleteById(id);
    }

    @Override
    public MesCalPlanDO getPlan(Long id) {
        return planMapper.selectById(id);
    }

    @Override
    public List<MesCalPlanDO> getPlanList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return planMapper.selectByIds(ids);
    }

    @Override
    public PageResult<MesCalPlanDO> getPlanPage(MesCalPlanPageReqVO pageReqVO) {
        return planMapper.selectPage(pageReqVO);
    }

    @Override
    public MesCalPlanDO validatePlanPrepare(Long planId) {
        MesCalPlanDO plan = validatePlanExists(planId);
        if (ObjUtil.notEqual(MesCalPlanStatusEnum.PREPARE.getStatus(), plan.getStatus())) {
            throw exception(CAL_PLAN_NOT_PREPARE);
        }
        return plan;
    }

    private MesCalPlanDO validatePlanExists(Long id) {
        MesCalPlanDO plan = planMapper.selectById(id);
        if (plan == null) {
            throw exception(CAL_PLAN_NOT_EXISTS);
        }
        return plan;
    }

    private void validatePlanCodeUnique(Long id, String code) {
        MesCalPlanDO plan = planMapper.selectByCode(code);
        if (plan == null) {
            return;
        }
        if (id == null || !id.equals(plan.getId())) {
            throw exception(CAL_PLAN_CODE_DUPLICATE);
        }
    }

    /**
     * 确认排班计划时，校验班组数量是否与轮班方式匹配
     */
    private void validateTeamCountForConfirm(Long planId, Integer shiftType) {
        Long teamCount = planTeamService.getPlanTeamCountByPlanId(planId);
        MesCalShiftTypeEnum shiftTypeEnum = MesCalShiftTypeEnum.valueOf(shiftType);
        if (teamCount < shiftTypeEnum.getRequiredTeamCount()) {
            throw exception(CAL_PLAN_TEAM_COUNT_NOT_MATCH);
        }
    }

}
