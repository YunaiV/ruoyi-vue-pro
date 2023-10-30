package cn.iocoder.yudao.module.crm.service.receivable;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.ReceivableCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.ReceivableExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.ReceivablePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.ReceivableUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.receivable.ReceivableConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.ReceivableDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.ReceivablePlanDO;
import cn.iocoder.yudao.module.crm.dal.mysql.receivable.ReceivableMapper;
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
public class ReceivableServiceImpl implements ReceivableService {

    @Resource
    private ReceivableMapper receivableMapper;
    @Resource
    private ContractService contractService;
    @Resource
    private CrmCustomerService crmCustomerService;
    @Resource
    private ReceivablePlanService receivablePlanService;

    @Override
    public Long createReceivable(ReceivableCreateReqVO createReqVO) {
        // 插入
        ReceivableDO receivable = ReceivableConvert.INSTANCE.convert(createReqVO);
        if (ObjectUtil.isNull(receivable.getStatus())){
            receivable.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        if (ObjectUtil.isNull(receivable.getCheckStatus())){
            receivable.setCheckStatus(AuditStatusEnum.AUDIT_NEW.getValue());
        }

        //校验
        checkReceivable(receivable);

        receivableMapper.insert(receivable);
        // 返回
        return receivable.getId();
    }

    private void checkReceivable(ReceivableDO receivable) {

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

        ReceivablePlanDO receivablePlan = receivablePlanService.getReceivablePlan(receivable.getPlanId());
        if(ObjectUtil.isNull(receivablePlan)){
            throw exception(RECEIVABLE_PLAN_NOT_EXISTS);
        }

    }

    @Override
    public void updateReceivable(ReceivableUpdateReqVO updateReqVO) {
        // 校验存在
        validateReceivableExists(updateReqVO.getId());

        // 更新
        ReceivableDO updateObj = ReceivableConvert.INSTANCE.convert(updateReqVO);
        receivableMapper.updateById(updateObj);
    }

    @Override
    public void deleteReceivable(Long id) {
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

    @Override
    public ReceivableDO getReceivable(Long id) {
        return receivableMapper.selectById(id);
    }

    @Override
    public List<ReceivableDO> getReceivableList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return receivableMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ReceivableDO> getReceivablePage(ReceivablePageReqVO pageReqVO) {
        return receivableMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ReceivableDO> getReceivableList(ReceivableExportReqVO exportReqVO) {
        return receivableMapper.selectList(exportReqVO);
    }

}
