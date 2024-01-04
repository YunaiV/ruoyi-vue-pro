package cn.iocoder.yudao.module.crm.service.receivable;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.receivable.CrmReceivableCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.receivable.CrmReceivablePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.receivable.CrmReceivableTransferReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.receivable.CrmReceivableUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.receivable.CrmReceivableConvert;
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
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;

/**
 * CRM 回款 Service 实现类
 *
 * @author 赤焰
 */
@Service
@Validated
public class CrmReceivableServiceImpl implements CrmReceivableService {

    @Resource
    private CrmReceivableMapper receivableMapper;

    @Resource
    private CrmContractService contractService;
    @Resource
    private CrmCustomerService customerService;
    @Resource
    private CrmReceivablePlanService receivablePlanService;
    @Resource
    private CrmPermissionService crmPermissionService;

    @Override
    // TODO @puhui999：操作日志
    public Long createReceivable(CrmReceivableCreateReqVO createReqVO) {
        // 插入还款
        CrmReceivableDO receivable = CrmReceivableConvert.INSTANCE.convert(createReqVO);
        if (ObjectUtil.isNull(receivable.getAuditStatus())) {
            receivable.setAuditStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        receivable.setAuditStatus(CrmAuditStatusEnum.DRAFT.getStatus());

        // TODO @liuhongfeng：一般来说，逻辑的写法，是要先检查，后操作 db；所以，你这个 check 应该放到  CrmReceivableDO receivable 之前；
        checkReceivable(receivable);

        receivableMapper.insert(receivable);

        // TODO @liuhongfeng：需要更新关联的 plan
        return receivable.getId();
    }

    // TODO @liuhongfeng：这里的括号要注意排版；
    private void checkReceivable(CrmReceivableDO receivable) {
        // TODO @liuhongfeng：校验 no 的唯一性
        // TODO @liuhongfeng：这个放在参数校验合适
        if (ObjectUtil.isNull(receivable.getContractId())) {
            throw exception(CONTRACT_NOT_EXISTS);
        }

        CrmContractDO contract = contractService.getContract(receivable.getContractId());
        if (ObjectUtil.isNull(contract)) {
            throw exception(CONTRACT_NOT_EXISTS);
        }

        CrmCustomerDO customer = customerService.getCustomer(receivable.getCustomerId());
        if (ObjectUtil.isNull(customer)) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }

        CrmReceivablePlanDO receivablePlan = receivablePlanService.getReceivablePlan(receivable.getPlanId());
        if (ObjectUtil.isNull(receivablePlan)) {
            throw exception(RECEIVABLE_PLAN_NOT_EXISTS);
        }

    }

    @Override
    // TODO @puhui999：操作日志
    // TODO @puhui999：权限校验
    public void updateReceivable(CrmReceivableUpdateReqVO updateReqVO) {
        // 校验存在
        validateReceivableExists(updateReqVO.getId());
        // TODO @liuhongfeng：只有在草稿、审核中，可以提交修改

        // 更新还款
        CrmReceivableDO updateObj = CrmReceivableConvert.INSTANCE.convert(updateReqVO);
        receivableMapper.updateById(updateObj);

        // TODO @liuhongfeng：需要更新关联的 plan
    }

    // TODO @liuhongfeng：缺一个取消合同的接口；只有草稿、审批中可以取消；CrmAuditStatusEnum

    // TODO @liuhongfeng：缺一个发起审批的接口；只有草稿可以发起审批；CrmAuditStatusEnum

    @Override
    // TODO @puhui999：操作日志
    // TODO @puhui999：权限校验
    public void deleteReceivable(Long id) {
        // TODO @liuhongfeng：如果被 CrmReceivablePlanDO 所使用，则不允许删除
        // 校验存在
        validateReceivableExists(id);
        // 删除
        receivableMapper.deleteById(id);
    }

    private void validateReceivableExists(Long id) {
        if (receivableMapper.selectById(id) == null) {
            throw exception(RECEIVABLE_NOT_EXISTS);
        }
    }

    // TODO @芋艿：数据权限
    @Override
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
    public void transferReceivable(CrmReceivableTransferReqVO reqVO, Long userId) {
        // 1 校验回款是否存在
        validateReceivableExists(reqVO.getId());

        // 2.1 数据权限转移
        crmPermissionService.transferPermission(
                CrmReceivableConvert.INSTANCE.convert(reqVO, userId).setBizType(CrmBizTypeEnum.CRM_RECEIVABLE.getType()));
        // 2.2 设置新的负责人
        receivableMapper.updateOwnerUserIdById(reqVO.getId(), reqVO.getNewOwnerUserId());

        // 3. TODO 记录转移日志
    }

}
