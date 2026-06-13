package cn.iocoder.yudao.module.mes.service.dv.checkplan;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan.vo.MesDvCheckPlanPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan.vo.MesDvCheckPlanSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkplan.MesDvCheckPlanDO;
import cn.iocoder.yudao.module.mes.dal.mysql.dv.checkplan.MesDvCheckPlanMapper;
import cn.iocoder.yudao.module.mes.enums.dv.MesDvCheckPlanStatusEnum;
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
 * MES 点检保养方案 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesDvCheckPlanServiceImpl implements MesDvCheckPlanService {

    @Resource
    private MesDvCheckPlanMapper checkPlanMapper;

    @Resource
    @Lazy
    private MesDvCheckPlanMachineryService checkPlanMachineryService;
    @Resource
    @Lazy
    private MesDvCheckPlanSubjectService checkPlanSubjectService;

    @Override
    public Long createCheckPlan(MesDvCheckPlanSaveReqVO createReqVO) {
        // 1. 校验编码唯一
        validateCheckPlanCodeUnique(null, createReqVO.getCode());

        // 2. 插入方案
        MesDvCheckPlanDO checkPlan = BeanUtils.toBean(createReqVO, MesDvCheckPlanDO.class);
        checkPlan.setStatus(MesDvCheckPlanStatusEnum.PREPARE.getStatus()); // 默认草稿
        checkPlanMapper.insert(checkPlan);
        return checkPlan.getId();
    }

    @Override
    public void updateCheckPlan(MesDvCheckPlanSaveReqVO updateReqVO) {
        // 1.1 校验存在
        MesDvCheckPlanDO existPlan = doValidateCheckPlanExists(updateReqVO.getId());
        // 1.2 校验草稿状态
        if (ObjUtil.notEqual(MesDvCheckPlanStatusEnum.PREPARE.getStatus(), existPlan.getStatus())) {
            throw exception(DV_CHECK_PLAN_NOT_PREPARE);
        }
        // 1.3 校验编码唯一
        validateCheckPlanCodeUnique(updateReqVO.getId(), updateReqVO.getCode());

        // 2. 更新
        updateReqVO.setStatus(null); // 不允许通过 update 修改状态
        MesDvCheckPlanDO updateObj = BeanUtils.toBean(updateReqVO, MesDvCheckPlanDO.class);
        checkPlanMapper.updateById(updateObj);
    }

    @Override
    public void enableCheckPlan(Long id) {
        // 1.1 校验存在 + 校验草稿状态
        validateCheckPlanPrepare(id);
        // 1.2 校验至少关联一台设备
        Long machineryCount = checkPlanMachineryService.getCheckPlanMachineryCountByPlanId(id);
        if (machineryCount <= 0) {
            throw exception(DV_CHECK_PLAN_NO_MACHINERY);
        }
        // 1.3 校验至少关联一个项目
        Long subjectCount = checkPlanSubjectService.getCheckPlanSubjectCountByPlanId(id);
        if (subjectCount <= 0) {
            throw exception(DV_CHECK_PLAN_NO_SUBJECT);
        }

        // 2. 更新状态为已启用
        checkPlanMapper.updateById(new MesDvCheckPlanDO().setId(id)
                .setStatus(MesDvCheckPlanStatusEnum.ENABLED.getStatus()));
    }

    @Override
    public void disableCheckPlan(Long id) {
        // 1. 校验存在 + 校验已启用状态
        MesDvCheckPlanDO plan = doValidateCheckPlanExists(id);
        if (ObjUtil.notEqual(MesDvCheckPlanStatusEnum.ENABLED.getStatus(), plan.getStatus())) {
            throw exception(DV_CHECK_PLAN_NOT_ENABLED);
        }

        // 2. 更新状态为草稿
        checkPlanMapper.updateById(new MesDvCheckPlanDO().setId(id)
                .setStatus(MesDvCheckPlanStatusEnum.PREPARE.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCheckPlan(Long id) {
        // 1. 校验存在 + 校验草稿状态
        validateCheckPlanPrepare(id);

        // 2.1 级联删除设备关联 + 项目关联
        checkPlanMachineryService.deleteByPlanId(id);
        checkPlanSubjectService.deleteByPlanId(id);
        // 2.2 删除方案
        checkPlanMapper.deleteById(id);
    }

    @Override
    public MesDvCheckPlanDO validateCheckPlanPrepare(Long planId) {
        MesDvCheckPlanDO plan = doValidateCheckPlanExists(planId);
        if (ObjUtil.notEqual(MesDvCheckPlanStatusEnum.PREPARE.getStatus(), plan.getStatus())) {
            throw exception(DV_CHECK_PLAN_NOT_PREPARE);
        }
        return plan;
    }

    @Override
    public MesDvCheckPlanDO getCheckPlan(Long id) {
        return checkPlanMapper.selectById(id);
    }

    @Override
    public PageResult<MesDvCheckPlanDO> getCheckPlanPage(MesDvCheckPlanPageReqVO pageReqVO) {
        return checkPlanMapper.selectPage(pageReqVO);
    }

    @Override
    public void validateCheckPlanExists(Long id) {
        if (checkPlanMapper.selectById(id) == null) {
            throw exception(DV_CHECK_PLAN_NOT_EXISTS);
        }
    }

    private MesDvCheckPlanDO doValidateCheckPlanExists(Long id) {
        MesDvCheckPlanDO plan = checkPlanMapper.selectById(id);
        if (plan == null) {
            throw exception(DV_CHECK_PLAN_NOT_EXISTS);
        }
        return plan;
    }

    @Override
    public MesDvCheckPlanDO validateCheckPlanExistsAndType(Long id, Integer type) {
        MesDvCheckPlanDO plan = doValidateCheckPlanExists(id);
        // 校验类型匹配
        if (ObjUtil.notEqual(plan.getType(), type)) {
            throw exception(DV_CHECK_PLAN_TYPE_MISMATCH);
        }
        // 校验方案已启用
        if (ObjUtil.notEqual(MesDvCheckPlanStatusEnum.ENABLED.getStatus(), plan.getStatus())) {
            throw exception(DV_CHECK_PLAN_NOT_ENABLED_FOR_RECORD);
        }
        return plan;
    }

    private void validateCheckPlanCodeUnique(Long id, String code) {
        MesDvCheckPlanDO plan = checkPlanMapper.selectByCode(code);
        if (plan == null) {
            return;
        }
        if (ObjUtil.notEqual(plan.getId(), id)) {
            throw exception(DV_CHECK_PLAN_CODE_DUPLICATE);
        }
    }

    @Override
    public List<MesDvCheckPlanDO> getCheckPlanList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return checkPlanMapper.selectByIds(ids);
    }

}
