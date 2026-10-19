package cn.iocoder.yudao.module.oa.service.travel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.controller.admin.travel.vo.reimbursement.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.travel.OaTravelReimbursementDO;
import cn.iocoder.yudao.module.oa.dal.mysql.travel.OaTravelReimbursementMapper;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.BpmModelConstants.*;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 出差报销 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaTravelReimbursementServiceImpl implements OaTravelReimbursementService {

    @Resource
    private OaTravelReimbursementMapper travelReimbursementMapper;

    @Resource
    private OaNoRedisDAO noRedisDAO;

    @Resource
    private OaTravelApplyService travelApplyService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private BpmProcessInstanceApi processInstanceApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTravelReimbursement(OaTravelReimbursementSaveReqVO reqVO, Long userId) {
        // 1.1 校验所选申请
        if (reqVO.getTravelApplyId() != null) {
            travelApplyService.validateApprovedTravelApply(reqVO.getTravelApplyId(), userId);
        }
        // 1.2 查询并校验申请人
        AdminUserRespDTO user = adminUserApi.validateUser(userId);
        // 1.3 生成单号并校验唯一性
        String no = noRedisDAO.generate(OaNoRedisDAO.TRAVEL_REIMBURSEMENT_NO_PREFIX);
        if (travelReimbursementMapper.selectByNo(no) != null) {
            throw exception(TRAVEL_NO_DUPLICATE);
        }

        // 2. 创建草稿
        OaTravelReimbursementDO record = buildTravelReimbursement(reqVO)
                .setNo(no)
                .setDeptId(user.getDeptId()).setStatus(BpmProcessInstanceStatusEnum.NOT_START.getStatus())
                .setPayStatus(false);
        travelReimbursementMapper.insert(record);
        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTravelReimbursement(OaTravelReimbursementSaveReqVO reqVO, Long userId) {
        // 1.1 查询并校验单据存在
        OaTravelReimbursementDO record = validateTravelReimbursementExists(reqVO.getId());
        // 1.2 校验本人归属
        validateTravelReimbursementOwner(record, userId);
        // 1.3 校验可编辑状态
        validateTravelReimbursementEditable(record);
        // 1.4 校验所选申请
        if (reqVO.getTravelApplyId() != null) {
            travelApplyService.validateApprovedTravelApply(reqVO.getTravelApplyId(), userId);
        }

        // 2. 更新单据
        travelReimbursementMapper.updateById(buildTravelReimbursement(reqVO));
    }

    /**
     * 转换草稿业务字段，统一计算天数和报销总金额
     *
     * @param reqVO 单据内容
     * @return 单据业务字段
     */
    private OaTravelReimbursementDO buildTravelReimbursement(OaTravelReimbursementSaveReqVO reqVO) {
        OaTravelReimbursementDO record = BeanUtils.toBean(reqVO, OaTravelReimbursementDO.class);
        record.setDays(LocalDateTimeUtils.getDaysBetweenCeiling(reqVO.getStartTime(), reqVO.getEndTime()));
        BigDecimal totalPrice = BigDecimal.ZERO;
        if (CollUtil.isNotEmpty(record.getItems())) {
            for (OaTravelReimbursementDO.Item item : record.getItems()) {
                if (item.getPrice() != null) {
                    totalPrice = totalPrice.add(item.getPrice());
                }
            }
        }
        record.setTotalPrice(record.getItems() == null ? null : totalPrice.setScale(2, RoundingMode.HALF_UP));
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitTravelReimbursement(OaTravelReimbursementSubmitReqVO submitReqVO, Long userId) {
        // 1.1 查询并校验单据存在
        OaTravelReimbursementDO record = validateTravelReimbursementExists(submitReqVO.getId());
        // 1.2 校验本人归属
        validateTravelReimbursementOwner(record, userId);
        // 1.3 校验可提交状态
        validateTravelReimbursementEditable(record);
        // 1.4 校验所选申请仍可关联
        if (record.getTravelApplyId() != null) {
            travelApplyService.validateApprovedTravelApply(record.getTravelApplyId(), userId);
        }

        // 2. 更新为审批中，兼容流程创建时的同步回调
        travelReimbursementMapper.updateById(new OaTravelReimbursementDO().setId(record.getId())
                .setStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus()).setProcessInstanceId(""));

        // 3.1 发起审批
        Map<String, Object> variables = new HashMap<>();
        variables.put("days", record.getDays());
        String processInstanceId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(TRAVEL_REIMBURSEMENT)
                        .setBusinessKey(record.getId().toString()).setVariables(variables)
                        .setStartUserSelectAssignees(submitReqVO.getStartUserSelectAssignees()));
        // 3.2 绑定流程编号
        travelReimbursementMapper.updateById(new OaTravelReimbursementDO().setId(record.getId()).setProcessInstanceId(processInstanceId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTravelReimbursement(Long id, Long userId) {
        // 1.1 校验单据存在
        OaTravelReimbursementDO record = validateTravelReimbursementExists(id);
        // 1.2 校验本人归属
        validateTravelReimbursementOwner(record, userId);
        // 1.3 仅审批中可以撤回
        if (ObjUtil.notEqual(record.getStatus(), BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
            throw exception(TRAVEL_STATUS_INVALID);
        }

        // 2. 使用 BPM 取消流程，由事件回写单据状态
        processInstanceApi.cancelProcessInstanceByStartUser(userId, record.getProcessInstanceId(), "申请人撤回出差报销");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTravelReimbursement(Long id, Long userId) {
        // 1.1 校验单据存在
        OaTravelReimbursementDO record = validateTravelReimbursementExists(id);
        // 1.2 校验本人归属
        validateTravelReimbursementOwner(record, userId);
        // 1.3 校验可删除状态
        validateTravelReimbursementEditable(record);

        // 2. 删除本人单据
        travelReimbursementMapper.deleteById(id);
    }

    @Override
    public OaTravelReimbursementDO getTravelReimbursement(Long id) {
        return travelReimbursementMapper.selectById(id);
    }

    @Override
    public PageResult<OaTravelReimbursementDO> getTravelReimbursementPage(Long userId, OaTravelReimbursementPageReqVO reqVO) {
        return travelReimbursementMapper.selectPage(userId, reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTravelReimbursementStatus(Long id, Integer status) {
        // 1. 校验报销单存在
        OaTravelReimbursementDO record = validateTravelReimbursementExists(id);
        // 重复结果不再触发关联申请的报销状态更新
        if (ObjUtil.equal(record.getStatus(), status)) {
            return;
        }

        // 2. 更新审批状态
        travelReimbursementMapper.updateById(new OaTravelReimbursementDO().setId(id).setStatus(status));

        // 3. 审批通过时标记关联申请已报销，不修改支付状态
        if (ObjUtil.equal(status, BpmProcessInstanceStatusEnum.APPROVE.getStatus()) && record.getTravelApplyId() != null) {
            travelApplyService.updateTravelApplyReimburseStatus(record.getTravelApplyId());
        }
    }

    /**
     * 校验单据存在
     *
     * @param id 单据编号
     * @return 单据
     */
    private OaTravelReimbursementDO validateTravelReimbursementExists(Long id) {
        OaTravelReimbursementDO record = travelReimbursementMapper.selectById(id);
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
    private void validateTravelReimbursementOwner(OaTravelReimbursementDO record, Long userId) {
        if (ObjUtil.notEqual(record.getCreator(), userId.toString())) {
            throw exception(TRAVEL_ACCESS_DENIED);
        }
    }

    /**
     * 校验可编辑状态
     *
     * @param record 单据
     */
    private void validateTravelReimbursementEditable(OaTravelReimbursementDO record) {
        if (ObjectUtils.notEqualsAny(record.getStatus(), BpmProcessInstanceStatusEnum.NOT_START.getStatus(),
                BpmProcessInstanceStatusEnum.REJECT.getStatus(), BpmProcessInstanceStatusEnum.CANCEL.getStatus())) {
            throw exception(TRAVEL_STATUS_INVALID);
        }
    }

}
