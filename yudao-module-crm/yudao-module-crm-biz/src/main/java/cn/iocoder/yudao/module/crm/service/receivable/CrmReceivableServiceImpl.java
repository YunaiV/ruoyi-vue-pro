package cn.iocoder.yudao.module.crm.service.receivable;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.receivable.CrmReceivablePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.receivable.CrmReceivableSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivableDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivablePlanDO;
import cn.iocoder.yudao.module.crm.dal.mysql.receivable.CrmReceivableMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmAuditStatusEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.permission.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.crm.enums.LogRecordConstants.*;
import static cn.iocoder.yudao.module.crm.util.CrmAuditStatusUtils.convertBpmResultToAuditStatus;

/**
 * CRM 回款 Service 实现类
 *
 * @author 赤焰
 */
@Service
@Validated
@Slf4j
public class CrmReceivableServiceImpl implements CrmReceivableService {

    /**
     * BPM 回款审批流程标识
     */
    public static final String RECEIVABLE_APPROVE = "receivable-approve";

    @Resource
    private CrmReceivableMapper receivableMapper;

    @Resource
    private CrmContractService contractService;
    @Resource
    private CrmCustomerService customerService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private CrmReceivablePlanService receivablePlanService;
    @Resource
    private CrmPermissionService permissionService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private BpmProcessInstanceApi bpmProcessInstanceApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_RECEIVABLE_TYPE, subType = CRM_RECEIVABLE_CREATE_SUB_TYPE, bizNo = "{{#receivable.id}}",
            success = CRM_RECEIVABLE_CREATE_SUCCESS)
    public Long createReceivable(CrmReceivableSaveReqVO createReqVO) {
        // 1. 校验关联数据存在
        validateRelationDataExists(createReqVO);
        // 2. 插入还款
        CrmReceivableDO receivable = BeanUtils.toBean(createReqVO, CrmReceivableDO.class).setAuditStatus(CrmAuditStatusEnum.DRAFT.getStatus());
        receivableMapper.insert(receivable);
        // 3. 创建数据权限
        permissionService.createPermission(new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_RECEIVABLE.getType())
                .setBizId(receivable.getId()).setUserId(createReqVO.getOwnerUserId()).setLevel(CrmPermissionLevelEnum.OWNER.getLevel())); // 设置当前操作的人为负责人
        // 4. 更新关联的回款计划
        if (Objects.nonNull(createReqVO.getPlanId())) {
            receivablePlanService.updateReceivableId(receivable.getPlanId(), receivable.getId());
        }
        // 5. 记录操作日志上下文
        LogRecordContext.putVariable("receivable", receivable);
        return receivable.getId();
    }

    private void validateRelationDataExists(CrmReceivableSaveReqVO reqVO) {
        adminUserApi.validateUser(reqVO.getOwnerUserId()); // 校验负责人存在
        CrmContractDO contract = contractService.getContract(reqVO.getContractId());
        if (ObjectUtil.isNull(contract)) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
        CrmCustomerDO customer = customerService.getCustomer(reqVO.getCustomerId());
        if (ObjectUtil.isNull(customer)) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        if (Objects.isNull(reqVO.getPlanId())) { // 没有回款计划编号则不校验
            return;
        }
        CrmReceivablePlanDO receivablePlan = receivablePlanService.getReceivablePlan(reqVO.getPlanId());
        if (ObjectUtil.isNull(receivablePlan)) {
            throw exception(RECEIVABLE_PLAN_NOT_EXISTS);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_RECEIVABLE_TYPE, subType = CRM_RECEIVABLE_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}",
            success = CRM_RECEIVABLE_UPDATE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_RECEIVABLE, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.WRITE)
    public void updateReceivable(CrmReceivableSaveReqVO updateReqVO) {
        Assert.notNull(updateReqVO.getId(), "回款编号不能为空");
        // 1.1 校验存在
        CrmReceivableDO receivable = validateReceivableExists(updateReqVO.getId());
        // 1.2 只有草稿、审批中，可以编辑；
        if (!ObjectUtils.equalsAny(receivable.getAuditStatus(), CrmAuditStatusEnum.DRAFT.getStatus(),
                CrmAuditStatusEnum.PROCESS.getStatus())) {
            throw exception(RECEIVABLE_UPDATE_FAIL_EDITING_PROHIBITED);
        }

        // 2. 更新还款
        CrmReceivableDO updateObj = BeanUtils.toBean(updateReqVO, CrmReceivableDO.class);
        receivableMapper.updateById(updateObj);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(receivable, CrmReceivableSaveReqVO.class));
        LogRecordContext.putVariable("receivable", receivable);
    }

    @Override
    public void updateReceivableAuditStatus(Long id, Integer bpmResult) {
        // 1.1 校验存在
        CrmReceivableDO receivable = validateReceivableExists(id);
        // 1.2 只有审批中，可以更新审批结果
        if (ObjUtil.notEqual(receivable.getAuditStatus(), CrmAuditStatusEnum.PROCESS.getStatus())) {
            log.error("[updateReceivableAuditStatus][receivable({}) 不处于审批中，无法更新审批结果({})]",
                    receivable.getId(), bpmResult);
            throw exception(RECEIVABLE_UPDATE_AUDIT_STATUS_FAIL_NOT_PROCESS);
        }

        // 2. 更新回款审批状态
        Integer auditStatus = convertBpmResultToAuditStatus(bpmResult);
        receivableMapper.updateById(new CrmReceivableDO().setId(id).setAuditStatus(auditStatus));
    }

    // TODO @liuhongfeng：缺一个取消回款的接口；只有草稿、审批中可以取消；CrmAuditStatusEnum

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_RECEIVABLE_TYPE, subType = CRM_RECEIVABLE_DELETE_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_RECEIVABLE_DELETE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_RECEIVABLE, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void deleteReceivable(Long id) {
        // 校验存在
        CrmReceivableDO receivable = validateReceivableExists(id);
        // 如果被 CrmReceivablePlanDO 所使用，则不允许删除
        if (Objects.nonNull(receivable.getPlanId()) && receivablePlanService.getReceivablePlan(receivable.getPlanId()) != null) {
            throw exception(RECEIVABLE_DELETE_FAIL);
        }
        // 删除
        receivableMapper.deleteById(id);

        // 删除数据权限
        permissionService.deletePermission(CrmBizTypeEnum.CRM_RECEIVABLE.getType(), id);

        // 记录操作日志上下文
        LogRecordContext.putVariable("receivable", receivable);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_RECEIVABLE_TYPE, subType = CRM_RECEIVABLE_SUBMIT_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_RECEIVABLE_SUBMIT_SUCCESS)
    public void submitReceivable(Long id, Long userId) {
        // 1. 校验回款是否在审批
        CrmReceivableDO receivable = validateReceivableExists(id);
        if (ObjUtil.notEqual(receivable.getAuditStatus(), CrmAuditStatusEnum.DRAFT.getStatus())) {
            throw exception(RECEIVABLE_SUBMIT_FAIL_NOT_DRAFT);
        }

        // 2. 创建回款审批流程实例
        String processInstanceId = bpmProcessInstanceApi.createProcessInstance(userId, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey(RECEIVABLE_APPROVE).setBusinessKey(String.valueOf(id)));

        // 3. 更新回款工作流编号
        receivableMapper.updateById(new CrmReceivableDO().setId(id).setProcessInstanceId(processInstanceId)
                .setAuditStatus(CrmAuditStatusEnum.PROCESS.getStatus()));

        // 4. 记录日志
        LogRecordContext.putVariable("receivableNo", receivable.getNo());
    }

    private CrmReceivableDO validateReceivableExists(Long id) {
        CrmReceivableDO receivable = receivableMapper.selectById(id);
        if (receivable == null) {
            throw exception(RECEIVABLE_NOT_EXISTS);
        }
        return receivable;
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_RECEIVABLE, bizId = "#id", level = CrmPermissionLevelEnum.READ)
    public CrmReceivableDO getReceivable(Long id) {
        return receivableMapper.selectById(id);
    }

    @Override
    public List<CrmReceivableDO> getReceivableList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return receivableMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CrmReceivableDO> getReceivablePage(CrmReceivablePageReqVO pageReqVO, Long userId) {
        return receivableMapper.selectPage(pageReqVO, userId);
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#pageReqVO.customerId", level = CrmPermissionLevelEnum.READ)
    public PageResult<CrmReceivableDO> getReceivablePageByCustomerId(CrmReceivablePageReqVO pageReqVO) {
        return receivableMapper.selectPageByCustomerId(pageReqVO);
    }

    @Override
    public Long getCheckReceivablesCount(Long userId) {
        return receivableMapper.selectCheckReceivablesCount(userId);
    }

    // TODO @芋艿：不确定这个方法还要不要；
    @Override
    public Long getReceivableCountByContractId(Long contractId) {
        return receivableMapper.selectCount(CrmReceivableDO::getContractId, contractId);
    }

}
