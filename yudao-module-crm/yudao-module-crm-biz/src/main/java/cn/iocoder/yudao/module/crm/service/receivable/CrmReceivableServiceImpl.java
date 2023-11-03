package cn.iocoder.yudao.module.crm.service.receivable;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.CrmReceivableCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.CrmReceivableExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.CrmReceivablePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.CrmReceivableUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.receivable.CrmReceivableConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivableDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivablePlanDO;
import cn.iocoder.yudao.module.crm.dal.mysql.receivable.CrmReceivableMapper;
import cn.iocoder.yudao.module.crm.enums.AuditStatusEnum;
import cn.iocoder.yudao.module.crm.service.contract.ContractService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;

/**
 * 回款管理 Service 实现类
 *
 * @author 赤焰
 */
@Service
@Validated
public class CrmReceivableServiceImpl implements CrmReceivableService {

    @Resource
    private CrmReceivableMapper crmReceivableMapper;
    @Resource
    private ContractService contractService;
    @Resource
    private CrmCustomerService crmCustomerService;
    @Resource
    private CrmReceivablePlanService crmReceivablePlanService;

    @Override
    public Long createReceivable(CrmReceivableCreateReqVO createReqVO) {
        // 插入
        CrmReceivableDO receivable = CrmReceivableConvert.INSTANCE.convert(createReqVO);
        if (ObjectUtil.isNull(receivable.getStatus())){
            receivable.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        if (ObjectUtil.isNull(receivable.getCheckStatus())){
            receivable.setCheckStatus(AuditStatusEnum.AUDIT_NEW.getValue());
        }

        //校验
        checkReceivable(receivable);

        crmReceivableMapper.insert(receivable);
        // 返回
        return receivable.getId();
    }

    private void checkReceivable(CrmReceivableDO receivable) {

        if(ObjectUtil.isNull(receivable.getContractId())){
            throw exception(CONTRACT_NOT_EXISTS);
        }

        ContractDO contract = contractService.getContract(receivable.getContractId());
        if(ObjectUtil.isNull(contract)){
            throw exception(CONTRACT_NOT_EXISTS);
        }

        CrmCustomerDO customer = crmCustomerService.getCustomer(receivable.getCustomerId());
        if(ObjectUtil.isNull(customer)){
            throw exception(CUSTOMER_NOT_EXISTS);
        }

        CrmReceivablePlanDO receivablePlan = crmReceivablePlanService.getReceivablePlan(receivable.getPlanId());
        if(ObjectUtil.isNull(receivablePlan)){
            throw exception(RECEIVABLE_PLAN_NOT_EXISTS);
        }

    }

    @Override
    public void updateReceivable(CrmReceivableUpdateReqVO updateReqVO) {
        // 校验存在
        validateReceivableExists(updateReqVO.getId());

        // 更新
        CrmReceivableDO updateObj = CrmReceivableConvert.INSTANCE.convert(updateReqVO);
        crmReceivableMapper.updateById(updateObj);
    }

    @Override
    public void deleteReceivable(Long id) {
        // 校验存在
        validateReceivableExists(id);
        // 删除
        crmReceivableMapper.deleteById(id);
    }

    private void validateReceivableExists(Long id) {
        if (crmReceivableMapper.selectById(id) == null) {
            throw exception(RECEIVABLE_NOT_EXISTS);
        }
    }

    @Override
    public CrmReceivableDO getReceivable(Long id) {
        return crmReceivableMapper.selectById(id);
    }

    @Override
    public List<CrmReceivableDO> getReceivableList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return crmReceivableMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CrmReceivableDO> getReceivablePage(CrmReceivablePageReqVO pageReqVO) {
        return crmReceivableMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CrmReceivableDO> getReceivableList(CrmReceivableExportReqVO exportReqVO) {
        return crmReceivableMapper.selectList(exportReqVO);
    }

}
