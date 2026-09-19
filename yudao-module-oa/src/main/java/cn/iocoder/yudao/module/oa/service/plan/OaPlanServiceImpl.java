package cn.iocoder.yudao.module.oa.service.plan;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.module.oa.controller.admin.plan.vo.OaPlanCommentReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.plan.vo.OaPlanPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.plan.vo.OaPlanSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.plan.OaPlanDO;
import cn.iocoder.yudao.module.oa.dal.mysql.plan.OaPlanMapper;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.PLAN_ACCESS_DENIED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.PLAN_NOT_EXISTS;

/**
 * OA 工作计划 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaPlanServiceImpl implements OaPlanService {

    @Resource
    private OaPlanMapper planMapper;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public Long createPlan(OaPlanSaveReqVO createReqVO) {
        // 1. 转换工作计划内容
        OaPlanDO plan = BeanUtils.toBean(createReqVO, OaPlanDO.class);

        // 2. 保存记录
        planMapper.insert(plan);
        return plan.getId();
    }

    @Override
    public void updatePlan(OaPlanSaveReqVO updateReqVO, Long userId) {
        // 1. 校验工作计划属于当前用户
        validatePlanOwner(updateReqVO.getId(), userId);

        // 2. 更新工作计划
        planMapper.updateById(BeanUtils.toBean(updateReqVO, OaPlanDO.class));
    }

    @Override
    public void deletePlan(Long id, Long userId) {
        // 1. 校验工作计划属于当前用户
        validatePlanOwner(id, userId);

        // 2. 删除工作计划
        planMapper.deleteById(id);
    }

    @Override
    public OaPlanDO getPlan(Long id, Long userId) {
        return validatePlanOwner(id, userId);
    }

    @Override
    public PageResult<OaPlanDO> getPlanPage(OaPlanPageReqVO pageReqVO, Long userId) {
        return planMapper.selectPage(pageReqVO, userId);
    }

    @Override
    public void addPlanComment(Long id, String comment, Long userId) {
        // 1.1 校验工作计划存在
        OaPlanDO plan = validatePlanExists(id);
        // 1.2 校验计划属于当前用户的管理范围
        List<AdminUserRespDTO> subordinateUsers = adminUserApi.getUserListBySubordinate(userId);
        if (!convertSet(subordinateUsers, AdminUserRespDTO::getId).contains(NumberUtils.parseLong(plan.getCreator()))) {
            throw exception(PLAN_ACCESS_DENIED);
        }

        // 2. 追加工作计划点评
        String updatedComment = StrUtil.isEmpty(plan.getComment()) ? comment
                : plan.getComment() + System.lineSeparator() + comment;
        ValidationUtils.validate(new OaPlanCommentReqVO().setId(id).setComment(updatedComment));
        planMapper.updateById(new OaPlanDO().setId(id).setComment(updatedComment));
    }

    @Override
    public Map<Long, OaPlanDO> getLatestPlanMap(Collection<Long> userIds, Integer type, LocalDateTime[] createTime) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        // 1. 查询指定成员在统计周期内的工作计划
        List<OaPlanDO> plans = planMapper.selectListByCreatorsAndTypeAndCreateTime(
                convertList(userIds, String::valueOf), type, createTime);
        // 2. 每位成员只保留创建时间最新的工作计划
        return convertMap(plans, plan -> NumberUtils.parseLong(plan.getCreator()));
    }

    /**
     * 校验工作计划属于指定用户
     *
     * @param id 计划编号
     * @param userId 用户编号
     * @return 工作计划
     */
    private OaPlanDO validatePlanOwner(Long id, Long userId) {
        OaPlanDO plan = validatePlanExists(id);
        if (ObjUtil.notEqual(plan.getCreator(), userId.toString())) {
            throw exception(PLAN_ACCESS_DENIED);
        }
        return plan;
    }

    /**
     * 校验工作计划是否存在
     *
     * @param id 计划编号
     * @return 工作计划
     */
    private OaPlanDO validatePlanExists(Long id) {
        OaPlanDO plan = planMapper.selectById(id);
        if (plan == null) {
            throw exception(PLAN_NOT_EXISTS);
        }
        return plan;
    }

}
