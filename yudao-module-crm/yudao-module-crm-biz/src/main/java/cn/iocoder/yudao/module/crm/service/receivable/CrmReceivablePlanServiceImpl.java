package cn.iocoder.yudao.module.crm.service.receivable;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanTransferReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.receivable.CrmReceivablePlanConvert;
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
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;

// TODO @liuhongfeng：参考 CrmReceivableServiceImpl 写的 todo 哈；

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
    private CrmContractService contractService;
    @Resource
    private CrmCustomerService customerService;
    @Resource
    private CrmPermissionService crmPermissionService;

    @Override
    public Long createReceivablePlan(CrmReceivablePlanCreateReqVO createReqVO, Long userId) {
        // 插入
        CrmReceivablePlanDO receivablePlan = CrmReceivablePlanConvert.INSTANCE.convert(createReqVO);
        receivablePlan.setFinishStatus(false);

        checkReceivablePlan(receivablePlan);

        receivablePlanMapper.insert(receivablePlan);
        // 创建数据权限
        crmPermissionService.createPermission(new CrmPermissionCreateReqBO().setUserId(userId)
                .setBizType(CrmBizTypeEnum.CRM_RECEIVABLE_PLAN.getType()).setBizId(receivablePlan.getId())
                .setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));
        return receivablePlan.getId();
    }

    private void checkReceivablePlan(CrmReceivablePlanDO receivablePlan) {

        if (ObjectUtil.isNull(receivablePlan.getContractId())) {
            throw exception(CONTRACT_NOT_EXISTS);
        }

        CrmContractDO contract = contractService.getContract(receivablePlan.getContractId());
        if (ObjectUtil.isNull(contract)) {
            throw exception(CONTRACT_NOT_EXISTS);
        }

        CrmCustomerDO customer = customerService.getCustomer(receivablePlan.getCustomerId());
        if (ObjectUtil.isNull(customer)) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }

    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_RECEIVABLE_PLAN, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.WRITE)
    public void updateReceivablePlan(CrmReceivablePlanUpdateReqVO updateReqVO) {
        // 校验存在
        validateReceivablePlanExists(updateReqVO.getId());

        // 更新
        CrmReceivablePlanDO updateObj = CrmReceivablePlanConvert.INSTANCE.convert(updateReqVO);
        receivablePlanMapper.updateById(updateObj);
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_RECEIVABLE_PLAN, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void deleteReceivablePlan(Long id) {
        // 校验存在
        validateReceivablePlanExists(id);
        // 删除
        receivablePlanMapper.deleteById(id);
    }

    private void validateReceivablePlanExists(Long id) {
        if (receivablePlanMapper.selectById(id) == null) {
            throw exception(RECEIVABLE_PLAN_NOT_EXISTS);
        }
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
    public void transferReceivablePlan(CrmReceivablePlanTransferReqVO reqVO, Long userId) {
        // 1 校验回款计划是否存在
        validateReceivablePlanExists(reqVO.getId());

        // 2.1 数据权限转移
        crmPermissionService.transferPermission(
                CrmReceivablePlanConvert.INSTANCE.convert(reqVO, userId).setBizType(CrmBizTypeEnum.CRM_RECEIVABLE_PLAN.getType()));
        // 2.2 设置新的负责人
        receivablePlanMapper.updateOwnerUserIdById(reqVO.getId(), reqVO.getNewOwnerUserId());

        // 3. TODO 记录转移日志
    }

}
