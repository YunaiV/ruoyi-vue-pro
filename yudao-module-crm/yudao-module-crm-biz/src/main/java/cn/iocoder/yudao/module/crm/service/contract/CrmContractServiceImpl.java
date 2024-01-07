package cn.iocoder.yudao.module.crm.service.contract;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractTransferReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.contract.CrmContractConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.mysql.contract.CrmContractMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.permission.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CONTRACT_NOT_EXISTS;

/**
 * CRM 合同 Service 实现类
 *
 * @author dhb52
 */
@Service
@Validated
public class CrmContractServiceImpl implements CrmContractService {

    @Resource
    private CrmContractMapper contractMapper;

    @Resource
    private CrmPermissionService crmPermissionService;

    @Override
    // TODO @puhui999：添加操作日志
    public Long createContract(CrmContractCreateReqVO createReqVO, Long userId) {
        // TODO @合同待定：插入合同商品；需要搞个 BusinessProductDO
        // 插入合同
        CrmContractDO contract = CrmContractConvert.INSTANCE.convert(createReqVO);
        contractMapper.insert(contract);

        // 创建数据权限
        crmPermissionService.createPermission(new CrmPermissionCreateReqBO().setUserId(userId)
                .setBizType(CrmBizTypeEnum.CRM_CONTRACT.getType()).setBizId(contract.getId())
                .setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));
        return contract.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTRACT, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.WRITE)
    // TODO @puhui999：添加操作日志
    public void updateContract(CrmContractUpdateReqVO updateReqVO) {
        // TODO @合同待定：只有草稿、审批中，可以编辑；
        // 校验存在
        validateContractExists(updateReqVO.getId());
        // 更新合同
        CrmContractDO updateObj = CrmContractConvert.INSTANCE.convert(updateReqVO);
        contractMapper.updateById(updateObj);
        // TODO @合同待定：插入合同商品；需要搞个 BusinessProductDO
    }

    // TODO @合同待定：缺一个取消合同的接口；只有草稿、审批中可以取消；CrmAuditStatusEnum

    // TODO @合同待定：缺一个发起审批的接口；只有草稿可以发起审批；CrmAuditStatusEnum

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTRACT, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void deleteContract(Long id) {
        // TODO @合同待定：如果被 CrmReceivableDO 所使用，则不允许删除
        // 校验存在
        validateContractExists(id);
        // 删除
        contractMapper.deleteById(id);
        // 删除数据权限
        crmPermissionService.deletePermission(CrmBizTypeEnum.CRM_CONTRACT.getType(), id);
    }

    private CrmContractDO validateContractExists(Long id) {
        CrmContractDO contract = contractMapper.selectById(id);
        if (contract == null) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
        return contract;
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTRACT, bizId = "#id", level = CrmPermissionLevelEnum.READ)
    public CrmContractDO getContract(Long id) {
        return contractMapper.selectById(id);
    }

    @Override
    public List<CrmContractDO> getContractList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return contractMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CrmContractDO> getContractPage(CrmContractPageReqVO pageReqVO, Long userId) {
        return contractMapper.selectPage(pageReqVO, userId);
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#pageReqVO.customerId", level = CrmPermissionLevelEnum.READ)
    public PageResult<CrmContractDO> getContractPageByCustomerId(CrmContractPageReqVO pageReqVO) {
        return contractMapper.selectPageByCustomerId(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 3. TODO @puhui999：记录转移日志
    // TODO @puhui999：权限校验，这里要搞哇？
    public void transferContract(CrmContractTransferReqVO reqVO, Long userId) {
        // 1. 校验合同是否存在
        validateContractExists(reqVO.getId());

        // 2.1 数据权限转移
        crmPermissionService.transferPermission(
                CrmContractConvert.INSTANCE.convert(reqVO, userId).setBizType(CrmBizTypeEnum.CRM_CONTRACT.getType()));
        // 2.2 设置负责人
        contractMapper.updateOwnerUserIdById(reqVO.getId(), reqVO.getNewOwnerUserId());
    }

    @Override
    public Long getContractCountByContactId(Long contactId) {
        return contractMapper.selectCountByContactId(contactId);
    }

    // TODO @合同待定：需要新增一个 ContractConfigDO 表，合同配置，重点是到期提醒；
}
