package cn.iocoder.yudao.module.crm.service.receivable;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.CrmReceivablePlanCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.CrmReceivablePlanExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.CrmReceivablePlanPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.CrmReceivablePlanUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.receivable.CrmReceivablePlanConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivablePlanDO;
import cn.iocoder.yudao.module.crm.dal.mysql.receivable.CrmReceivablePlanMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmAuditStatusEnum;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
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
    private CrmReceivablePlanMapper crmReceivablePlanMapper;
    @Resource
    private CrmContractService contractService;
    @Resource
    private CrmCustomerService crmCustomerService;

    @Override
    public Long createReceivablePlan(CrmReceivablePlanCreateReqVO createReqVO) {
        // 插入
        CrmReceivablePlanDO receivablePlan = CrmReceivablePlanConvert.INSTANCE.convert(createReqVO);
        if (ObjectUtil.isNull(receivablePlan.getStatus())){
            receivablePlan.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        if (ObjectUtil.isNull(receivablePlan.getCheckStatus())){
            receivablePlan.setCheckStatus(CrmAuditStatusEnum.DRAFT.getStatus());
        }

        checkReceivablePlan(receivablePlan);

        crmReceivablePlanMapper.insert(receivablePlan);
        // 返回
        return receivablePlan.getId();
    }

    private void checkReceivablePlan(CrmReceivablePlanDO receivablePlan) {

        if(ObjectUtil.isNull(receivablePlan.getContractId())){
            throw exception(CONTRACT_NOT_EXISTS);
        }

        CrmContractDO contract = contractService.getContract(receivablePlan.getContractId());
        if(ObjectUtil.isNull(contract)){
            throw exception(CONTRACT_NOT_EXISTS);
        }

        CrmCustomerDO customer = crmCustomerService.getCustomer(receivablePlan.getCustomerId());
        if(ObjectUtil.isNull(customer)){
            throw exception(CUSTOMER_NOT_EXISTS);
        }

    }

    @Override
    public void updateReceivablePlan(CrmReceivablePlanUpdateReqVO updateReqVO) {
        // 校验存在
        validateReceivablePlanExists(updateReqVO.getId());

        // 更新
        CrmReceivablePlanDO updateObj = CrmReceivablePlanConvert.INSTANCE.convert(updateReqVO);
        crmReceivablePlanMapper.updateById(updateObj);
    }

    @Override
    public void deleteReceivablePlan(Long id) {
        // 校验存在
        validateReceivablePlanExists(id);
        // 删除
        crmReceivablePlanMapper.deleteById(id);
    }

    private void validateReceivablePlanExists(Long id) {
        if (crmReceivablePlanMapper.selectById(id) == null) {
            throw exception(RECEIVABLE_PLAN_NOT_EXISTS);
        }
    }

    @Override
    public CrmReceivablePlanDO getReceivablePlan(Long id) {
        return crmReceivablePlanMapper.selectById(id);
    }

    @Override
    public List<CrmReceivablePlanDO> getReceivablePlanList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return crmReceivablePlanMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CrmReceivablePlanDO> getReceivablePlanPage(CrmReceivablePlanPageReqVO pageReqVO) {
        return crmReceivablePlanMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CrmReceivablePlanDO> getReceivablePlanList(CrmReceivablePlanExportReqVO exportReqVO) {
        return crmReceivablePlanMapper.selectList(exportReqVO);
    }

}
