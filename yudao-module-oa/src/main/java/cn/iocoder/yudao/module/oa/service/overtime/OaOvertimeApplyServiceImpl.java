package cn.iocoder.yudao.module.oa.service.overtime;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.controller.admin.overtime.vo.OaOvertimeApplyPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.overtime.vo.OaOvertimeApplySaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.overtime.vo.OaOvertimeApplySubmitReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.overtime.OaOvertimeApplyDO;
import cn.iocoder.yudao.module.oa.dal.mysql.overtime.OaOvertimeApplyMapper;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 加班申请 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaOvertimeApplyServiceImpl implements OaOvertimeApplyService {

    @Resource
    private OaOvertimeApplyMapper overtimeApplyMapper;
    @Resource
    private BpmProcessInstanceApi processInstanceApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOvertimeApply(OaOvertimeApplySaveReqVO saveReqVO) {
        // TODO DONE @AI：天数计算复用 LocalDateTimeUtils，仍保留一位小数并四舍五入。
        // 1. 转换申请内容并计算天数
        OaOvertimeApplyDO overtimeApply = BeanUtils.toBean(saveReqVO, OaOvertimeApplyDO.class)
                .setDays(LocalDateTimeUtils.getDaysBetween(saveReqVO.getStartTime(), saveReqVO.getEndTime(), 1));

        // 2. 保存草稿
        overtimeApply.setId(null).setStatus(BpmProcessInstanceStatusEnum.NOT_START.getStatus());
        overtimeApplyMapper.insert(overtimeApply);
        return overtimeApply.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOvertimeApply(OaOvertimeApplySaveReqVO saveReqVO, Long userId) {
        // 1. 校验申请可修改
        validateOvertimeApplyEditable(saveReqVO.getId(), userId);

        // 2. 更新加班申请
        OaOvertimeApplyDO overtimeApply = BeanUtils.toBean(saveReqVO, OaOvertimeApplyDO.class)
                .setDays(LocalDateTimeUtils.getDaysBetween(saveReqVO.getStartTime(), saveReqVO.getEndTime(), 1));
        overtimeApplyMapper.updateById(overtimeApply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitOvertimeApply(OaOvertimeApplySubmitReqVO submitReqVO, Long userId) {
        // 1. 校验申请可提交
        OaOvertimeApplyDO overtimeApply = validateOvertimeApplyEditable(submitReqVO.getId(), userId);

        // 2. 更新为审批中
        overtimeApplyMapper.updateById(new OaOvertimeApplyDO().setId(overtimeApply.getId())
                .setStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus()));

        // 3.1 发起审批
        Map<String, Object> variables = new HashMap<>();
        variables.put("days", overtimeApply.getDays());
        String processInstanceId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(BpmModelConstants.OVERTIME_APPLY)
                        .setBusinessKey(overtimeApply.getId().toString()).setVariables(variables)
                        .setStartUserSelectAssignees(submitReqVO.getStartUserSelectAssignees()));
        // 3.2 绑定流程编号
        overtimeApplyMapper.updateById(new OaOvertimeApplyDO().setId(overtimeApply.getId()).setProcessInstanceId(processInstanceId));
    }

    @Override
    public OaOvertimeApplyDO getOvertimeApply(Long id) {
        return overtimeApplyMapper.selectById(id);
    }

    @Override
    public PageResult<OaOvertimeApplyDO> getOvertimeApplyPage(Long userId, OaOvertimeApplyPageReqVO pageReqVO) {
        return overtimeApplyMapper.selectPage(userId, pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOvertimeApplyStatus(Long id, Integer status) {
        // 1. 校验申请存在
        validateOvertimeApplyExists(id);

        // 2. 更新审批状态
        overtimeApplyMapper.updateById(new OaOvertimeApplyDO().setId(id).setStatus(status));
    }

    /**
     * 校验申请可修改或提交
     *
     * @param id 申请编号
     * @param userId 当前用户编号
     * @return 本人的未提交申请
     */
    private OaOvertimeApplyDO validateOvertimeApplyEditable(Long id, Long userId) {
        // 1. 查询申请
        OaOvertimeApplyDO overtimeApply = overtimeApplyMapper.selectById(id);
        if (overtimeApply == null) {
            throw exception(APPLY_NOT_EXISTS);
        }
        // 2. 校验本人归属
        if (ObjUtil.notEqual(overtimeApply.getCreator(), userId.toString())) {
            throw exception(APPLY_ACCESS_DENIED);
        }
        // 3. 只有未提交草稿允许修改或提交
        if (ObjUtil.notEqual(overtimeApply.getStatus(), BpmProcessInstanceStatusEnum.NOT_START.getStatus())) {
            throw exception(APPLY_STATUS_INVALID);
        }
        return overtimeApply;
    }

    /**
     * 校验加班申请存在
     *
     * @param id 申请编号
     * @return 申请
     */
    private OaOvertimeApplyDO validateOvertimeApplyExists(Long id) {
        OaOvertimeApplyDO overtimeApply = overtimeApplyMapper.selectById(id);
        if (overtimeApply == null) {
            throw exception(APPLY_NOT_EXISTS);
        }
        return overtimeApply;
    }

}
