package cn.iocoder.yudao.module.crm.service.receivable;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivablePlanDO;
import cn.iocoder.yudao.module.crm.dal.mysql.receivable.CrmReceivablePlanMapper;
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

/**
 * 回款计划 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CrmReceivablePlanServiceImpl implements CrmReceivablePlanService {

    @Resource
    private CrmReceivablePlanMapper receivablePlanMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private CrmReceivableService receivableService;
    @Resource
    private CrmContractService contractService;
    @Resource
    private CrmCustomerService customerService;
    @Resource
    private CrmPermissionService permissionService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_RECEIVABLE_PLAN_TYPE, subType = CRM_RECEIVABLE_PLAN_CREATE_SUB_TYPE, bizNo = "{{#receivablePlan.id}}",
            success = CRM_RECEIVABLE_PLAN_CREATE_SUCCESS)
    public Long createReceivablePlan(CrmReceivablePlanSaveReqVO createReqVO) {
        // 1.1 校验关联数据是否存在
        validateRelationDataExists(createReqVO);
        // 1.2 查验关联合同回款数量
        Long count = receivableService.getReceivableCountByContractId(createReqVO.getContractId());
        int period = (int) (count + 1);
        createReqVO.setPeriod(createReqVO.getPeriod() != period ? period : createReqVO.getPeriod()); // 如果期数不对则纠正

        // 2. 插入还款计划
        CrmReceivablePlanDO receivablePlan = BeanUtils.toBean(createReqVO, CrmReceivablePlanDO.class).setId(null).setFinishStatus(false);
        receivablePlanMapper.insert(receivablePlan);

        // 3. 创建数据权限
        permissionService.createPermission(new CrmPermissionCreateReqBO().setUserId(createReqVO.getOwnerUserId())
                .setBizType(CrmBizTypeEnum.CRM_RECEIVABLE_PLAN.getType()).setBizId(receivablePlan.getId())
                .setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("receivablePlan", receivablePlan);
        return receivablePlan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_RECEIVABLE_PLAN_TYPE, subType = CRM_RECEIVABLE_PLAN_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}",
            success = CRM_RECEIVABLE_PLAN_UPDATE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_RECEIVABLE_PLAN, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.WRITE)
    public void updateReceivablePlan(CrmReceivablePlanSaveReqVO updateReqVO) {
        // 1. 校验存在
        validateRelationDataExists(updateReqVO);
        CrmReceivablePlanDO oldReceivablePlan = validateReceivablePlanExists(updateReqVO.getId());
        if (Objects.nonNull(oldReceivablePlan.getReceivableId())) { // 如果已经有对应的还款，则不允许编辑；
            throw exception(RECEIVABLE_PLAN_UPDATE_FAIL, "已经有对应的还款");
        }

        // 2. 更新
        CrmReceivablePlanDO updateObj = BeanUtils.toBean(updateReqVO, CrmReceivablePlanDO.class);
        receivablePlanMapper.updateById(updateObj);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(oldReceivablePlan, CrmReceivablePlanSaveReqVO.class));
        LogRecordContext.putVariable("receivablePlan", oldReceivablePlan);
    }

    @Override
    public void updateReceivableId(Long id, Long receivableId) {
        // 校验存在
        validateReceivablePlanExists(id);
        // 更新回款计划
        receivablePlanMapper.updateById(new CrmReceivablePlanDO().setReceivableId(receivableId).setFinishStatus(true));
    }

    private void validateRelationDataExists(CrmReceivablePlanSaveReqVO reqVO) {
        adminUserApi.validateUser(reqVO.getOwnerUserId()); // 校验负责人存在
        CrmContractDO contract = contractService.getContract(reqVO.getContractId());
        if (ObjectUtil.isNull(contract)) { // 合同不存在
            throw exception(CONTRACT_NOT_EXISTS);
        }
        CrmCustomerDO customer = customerService.getCustomer(reqVO.getCustomerId());
        if (ObjectUtil.isNull(customer)) { // 客户不存在
            throw exception(CUSTOMER_NOT_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_RECEIVABLE_PLAN_TYPE, subType = CRM_RECEIVABLE_PLAN_DELETE_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_RECEIVABLE_PLAN_DELETE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_RECEIVABLE_PLAN, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void deleteReceivablePlan(Long id) {
        // 校验存在
        CrmReceivablePlanDO receivablePlan = validateReceivablePlanExists(id);
        // 删除
        receivablePlanMapper.deleteById(id);
        // 删除数据权限
        permissionService.deletePermission(CrmBizTypeEnum.CRM_CUSTOMER.getType(), id);
        // 记录操作日志上下文
        LogRecordContext.putVariable("receivablePlan", receivablePlan);
    }

    private CrmReceivablePlanDO validateReceivablePlanExists(Long id) {
        CrmReceivablePlanDO receivablePlan = receivablePlanMapper.selectById(id);
        if (receivablePlan == null) {
            throw exception(RECEIVABLE_PLAN_NOT_EXISTS);
        }
        return receivablePlan;
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_RECEIVABLE_PLAN, bizId = "#id", level = CrmPermissionLevelEnum.READ)
    public CrmReceivablePlanDO getReceivablePlan(Long id) {
        return receivablePlanMapper.selectById(id);
    }

    @Override
    public List<CrmReceivablePlanDO> getReceivablePlanList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return receivablePlanMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CrmReceivablePlanDO> getReceivablePlanPage(CrmReceivablePlanPageReqVO pageReqVO, Long userId) {
        return receivablePlanMapper.selectPage(pageReqVO, userId);
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#pageReqVO.customerId", level = CrmPermissionLevelEnum.READ)
    public PageResult<CrmReceivablePlanDO> getReceivablePlanPageByCustomerId(CrmReceivablePlanPageReqVO pageReqVO) {
        return receivablePlanMapper.selectPageByCustomerId(pageReqVO);
    }

    @Override
    public Long getRemindReceivablePlanCount(Long userId) {
        return receivablePlanMapper.selectRemindReceivablePlanCount(userId);
    }

}
