package cn.iocoder.yudao.module.oa.service.regular;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.controller.admin.regular.vo.OaRegularApplyPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.regular.vo.OaRegularApplySaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.regular.vo.OaRegularApplySubmitReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.regular.OaRegularApplyDO;
import cn.iocoder.yudao.module.oa.dal.mysql.regular.OaRegularApplyMapper;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 转正申请 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaRegularApplyServiceImpl implements OaRegularApplyService {

    @Resource
    private OaRegularApplyMapper regularApplyMapper;
    @Resource
    private BpmProcessInstanceApi processInstanceApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRegularApply(OaRegularApplySaveReqVO saveReqVO) {
        // 1. 转换试用内容并计算天数
        OaRegularApplyDO regularApply = BeanUtils.toBean(saveReqVO, OaRegularApplyDO.class)
                .setDays(LocalDateTimeUtils.getDaysBetweenCeiling(saveReqVO.getStartTime(), saveReqVO.getEndTime()));

        // 2. 保存草稿
        regularApply.setId(null).setStatus(BpmProcessInstanceStatusEnum.NOT_START.getStatus());
        regularApplyMapper.insert(regularApply);
        return regularApply.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRegularApply(OaRegularApplySaveReqVO saveReqVO, Long userId) {
        // 1. 校验申请可修改
        validateRegularApplyEditable(saveReqVO.getId(), userId);

        // 2. 更新转正申请
        OaRegularApplyDO regularApply = BeanUtils.toBean(saveReqVO, OaRegularApplyDO.class)
                .setDays(LocalDateTimeUtils.getDaysBetweenCeiling(saveReqVO.getStartTime(), saveReqVO.getEndTime()));
        regularApplyMapper.updateById(regularApply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRegularApply(OaRegularApplySubmitReqVO submitReqVO, Long userId) {
        // 1. 校验申请可提交
        OaRegularApplyDO regularApply = validateRegularApplyEditable(submitReqVO.getId(), userId);

        // 2. 更新为审批中
        regularApplyMapper.updateById(new OaRegularApplyDO().setId(regularApply.getId())
                .setStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus()));

        // 3.1 发起审批
        Map<String, Object> variables = new HashMap<>();
        variables.put("days", regularApply.getDays());
        String processInstanceId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(BpmModelConstants.REGULAR_APPLY)
                        .setBusinessKey(regularApply.getId().toString()).setVariables(variables)
                        .setStartUserSelectAssignees(submitReqVO.getStartUserSelectAssignees()));
        // 3.2 绑定流程编号
        regularApplyMapper.updateById(new OaRegularApplyDO().setId(regularApply.getId()).setProcessInstanceId(processInstanceId));
    }

    @Override
    public OaRegularApplyDO getRegularApply(Long id) {
        return regularApplyMapper.selectById(id);
    }

    @Override
    public PageResult<OaRegularApplyDO> getRegularApplyPage(Long userId, OaRegularApplyPageReqVO pageReqVO) {
        return regularApplyMapper.selectPage(userId, pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRegularApplyStatus(Long id, Integer status) {
        // 1. 校验申请存在
        validateRegularApplyExists(id);

        // 2. 更新审批状态
        regularApplyMapper.updateById(new OaRegularApplyDO().setId(id).setStatus(status));
    }

    /**
     * 校验申请可修改或提交
     *
     * @param id 申请编号
     * @param userId 当前用户编号
     * @return 本人的未提交申请
     */
    private OaRegularApplyDO validateRegularApplyEditable(Long id, Long userId) {
        // 1. 查询申请
        OaRegularApplyDO regularApply = regularApplyMapper.selectById(id);
        if (regularApply == null) {
            throw exception(APPLY_NOT_EXISTS);
        }
        // 2. 校验本人归属
        if (ObjUtil.notEqual(regularApply.getCreator(), userId.toString())) {
            throw exception(APPLY_ACCESS_DENIED);
        }
        // 3. 只有未提交草稿允许修改或提交
        if (ObjUtil.notEqual(regularApply.getStatus(), BpmProcessInstanceStatusEnum.NOT_START.getStatus())) {
            throw exception(APPLY_STATUS_INVALID);
        }
        return regularApply;
    }

    /**
     * 校验转正申请存在
     *
     * @param id 申请编号
     * @return 申请
     */
    private OaRegularApplyDO validateRegularApplyExists(Long id) {
        OaRegularApplyDO regularApply = regularApplyMapper.selectById(id);
        if (regularApply == null) {
            throw exception(APPLY_NOT_EXISTS);
        }
        return regularApply;
    }

}
