package cn.iocoder.yudao.module.oa.service.travel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.controller.admin.travel.vo.apply.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.travel.OaTravelApplyDO;
import cn.iocoder.yudao.module.oa.dal.mysql.travel.OaTravelApplyMapper;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import cn.iocoder.yudao.module.oa.service.attendance.OaAttendanceService;
import cn.iocoder.yudao.module.oa.enums.attendance.OaAttendanceTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.BpmModelConstants.*;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 出差申请 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaTravelApplyServiceImpl implements OaTravelApplyService {

    @Resource
    @Lazy
    private OaAttendanceService attendanceService;

    @Resource
    private OaTravelApplyMapper travelApplyMapper;

    @Resource
    private OaNoRedisDAO noRedisDAO;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private BpmProcessInstanceApi processInstanceApi;

    @Override
    public Map<Long, Integer> getApprovedTravelDaysMap(Collection<Long> userIds, LocalDateTime[] startTime) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        return travelApplyMapper.selectDaysMapByCreatorsAndStatusAndStartTime(
                userIds, BpmProcessInstanceStatusEnum.APPROVE.getStatus(), startTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTravelApply(OaTravelApplySaveReqVO reqVO, Long userId) {
        // 1.1 查询并校验申请人
        AdminUserRespDTO user = adminUserApi.validateUser(userId);
        // 1.2 生成单号并校验唯一性
        String no = noRedisDAO.generate(OaNoRedisDAO.TRAVEL_APPLY_NO_PREFIX);
        if (travelApplyMapper.selectByNo(no) != null) {
            throw exception(TRAVEL_NO_DUPLICATE);
        }

        // 2. 创建草稿
        OaTravelApplyDO record = BeanUtils.toBean(reqVO, OaTravelApplyDO.class)
                .setDays(LocalDateTimeUtils.getDaysBetweenCeiling(reqVO.getStartTime(), reqVO.getEndTime())).setNo(no)
                .setDeptId(user.getDeptId()).setStatus(BpmProcessInstanceStatusEnum.NOT_START.getStatus())
                .setReimburseStatus(false);
        travelApplyMapper.insert(record);
        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTravelApply(OaTravelApplySaveReqVO reqVO, Long userId) {
        // 1.1 查询并校验单据存在
        OaTravelApplyDO record = validateTravelApplyExists(reqVO.getId());
        // 1.2 校验本人归属
        validateTravelApplyOwner(record, userId);
        // 1.3 校验可编辑状态
        validateTravelApplyEditable(record);

        // 2. 更新单据
        OaTravelApplyDO update = BeanUtils.toBean(reqVO, OaTravelApplyDO.class)
                .setDays(LocalDateTimeUtils.getDaysBetweenCeiling(reqVO.getStartTime(), reqVO.getEndTime()));
        travelApplyMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitTravelApply(OaTravelApplySubmitReqVO submitReqVO, Long userId) {
        // 1.1 查询并校验单据存在
        OaTravelApplyDO record = validateTravelApplyExists(submitReqVO.getId());
        // 1.2 校验本人归属
        validateTravelApplyOwner(record, userId);
        // 1.3 校验可提交状态
        validateTravelApplyEditable(record);

        // 2. 更新为审批中，兼容流程创建时的同步回调
        travelApplyMapper.updateById(new OaTravelApplyDO().setId(record.getId())
                .setStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus()).setProcessInstanceId(""));

        // 3.1 发起审批
        Map<String, Object> variables = new HashMap<>();
        variables.put("days", record.getDays());
        String processInstanceId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(TRAVEL_APPLY)
                        .setBusinessKey(record.getId().toString()).setVariables(variables)
                        .setStartUserSelectAssignees(submitReqVO.getStartUserSelectAssignees()));
        // 3.2 绑定流程编号
        travelApplyMapper.updateById(new OaTravelApplyDO().setId(record.getId()).setProcessInstanceId(processInstanceId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTravelApply(Long id, Long userId) {
        // 1.1 校验单据存在
        OaTravelApplyDO record = validateTravelApplyExists(id);
        // 1.2 校验本人归属
        validateTravelApplyOwner(record, userId);
        // 1.3 仅审批中可以撤回
        if (ObjUtil.notEqual(record.getStatus(), BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
            throw exception(TRAVEL_STATUS_INVALID);
        }

        // 2. 使用 BPM 取消流程，由事件回写单据状态
        processInstanceApi.cancelProcessInstanceByStartUser(userId, record.getProcessInstanceId(), "申请人撤回出差申请");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTravelApply(Long id, Long userId) {
        // 1.1 校验单据存在
        OaTravelApplyDO record = validateTravelApplyExists(id);
        // 1.2 校验本人归属
        validateTravelApplyOwner(record, userId);
        // 1.3 校验可删除状态
        validateTravelApplyEditable(record);

        // 2. 删除本人单据
        travelApplyMapper.deleteById(id);
    }

    @Override
    public OaTravelApplyDO getTravelApply(Long id) {
        return travelApplyMapper.selectById(id);
    }

    @Override
    public PageResult<OaTravelApplyDO> getTravelApplyPage(Long userId, OaTravelApplyPageReqVO reqVO) {
        return travelApplyMapper.selectPage(userId, reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTravelApplyStatus(Long id, Integer status) {
        // 1. 校验申请存在
        OaTravelApplyDO apply = validateTravelApplyExists(id);

        // 2. 更新审批状态
        travelApplyMapper.updateById(new OaTravelApplyDO().setId(id).setStatus(status));

        // 3. 审批通过后生成考勤明细
        if (ObjUtil.equal(status, BpmProcessInstanceStatusEnum.APPROVE.getStatus())) {
            attendanceService.createApplyAttendance(Long.valueOf(apply.getCreator()),
                    OaAttendanceTypeEnum.TRAVEL, apply.getStartTime());
        }
    }

    @Override
    public List<OaTravelApplyDO> getApprovedTravelApplyList(Long userId) {
        return travelApplyMapper.selectListByCreatorAndStatus(userId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
    }

    @Override
    public OaTravelApplyDO validateApprovedTravelApply(Long id, Long userId) {
        // 1. 校验申请存在
        OaTravelApplyDO record = validateTravelApplyExists(id);
        // 2. 校验申请归属
        validateTravelApplyOwner(record, userId);
        // 3. 校验申请已通过审批
        if (ObjUtil.notEqual(record.getStatus(), BpmProcessInstanceStatusEnum.APPROVE.getStatus())) {
            throw exception(TRAVEL_APPLY_NOT_APPROVED);
        }
        // 4. 已报销的行程不再创建或提交报销单
        if (Boolean.TRUE.equals(record.getReimburseStatus())) {
            throw exception(TRAVEL_APPLY_ALREADY_REIMBURSED);
        }
        return record;
    }

    @Override
    public List<OaTravelApplyDO> getTravelApplyList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return travelApplyMapper.selectByIds(ids);
    }

    @Override
    public void updateTravelApplyReimburseStatus(Long id) {
        travelApplyMapper.updateById(new OaTravelApplyDO().setId(id)
                .setReimburseStatus(true));
    }

    /**
     * 校验单据存在
     *
     * @param id 单据编号
     * @return 单据
     */
    private OaTravelApplyDO validateTravelApplyExists(Long id) {
        OaTravelApplyDO record = travelApplyMapper.selectById(id);
        if (record == null) {
            throw exception(TRAVEL_NOT_EXISTS);
        }
        return record;
    }

    /**
     * 校验本人单据
     *
     * @param record 单据
     * @param userId 用户编号
     */
    private void validateTravelApplyOwner(OaTravelApplyDO record, Long userId) {
        if (ObjUtil.notEqual(record.getCreator(), userId.toString())) {
            throw exception(TRAVEL_ACCESS_DENIED);
        }
    }

    /**
     * 校验可编辑状态
     *
     * @param record 单据
     */
    private void validateTravelApplyEditable(OaTravelApplyDO record) {
        if (ObjectUtils.notEqualsAny(record.getStatus(), BpmProcessInstanceStatusEnum.NOT_START.getStatus(),
                BpmProcessInstanceStatusEnum.REJECT.getStatus(), BpmProcessInstanceStatusEnum.CANCEL.getStatus())) {
            throw exception(TRAVEL_STATUS_INVALID);
        }
    }

}
